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
package com.arialyy.aria.m3u8;

import android.net.TrafficStats;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.core.processor.IBandWidthUrlConverter;
import com.arialyy.aria.core.processor.IKeyUrlConverter;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.exception.AriaM3U8Exception;
import com.arialyy.aria.http.ConnectionHelp;
import com.arialyy.aria.http.HttpTaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.FileUtil;
import com.arialyy.aria.util.Regular;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析url中获取到到m3u8文件信息
 * 协议地址：https://tools.ietf.org/html/rfc8216
 * https://www.cnblogs.com/renhui/p/10351870.html
 * https://blog.csdn.net/Guofengpu/article/details/54922865
 */
final public class M3U8InfoTask implements IInfoTask {
  public static final String M3U8_INDEX_FORMAT = "%s.index";
  private final String TAG = "M3U8InfoThread";
  private DownloadEntity mEntity;
  private DTaskWrapper mTaskWrapper;
  private int mConnectTimeOut;
  private OnGetLivePeerCallback onGetPeerCallback;
  private HttpTaskOption mHttpOption;
  private M3U8TaskOption mM3U8Option;
  private Callback mCallback;
  /**
   * 是否停止获取切片信息，{@code true}停止获取切片信息
   */
  private boolean isStop = false;

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }

  public interface OnGetLivePeerCallback {
    void onGetPeer(String url, String extInf);
  }

  public M3U8InfoTask(DTaskWrapper taskWrapper) {
    this.mTaskWrapper = taskWrapper;
    mEntity = taskWrapper.getEntity();
    mConnectTimeOut = AriaConfig.getInstance().getDConfig().getConnectTimeOut();
    mHttpOption = (HttpTaskOption) taskWrapper.getTaskOption();
    mM3U8Option = (M3U8TaskOption) taskWrapper.getM3u8Option();
    mEntity.getM3U8Entity().setLive(mTaskWrapper.getRequestType() == AbsTaskWrapper.M3U8_LIVE);
  }

  @Override public void run() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    TrafficStats.setThreadStatsTag(UUID.randomUUID().toString().hashCode());
    HttpURLConnection conn = null;
    try {
      URL url = ConnectionHelp.handleUrl(mEntity.getUrl(), mHttpOption);
      conn = ConnectionHelp.handleConnection(url, mHttpOption);
      ConnectionHelp.setConnectParam(mHttpOption, conn);
      conn.setConnectTimeout(mConnectTimeOut);
      conn.connect();
      handleConnect(mEntity.getUrl(), conn);
    } catch (IOException e) {
      failDownload(e.getMessage(), false);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  @Override public void setCallback(Callback callback) {
    mCallback = callback;
  }

  @Override public void stop() {
    this.isStop = true;
  }

  @Override public void cancel() {
    this.isStop = true;
  }

  private void handleConnect(String tsListUrl, HttpURLConnection conn) throws IOException {
    int code = conn.getResponseCode();
    if (code == HttpURLConnection.HTTP_OK) {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(ConnectionHelp.convertInputStream(conn)));
      String line = reader.readLine();
      if (TextUtils.isEmpty(line) || !line.equalsIgnoreCase("#EXTM3U")) {
        failDownload("读取M3U8信息失败，读取不到#EXTM3U标签", false);
        return;
      }
      List<String> extInf = new ArrayList<>();
      boolean isLive = mTaskWrapper.getRequestType() == ITaskWrapper.M3U8_LIVE;
      boolean isGenerateIndexFile =
          ((M3U8TaskOption) mTaskWrapper.getM3u8Option()).isGenerateIndexFile();
      // 写入索引信息的流
      FileOutputStream fos = null;
      if (isGenerateIndexFile) {
        String indexPath = String.format(M3U8_INDEX_FORMAT, mEntity.getFilePath());
        File indexFile = new File(indexPath);
        if (!indexFile.exists()) {
          FileUtil.createFile(indexPath);
        } else {
          //FileUtil.deleteFile(indexPath);
        }
        fos = new FileOutputStream(indexFile);
        ALog.d(TAG, line);
        addIndexInfo(true, fos, line);
      }
      while ((line = reader.readLine()) != null) {
        if (isStop) {
          break;
        }
        ALog.d(TAG, line);
        if (line.startsWith("#EXT-X-ENDLIST")) {
          // 点播文件的下载写入结束标志，直播文件的下载在停止时才写入结束标志
          addIndexInfo(isGenerateIndexFile && !isLive, fos, line);
          break;
        }

        if (line.startsWith("#EXTINF")) {
          String url = reader.readLine();
          if (isLive) {
            if (onGetPeerCallback != null) {
              onGetPeerCallback.onGetPeer(url, line);
            }
          } else {
            extInf.add(url);
          }
          ALog.d(TAG, url);
          addIndexInfo(isGenerateIndexFile && !isLive, fos, line);
          addIndexInfo(isGenerateIndexFile && !isLive, fos, url);
          continue;
        }

        if (line.startsWith("#EXT-X-STREAM-INF")) {
          addIndexInfo(isGenerateIndexFile, fos, line);
          int setBand = mM3U8Option.getBandWidth();
          int bandWidth = getBandWidth(line);
          // 多码率的m3u8配置文件，清空信息
          //if (isGenerateIndexFile && mInfos != null) {
          //  mInfos.clear();
          //}
          if (setBand == 0) {
            handleBandWidth(conn, reader.readLine());
          } else if (bandWidth == setBand) {
            handleBandWidth(conn, reader.readLine());
          } else {
            failDownload(String.format("【%s】码率不存在", setBand), false);
          }
          return;
        }

        if (line.startsWith("#EXT-X-KEY")) {
          addIndexInfo(isGenerateIndexFile, fos, line);
          getKeyInfo(tsListUrl, line);
          continue;
        }
        addIndexInfo(isGenerateIndexFile, fos, line);
      }

      if (!isLive && extInf.isEmpty()) {
        failDownload(String.format("获取M3U8下载地址列表失败，url: %s", mEntity.getUrl()), false);
        return;
      }
      if (!isLive && mEntity.getM3U8Entity().getPeerNum() == 0) {
        mEntity.getM3U8Entity().setPeerNum(extInf.size());
        mEntity.getM3U8Entity().update();
      }
      CompleteInfo info = new CompleteInfo();
      info.obj = extInf;
      onSucceed(info);
      if (fos != null) {
        fos.close();
      }
    } else if (code == HttpURLConnection.HTTP_MOVED_TEMP
        || code == HttpURLConnection.HTTP_MOVED_PERM
        || code == HttpURLConnection.HTTP_SEE_OTHER
        || code == HttpURLConnection.HTTP_CREATED // 201 跳转
        || code == 307) {
      handleUrlReTurn(conn, conn.getHeaderField("Location"));
    } else if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
      failDownload("下载失败错误，错误码：" + code, false);
    } else {
      failDownload(String.format("不支持的响应，code: %s", code), true);
    }
  }

  private void onSucceed(CompleteInfo info) {
    if (isStop) {
      return;
    }
    mCallback.onSucceed(mEntity.getKey(), info);
  }

  /**
   * 添加切片信息到索引文件中
   * 直播下载的索引只记录头部信息，不记录EXTINF中的信息，该信息在onGetPeer的方法中添加。
   * 点播下载记录所有信息
   *
   * @param write true 将信息写入文件
   * @param info 切片信息
   */
  private void addIndexInfo(boolean write, FileOutputStream fos, String info)
      throws IOException {
    if (!write) {
      return;
    }
    fos.write(info.concat("\r\n").getBytes(Charset.forName("UTF-8")));
  }

  /**
   * 是否停止获取切片信息，{@code true}停止获取切片信息
   */
  public void setStop(boolean isStop) {
    this.isStop = isStop;
  }

  /**
   * 直播切片信息获取回调
   */
  public void setOnGetPeerCallback(OnGetLivePeerCallback peerCallback) {
    onGetPeerCallback = peerCallback;
  }

  /**
   * 获取加密的密钥信息
   */
  private void getKeyInfo(String tsListUrl, String line) {
    String temp = line.substring(line.indexOf(":") + 1);
    String[] params = temp.split(",");
    M3U8Entity m3U8Entity = mEntity.getM3U8Entity();
    for (String param : params) {
      if (param.startsWith("METHOD")) {
        m3U8Entity.method = param.split("=")[1];
      } else if (param.startsWith("URI")) {
        m3U8Entity.keyUrl = param.split("=")[1].replaceAll("\"", "");
        String keyPath;
        if (((M3U8TaskOption) mTaskWrapper.getM3u8Option()).getKeyPath() == null) {
          keyPath = new File(mEntity.getFilePath()).getParent() + "/"
              + CommonUtil.getStrMd5(m3U8Entity.keyUrl) + ".key";
        } else {
          keyPath = ((M3U8TaskOption) mTaskWrapper.getM3u8Option()).getKeyPath();
        }
        m3U8Entity.keyPath = keyPath;
      } else if (param.startsWith("IV")) {
        m3U8Entity.iv = param.split("=")[1];
      } else if (param.startsWith("KEYFORMAT")) {
        m3U8Entity.keyFormat = param.split("=")[1];
      } else if (param.startsWith("KEYFORMATVERSIONS")) {
        m3U8Entity.keyFormatVersion = param.split("=")[1];
      }
    }
    downloadKey(tsListUrl, m3U8Entity);
  }

  /**
   * 读取bandwidth
   */
  private int getBandWidth(String line) {
    Pattern p = Pattern.compile(Regular.BANDWIDTH);
    Matcher m = p.matcher(line);
    if (m.find()) {
      return Integer.parseInt(m.group());
    }
    return 0;
  }

  /**
   * 处理30x跳转
   */
  private void handleUrlReTurn(HttpURLConnection conn, String newUrl) throws IOException {
    ALog.d(TAG, "30x跳转，新url为【" + newUrl + "】");
    if (TextUtils.isEmpty(newUrl) || newUrl.equalsIgnoreCase("null")) {
      if (mCallback != null) {
        mCallback.onFail(mEntity, new AriaM3U8Exception("获取重定向链接失败"), false);
      }
      return;
    }

    if (newUrl.startsWith("/")) {
      Uri uri = Uri.parse(mEntity.getUrl());
      newUrl = uri.getHost() + newUrl;
    }

    if (!CheckUtil.checkUrl(newUrl)) {
      failDownload("下载失败，重定向url错误", false);
      return;
    }
    mHttpOption.setRedirectUrl(newUrl);
    mEntity.setRedirect(true);
    mEntity.setRedirectUrl(newUrl);
    String cookies = conn.getHeaderField("Set-Cookie");
    conn.disconnect(); // 关闭上一个连接
    URL url = ConnectionHelp.handleUrl(newUrl, mHttpOption);
    conn = ConnectionHelp.handleConnection(url, mHttpOption);
    ConnectionHelp.setConnectParam(mHttpOption, conn);
    conn.setRequestProperty("Cookie", cookies);
    conn.setConnectTimeout(mConnectTimeOut);
    conn.connect();
    handleConnect(newUrl, conn);
    conn.disconnect();
  }

  /**
   * 处理码率
   */
  private void handleBandWidth(HttpURLConnection conn, String bandWidthM3u8Url) throws IOException {
    IBandWidthUrlConverter converter = mM3U8Option.isUseDefConvert() ? new BandWidthDefConverter()
        : mM3U8Option.getBandWidthUrlConverter();
    if (converter != null) {
      bandWidthM3u8Url = converter.convert(mEntity.getUrl(), bandWidthM3u8Url);
      if (!bandWidthM3u8Url.startsWith("http")) {
        failDownload(String.format("码率转换器转换后的url地址无效，转换后的url：%s", bandWidthM3u8Url), false);
        return;
      }
    } else {
      ALog.d(TAG, "没有设置码率转换器");
    }
    mM3U8Option.setBandWidthUrl(bandWidthM3u8Url);
    ALog.d(TAG, String.format("新码率url：%s", bandWidthM3u8Url));
    String cookies = conn.getHeaderField("Set-Cookie");
    conn.disconnect();    // 关闭上一个连接
    URL url = ConnectionHelp.handleUrl(bandWidthM3u8Url, mHttpOption);
    conn = ConnectionHelp.handleConnection(url, mHttpOption);
    ConnectionHelp.setConnectParam(mHttpOption, conn);
    conn.setRequestProperty("Cookie", cookies);
    conn.setConnectTimeout(mConnectTimeOut);
    conn.connect();
    handleConnect(bandWidthM3u8Url, conn);
    conn.disconnect();
  }

  private void failDownload(String errorInfo, boolean needRetry) {
    if (isStop) {
      return;
    }
    mCallback.onFail(mEntity, new AriaM3U8Exception(errorInfo), needRetry);
  }

  /**
   * 密钥不存在，下载密钥
   */
  private void downloadKey(String tsListUr, M3U8Entity info) {
    HttpURLConnection conn = null;
    FileOutputStream fos = null;
    try {
      File keyF = new File(info.keyPath);
      if (!keyF.exists()) {
        ALog.d(TAG, "密钥不存在，下载密钥");
        FileUtil.createFile(keyF);
      } else {
        return;
      }

      IKeyUrlConverter keyUrlConverter = mM3U8Option.getKeyUrlConverter();
      String keyUrl = info.keyUrl;
      if (keyUrlConverter != null) {
        keyUrl = keyUrlConverter.convert(mEntity.getUrl(), tsListUr, keyUrl);
      }
      if (TextUtils.isEmpty(keyUrl)) {
        ALog.e(TAG, "m3u8密钥key url 为空");
        return;
      }

      URL url = ConnectionHelp.handleUrl(keyUrl, mHttpOption);
      conn = ConnectionHelp.handleConnection(url, mHttpOption);
      ConnectionHelp.setConnectParam(mHttpOption, conn);
      conn.setConnectTimeout(mConnectTimeOut);
      conn.connect();
      InputStream is = ConnectionHelp.convertInputStream(conn);
      fos = new FileOutputStream(keyF);
      byte[] buffer = new byte[1024];
      int len;
      while ((len = is.read(buffer)) != -1) {
        fos.write(buffer, 0, len);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fos != null) {
          fos.close();
        }
        if (conn != null) {
          conn.disconnect();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
