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

import com.arialyy.aria.core.common.AbsBuilderTarget;
import com.arialyy.aria.core.common.FtpOption;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.manager.SubTaskManager;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by Aria.Lao on 2017/7/26.
 * ftp文件夹下载
 */
public class FtpDirBuilderTarget extends AbsBuilderTarget<FtpDirBuilderTarget> {
  private FtpDirConfigHandler<FtpDirBuilderTarget> mConfigHandler;
  private String url;

  FtpDirBuilderTarget(String url) {
    this.url = url;
    mConfigHandler = new FtpDirConfigHandler<>(this, -1);
    getEntity().setGroupHash(url);
    getTaskWrapper().setNewTask(true);
  }

  /**
   * 设置任务组的文件夹路径，在Aria中，任务组的所有子任务都会下载到以任务组组名的文件夹中。
   * 如：groupDirPath = "/mnt/sdcard/download/group_test"
   * <pre>
   *   {@code
   *      + mnt
   *        + sdcard
   *          + download
   *            + group_test
   *              - task1.apk
   *              - task2.apk
   *              - task3.apk
   *              ....
   *
   *   }
   * </pre>
   *
   * @param dirPath 任务组保存文件夹路径
   */
  public FtpDirBuilderTarget setDirPath(String dirPath) {
    return mConfigHandler.setDirPath(dirPath);
  }

  /**
   * 设置任务组别名
   */
  public FtpDirBuilderTarget setGroupAlias(String alias) {
    mConfigHandler.setGroupAlias(alias);
    return this;
  }

  /**
   * 设置登陆、字符串编码、ftps等参数
   */
  public FtpDirBuilderTarget option(FtpOption option) {
    if (option == null) {
      throw new NullPointerException("ftp 任务配置为空");
    }
    option.setUrlEntity(CommonUtil.getFtpUrlInfo(url));
    getTaskWrapper().getOptionParams().setParams(option);
    return this;
  }

  @Override public DownloadGroupEntity getEntity() {
    return (DownloadGroupEntity) super.getEntity();
  }

  /**
   * 获取子任务管理器
   *
   * @return 子任务管理器
   */
  public SubTaskManager getSubTaskManager() {
    return mConfigHandler.getSubTaskManager();
  }
}
