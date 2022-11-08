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
package com.arialyy.aria.core.task;

import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.config.BaseTaskConfig;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.BandwidthLimiter;
import com.arialyy.aria.util.CommonUtil;

/**
 * @author lyy
 * Date: 2019-09-18
 */
public abstract class AbsThreadTaskAdapter implements IThreadTaskAdapter {

  protected String TAG = CommonUtil.getClassName(getClass());
  /**
   * 速度限制工具
   */
  protected BandwidthLimiter mSpeedBandUtil;
  private ThreadRecord mThreadRecord;
  private IThreadTaskObserver mObserver;
  private AbsTaskWrapper mWrapper;
  private SubThreadConfig mThreadConfig;
  private IThreadTask mThreadTask;

  protected AbsThreadTaskAdapter(SubThreadConfig config) {
    mThreadRecord = config.record;
    mWrapper = config.taskWrapper;
    mThreadConfig = config;
    if (getTaskConfig().getMaxSpeed() > 0) {
      mSpeedBandUtil = new BandwidthLimiter(getTaskConfig().getMaxSpeed(), config.startThreadNum);
    }
  }

  @Override public void call(IThreadTask threadTask) throws Exception {
    mThreadTask = threadTask;
    handlerThreadTask();
  }

  /**
   * 开始处理线程任务
   */
  protected abstract void handlerThreadTask();

  /**
   * 当前线程的下去区间的进度
   */
  protected long getRangeProgress() {
    return mObserver.getThreadProgress();
  }

  protected ThreadRecord getThreadRecord() {
    return mThreadRecord;
  }

  protected AbsTaskWrapper getTaskWrapper() {
    return mWrapper;
  }

  /**
   * 获取任务配置信息
   */
  protected BaseTaskConfig getTaskConfig() {
    return getTaskWrapper().getConfig();
  }

  protected IThreadTask getThreadTask() {
    return mThreadTask;
  }

  /**
   * 获取线程配置信息
   */
  protected SubThreadConfig getThreadConfig() {
    return mThreadConfig;
  }

  @Override public void attach(IThreadTaskObserver observer) {
    mObserver = observer;
  }

  @Override public void setMaxSpeed(int speed) {
    if (mSpeedBandUtil == null) {
      mSpeedBandUtil =
          new BandwidthLimiter(getTaskConfig().getMaxSpeed(), getThreadConfig().startThreadNum);
    }
    mSpeedBandUtil.setMaxRate(speed);
  }

  protected void complete() {
    if (mObserver != null) {
      mObserver.updateCompleteState();
    }
  }

  protected void fail(AriaException ex, boolean needRetry) {
    if (mObserver != null) {
      mObserver.updateFailState(ex, needRetry);
    }
  }

  protected void progress(long len) {
    if (mObserver != null) {
      mObserver.updateProgress(len);
    }
  }
}
