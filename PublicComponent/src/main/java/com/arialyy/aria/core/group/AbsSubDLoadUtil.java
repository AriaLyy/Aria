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
package com.arialyy.aria.core.group;

import android.os.Handler;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.IUtil;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.loader.LoaderStructure;
import com.arialyy.aria.core.loader.SubLoader;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;

/**
 * 子任务下载器工具，需要在线程池中执行
 */
public abstract class AbsSubDLoadUtil implements IUtil, Runnable {
  protected final String TAG = CommonUtil.getClassName(getClass());

  protected SubLoader mDLoader;
  private DTaskWrapper mWrapper;
  private Handler mSchedulers;
  private boolean needGetInfo;
  private boolean isStop = false, isCancel = false;
  private String parentKey;

  /**
   * @param schedulers 调度器
   * @param needGetInfo {@code true} 需要获取文件信息。{@code false} 不需要获取文件信息
   */
  protected AbsSubDLoadUtil(Handler schedulers, boolean needGetInfo, String parentKey) {
    mSchedulers = schedulers;
    this.parentKey = parentKey;
    this.needGetInfo = needGetInfo;
  }

  @Override public IUtil setParams(AbsTaskWrapper taskWrapper, IEventListener listener) {
    mWrapper = (DTaskWrapper) taskWrapper;
    mDLoader = getLoader();
    return this;
  }

  /**
   * 创建加载器
   */
  protected abstract SubLoader getLoader();

  protected abstract LoaderStructure buildLoaderStructure();

  public String getParentKey() {
    return parentKey;
  }

  protected boolean isNeedGetInfo() {
    return needGetInfo;
  }

  public Handler getSchedulers() {
    return mSchedulers;
  }

  @Override public String getKey() {
    return mDLoader.getKey();
  }

  public DTaskWrapper getWrapper() {
    return mWrapper;
  }

  public DownloadEntity getEntity() {
    return mWrapper.getEntity();
  }

  public TaskRecord getRecord(){
    return getLoader().getRecord();
  }

  @Override public void run() {
    if (isStop || isCancel) {
      return;
    }
    buildLoaderStructure();
    new Thread(mDLoader).start();
  }

  /**
   * 请在线程池中使用
   */
  @Deprecated
  @Override public void start() {
    throw new AssertionError("请在线程池中使用");
  }

  /**
   * 重新开始任务
   */
  void reStart() {
    if (mDLoader != null) {
      mDLoader.retryTask();
    }
  }

  /**
   * @deprecated 子任务不实现这个
   */
  @Deprecated
  @Override public long getFileSize() {
    return -1;
  }

  /**
   * 子任务不实现这个
   */
  @Deprecated
  @Override public long getCurrentLocation() {
    return -1;
  }

  @Override public boolean isRunning() {
    return mDLoader != null && mDLoader.isRunning();
  }

  @Override public void cancel() {
    if (isCancel) {
      ALog.w(TAG, "子任务已取消");
      return;
    }
    isCancel = true;
    if (mDLoader != null && isRunning()) {
      mDLoader.cancel();
    } else {
      mSchedulers.obtainMessage(ISchedulers.CANCEL, this).sendToTarget();
    }
  }

  @Override public void stop() {
    if (isStop) {
      ALog.w(TAG, "任务已停止");
      return;
    }
    isStop = true;
    if (mDLoader != null && isRunning()) {
      mDLoader.stop();
    } else {
      mSchedulers.obtainMessage(ISchedulers.STOP, this).sendToTarget();
    }
  }
}
