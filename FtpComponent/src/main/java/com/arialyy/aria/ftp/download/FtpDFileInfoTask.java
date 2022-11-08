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
import aria.apache.commons.net.ftp.FTPFile;
import aria.apache.commons.net.ftp.FTPReply;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.exception.AriaFTPException;
import com.arialyy.aria.ftp.AbsFtpInfoTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.io.IOException;

/**
 * Created by Aria.Lao on 2017/7/25.
 * 获取ftp文件信息
 */
final class FtpDFileInfoTask extends AbsFtpInfoTask<DownloadEntity, DTaskWrapper> {

  FtpDFileInfoTask(DTaskWrapper taskEntity) {
    super(taskEntity);
  }

  @Override protected void handleFile(FTPClient client, String remotePath, FTPFile ftpFile) {
    super.handleFile(client, remotePath, ftpFile);
    if (!FileUtil.checkMemorySpace(mEntity.getFilePath(), ftpFile.getSize())) {
      handleFail(client, "内存空间不足", new AriaFTPException(
              String.format("获取ftp文件信息失败，内存空间不足, filePath: %s", mEntity.getFilePath())),
          false);
    }
  }

  @Override
  protected void handelFileInfo(FTPClient client, FTPFile[] files, String convertedRemotePath)
      throws IOException {
    boolean isExist = files.length != 0;
    if (!isExist) {
      int i = convertedRemotePath.lastIndexOf(File.separator);
      FTPFile[] files1;
      if (i == -1) {
        files1 = client.listFiles();
      } else {
        files1 = client.listFiles(convertedRemotePath.substring(0, i + 1));
      }
      if (files1.length > 0) {
        ALog.i(TAG,
            String.format("路径【%s】下的文件列表 ===================================", getRemotePath()));
        for (FTPFile file : files1) {
          ALog.d(TAG, file.toString());
        }
        ALog.i(TAG,
            "================================= --end-- ===================================");
      } else {
        ALog.w(TAG, String.format("获取文件列表失败，msg：%s", client.getReplyString()));
      }
      closeClient(client);

      handleFail(client,
          String.format("文件不存在，url: %s, remotePath：%s", mTaskOption.getUrlEntity().url,
              getRemotePath()), null, false);
      return;
    }

    // 处理拦截功能
    if (!onInterceptor(client, files)) {
      closeClient(client);
      ALog.d(TAG, "拦截器处理完成任务");
      return;
    }

    //为了防止编码错乱，需要使用原始字符串
    mSize = getFileSize(files, client, getRemotePath());
    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      closeClient(client);
      handleFail(client, "获取文件信息错误，url: " + mTaskOption.getUrlEntity().url, null, true);
      return;
    }
    mTaskWrapper.setCode(reply);
    if (mSize != 0) {
      mEntity.setFileSize(mSize);
    }
    onPreComplete(reply);
    mEntity.update();
  }

  @Override protected String getRemotePath() {
    return mTaskOption.getUrlEntity().remotePath;
  }

  @Override protected void onPreComplete(int code) {
    ALog.i(TAG, "FTP下载预处理完成");
    super.onPreComplete(code);
    if (mSize != mTaskWrapper.getEntity().getFileSize()) {
      mTaskWrapper.setNewTask(true);
    }
    mEntity.setFileSize(mSize);
    onSucceed(new CompleteInfo(code, mTaskWrapper));
  }
}
