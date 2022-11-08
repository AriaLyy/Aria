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
package com.arialyy.aria.core.download.target;

import com.arialyy.aria.core.common.AbsNormalTarget;
import com.arialyy.aria.core.common.FtpOption;
import com.arialyy.aria.core.common.SFtpOption;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2016/12/5.
 * https://github.com/AriaLyy/Aria
 */
public class FtpNormalTarget extends AbsNormalTarget<FtpNormalTarget> {
  private DNormalConfigHandler<FtpNormalTarget> mConfigHandler;

  FtpNormalTarget(long taskId) {
    mConfigHandler = new DNormalConfigHandler<>(this, taskId);
    getTaskWrapper().setRequestType(ITaskWrapper.D_FTP);
    getTaskWrapper().setNewTask(false);
  }

  /**
   * 设置登陆、字符串编码、ftps等参数
   */
  public FtpNormalTarget option(FtpOption option) {
    if (option == null) {
      throw new NullPointerException("ftp 任务配置为空");
    }
    option.setUrlEntity(CommonUtil.getFtpUrlInfo(getEntity().getUrl()));
    getTaskWrapper().getOptionParams().setParams(option);
    return this;
  }

  /**
   * 设置登陆、字符串编码、sftp等参数
   */
  public FtpNormalTarget sftpOption(SFtpOption option) {
    if (option == null) {
      throw new NullPointerException("ftp 任务配置为空");
    }
    option.setUrlEntity(CommonUtil.getFtpUrlInfo(mConfigHandler.getUrl()));
    getTaskWrapper().getOptionParams().setParams(option);
    getEntity().setTaskType(ITaskWrapper.D_SFTP);
    getTaskWrapper().setRequestType(ITaskWrapper.D_SFTP);
    return this;
  }

  /**
   * 设置文件保存文件夹路径
   * 关于文件名：
   * 1、如果保存路径是该文件的保存路径，如：/mnt/sdcard/file.zip，则使用路径中的文件名file.zip
   * 2、如果保存路径是文件夹路径，如：/mnt/sdcard/，则使用FTP服务器该文件的文件名
   */
  public FtpNormalTarget modifyFilePath(String filePath) {
    int lastIndex = mConfigHandler.getUrl().lastIndexOf("/");
    getEntity().setFileName(mConfigHandler.getUrl().substring(lastIndex + 1));
    mConfigHandler.setTempFilePath(filePath);
    return this;
  }

  /**
   * 更新下载地址
   */
  public FtpNormalTarget updateUrl(String newUrl) {
    return mConfigHandler.updateUrl(newUrl);
  }

  @Override public boolean isRunning() {
    return mConfigHandler.isRunning();
  }

  @Override public boolean taskExists() {
    return mConfigHandler.taskExists();
  }

  @Override public DownloadEntity getEntity() {
    return (DownloadEntity) super.getEntity();
  }
}
