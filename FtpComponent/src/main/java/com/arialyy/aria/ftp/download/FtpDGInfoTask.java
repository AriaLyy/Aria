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

import android.net.Uri;
import android.text.TextUtils;
import aria.apache.commons.net.ftp.FTPClient;
import aria.apache.commons.net.ftp.FTPFile;
import aria.apache.commons.net.ftp.FTPReply;
import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.ftp.AbsFtpInfoTask;
import com.arialyy.aria.ftp.FtpTaskOption;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.DeleteDGRecord;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Aria.Lao on 2017/7/25. 获取ftp文件夹信息
 */
final class FtpDGInfoTask extends AbsFtpInfoTask<DownloadGroupEntity, DGTaskWrapper> {

  FtpDGInfoTask(DGTaskWrapper wrapper) {
    super(wrapper);
  }

  @Override public void run() {
    if (mTaskWrapper.getEntity().getFileSize() > 1 && checkSubOption()) {
      onSucceed(new CompleteInfo(200, mTaskWrapper));
    } else {
      super.run();
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

  /**
   * 检查子任务的任务设置
   *
   * @return true 子任务任务设置成功，false 子任务任务设置失败
   */
  private boolean checkSubOption() {
    for (DTaskWrapper wrapper : mTaskWrapper.getSubTaskWrapper()) {
      if (wrapper.getTaskOption() == null) {
        return false;
      }
    }
    return true;
  }

  @Override protected String getRemotePath() {
    return mTaskOption.getUrlEntity().remotePath;
  }

  @Override protected void handleFile(FTPClient client, String remotePath, FTPFile ftpFile) {
    super.handleFile(client, remotePath, ftpFile);
    addEntity(remotePath, ftpFile);
  }

  @Override protected void onPreComplete(int code) {
    super.onPreComplete(code);
    mEntity.setFileSize(mSize);
    for (DTaskWrapper wrapper : mTaskWrapper.getSubTaskWrapper()) {
      cloneInfo(wrapper);
    }
    onSucceed(new CompleteInfo(code, mTaskWrapper));
  }

  private void cloneInfo(DTaskWrapper subWrapper) {
    FtpTaskOption option = (FtpTaskOption) mTaskWrapper.getTaskOption();
    FtpUrlEntity urlEntity = option.getUrlEntity().clone();
    Uri uri = Uri.parse(subWrapper.getEntity().getUrl());
    String remotePath = uri.getPath();
    urlEntity.remotePath = TextUtils.isEmpty(remotePath) ? "/" : remotePath;

    FtpTaskOption subOption = new FtpTaskOption();
    subOption.setUrlEntity(urlEntity);
    subOption.setCharSet(option.getCharSet());
    subOption.setProxy(option.getProxy());
    subOption.setClientConfig(option.getClientConfig());
    subOption.setNewFileName(option.getNewFileName());
    subOption.setProxy(option.getProxy());
    subOption.setUploadInterceptor(option.getUploadInterceptor());

    subWrapper.setTaskOption(subOption);
  }

  /**
   * FTP文件夹的子任务实体 在这生成
   */
  private void addEntity(String remotePath, FTPFile ftpFile) {
    final FtpUrlEntity urlEntity = mTaskOption.getUrlEntity().clone();
    String url =
        urlEntity.scheme + "://" + urlEntity.hostName + ":" + urlEntity.port + "/" + remotePath;
    if (checkEntityExist(url)) {
      ALog.w(TAG, "子任务已存在，取消子任务的添加，url = " + url);
      return;
    }
    DownloadEntity entity = new DownloadEntity();
    entity.setUrl(url);
    entity.setFilePath(mEntity.getDirPath() + "/" + remotePath);
    int lastIndex = remotePath.lastIndexOf("/");
    String fileName = lastIndex < 0 ? CommonUtil.keyToHashKey(remotePath)
        : remotePath.substring(lastIndex + 1);
    entity.setFileName(
        new String(fileName.getBytes(), Charset.forName(mTaskOption.getCharSet())));
    entity.setGroupHash(mEntity.getGroupHash());
    entity.setGroupChild(true);
    entity.setConvertFileSize(CommonUtil.formatFileSize(ftpFile.getSize()));
    entity.setFileSize(ftpFile.getSize());

    if (DbEntity.checkDataExist(DownloadEntity.class, "downloadPath=?", entity.getFilePath())) {
      DbEntity.deleteData(DownloadEntity.class, "downloadPath=?", entity.getFilePath());
    }
    entity.insert();

    DTaskWrapper subWrapper = new DTaskWrapper(entity);
    subWrapper.setGroupTask(true);
    subWrapper.setGroupHash(mEntity.getGroupHash());
    subWrapper.setRequestType(AbsTaskWrapper.D_FTP);
    urlEntity.url = entity.getUrl();
    urlEntity.remotePath = remotePath;

    cloneInfo(subWrapper, urlEntity);

    if (mEntity.getUrls() == null) {
      mEntity.setUrls(new ArrayList<String>());
    }
    mEntity.getSubEntities().add(entity);
    mTaskWrapper.getSubTaskWrapper().add(subWrapper);
  }

  private void cloneInfo(DTaskWrapper subWrapper, FtpUrlEntity urlEntity) {
    FtpTaskOption subOption = new FtpTaskOption();
    subOption.setUrlEntity(urlEntity);
    subOption.setCharSet(mTaskOption.getCharSet());
    subOption.setProxy(mTaskOption.getProxy());
    subOption.setClientConfig(mTaskOption.getClientConfig());
    subOption.setNewFileName(mTaskOption.getNewFileName());
    subOption.setProxy(mTaskOption.getProxy());
    subOption.setUploadInterceptor(mTaskOption.getUploadInterceptor());

    subWrapper.setTaskOption(subOption);
  }

  /**
   * 检查子任务是否已经存在，如果子任务存在，取消添加操作
   *
   * @param key url
   * @return true 子任务已存在，false 子任务不存在
   */
  private boolean checkEntityExist(String key) {
    for (DTaskWrapper wrapper : mTaskWrapper.getSubTaskWrapper()) {
      if (wrapper.getKey().equals(key)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void handleFail(FTPClient client, String msg, Exception e, boolean needRetry) {
    super.handleFail(client, msg, e, needRetry);
    DeleteDGRecord.getInstance().deleteRecord(mTaskWrapper.getEntity(), true, true);
  }
}
