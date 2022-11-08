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
package com.arialyy.aria.http.upload;

import android.text.TextUtils;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.exception.AriaHTTPException;
import com.arialyy.aria.http.BaseHttpThreadTaskAdapter;
import com.arialyy.aria.http.ConnectionHelp;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aria.Lao on 2017/7/28. 不支持断点的HTTP上传任务
 */
final class HttpUThreadTaskAdapter extends BaseHttpThreadTaskAdapter {

  private final String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
  private final String PREFIX = "--", LINE_END = "\r\n";
  private HttpURLConnection mHttpConn;
  private OutputStream mOutputStream;

  HttpUThreadTaskAdapter(SubThreadConfig config) {
    super(config);
  }

  @Override protected void handlerThreadTask() {
    File uploadFile = new File(getEntity().getFilePath());
    if (!uploadFile.exists()) {
      fail(new AriaHTTPException(
          String.format("上传失败，文件不存在；filePath: %s, url: %s", getEntity().getFilePath(),
              getEntity().getUrl())));
      return;
    }
    URL url;
    try {
      url = new URL(CommonUtil.convertUrl(getThreadConfig().url));

      mHttpConn = (HttpURLConnection) url.openConnection();
      mHttpConn.setRequestMethod(mTaskOption.getRequestEnum().name);
      mHttpConn.setUseCaches(false);
      mHttpConn.setDoOutput(true);
      mHttpConn.setDoInput(true);
      mHttpConn.setRequestProperty("Connection", "Keep-Alive");
      mHttpConn.setRequestProperty("Content-Type",
          getContentType() + "; boundary=" + BOUNDARY);
      mHttpConn.setRequestProperty("User-Agent", getUserAgent());
      mHttpConn.setConnectTimeout(getTaskConfig().getConnectTimeOut());
      mHttpConn.setReadTimeout(getTaskConfig().getIOTimeOut());
      //内部缓冲区---分段上传防止oom
      mHttpConn.setChunkedStreamingMode(getTaskConfig().getBuffSize());

      //添加Http请求头部
      Set<String> keys = mTaskOption.getHeaders().keySet();
      for (String key : keys) {
        mHttpConn.setRequestProperty(key, mTaskOption.getHeaders().get(key));
      }
      mOutputStream = mHttpConn.getOutputStream();
      PrintWriter writer =
          new PrintWriter(new OutputStreamWriter(mOutputStream, mTaskOption.getCharSet()), true);
      // 添加参数
      Map<String, String> params = mTaskOption.getParams();
      if (params != null && !params.isEmpty()) {
        for (String key : params.keySet()) {
          addFormField(writer, key, params.get(key));
        }
      }

      //添加文件上传表单字段
      keys = mTaskOption.getFormFields().keySet();
      for (String key : keys) {
        addFormField(writer, key, mTaskOption.getFormFields().get(key));
      }

      uploadFile(writer, mTaskOption.getAttachment(), uploadFile);
      finish(writer);
    } catch (Exception e) {
      e.printStackTrace();
      fail(new AriaHTTPException(
          String.format("上传失败，filePath: %s, url: %s", getEntity().getFilePath(),
              getEntity().getUrl()), e));
    }
  }

  private void fail(AriaHTTPException e1) {
    try {
      fail(e1, false);
      if (mOutputStream != null) {
        mOutputStream.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String getContentType() {
    //return (mTaskOption.getHeaders() == null || TextUtils.isEmpty(
    //    mTaskOption.getHeaders().get("Content-Type"))) ? "multipart/form-data"
    //    : mTaskOption.getHeaders().get("Content-Type");
    return "multipart/form-data";
  }

  private String getUserAgent() {
    return (mTaskOption.getHeaders() == null || TextUtils.isEmpty(
        mTaskOption.getHeaders().get("User-Agent")))
        ? "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)"
        : mTaskOption.getHeaders().get("User-Agent");
  }

  /**
   * 添加文件上传表单字段
   */
  private void addFormField(PrintWriter writer, String name, String value) {
    writer.append(PREFIX).append(BOUNDARY).append(LINE_END);
    writer.append("Content-Disposition: form-data; name=\"")
        .append(name)
        .append("\"")
        .append(LINE_END);
    writer.append("Content-Type: text/plain; charset=")
        .append(mTaskOption.getCharSet())
        .append(LINE_END);
    writer.append(LINE_END);
    writer.append(value).append(LINE_END);
    writer.flush();
  }

  /**
   * 上传文件
   *
   * @param attachment 文件上传attachment
   * @throws IOException
   */
  private void uploadFile(PrintWriter writer, String attachment, File uploadFile)
      throws IOException {
    writer.append(PREFIX).append(BOUNDARY).append(LINE_END);
    writer.append("Content-Disposition: form-data; name=\"")
        .append(attachment)
        .append("\"; filename=\"")
        .append(getEntity().getFileName())
        .append("\"")
        .append(LINE_END);
    writer.append("Content-Type: ")
        .append(URLConnection.guessContentTypeFromName(getEntity().getFileName()))
        .append(LINE_END);
    writer.append("Content-Transfer-Encoding: binary").append(LINE_END);
    writer.append(LINE_END);
    writer.flush();

    FileInputStream inputStream = new FileInputStream(uploadFile);
    byte[] buffer = new byte[4096];
    int bytesLen;
    while ((bytesLen = inputStream.read(buffer)) != -1) {
      mOutputStream.write(buffer, 0, bytesLen);
      progress(bytesLen);
      if (getThreadTask().isBreak()) {
        break;
      }
      if (mSpeedBandUtil != null) {
        mSpeedBandUtil.limitNextBytes(bytesLen);
      }
    }

    mOutputStream.flush();
    inputStream.close();
    writer.append(LINE_END).flush();
    // 保证上传的文件和本地的一致，https://www.cnblogs.com/tugenhua0707/p/8975121.html
    writer.append(PREFIX).append(BOUNDARY).append(PREFIX).append(LINE_END).flush();
  }

  /**
   * 任务结束操作
   *
   * @throws IOException
   */
  private String finish(PrintWriter writer) throws IOException {
    StringBuilder response = new StringBuilder();
    int status = mHttpConn.getResponseCode();

    if (status == HttpURLConnection.HTTP_OK) {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(ConnectionHelp.convertInputStream(mHttpConn)));
      String line;
      while (getThreadTask().isLive() && (line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      mHttpConn.disconnect();
      getEntity().setResponseStr(response.toString());
      complete();
    } else {
      String msg = "response msg: " + mHttpConn.getResponseMessage() + "，code: " + status;
      ALog.e(TAG, msg);
      getEntity().setResponseStr(response.toString());
      fail(new AriaHTTPException(msg), false);
      response.append(status);
    }
    writer.flush();
    writer.close();
    mOutputStream.close();
    return response.toString();
  }

  @Override protected UploadEntity getEntity() {
    return (UploadEntity) super.getEntity();
  }
}
