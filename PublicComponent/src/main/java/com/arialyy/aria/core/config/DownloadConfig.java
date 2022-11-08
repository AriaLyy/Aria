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

import com.arialyy.aria.core.event.DMaxNumEvent;
import com.arialyy.aria.core.event.DSpeedEvent;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.util.ALog;
import java.io.Serializable;

/**
 * 下载配置
 */
public class DownloadConfig extends BaseTaskConfig implements Serializable {
  /**
   * 下载线程数，下载线程数不能小于1
   * 注意：
   * 1、线程下载数改变后，新的下载任务才会生效；
   * 2、如果任务大小小于1m，该设置不会生效；
   * 3、从3.4.1开始，如果线程数为1，文件初始化时将不再预占用对应长度的空间，下载多少byte，则占多大的空间；
   * 对于采用多线程的任务或旧任务，依然采用原来的文件空间占用方式；
   */
  int threadNum = 3;

  /**
   * 多线程下载是否使用块下载模式，{@code true}使用，{@code false}不使用
   * 注意：
   * 1、使用分块模式，在读写性能底下的手机上，合并文件需要的时间会更加长；
   * 2、优点是使用多线程的块下载，初始化时，文件初始化时将不会预占用对应长度的空间；
   * 3、只对新的多线程下载任务有效 4、只对多线程的任务有效
   */
  boolean useBlock = true;

  /**
   * 设置http下载获取文件大小是否使用Head请求。true：使用head请求，false：使用默认的get请求
   */
  boolean useHeadRequest = false;

  public boolean isUseHeadRequest() {
    return useHeadRequest;
  }

  public DownloadConfig setUseHeadRequest(boolean useHeadRequest) {
    this.useHeadRequest = useHeadRequest;
    save();
    return this;
  }

  public boolean isUseBlock() {
    return useBlock;
  }

  @Override public DownloadConfig setMaxSpeed(int maxSpeed) {
    super.setMaxSpeed(maxSpeed);
    EventMsgUtil.getDefault().post(new DSpeedEvent(maxSpeed));
    return this;
  }

  public DownloadConfig setUseBlock(boolean useBlock) {
    this.useBlock = useBlock;
    save();
    return this;
  }

  public DownloadConfig setMaxTaskNum(int maxTaskNum) {
    if (maxTaskNum <= 0) {
      ALog.e(TAG, "下载任务最大任务数不能小于0");
      return this;
    }
    super.setMaxTaskNum(maxTaskNum);
    EventMsgUtil.getDefault().post(new DMaxNumEvent(maxTaskNum));
    return this;
  }

  public DownloadConfig setThreadNum(int threadNum) {
    this.threadNum = threadNum;
    save();
    return this;
  }

  public int getThreadNum() {
    return threadNum;
  }

  DownloadConfig() {
  }

  @Override int getType() {
    return TYPE_DOWNLOAD;
  }
}