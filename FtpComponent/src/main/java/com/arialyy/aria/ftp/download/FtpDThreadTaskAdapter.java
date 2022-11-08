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
package com.arialyy.aria.ftp.download;

import aria.apache.commons.net.ftp.FTPClient;
import aria.apache.commons.net.ftp.FTPReply;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.exception.AriaFTPException;
import com.arialyy.aria.ftp.BaseFtpThreadTaskAdapter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * @author lyy
 * Date: 2019-09-18
 */
final class FtpDThreadTaskAdapter extends BaseFtpThreadTaskAdapter {

  FtpDThreadTaskAdapter(SubThreadConfig config) {
    super(config);
  }

  @Override protected void handlerThreadTask() {
    if (getThreadRecord().isComplete) {
      handleComplete();
      return;
    }
    FTPClient client = null;
    InputStream is = null;

    try {
      ALog.d(TAG,
          String.format("任务【%s】线程__%s__开始下载【开始位置 : %s，结束位置：%s】", getTaskWrapper().getKey(),
              getThreadRecord().threadId, getThreadRecord().startLocation,
              getThreadRecord().endLocation));
      client = createClient();
      if (client == null) {
        fail(new AriaFTPException("ftp client 创建失败"), false);
        return;
      }
      if (getThreadRecord().startLocation > 0) {
        client.setRestartOffset(getThreadRecord().startLocation);
      }
      //发送第二次指令时，还需要再做一次判断
      int reply = client.getReplyCode();
      if (!FTPReply.isPositivePreliminary(reply) && reply != FTPReply.COMMAND_OK) {
        fail(new AriaFTPException(
            String.format("获取文件信息错误，错误码为：%s，msg：%s", reply, client.getReplyString())), false);
        client.disconnect();
        return;
      }
      String remotePath =
          CommonUtil.convertFtpChar(charSet, mTaskOption.getUrlEntity().remotePath);
      ALog.i(TAG, String.format("remotePath【%s】", remotePath));
      is = client.retrieveFileStream(remotePath);
      reply = client.getReplyCode();
      if (!FTPReply.isPositivePreliminary(reply)) {
        fail(new AriaFTPException(
            String.format("获取流失败，错误码为：%s，msg：%s", reply, client.getReplyString())), true);
        client.disconnect();
        return;
      }

      if (getThreadConfig().isBlock) {
        readDynamicFile(is);
      } else {
        readNormal(is);
        handleComplete();
      }
    } catch (IOException e) {
      fail(new AriaFTPException(String.format("下载失败【%s】", getThreadConfig().url), e), true);
    } catch (Exception e) {
      fail(new AriaFTPException(String.format("下载失败【%s】", getThreadConfig().url), e), false);
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      closeClient(client);
    }
  }

  /**
   * 处理线程完成的情况
   */
  private void handleComplete() {
    if (getThreadTask().isBreak()) {
      return;
    }
    if (!getThreadTask().checkBlock()) {
      return;
    }
    complete();
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
      handleComplete();
    } catch (IOException e) {
      fail(new AriaFTPException(String.format("下载失败【%s】", getThreadConfig().url), e), true);
    } finally {
      try {
        if (fos != null) {
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

  /**
   * 多线程写文件方式
   */
  private void readNormal(InputStream is) {
    BufferedRandomAccessFile file = null;
    try {
      file =
          new BufferedRandomAccessFile(getThreadConfig().tempFile, "rwd",
              getTaskConfig().getBuffSize());
      if (getThreadRecord().startLocation > 0) {
        file.seek(getThreadRecord().startLocation);
      }
      byte[] buffer = new byte[getTaskConfig().getBuffSize()];
      int len;
      while (getThreadTask().isLive() && (len = is.read(buffer)) != -1) {
        if (getThreadTask().isBreak()) {
          break;
        }
        if (mSpeedBandUtil != null) {
          mSpeedBandUtil.limitNextBytes(len);
        }
        if (getRangeProgress() + len >= getThreadRecord().endLocation) {
          len = (int) (getThreadRecord().endLocation - getRangeProgress());
          file.write(buffer, 0, len);
          progress(len);
          break;
        } else {
          file.write(buffer, 0, len);
          progress(len);
        }
      }
    } catch (IOException e) {
      fail(new AriaFTPException(String.format("下载失败【%s】", getThreadConfig().url), e), true);
    } finally {
      try {
        if (file != null) {
          file.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
