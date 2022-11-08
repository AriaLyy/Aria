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

import android.net.Uri;
import android.text.TextUtils;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.AbsThreadTaskAdapter;
import com.arialyy.aria.exception.AriaM3U8Exception;
import com.arialyy.aria.http.ConnectionHelp;
import com.arialyy.aria.http.HttpTaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyy on 2017/1/18. 下载线程
 */
public final class M3U8ThreadTaskAdapter extends AbsThreadTaskAdapter {
  private final String TAG = CommonUtil.getClassName(this);
  private HttpTaskOption mHttpTaskOption;
  private BufferedInputStream is = null;

  public M3U8ThreadTaskAdapter(SubThreadConfig config) {
    super(config);
    mHttpTaskOption = (HttpTaskOption) getTaskWrapper().getTaskOption();
  }

  @Override protected void handlerThreadTask() {
    if (getThreadRecord().isComplete) {
      handleComplete();
      return;
    }
    HttpURLConnection conn = null;
    try {
      URL url = ConnectionHelp.handleUrl(getThreadConfig().url, mHttpTaskOption);
      conn = ConnectionHelp.handleConnection(url, mHttpTaskOption);
      ALog.d(TAG, String.format("分片【%s】开始下载", getThreadRecord().threadId));

      if (mHttpTaskOption.isChunked()) {
        conn.setDoInput(true);
        conn.setChunkedStreamingMode(0);
      }
      // 传递参数
      if (mHttpTaskOption.getRequestEnum() == RequestEnum.POST) {
        Map<String, String> params = mHttpTaskOption.getParams();
        if (params != null) {
          OutputStreamWriter dos = new OutputStreamWriter(conn.getOutputStream());
          Set<String> keys = params.keySet();
          StringBuilder sb = new StringBuilder();
          for (String key : keys) {
            sb.append(key).append("=").append(URLEncoder.encode(params.get(key))).append("&");
          }
          String paramStr = sb.toString();
          paramStr = paramStr.substring(0, paramStr.length() - 1);
          dos.write(paramStr);
          dos.flush();
          dos.close();
        }
      }

      handleConn(conn);
    } catch (MalformedURLException e) {
      fail(new AriaM3U8Exception(
          String.format("分片【%s】下载失败，filePath: %s, url: %s", getThreadRecord().threadId,
              getThreadConfig().tempFile.getPath(), getEntity().getUrl()), e), false);
    } catch (IOException e) {
      fail(new AriaM3U8Exception(
          String.format("分片【%s】下载失败，filePath: %s, url: %s", getThreadRecord().threadId,
              getThreadConfig().tempFile.getPath(), getEntity().getUrl()), e), true);
    } catch (Exception e) {
      fail(new AriaM3U8Exception(
          String.format("分片【%s】下载失败，filePath: %s, url: %s", getThreadRecord().threadId,
              getThreadConfig().tempFile.getPath(), getEntity().getUrl()), e), false);
    } finally {
      try {
        if (is != null) {
          is.close();
        }
        if (conn != null) {
          conn.disconnect();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void handleConn(HttpURLConnection conn) throws IOException {
    ConnectionHelp.setConnectParam(mHttpTaskOption, conn);
    conn.setConnectTimeout(getTaskConfig().getConnectTimeOut());
    conn.setReadTimeout(getTaskConfig().getIOTimeOut());  //设置读取流的等待时间,必须设置该参数

    conn.connect();
    int code = conn.getResponseCode();
    if (code == HttpURLConnection.HTTP_OK) {
      is = new BufferedInputStream(ConnectionHelp.convertInputStream(conn));
      if (mHttpTaskOption.isChunked()) {
        readChunked(is);
      } else if (getThreadConfig().isBlock) {
        readDynamicFile(is);
      }
    } else if (code == HttpURLConnection.HTTP_MOVED_TEMP
        || code == HttpURLConnection.HTTP_MOVED_PERM
        || code == HttpURLConnection.HTTP_SEE_OTHER
        || code == HttpURLConnection.HTTP_CREATED // 201 跳转
        || code == 307) {
      handleUrlReTurn(conn, conn.getHeaderField("Location"));
    } else {
      fail(new AriaM3U8Exception(
              String.format("连接错误，http错误码：%s，url：%s", code, getThreadConfig().url)),
          false);
    }
    conn.disconnect();
  }

  /**
   * 处理30x跳转
   */
  private void handleUrlReTurn(HttpURLConnection conn, String newUrl) throws IOException {
    ALog.d(TAG, "30x跳转，新url为【" + newUrl + "】");
    if (TextUtils.isEmpty(newUrl) || newUrl.equalsIgnoreCase("null")) {
      fail(new AriaM3U8Exception("下载失败，重定向url为空"), false);
      return;
    }

    if (newUrl.startsWith("/")) {
      Uri uri = Uri.parse(getThreadConfig().url);
      newUrl = uri.getHost() + newUrl;
    }

    if (!CheckUtil.checkUrl(newUrl)) {
      fail(new AriaM3U8Exception("下载失败，重定向url错误"), false);
      return;
    }
    String cookies = conn.getHeaderField("Set-Cookie");
    conn.disconnect(); // 关闭上一个连接
    URL url = ConnectionHelp.handleUrl(newUrl, mHttpTaskOption);
    conn = ConnectionHelp.handleConnection(url, mHttpTaskOption);
    if (!TextUtils.isEmpty(cookies)) {
      conn.setRequestProperty("Cookie", cookies);
    }
    if (mHttpTaskOption.isChunked()) {
      conn.setDoInput(true);
      conn.setChunkedStreamingMode(0);
    }
    handleConn(conn);
  }

  /**
   * 读取chunked数据
   */
  private void readChunked(InputStream is) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(getThreadConfig().tempFile, true);
      byte[] buffer = new byte[getTaskConfig().getBuffSize()];
      int len;
      while (getThreadTask().isLive() && (len = is.read(buffer)) != -1) {
        if (getThreadTask().isBreak()) {
          break;
        }
        if (mSpeedBandUtil != null) {
          mSpeedBandUtil.limitNextBytes(len);
        }
        fos.write(buffer, 0, len);
        progress(len);
      }
      handleComplete();
    } catch (IOException e) {
      fail(new AriaM3U8Exception(
          String.format("文件下载失败，savePath: %s, url: %s", getThreadConfig().tempFile.getPath(),
              getThreadConfig().url), e), true);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 动态长度文件读取方式
   */
  private void readDynamicFile(InputStream is) {
    FileOutputStream fos = null;
    FileChannel foc = null;
    ReadableByteChannel fic = null;
    try {
      int len;
      fos = new FileOutputStream(getThreadConfig().tempFile, true);
      foc = fos.getChannel();
      fic = Channels.newChannel(is);
      ByteBuffer bf = ByteBuffer.allocate(getTaskConfig().getBuffSize());
      //如果要通过 Future 的 cancel 方法取消正在运行的任务，那么该任务必定是可以 对线程中断做出响应 的任务。

      while (getThreadTask().isLive() && (len = fic.read(bf)) != -1) {
        if (getThreadTask().isBreak()) {
          break;
        }
        if (mSpeedBandUtil != null) {
          mSpeedBandUtil.limitNextBytes(len);
        }
        bf.flip();
        foc.write(bf);
        bf.compact();
        progress(len);
      }
      handleComplete();
    } catch (IOException e) {
      fail(new AriaM3U8Exception(
          String.format("文件下载失败，savePath: %s, url: %s", getThreadConfig().tempFile.getPath(),
              getThreadConfig().url), e), true);
    } finally {
      try {
        if (fos != null) {
          fos.flush();
          fos.close();
        }
        if (foc != null) {
          foc.close();
        }
        if (fic != null) {
          fic.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private DownloadEntity getEntity() {
    return (DownloadEntity) getTaskWrapper().getEntity();
  }

  /**
   * 处理完成配置文件的更新或事件回调
   */
  private void handleComplete() {
    if (getThreadTask().isBreak()) {
      return;
    }
    complete();
  }
}
