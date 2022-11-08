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
package com.arialyy.aria.core.download;

import com.arialyy.aria.core.TaskOptionParams;
import com.arialyy.aria.core.config.Configuration;
import com.arialyy.aria.core.config.DownloadConfig;
import com.arialyy.aria.core.inf.ITaskOption;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ComponentUtil;

/**
 * Created by lyy on 2017/1/23. 下载任务实体和下载实体为一对一关系，下载实体删除，任务实体自动删除
 */
public class DTaskWrapper extends AbsTaskWrapper<DownloadEntity> {

  /**
   * 所属的任务组组名，如果不属于任务组，则为null
   */
  private String groupHash;

  /**
   * 该任务是否属于任务组
   */
  private boolean isGroupTask = false;

  /**
   * M3u8任务配置信息
   */
  private ITaskOption m3u8Option;

  private TaskOptionParams m3u8Params = new TaskOptionParams();

  /**
   * 文件下载url的临时保存变量
   */
  private String mTempUrl;

  /**
   * 文件保存路径的临时变量
   */
  private String mTempFilePath;

  public DTaskWrapper(DownloadEntity entity) {
    super(entity);
  }

  public ITaskOption getM3u8Option() {
    return m3u8Option;
  }

  public void generateM3u8Option(Class<? extends ITaskOption> clazz) {
    m3u8Option = ComponentUtil.getInstance().buildTaskOption(clazz, m3u8Params);
  }

  public TaskOptionParams getM3U8Params() {
    if (m3u8Params == null) {
      m3u8Params = new TaskOptionParams();
    }
    return m3u8Params;
  }

  /**
   * Task实体对应的key，下载url
   */
  @Override public String getKey() {
    return getEntity().getKey();
  }

  @Override public DownloadConfig getConfig() {
    if (isGroupTask) {
      return Configuration.getInstance().dGroupCfg.getSubConfig();
    } else {
      return Configuration.getInstance().downloadCfg;
    }
  }

  public String getGroupHash() {
    return groupHash;
  }

  public boolean isGroupTask() {
    return isGroupTask;
  }

  public void setGroupHash(String groupHash) {
    this.groupHash = groupHash;
  }

  public void setGroupTask(boolean groupTask) {
    isGroupTask = groupTask;
  }

  public String getTempUrl() {
    return mTempUrl;
  }

  public void setTempUrl(String mTempUrl) {
    this.mTempUrl = mTempUrl;
  }

  public String getTempFilePath() {
    return mTempFilePath;
  }

  public void setTempFilePath(String mTempFilePath) {
    this.mTempFilePath = mTempFilePath;
  }
}
