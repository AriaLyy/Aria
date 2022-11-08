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
package com.arialyy.aria.core.upload.target;

import com.arialyy.aria.core.common.AbsNormalTarget;
import com.arialyy.aria.core.common.FtpOption;
import com.arialyy.aria.core.common.SFtpOption;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by Aria.Lao on 2017/7/27.
 * ftp单任务上传
 */
public class FtpNormalTarget extends AbsNormalTarget<FtpNormalTarget> {
  private UNormalConfigHandler<FtpNormalTarget> mConfigHandler;

  FtpNormalTarget(long taskId) {
    mConfigHandler = new UNormalConfigHandler<>(this, taskId);
    getTaskWrapper().setRequestType(ITaskWrapper.U_FTP);
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
    option.setUrlEntity(CommonUtil.getFtpUrlInfo(getEntity().getUrl()));
    getTaskWrapper().getOptionParams().setParams(option);
    (getEntity()).setTaskType(ITaskWrapper.U_SFTP);
    getTaskWrapper().setRequestType(ITaskWrapper.U_SFTP);
    return this;
  }

  @Override public UploadEntity getEntity() {
    return (UploadEntity) super.getEntity();
  }

  @Override public boolean isRunning() {
    return mConfigHandler.isRunning();
  }

  @Override public boolean taskExists() {
    return mConfigHandler.taskExists();
  }
}
