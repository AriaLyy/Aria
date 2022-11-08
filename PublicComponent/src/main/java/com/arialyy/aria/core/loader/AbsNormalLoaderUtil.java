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

package com.arialyy.aria.core.loader;

import com.arialyy.aria.core.inf.IUtil;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2015/8/25.
 * HTTP\FTP单任务下载工具
 */
public abstract class AbsNormalLoaderUtil implements IUtil {
  protected String TAG = CommonUtil.getClassName(getClass());
  private IEventListener mListener;
  protected AbsNormalLoader mLoader;
  private AbsTaskWrapper mTaskWrapper;
  private boolean isStop = false, isCancel = false;

  protected AbsNormalLoaderUtil() {
  }

  @Override public IUtil setParams(AbsTaskWrapper taskWrapper, IEventListener listener) {
    mTaskWrapper = taskWrapper;
    mListener = listener;
    mLoader = getLoader();
    return this;
  }

  /**
   * 获取加载器
   */
  public abstract AbsNormalLoader getLoader();

  /**
   * 获取构造器
   */
  public abstract LoaderStructure BuildLoaderStructure();

  @Override public String getKey() {
    return mTaskWrapper.getKey();
  }

  @Override public long getFileSize() {
    return mLoader.getFileSize();
  }

  /**
   * 获取当前下载位置
   */
  @Override public long getCurrentLocation() {
    return mLoader.getCurrentProgress();
  }

  @Override public boolean isRunning() {
    return mLoader.isRunning();
  }

  /**
   * 取消下载
   */
  @Override public void cancel() {
    isCancel = true;
    mLoader.cancel();
    onCancel();
  }

  protected void onCancel() {

  }

  /**
   * 停止下载
   */
  @Override public void stop() {
    isStop = true;
    mLoader.stop();
    onStop();
  }

  protected void onStop() {

  }

  /**
   * 多线程断点续传下载文件，开始下载
   */
  @Override public void start() {
    if (isStop || isCancel) {
      ALog.w(TAG, "启动任务失败，任务已停止或已取消");
      return;
    }
    mListener.onPre();
    // 如果网址没有变，而服务器端端文件改变，以下代码就没有用了
    //if (mTaskWrapper.getEntity().getFileSize() <= 1
    //    || mTaskWrapper.isRefreshInfo()
    //    || mTaskWrapper.getRequestType() == AbsTaskWrapper.D_FTP
    //    || mTaskWrapper.getState() == IEntity.STATE_FAIL) {
    //  new Thread(createInfoThread()).create();
    //} else {
    //  mDownloader.create();
    //}

    BuildLoaderStructure();
    new Thread(mLoader).start();
    onStart();
  }

  protected void onStart() {

  }

  public boolean isStop() {
    return isStop;
  }

  public boolean isCancel() {
    return isCancel;
  }

  public IEventListener getListener() {
    return mListener;
  }

  protected void fail(AriaException e, boolean needRetry) {
    if (isStop || isCancel) {
      return;
    }
    mListener.onFail(needRetry, e);
    mLoader.onDestroy();
  }

  public AbsTaskWrapper getTaskWrapper() {
    return mTaskWrapper;
  }
}