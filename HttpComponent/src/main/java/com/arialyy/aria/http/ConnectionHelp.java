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
package com.arialyy.aria.http;

import android.text.TextUtils;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.ProtocolType;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.SSLContextUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by lyy on 2017/1/18. 链接帮助类
 */
public final class ConnectionHelp {
  private static final String TAG = "ConnectionHelp";

  /**
   * 处理url参数
   */
  public static URL handleUrl(String url, HttpTaskOption taskDelegate)
      throws MalformedURLException {
    Map<String, String> params = taskDelegate.getParams();
    if (params != null && taskDelegate.getRequestEnum() == RequestEnum.GET) {
      if (url.contains("?")) {
        ALog.e(TAG, String.format("设置参数失败，url中已经有?，url: %s", url));
        return new URL(CommonUtil.convertUrl(url));
      }
      StringBuilder sb = new StringBuilder();
      sb.append(url).append("?");
      Set<String> keys = params.keySet();
      for (String key : keys) {
        sb.append(key).append("=").append(URLEncoder.encode(params.get(key))).append("&");
      }
      String temp = sb.toString();
      temp = temp.substring(0, temp.length() - 1);
      return new URL(CommonUtil.convertUrl(temp));
    } else {
      return new URL(CommonUtil.convertUrl(url));
    }
  }

  /**
   * 转换HttpUrlConnect的inputStream流
   *
   * @return {@link GZIPInputStream}、{@link InflaterInputStream}
   * @throws IOException
   */
  public static InputStream convertInputStream(HttpURLConnection connection) throws IOException {
    String encoding = connection.getHeaderField("Content-Encoding");
    if (TextUtils.isEmpty(encoding)) {
      return connection.getInputStream();
    }
    if (encoding.contains("gzip")) {
      return new GZIPInputStream(connection.getInputStream());
    } else if (encoding.contains("deflate")) {
      return new InflaterInputStream(connection.getInputStream());
    } else {
      return connection.getInputStream();
    }
  }

  /**
   * 处理链接
   *
   * @throws IOException
   */
  public static HttpURLConnection handleConnection(URL url, HttpTaskOption taskDelegate)
      throws IOException {
    HttpURLConnection conn;
    URLConnection urlConn;
    if (taskDelegate.getProxy() != null) {
      urlConn = url.openConnection(taskDelegate.getProxy());
    } else {
      urlConn = url.openConnection();
    }
    if (urlConn instanceof HttpsURLConnection) {
      AriaConfig config = AriaConfig.getInstance();
      conn = (HttpsURLConnection) urlConn;
      SSLContext sslContext =
          SSLContextUtil.getSSLContextFromAssets(config.getDConfig().getCaName(),
              config.getDConfig().getCaPath(), ProtocolType.Default);
      if (sslContext == null) {
        sslContext = SSLContextUtil.getDefaultSLLContext(ProtocolType.Default);
      }
      SSLSocketFactory ssf = sslContext.getSocketFactory();
      ((HttpsURLConnection) conn).setSSLSocketFactory(ssf);
      ((HttpsURLConnection) conn).setHostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
    } else {
      conn = (HttpURLConnection) urlConn;
    }
    return conn;
  }

  /**
   * 设置头部参数
   */
  public static HttpURLConnection setConnectParam(HttpTaskOption delegate, HttpURLConnection conn) {
    if (delegate.getRequestEnum() == RequestEnum.POST) {
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
    }
    Set<String> keys = null;
    if (delegate.getHeaders() != null && delegate.getHeaders().size() > 0) {
      keys = delegate.getHeaders().keySet();
      for (String key : keys) {
        conn.setRequestProperty(key, delegate.getHeaders().get(key));
      }
    }
    if (conn.getRequestProperty("Accept-Language") == null) {
      conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7");
    }
    if (conn.getRequestProperty("Accept-Encoding") == null) {
      conn.setRequestProperty("Accept-Encoding", "identity");
    }
    if (conn.getRequestProperty("Accept-Charset") == null) {
      conn.setRequestProperty("Accept-Charset", "UTF-8");
    }
    if (conn.getRequestProperty("Connection") == null) {
      conn.setRequestProperty("Connection", "Keep-Alive");
    }
    if (conn.getRequestProperty("Charset") == null) {
      conn.setRequestProperty("Charset", "UTF-8");
    }
    if (conn.getRequestProperty("User-Agent") == null) {
      conn.setRequestProperty("User-Agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
    }
    if (conn.getRequestProperty("Accept") == null) {
      StringBuilder accept = new StringBuilder();
      accept.append("image/gif, ")
          .append("image/jpeg, ")
          .append("image/pjpeg, ")
          .append("image/webp, ")
          .append("image/apng, ")
          .append("application/xml, ")
          .append("application/xaml+xml, ")
          .append("application/xhtml+xml, ")
          .append("application/x-shockwave-flash, ")
          .append("application/x-ms-xbap, ")
          .append("application/x-ms-application, ")
          .append("application/msword, ")
          .append("application/vnd.ms-excel, ")
          .append("application/vnd.ms-xpsdocument, ")
          .append("application/vnd.ms-powerpoint, ")
          .append("application/signed-exchange, ")
          .append("text/plain, ")
          .append("text/html, ")
          .append("*/*");
      conn.setRequestProperty("Accept", accept.toString());
    }
    //302获取重定向地址
    conn.setInstanceFollowRedirects(false);

    CookieManager manager = delegate.getCookieManager();
    if (manager != null) {
      CookieStore store = manager.getCookieStore();
      if (store != null && store.getCookies().size() > 0) {
        conn.setRequestProperty("Cookie",
            TextUtils.join(";", store.getCookies()));
      }
    }

    return conn;
  }
}
