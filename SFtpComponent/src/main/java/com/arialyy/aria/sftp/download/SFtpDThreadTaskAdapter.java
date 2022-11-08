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
package com.arialyy.aria.sftp.download;

import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.task.AbsThreadTaskAdapter;
import com.arialyy.aria.exception.AriaSFTPException;
import com.arialyy.aria.sftp.SFtpTaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * sftp 线程任务适配器
 *
 * @author lyy
 */
final class SFtpDThreadTaskAdapter extends AbsThreadTaskAdapter {
  private ChannelSftp channelSftp;
  private Session session;
  private SFtpTaskOption option;

  SFtpDThreadTaskAdapter(SubThreadConfig config) {
    super(config);
    session = (Session) config.obj;
    option = (SFtpTaskOption) getTaskWrapper().getTaskOption();
  }

  @Override protected void handlerThreadTask() {
    if (session == null) {
      fail(new AriaSFTPException("session 为空"), false);
      return;
    }
    try {
      int timeout = getTaskConfig().getConnectTimeOut();
      if (!session.isConnected()) {
        session.connect(timeout);
      }
      channelSftp = (ChannelSftp) session.openChannel("sftp");
      channelSftp.connect(timeout);
      ALog.d(TAG,
          String.format("任务【%s】线程__%s__开始下载【开始位置 : %s，结束位置：%s】", getTaskWrapper().getKey(),
              getThreadRecord().threadId, getThreadRecord().startLocation,
              getThreadRecord().endLocation));

      // 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码
      String charSet = option.getCharSet();
      String remotePath =
          CommonUtil.convertSFtpChar(charSet, option.getUrlEntity().remotePath);
      download(remotePath);
    } catch (SftpException e) {
      fail(new AriaSFTPException("sftp错误，错误类型：" + e.id, e), false);
    } catch (UnsupportedEncodingException e) {
      fail(new AriaSFTPException("字符编码错误", e), false);
    } catch (IOException e) {
      fail(new AriaSFTPException("", e), true);
    } catch (JSchException e) {
      fail(new AriaSFTPException("jsch 错误", e), false);
    } finally {
      channelSftp.disconnect();
    }
  }

  /**
   * 下载
   */
  private void download(String remotePath) throws SftpException, IOException {
    InputStream is =
        channelSftp.get(remotePath, new Monitor(), getThreadRecord().startLocation);
    FileOutputStream fos = new FileOutputStream(getThreadConfig().tempFile, true);
    FileChannel foc = fos.getChannel();
    ReadableByteChannel fic = Channels.newChannel(is);
    ByteBuffer bf = ByteBuffer.allocate(getTaskConfig().getBuffSize());
    int len;
    while (getThreadTask().isLive() && (len = fic.read(bf)) != -1) {
      if (getThreadTask().isBreak()) {
        break;
      }
      if (mSpeedBandUtil != null) {
        mSpeedBandUtil.limitNextBytes(len);
      }
      if (getRangeProgress() + len >= getThreadRecord().endLocation) {
        len = (int) (getThreadRecord().endLocation - getRangeProgress());
        bf.flip();
        fos.write(bf.array(), 0, len);
        bf.compact();
        progress(len);
        break;
      } else {
        bf.flip();
        foc.write(bf);
        bf.compact();
        progress(len);
      }
    }
    fos.flush();
    fos.close();
    is.close();
  }

  private class Monitor implements SftpProgressMonitor {

    private Monitor() {
    }

    @Override public void init(int op, String src, String dest, long max) {
      ALog.d(TAG, String.format("op = %s; src = %s; dest = %s; max = %s", op, src, dest, max));
    }

    /**
     * @param count 已传输的数据
     * @return false 取消任务
     */
    @Override public boolean count(long count) {

      /*
       * jsch 如果是恢复任务，第一次回调count会将已下载的长度返回，后面才是新增的文件长度。
       * 所以恢复任务的话，需要忽略一次回调
       */
      if (getRangeProgress() > getThreadRecord().endLocation) {
        return false;
      }
      return !getThreadTask().isBreak();
    }

    @Override public void end() {
      if (getThreadTask().isBreak()) {
        return;
      }

      complete();
    }
  }
}
