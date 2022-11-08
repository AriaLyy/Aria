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
package com.arialyy.aria.ftp.upload;

import android.text.TextUtils;
import aria.apache.commons.net.ftp.FTPClient;
import aria.apache.commons.net.ftp.FTPReply;
import aria.apache.commons.net.ftp.OnFtpInputStreamListener;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.exception.AriaFTPException;
import com.arialyy.aria.ftp.BaseFtpThreadTaskAdapter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aria.Lao on 2017/7/28. D_FTP 单线程上传任务，需要FTP 服务器给用户打开append和write的权限
 */
final class FtpUThreadTaskAdapter extends BaseFtpThreadTaskAdapter {
  private String dir, remotePath;
  private boolean storeSuccess = false;
  private ScheduledThreadPoolExecutor timer;
  private FTPClient client = null;
  private boolean isTimeOut = true;
  private FtpFISAdapter fa;

  FtpUThreadTaskAdapter(SubThreadConfig config) {
    super(config);
  }

  @Override protected void handlerThreadTask() {
    BufferedRandomAccessFile file = null;
    try {
      ALog.d(TAG,
          String.format("任务【%s】线程__%s__开始上传【开始位置 : %s，结束位置：%s】", getEntity().getKey(),
              getThreadRecord().threadId, getThreadRecord().startLocation,
              getThreadRecord().endLocation));
      client = createClient();
      if (client == null) {
        return;
      }
      initPath();
      boolean b = client.makeDirectory(dir);
      if (!b) {
        ALog.w(TAG, String.format("创建目录失败，错误码为：%s, msg：%s", client.getReplyCode(),
            client.getReplyString()));
      }
      client.changeWorkingDirectory(dir);
      client.setRestartOffset(getThreadRecord().startLocation);
      int reply = client.getReplyCode();
      if (!FTPReply.isPositivePreliminary(reply) && reply != FTPReply.FILE_ACTION_OK) {
        fail(new AriaFTPException(
            String.format("文件上传错误，错误码为：%s, msg：%s, filePath: %s", reply,
                client.getReplyString(), getEntity().getFilePath())), false);
        client.disconnect();
        return;
      }

      file =
          new BufferedRandomAccessFile(getThreadConfig().tempFile, "rwd",
              getTaskConfig().getBuffSize());
      if (getThreadRecord().startLocation > 0) {
        file.seek(getThreadRecord().startLocation);
      }
      boolean complete = upload(file);
      if (getThreadTask().isBreak()) {
        return;
      }
      if (!complete) {
        fail(new AriaFTPException("ftp文件上传失败"), false);
        return;
      }
      ALog.i(TAG,
          String.format("任务【%s】线程__%s__上传完毕", getEntity().getKey(), getThreadRecord().threadId));
      complete();
    } catch (IOException e) {
      e.printStackTrace();
      fail(new AriaFTPException(
          String.format("上传失败，filePath: %s, uploadUrl: %s", getEntity().getFilePath(),
              getThreadConfig().url)), true);
    } catch (Exception e) {
      fail(new AriaFTPException(null, e), false);
    } finally {
      try {
        if (file != null) {
          file.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      closeClient(client);
      closeTimer();
    }
  }

  private UploadEntity getEntity() {
    return (UploadEntity) getTaskWrapper().getEntity();
  }

  private void initPath() throws UnsupportedEncodingException {
    dir = CommonUtil.convertFtpChar(charSet, mTaskOption.getUrlEntity().remotePath);

    String fileName = TextUtils.isEmpty(mTaskOption.getNewFileName()) ? getEntity().getFileName()
        : mTaskOption.getNewFileName();

    remotePath =
        CommonUtil.convertFtpChar(charSet,
            String.format("%s/%s", mTaskOption.getUrlEntity().remotePath, fileName));
  }

  /**
   * 启动监听定时器，当网络断开时，如果该任务的FTP服务器的传输线程没有断开，当客户端重新连接时，客户端将无法发送数据到服务端
   * 每隔10s检查一次。
   */
  private void startTimer() {
    timer = new ScheduledThreadPoolExecutor(1);
    timer.scheduleWithFixedDelay(new Runnable() {
      @Override public void run() {
        try {
          if (isTimeOut) {
            fail(new AriaFTPException("socket连接失败，该问题一般出现于网络断开，客户端重新连接，"
                + "但是服务器端无法创建socket缺没有返回错误码的情况。"), false);
            if (fa != null) {
              fa.close();
            }
            closeTimer();
          }
          isTimeOut = true;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }, 10, 10, TimeUnit.SECONDS);
  }

  private void closeTimer() {
    ALog.d(TAG, "closeTimer");
    if (timer != null && !timer.isShutdown()) {
      timer.shutdown();
    }
  }

  /**
   * 上传
   *
   * @return {@code true}上传成功、{@code false} 上传失败
   */
  private boolean upload(final BufferedRandomAccessFile bis)
      throws IOException {
    fa = new FtpFISAdapter(bis);
    storeSuccess = false;
    startTimer();
    try {
      ALog.d(TAG, String.format("remotePath: %s", remotePath));
      storeSuccess = client.storeFile(remotePath, fa, new OnFtpInputStreamListener() {
        boolean isStoped = false;

        @Override public void onFtpInputStream(FTPClient client, long totalBytesTransferred,
            int bytesTransferred, long streamSize) {
          try {
            isTimeOut = false;
            if (getThreadTask().isBreak() && !isStoped) {
              isStoped = true;
              client.abor();
              return;
            }
            if (mSpeedBandUtil != null) {
              mSpeedBandUtil.limitNextBytes(bytesTransferred);
            }
            progress(bytesTransferred);
          } catch (IOException e) {
            e.printStackTrace();
            storeSuccess = false;
          }
        }
      });
    } catch (IOException e) {
      String msg = String.format("文件上传错误，错误码为：%s, msg：%s, filePath: %s", client.getReply(),
          client.getReplyString(), getEntity().getFilePath());
      e.printStackTrace();
      if (e.getMessage().contains("AriaIOException caught while copying")) {
        e.printStackTrace();
      } else {
        fail(new AriaFTPException(msg, e), !storeSuccess);
      }
      return false;
    } finally {
      fa.close();
    }
    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      if (reply != FTPReply.TRANSFER_ABORTED) {
        fail(new AriaFTPException(
            String.format("文件上传错误，错误码为：%s, msg：%s, filePath: %s", reply, client.getReplyString(),
                getEntity().getFilePath())), false);
      }
      closeClient(client);
      return false;
    }
    return storeSuccess;
  }
}
