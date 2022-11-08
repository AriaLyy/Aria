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

import android.net.TrafficStats;
import android.os.Process;
import android.text.TextUtils;
import aria.apache.commons.net.ftp.FTP;
import aria.apache.commons.net.ftp.FTPClient;
import aria.apache.commons.net.ftp.FTPClientConfig;
import aria.apache.commons.net.ftp.FTPFile;
import aria.apache.commons.net.ftp.FTPReply;
import aria.apache.commons.net.ftp.FTPSClient;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.common.FtpConnectionMode;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.exception.AriaFTPException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.SSLContextUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import javax.net.ssl.SSLContext;

/**
 * Created by Aria.Lao on 2017/7/25. 获取ftp文件夹信息
 */
public abstract class AbsFtpInfoTask<ENTITY extends AbsEntity, TASK_WRAPPER extends AbsTaskWrapper<ENTITY>>
    implements Runnable, IInfoTask {

  protected final String TAG = CommonUtil.getClassName(getClass());
  protected ENTITY mEntity;
  protected TASK_WRAPPER mTaskWrapper;
  protected FtpTaskOption mTaskOption;
  private int mConnectTimeOut;
  protected long mSize = 0;
  protected String charSet = "UTF-8";
  private Callback callback;
  private boolean isStop = false, isCancel = false;

  public AbsFtpInfoTask(TASK_WRAPPER taskWrapper) {
    mTaskWrapper = taskWrapper;
    mEntity = taskWrapper.getEntity();
    mTaskOption = (FtpTaskOption) taskWrapper.getTaskOption();
    mConnectTimeOut = AriaConfig.getInstance().getDConfig().getConnectTimeOut();
  }

  /**
   * 获取请求的远程文件路径
   *
   * @return 远程文件路径
   */
  protected abstract String getRemotePath();

  /**
   * 处理ftp列表信息
   *
   * @param client ftp 客户端对象
   * @param files remotePath 对应的文件列表
   * @param convertedRemotePath 已转换的可被服务器识别的remotePath
   */
  protected abstract void handelFileInfo(FTPClient client, FTPFile[] files,
      String convertedRemotePath) throws IOException;

  @Override public void stop() {
    isStop = true;
  }

  @Override public void cancel() {
    isCancel = true;
  }

  @Override public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @Override
  public void run() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    TrafficStats.setThreadStatsTag(UUID.randomUUID().toString().hashCode());
    FTPClient client = null;
    try {
      client = createFtpClient();
      if (client == null) {
        ALog.e(TAG, String.format("任务【%s】失败", mTaskOption.getUrlEntity().url));
        return;
      }
      String convertedRemotePath = CommonUtil.convertFtpChar(charSet, getRemotePath());
      FTPFile[] files = client.listFiles(convertedRemotePath);
      handelFileInfo(client, files, convertedRemotePath);
    } catch (IOException e) {
      e.printStackTrace();
      handleFail(client, "FTP错误信息", e, true);
    } catch (InterruptedException e) {
      e.printStackTrace();
      handleFail(client, "FTP错误信息", e, true);
    } finally {
      closeClient(client);
    }
  }

  /**
   * 处理拦截
   *
   * @param ftpFiles remotePath路径下的所有文件
   * @return {@code false} 拦截器处理完成任务，任务将不再执行，{@code true} 拦截器处理任务完成任务，任务继续执行
   */
  protected boolean onInterceptor(FTPClient client, FTPFile[] ftpFiles) {
    return true;
  }

  protected void onPreComplete(int code) {

  }

  /**
   * 创建FTP客户端
   */
  private FTPClient createFtpClient() throws IOException, InterruptedException {
    FTPClient client = null;
    final FtpUrlEntity urlEntity = mTaskOption.getUrlEntity();
    if (CheckUtil.checkIp(urlEntity.hostName)) {
      client = newInstanceClient(urlEntity);
      client.setConnectTimeout(mConnectTimeOut);  // 连接10s超时
      InetAddress ip = InetAddress.getByName(urlEntity.hostName);

      client = connect(client, new InetAddress[] { ip }, 0, Integer.parseInt(urlEntity.port));
      mTaskOption.getUrlEntity().validAddr = ip;
    } else {
      DNSQueryThread dnsThread = new DNSQueryThread(urlEntity.hostName);
      dnsThread.start();
      dnsThread.join(mConnectTimeOut);
      InetAddress[] ips = dnsThread.getIps();
      client = connect(newInstanceClient(urlEntity), ips, 0, Integer.parseInt(urlEntity.port));
    }

    if (client == null) {
      handleFail(client, String.format("链接失败, url: %s", mTaskOption.getUrlEntity().url), null,
          true);
      return null;
    }

    boolean loginSuccess = true;
    if (urlEntity.needLogin) {
      try {
        if (TextUtils.isEmpty(urlEntity.account)) {
          loginSuccess = client.login(urlEntity.user, urlEntity.password);
        } else {
          loginSuccess = client.login(urlEntity.user, urlEntity.password, urlEntity.account);
        }
      } catch (IOException e) {
        e.printStackTrace();
        ALog.e(TAG, String.format("登录失败，错误码为：%s， msg：%s", client.getReplyCode(),
            client.getReplyString()));
        return null;
      }
    }

    if (!loginSuccess) {
      handleFail(client, "登录失败", null, false);
      client.disconnect();
      return null;
    }

    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      handleFail(client, String.format("无法连接到ftp服务器，filePath: %s, url: %s", mEntity.getKey(),
          mTaskOption.getUrlEntity().url), null, true);
      client.disconnect();
      return null;
    }
    // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码
    charSet = "UTF-8";
    reply = client.sendCommand("OPTS UTF8", "ON");
    if (reply != FTPReply.COMMAND_IS_SUPERFLUOUS) {
      ALog.i(TAG, "D_FTP 服务器不支持开启UTF8编码，尝试使用Aria手动设置的编码");
      if (!TextUtils.isEmpty(mTaskOption.getCharSet())) {
        charSet = mTaskOption.getCharSet();
      }
    }
    client.setControlEncoding(charSet);
    client.setDataTimeout(10 * 1000);
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

    return client;
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
    if (ips == null || ips.length == 0) {
      ALog.w(TAG, "无可用ip");
      return null;
    }
    try {
      client.setConnectTimeout(mConnectTimeOut);  //需要先设置超时，这样才不会出现阻塞
      client.connect(ips[index], port);
      mTaskOption.getUrlEntity().validAddr = ips[index];

      FtpUrlEntity urlEntity = mTaskOption.getUrlEntity();
      if (urlEntity.isFtps) {
        FTPSClient sClient = (FTPSClient) client;
        sClient.execPBSZ(0);
        sClient.execPROT("P");
      }

      return client;
    } catch (IOException e) {
      e.printStackTrace();
      closeClient(client);
      if (index + 1 >= ips.length) {
        ALog.w(TAG, "遇到[ECONNREFUSED-连接被服务器拒绝]错误，已没有其他地址，链接失败；如果是ftps，请检查端口是否使用了ftp的端口而不是ftps的端口");
        return null;
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      ALog.w(TAG, "遇到[ECONNREFUSED-连接被服务器拒绝]错误，正在尝试下一个地址");
      return connect(newInstanceClient(mTaskOption.getUrlEntity()), ips, index + 1, port);
    }
  }

  /**
   * 遍历FTP服务器上对应文件或文件夹大小
   *
   * @throws IOException 字符串编码转换错误
   */
  protected long getFileSize(FTPFile[] files, FTPClient client, String dirName) throws IOException {
    long size = 0;
    String path = dirName + "/";
    for (FTPFile file : files) {
      if (file.isFile()) {
        size += file.getSize();
        handleFile(client, path + file.getName(), file);
      } else {
        String remotePath = CommonUtil.convertFtpChar(charSet, path + file.getName());
        size += getFileSize(client.listFiles(remotePath), client, path + file.getName());
      }
    }
    return size;
  }

  /**
   * 处理FTP文件信息
   *
   * @param remotePath ftp服务器文件夹路径
   * @param ftpFile ftp服务器上对应的文件
   */
  protected void handleFile(FTPClient client, String remotePath, FTPFile ftpFile) {
  }

  protected void handleFail(FTPClient client, String msg, Exception e, boolean needRetry) {
    if (isStop || isCancel) {
      return;
    }
    if (callback != null) {
      if (client == null) {
        msg = "创建ftp客户端失败";
        needRetry = false;
      } else {
        msg = String.format("%s, code: %s, msg: %s", msg, client.getReplyCode(),
            client.getReplyString());
        needRetry = needRetry && !CheckUtil.ftpIsBadRequest(client.getReplyCode());
      }

      callback.onFail(mEntity, new AriaFTPException(msg), needRetry);
    }
  }

  protected void onSucceed(CompleteInfo info){
    callback.onSucceed(mEntity.getKey(), info);
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }

  /**
   * 获取可用IP的超时线程，InetAddress.getByName没有超时功能，需要自己处理超时
   */
  private static class DNSQueryThread extends Thread {

    private String hostName;
    private InetAddress[] ips;

    DNSQueryThread(String hostName) {
      this.hostName = hostName;
    }

    @Override
    public void run() {
      try {
        ips = InetAddress.getAllByName(hostName);
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
    }

    synchronized InetAddress[] getIps() {
      return ips;
    }
  }
}
