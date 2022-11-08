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
package com.arialyy.aria.core.config;

import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.event.DGMaxNumEvent;
import com.arialyy.aria.core.event.DSpeedEvent;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.util.ALog;
import java.io.Serializable;

/**
 * 下载类型的组合任务
 */
public class DGroupConfig extends BaseTaskConfig implements Serializable {

  /**
   * 能同时下载的子任务最大任务数，默认3
   */
  int subMaxTaskNum = 3;

  /**
   * 子任务失败时回调stop，默认true
   */
  private boolean subFailAsStop = true;

  /**
   * 子任务重试次数，默认为5
   */
  int subReTryNum = 5;

  /**
   * 子任务下载失败时的重试间隔，单位为毫秒，默认2000毫秒-
   */
  int subReTryInterval = 2000;

  private DownloadConfig subConfig;

  DGroupConfig() {
    getSubConfig();
  }

  @Override int getType() {
    return TYPE_DGROUP;
  }

  @Override public DGroupConfig setMaxSpeed(int maxSpeed) {
    super.setMaxSpeed(maxSpeed);
    EventMsgUtil.getDefault().post(new DSpeedEvent(maxSpeed));
    return this;
  }

  public DownloadConfig getSubConfig() {
    subConfig = AriaConfig.getInstance().getDConfig();
    return subConfig;
  }

  public DGroupConfig setMaxTaskNum(int maxTaskNum) {
    if (maxTaskNum <= 0) {
      ALog.e(TAG, "组合任务最大任务数不能小于0");
      return this;
    }
    super.setMaxTaskNum(maxTaskNum);
    EventMsgUtil.getDefault().post(new DGMaxNumEvent(maxTaskNum));
    return this;
  }

  public int getSubMaxTaskNum() {
    return subMaxTaskNum;
  }

  public DGroupConfig setSubMaxTaskNum(int subMaxTaskNum) {
    this.subMaxTaskNum = subMaxTaskNum;
    save();
    return this;
  }

  public int getSubReTryNum() {
    return subReTryNum;
  }

  public DGroupConfig setSubReTryNum(int subReTryNum) {
    this.subReTryNum = subReTryNum;
    subConfig.reTryNum = subReTryNum;
    save();
    return this;
  }

  public int getSubReTryInterval() {
    return subReTryInterval;
  }

  public DGroupConfig setSubReTryInterval(int subReTryInterval) {
    this.subReTryInterval = subReTryInterval;
    subConfig.reTryInterval = subReTryInterval;
    save();
    return this;
  }

  public boolean isSubFailAsStop() {
    return subFailAsStop;
  }

  public DGroupConfig setSubFailAsStop(boolean subFailAsStop) {
    this.subFailAsStop = subFailAsStop;
    save();
    return this;
  }
}