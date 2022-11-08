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
package com.arialyy.aria.ftp;

import android.text.TextUtils;
import aria.apache.commons.net.ftp.FTPClientConfig;
import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.common.FtpConnectionMode;
import com.arialyy.aria.core.inf.ITaskOption;
import com.arialyy.aria.core.processor.FtpInterceptHandler;
import com.arialyy.aria.core.processor.IFtpUploadInterceptor;
import java.lang.ref.SoftReference;
import java.net.Proxy;

/**
 * fTP任务设置的信息，如：用户名、密码、端口等信息
 */
public final class FtpTaskOption implements ITaskOption {

  /**
   * 账号和密码
   */
  private FtpUrlEntity urlEntity;

  private Proxy proxy;

  /**
   * 字符编码，默认为"utf-8"
   */
  private String charSet = "utf-8";

  /**
   * 上传拦截器
   */
  private SoftReference<IFtpUploadInterceptor> uploadInterceptor;

  /**
   * 上传到服务器文件的新文件名{@link FtpInterceptHandler#getNewFileName()}
   */
  private String newFileName;

  /**
   * client配置信息
   */
  private FTPClientConfig clientConfig;

  /**
   * 连接模式，默认为被动模式
   * {@link FtpConnectionMode}
   */
  private int connMode;

  /**
   * 主动模式下的端口范围
   */
  private int minPort, maxPort;

  /**
   * 主动模式下，对外ip（可被Ftp服务器访问的ip）
   */
  private String activeExternalIPAddress;

  //---------------- ftp client 配置信息 start
  private String defaultDateFormatStr = null;
  private String recentDateFormatStr = null;
  private String serverLanguageCode = null;
  private String shortMonthNames = null;
  private String serverTimeZoneId = null;
  private String systemKey = FTPClientConfig.SYST_UNIX;
  //---------------- ftp client 配置信息 end

  public String getActiveExternalIPAddress() {
    return activeExternalIPAddress;
  }

  public void setActiveExternalIPAddress(String activeExternalIPAddress) {
    this.activeExternalIPAddress = activeExternalIPAddress;
  }

  public int getMinPort() {
    return minPort;
  }

  public void setMinPort(int minPort) {
    this.minPort = minPort;
  }

  public int getMaxPort() {
    return maxPort;
  }

  public void setMaxPort(int maxPort) {
    this.maxPort = maxPort;
  }

  public int getConnMode() {
    return connMode;
  }

  public void setConnMode(int connMode) {
    this.connMode = connMode;
  }

  public FTPClientConfig getClientConfig() {
    if (clientConfig == null) {
      clientConfig = new FTPClientConfig(systemKey);
      if (defaultDateFormatStr != null) {
        clientConfig.setDefaultDateFormatStr(defaultDateFormatStr);
      }
      if (recentDateFormatStr != null) {
        clientConfig.setRecentDateFormatStr(recentDateFormatStr);
      }
      if (serverLanguageCode != null) {
        clientConfig.setServerLanguageCode(serverLanguageCode);
      }
      if (shortMonthNames != null) {
        clientConfig.setShortMonthNames(shortMonthNames);
      }
      if (serverTimeZoneId != null) {
        clientConfig.setServerTimeZoneId(serverTimeZoneId);
      }
    }
    return clientConfig;
  }

  public void setClientConfig(FTPClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  public String getNewFileName() {
    return newFileName;
  }

  public void setNewFileName(String newFileName) {
    this.newFileName = newFileName;
  }

  public IFtpUploadInterceptor getUploadInterceptor() {
    return uploadInterceptor == null ? null : uploadInterceptor.get();
  }

  public void setUploadInterceptor(IFtpUploadInterceptor uploadInterceptor) {
    this.uploadInterceptor = new SoftReference<>(uploadInterceptor);
  }

  public FtpUrlEntity getUrlEntity() {
    return urlEntity;
  }

  public void setUrlEntity(FtpUrlEntity urlEntity) {
    this.urlEntity = urlEntity;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public String getCharSet() {
    return TextUtils.isEmpty(charSet) ? "UTF-8" : charSet;
  }

  public void setCharSet(String charSet) {
    this.charSet = charSet;
  }
}
