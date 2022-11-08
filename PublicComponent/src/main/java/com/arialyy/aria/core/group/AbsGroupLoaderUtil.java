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

import com.arialyy.aria.core.inf.IUtil;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.loader.LoaderStructure;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by AriaL on 2017/6/30.
 * 任务组核心逻辑
 */
public abstract class AbsGroupLoaderUtil implements IUtil {

  protected String TAG = CommonUtil.getClassName(getClass());
  private IEventListener mListener;
  protected AbsGroupLoader mLoader;
  private AbsTaskWrapper mTaskWrapper;
  private boolean isStop = false, isCancel = false;


  @Override public IUtil setParams(AbsTaskWrapper taskWrapper, IEventListener listener) {
    mTaskWrapper = taskWrapper;
    mListener = listener;
    mLoader = getLoader();
    return this;
  }

  protected abstract AbsGroupLoader getLoader();

  protected abstract LoaderStructure buildLoaderStructure();

  public IEventListener getListener() {
    return mListener;
  }

  public AbsTaskWrapper getTaskWrapper() {
    return mTaskWrapper;
  }

  @Override public String getKey() {
    return mTaskWrapper.getKey();
  }

  @Override public long getFileSize() {
    return mTaskWrapper.getEntity().getFileSize();
  }

  @Override public long getCurrentLocation() {
    return mLoader.getCurrentProgress();
  }

  @Override public boolean isRunning() {
    return mLoader.isRunning();
  }

  public void startSubTask(String url) {
    getLoader().startSubTask(url);
  }

  public void stopSubTask(String url) {
    getLoader().stopSubTask(url);
  }

  /**
   * 取消下载
   */
  @Override public void cancel() {
    isCancel = true;
    mLoader.cancel();
  }

  /**
   * 停止下载
   */
  @Override public void stop() {
    isStop = true;
    mLoader.stop();
  }

  @Override public void start() {
    if (isStop || isCancel) {
      ALog.w(TAG, "启动组合任务失败，任务已停止或已取消");
      return;
    }
    mListener.onPre();

    buildLoaderStructure();
    new Thread(mLoader).start();
  }
}
