/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.http.download;

import android.net.TrafficStats;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;
import com.arialyy.aria.exception.AriaHTTPException;
import com.arialyy.aria.http.ConnectionHelp;
import com.arialyy.aria.http.HttpTaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.FileUtil;
import com.arialyy.aria.util.RecordUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 下载文件信息获取
 */
final class HttpDFileInfoTask implements IInfoTask, Runnable {
  private static final String TAG = "HttpDFileInfoTask";
  private DownloadEntity mEntity;
  private DTaskWrapper mTaskWrapper;
  private int mConnectTimeOut;
  private Callback callback;
  private HttpTaskOption taskOption;
  private boolean isStop = false, isCancel = false;

  HttpDFileInfoTask(DTaskWrapper taskWrapper) {
    this.mTaskWrapper = taskWrapper;
    mEntity = taskWrapper.getEntity();
    mConnectTimeOut = AriaConfig.getInstance().getDConfig().getConnectTimeOut();
    taskOption = (HttpTaskOption) taskWrapper.getTaskOption();
  }

  @Override public void run() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    TrafficStats.setThreadStatsTag(UUID.randomUUID().toString().hashCode());
    HttpURLConnection conn = null;
    try {
      URL url = ConnectionHelp.handleUrl(mEntity.getUrl(), taskOption);
      conn = ConnectionHelp.handleConnection(url, taskOption);
      ConnectionHelp.setConnectParam(taskOption, conn);
      conn.setRequestProperty("Range", "bytes=" + 0 + "-");
      if (AriaConfig.getInstance().getDConfig().isUseHeadRequest()) {
        ALog.d(TAG, "head请求");
        conn.setRequestMethod("HEAD");
      }
      conn.setConnectTimeout(mConnectTimeOut);
      conn.connect();
      handleConnect(conn);
    } catch (IOException e) {
      failDownload(new AriaHTTPException(
          String.format("下载失败，filePath: %s, url: %s", mEntity.getFilePath(), mEntity.getUrl()),
          e), true);
    } finally {
      if (conn != null) {
        try {
          InputStream is = conn.getInputStream();
          if (is != null) {
            is.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        conn.disconnect();
      }
    }
  }

  @Override public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @Override public void stop() {
    isStop = true;
  }

  @Override public void cancel() {
    isCancel = true;
  }

  private void handleConnect(HttpURLConnection conn) throws IOException {
    if (taskOption.getRequestEnum() == RequestEnum.POST) {
      Map<String, String> params = taskOption.getParams();
      if (params != null) {
        OutputStreamWriter dos = new OutputStreamWriter(conn.getOutputStream());
        Set<String> keys = params.keySet();
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
          sb.append(key).append("=").append(URLEncoder.encode(params.get(key))).append("&");
        }
        String url = sb.toString();
        url = url.substring(0, url.length() - 1);
        dos.write(url);
        dos.flush();
        dos.close();
      }
    }

    IHttpFileLenAdapter lenAdapter = taskOption.getFileLenAdapter();
    if (lenAdapter == null) {
      lenAdapter = new FileLenAdapter();
    } else {
      ALog.d(TAG, "使用自定义adapter");
    }
    long len = lenAdapter.handleFileLen(conn.getHeaderFields());

    if (!FileUtil.checkMemorySpace(mEntity.getFilePath(), len)) {
      failDownload(new AriaHTTPException(
          String.format("下载失败，内存空间不足；filePath: %s, url: %s", mEntity.getFilePath(),
              mEntity.getUrl())), false);
      return;
    }

    int code = conn.getResponseCode();
    boolean end = false;
    if (TextUtils.isEmpty(mEntity.getMd5Code())) {
      String md5Code = conn.getHeaderField("Content-MD5");
      mEntity.setMd5Code(md5Code);
    }

    boolean isChunked = false;
    final String str = conn.getHeaderField("Transfer-Encoding");
    if (!TextUtils.isEmpty(str) && str.equals("chunked")) {
      isChunked = true;
    }
    Map<String, List<String>> headers = conn.getHeaderFields();
    String disposition = conn.getHeaderField("Content-Disposition");

    if (taskOption.isUseServerFileName()) {
      if (!TextUtils.isEmpty(disposition)) {
        mEntity.setDisposition(CommonUtil.encryptBASE64(disposition));
        handleContentDisposition(disposition);
      } else {
        if (taskOption.getFileNameAdapter() != null) {
          String newName =
              taskOption.getFileNameAdapter().handleFileName(headers, mEntity.getKey());
          mEntity.setServerFileName(newName);
          renameFile(newName);
        }
      }
    }

    CookieManager msCookieManager = new CookieManager();
    List<String> cookiesHeader = headers.get("Set-Cookie");

    if (cookiesHeader != null) {
      for (String cookie : cookiesHeader) {
        msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
      }
      taskOption.setCookieManager(msCookieManager);
    }

    mTaskWrapper.setCode(code);
    if (code == HttpURLConnection.HTTP_PARTIAL) {
      if (!checkLen(len) && !isChunked) {
        if (len < 0) {
          failDownload(
              new AriaHTTPException(String.format("任务下载失败，文件长度小于0， url: %s", mEntity.getUrl())),
              false);
        }
        return;
      }
      mEntity.setFileSize(len);
      mTaskWrapper.setSupportBP(true);
      end = true;
    } else if (code == HttpURLConnection.HTTP_OK) {
      String contentType = conn.getHeaderField("Content-Type");
      if (TextUtils.isEmpty(contentType)) {
        return;
      }
      if (contentType.equals("text/html")) {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(ConnectionHelp.convertInputStream(conn)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line);
        }
        reader.close();
        handleUrlReTurn(conn, CommonUtil.getWindowReplaceUrl(sb.toString()));
        return;
      } else if (!checkLen(len) && !isChunked) {
        if (len < 0) {
          failDownload(
              new AriaHTTPException(
                  String.format("任务下载失败，文件长度小于0， url: %s", mEntity.getUrl())),
              false);
        }
        ALog.d(TAG, "len < 0");
        return;
      }
      mEntity.setFileSize(len);
      mTaskWrapper.setNewTask(true);
      mTaskWrapper.setSupportBP(false);
      end = true;
    } else if (code == HttpURLConnection.HTTP_MOVED_TEMP
        || code == HttpURLConnection.HTTP_MOVED_PERM
        || code == HttpURLConnection.HTTP_SEE_OTHER
        || code == HttpURLConnection.HTTP_CREATED // 201 跳转
        || code == 307) {
      handleUrlReTurn(conn, conn.getHeaderField("Location"));
    } else if (code == 416) { // 处理0k长度的文件的情况
      ALog.w(TAG, "文件长度为0，不支持断点");
      mTaskWrapper.setSupportBP(false);
      mTaskWrapper.setNewTask(true);
      end = true;
    } else if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
      failDownload(new AriaHTTPException(
          String.format("任务下载失败，errorCode：%s, url: %s", code, mEntity.getUrl())), false);
    } else {
      failDownload(new AriaHTTPException(
          String.format("任务下载失败，errorCode：%s, errorMsg: %s, url: %s", code,
              conn.getResponseMessage(), mEntity.getUrl())), !CheckUtil.httpIsBadRequest(code));
    }
    if (isStop || isCancel) {
      return;
    }
    if (end) {
      taskOption.setChunked(isChunked);
      CompleteInfo info = new CompleteInfo(code, mTaskWrapper);
      callback.onSucceed(mEntity.getUrl(), info);
      mEntity.update();
    }
  }

  /**
   * 处理"Content-Disposition"参数
   * <a href=https://cloud.tencent.com/developer/section/1189916>Content-Disposition</a></>
   *
   * @throws UnsupportedEncodingException
   */
  private void handleContentDisposition(String disposition) throws UnsupportedEncodingException {
    if (disposition.contains(";")) {
      String[] infos = disposition.split(";");
      if (infos[0].equals("attachment")) {
        for (String info : infos) {
          if (info.startsWith("filename") && info.contains("=")) {
            String[] temp = info.split("=");
            if (temp.length > 1) {
              String newName = URLDecoder.decode(temp[1], "utf-8").replaceAll("\"", "");
              mEntity.setServerFileName(newName);
              renameFile(newName);
              break;
            }
          }
        }
      } else if (infos[0].equals("form-data") && infos.length > 2) {
        String[] temp = infos[2].split("=");
        if (temp.length > 1) {
          String newName = URLDecoder.decode(temp[1], "utf-8").replaceAll("\"", "");
          mEntity.setServerFileName(newName);
          renameFile(newName);
        }
      } else {
        ALog.w(TAG, "不识别的Content-Disposition参数");
      }
    }
  }

  /**
   * 重命名文件
   */
  private void renameFile(String newName) {
    if (TextUtils.isEmpty(newName)) {
      ALog.w(TAG, "重命名失败【服务器返回的文件名为空】");
      return;
    }
    ALog.d(TAG, String.format("文件重命名为：%s", newName));
    File oldFile = new File(mEntity.getFilePath());
    String newPath = oldFile.getParent() + "/" + newName;
    if (!CheckUtil.checkDPathConflicts(false, newPath, mTaskWrapper.getRequestType())) {
      ALog.e(TAG, "文件重命名失败");
      return;
    }
    if (oldFile.exists()) {
      boolean b = oldFile.renameTo(new File(newPath));
      ALog.d(TAG, String.format("文件重命名%s", b ? "成功" : "失败"));
    }
    mEntity.setFileName(newName);
    mEntity.setFilePath(newPath);
    RecordUtil.modifyTaskRecord(oldFile.getPath(), newPath, mEntity.getTaskType());
  }

  /**
   * 处理30x跳转
   */
  private void handleUrlReTurn(HttpURLConnection conn, String newUrl) throws IOException {
    ALog.d(TAG, "30x跳转，新url为【" + newUrl + "】");
    if (TextUtils.isEmpty(newUrl) || newUrl.equalsIgnoreCase("null")) {
      if (callback != null) {
        callback.onFail(mEntity, new AriaHTTPException("获取重定向链接失败"), false);
      }
      return;
    }
    if (newUrl.startsWith("/")) {
      Uri uri = Uri.parse(mEntity.getUrl());
      newUrl = uri.getHost() + newUrl;
    }

    if (!CheckUtil.checkUrl(newUrl)) {
      failDownload(new AriaHTTPException("下载失败，重定向url错误"), false);
      return;
    }
    taskOption.setRedirectUrl(newUrl);
    mEntity.setRedirect(true);
    mEntity.setRedirectUrl(newUrl);
    String cookies = conn.getHeaderField("Set-Cookie");
    conn.disconnect();
    URL url = ConnectionHelp.handleUrl(newUrl, taskOption);
    conn = ConnectionHelp.handleConnection(url, taskOption);
    ConnectionHelp.setConnectParam(taskOption, conn);
    conn.setRequestProperty("Cookie", cookies);
    conn.setRequestProperty("Range", "bytes=" + 0 + "-");
    if (AriaConfig.getInstance().getDConfig().isUseHeadRequest()) {
      conn.setRequestMethod("HEAD");
    }
    conn.setConnectTimeout(mConnectTimeOut);
    conn.connect();
    handleConnect(conn);
    conn.disconnect();
  }

  /**
   * 检查长度是否合法，并且检查新获取的文件长度是否和数据库的文件长度一直，如果不一致，则表示该任务为新任务
   *
   * @param len 从服务器获取的文件长度
   * @return {@code true}合法
   */
  private boolean checkLen(long len) {
    if (len != mEntity.getFileSize()) {
      ALog.d(TAG, "长度不一致，任务为新任务");
      mTaskWrapper.setNewTask(true);
    }
    return true;
  }

  private void failDownload(AriaHTTPException e, boolean needRetry) {
    if (isStop || isCancel) {
      return;
    }
    if (callback != null) {
      callback.onFail(mEntity, e, needRetry);
    }
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }

  private static class FileLenAdapter implements IHttpFileLenAdapter {

    @Override public long handleFileLen(Map<String, List<String>> headers) {
      if (headers == null || headers.isEmpty()) {
        ALog.e(TAG, "header为空，获取文件长度失败");
        return -1;
      }
      List<String> sLength = headers.get("Content-Length");
      if (sLength == null || sLength.isEmpty()) {
        return -1;
      }
      String temp = sLength.get(0);
      long len = TextUtils.isEmpty(temp) ? -1 : Long.parseLong(temp);
      // 某些服务，如果设置了conn.setRequestProperty("Range", "bytes=" + 0 + "-");
      // 会返回 Content-Range: bytes 0-225427911/225427913
      if (len < 0) {
        List<String> sRange = headers.get("Content-Range");
        if (sRange == null || sRange.isEmpty()) {
          len = -1;
        } else {
          int start = temp.indexOf("/");
          len = Long.parseLong(temp.substring(start + 1));
        }
      }

      return len;
    }
  }
}