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
import aria.apache.commons.net.ftp.FTP;
import aria.apache.commons.net.ftp.FTPClient;
import aria.apache.commons.net.ftp.FTPClientConfig;
import aria.apache.commons.net.ftp.FTPReply;
import aria.apache.commons.net.ftp.FTPSClient;
import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.common.FtpConnectionMode;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.task.AbsThreadTaskAdapter;
import com.arialyy.aria.exception.AriaFTPException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.SSLContextUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.net.ssl.SSLContext;

/**
 * @author lyy
 * Date: 2019-09-18
 */
public abstract class BaseFtpThreadTaskAdapter extends AbsThreadTaskAdapter {

  protected FtpTaskOption mTaskOption;
  protected String charSet;

  protected BaseFtpThreadTaskAdapter(SubThreadConfig config) {
    super(config);
    mTaskOption = (FtpTaskOption) getTaskWrapper().getTaskOption();
  }

  protected void closeClient(FTPClient client) {
    try {
      if (client != null && client.isConnected()) {
        client.logout();
        client.disconnect();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 构建FTP客户端
   */
  protected FTPClient createClient() {
    FTPClient client = null;
    final FtpUrlEntity urlEntity = mTaskOption.getUrlEntity();
    if (urlEntity.validAddr == null) {
      try {
        InetAddress[] ips = InetAddress.getAllByName(urlEntity.hostName);
        client = connect(newInstanceClient(urlEntity), ips, 0, Integer.parseInt(urlEntity.port));
        if (client == null) {
          return null;
        }
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
    } else {
      client = newInstanceClient(urlEntity);
      try {
        client.connect(urlEntity.validAddr, Integer.parseInt(urlEntity.port));
      } catch (java.io.IOException e) {
        ALog.e(TAG, ALog.getExceptionString(e));
        return null;
      }
    }

    if (client == null) {
      return null;
    }

    try {
      if (urlEntity.isFtps) {
        FTPSClient sClient = (FTPSClient) client;
        sClient.execPBSZ(0);
        sClient.execPROT("P");
      }

      if (urlEntity.needLogin) {
        if (TextUtils.isEmpty(urlEntity.account)) {
          client.login(urlEntity.user, urlEntity.password);
        } else {
          client.login(urlEntity.user, urlEntity.password, urlEntity.account);
        }
      }
      int reply = client.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        client.disconnect();
        fail(new AriaFTPException(
            String.format("无法连接到ftp服务器，错误码为：%s，msg:%s", reply, client.getReplyString())), false);
        return null;
      }
      // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码
      charSet = "UTF-8";
      if (reply != FTPReply.COMMAND_IS_SUPERFLUOUS) {
        if (!TextUtils.isEmpty(mTaskOption.getCharSet())) {
          charSet = mTaskOption.getCharSet();
        }
      }
      client.setControlEncoding(charSet);
      //client.setDataTimeout(getTaskConfig().getIOTimeOut());
      client.setDataTimeout(1000);
      client.setConnectTimeout(getTaskConfig().getConnectTimeOut());
      if (mTaskOption.getConnMode() == FtpConnectionMode.DATA_CONNECTION_MODE_ACTIVITY) {
        client.enterLocalActiveMode();
        if (mTaskOption.getMinPort() != 0 && mTaskOption.getMaxPort() != 0) {
          client.setActivePortRange(mTaskOption.getMinPort(), mTaskOption.getMaxPort());
        }
        if (!TextUtils.isEmpty(mTaskOption.getActiveExternalIPAddress())) {
          client.setActiveExternalIPAddress(mTaskOption.getActiveExternalIPAddress());
        }
      } else {
        client.enterLocalPassiveMode();
      }
      client.setFileType(FTP.BINARY_FILE_TYPE);
      client.setControlKeepAliveTimeout(5000);
    } catch (IOException e) {
      closeClient(client);
      e.printStackTrace();
    }
    return client;
  }

  /**
   * 创建FTP/FTPS客户端
   */
  private FTPClient newInstanceClient(FtpUrlEntity urlEntity) {
    FTPClient temp;
    if (urlEntity.isFtps) {
      FTPSClient sClient;
      SSLContext sslContext = SSLContextUtil.getSSLContext(
          urlEntity.idEntity.keyAlias, urlEntity.idEntity.storePath, urlEntity.protocol);
      if (sslContext == null) {
        sClient = new FTPSClient(urlEntity.protocol, urlEntity.isImplicit);
      } else {
        sClient = new FTPSClient(true, sslContext);
      }

      temp = sClient;
    } else {
      temp = new FTPClient();
    }

    FTPClientConfig clientConfig;
    if (mTaskOption.getClientConfig() != null) {
      clientConfig = mTaskOption.getClientConfig();
    } else {
      clientConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
      clientConfig.setServerLanguageCode("en");
    }
    temp.configure(clientConfig);

    return temp;
  }

  /**
   * 连接到ftp服务器
   */
  private FTPClient connect(FTPClient client, InetAddress[] ips, int index, int port) {
    try {
      client.connect(ips[index], port);
      mTaskOption.getUrlEntity().validAddr = ips[index];
      return client;
    } catch (java.io.IOException e) {
      try {
        if (client.isConnected()) {
          client.disconnect();
        }
      } catch (java.io.IOException e1) {
        e1.printStackTrace();
      }
      if (index + 1 >= ips.length) {
        ALog.w(TAG, "遇到[ECONNREFUSED-连接被服务器拒绝]错误，已没有其他地址，链接失败");
        return null;
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      ALog.w(TAG, "遇到[ECONNREFUSED-连接被服务器拒绝]错误，正在尝试下一个地址");
      return connect(new FTPClient(), ips, index + 1, port);
    }
  }
}


