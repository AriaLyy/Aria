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
import aria.apache.commons.net.ftp.FTPFile;
import aria.apache.commons.net.ftp.FTPReply;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.processor.FtpInterceptHandler;
import com.arialyy.aria.core.processor.IFtpUploadInterceptor;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.ftp.AbsFtpInfoTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aria.Lao on 2017/9/26.
 * 单任务上传远程服务器文件信息
 */
final class FtpUFileInfoTask extends AbsFtpInfoTask<UploadEntity, UTaskWrapper> {
  static final int CODE_COMPLETE = 0xab1;
  private boolean isComplete = false;
  private String remotePath;
  private FTPFile ftpFile;

  FtpUFileInfoTask(UTaskWrapper taskEntity) {
    super(taskEntity);
  }

  @Override protected String getRemotePath() {
    return remotePath == null ?
        mTaskOption.getUrlEntity().remotePath + "/" + mEntity.getFileName() : remotePath;
  }

  @Override
  protected void handelFileInfo(FTPClient client, FTPFile[] files, String convertedRemotePath)
      throws IOException {
    // 处理拦截功能
    if (!onInterceptor(client, files)) {
      closeClient(client);
      ALog.d(TAG, "拦截器处理完成任务");
      return;
    }

    handleFile(client, getRemotePath(), files.length == 0 ? null : files[0]);
    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      //服务器上没有该文件路径，表示该任务为新的上传任务
      mTaskWrapper.setNewTask(true);
    }
    mTaskWrapper.setCode(reply);
    onPreComplete(reply);
    mEntity.update();
  }

  @Override protected boolean onInterceptor(FTPClient client, FTPFile[] ftpFiles) {
    // 旧任务将不做处理，否则断点续传上传将失效
    //if (!mTaskWrapper.isNewTask()) {
    //  ALog.d(TAG, "任务是旧任务，忽略该拦截器");
    //  return true;
    //}
    try {
      IFtpUploadInterceptor interceptor = mTaskOption.getUploadInterceptor();
      if (interceptor != null) {
        /*
         * true 使用拦截器，false 不使用拦截器
         */
        List<String> files = new ArrayList<>();
        for (FTPFile ftpFile : ftpFiles) {
          if (ftpFile.isDirectory()) {
            continue;
          }
          files.add(ftpFile.getName());
        }

        FtpInterceptHandler interceptHandler = interceptor.onIntercept(mEntity, files);

        /*
         * 处理远端有同名文件的情况
         */
        if (files.contains(mEntity.getFileName())) {
          if (interceptHandler.isCoverServerFile()) {
            ALog.i(TAG, String.format("远端已拥有同名文件，将覆盖该文件，文件名：%s", mEntity.getFileName()));
            boolean b = client.deleteFile(CommonUtil.convertFtpChar(charSet, getRemotePath()));
            ALog.d(TAG,
                String.format("删除文件%s，code: %s， msg: %s", b ? "成功" : "失败", client.getReplyCode(),
                    client.getReplyString()));
          } else if (!TextUtils.isEmpty(interceptHandler.getNewFileName())) {
            ALog.i(TAG, String.format("远端已拥有同名文件，将修改remotePath，原文件名：%s，新文件名：%s",
                mEntity.getFileName(), interceptHandler.getNewFileName()));
            remotePath = mTaskOption.getUrlEntity().remotePath
                + "/"
                + interceptHandler.getNewFileName();
            mTaskOption.setNewFileName(interceptHandler.getNewFileName());

            closeClient(client);
            run();
            return false;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * 如果服务器的文件长度和本地上传文件的文件长度一致，则任务任务已完成。
   * 否则重新修改保存的停止位置，这是因为outputStream是读不到服务器是否成功写入的。
   * 而threadTask的保存的停止位置是File的InputStream的，所有就会导致两端停止位置不一致
   *
   * @param remotePath ftp服务器文件夹路径
   * @param ftpFile ftp服务器上对应的文件
   */
  @Override protected void handleFile(FTPClient client,String remotePath, FTPFile ftpFile) {
    super.handleFile(client, remotePath, ftpFile);
    this.ftpFile = ftpFile;
    if (ftpFile != null && ftpFile.getSize() == mEntity.getFileSize()) {
      isComplete = true;
    }
  }

  @Override protected void onPreComplete(int code) {
    super.onPreComplete(code);
    CompleteInfo info = new CompleteInfo(isComplete ? CODE_COMPLETE : code, mTaskWrapper);
    info.obj = ftpFile;
    onSucceed(info);
  }
}
