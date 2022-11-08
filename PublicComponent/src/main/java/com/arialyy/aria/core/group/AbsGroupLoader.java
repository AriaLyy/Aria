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
import android.os.Looper;
import com.arialyy.aria.core.config.Configuration;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.listener.IDGroupListener;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.ILoader;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.loader.IThreadTaskBuilder;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 组合任务加载器
 */
public abstract class AbsGroupLoader implements ILoaderVisitor, ILoader {
  protected final String TAG = CommonUtil.getClassName(getClass());

  private long mCurrentLocation = 0;
  private IDGroupListener mListener;
  private ScheduledThreadPoolExecutor mTimer;
  private long mUpdateInterval;
  private boolean isStop = false, isCancel = false;
  private Handler mScheduler;
  private SimpleSubQueue mSubQueue = SimpleSubQueue.newInstance();
  private Map<String, AbsSubDLoadUtil> mExeLoader = new WeakHashMap<>();
  private Map<String, DTaskWrapper> mCache = new WeakHashMap<>();
  private DGTaskWrapper mGTWrapper;
  private GroupRunState mState;

  protected IInfoTask mInfoTask;

  protected AbsGroupLoader(AbsTaskWrapper groupWrapper, IEventListener listener) {
    mListener = (IDGroupListener) listener;
    mGTWrapper = (DGTaskWrapper) groupWrapper;
    mUpdateInterval = Configuration.getInstance().downloadCfg.getUpdateInterval();
  }

  /**
   * 处理任务
   */
  protected abstract void handlerTask(Looper looper);

  /**
   * 创建子任务加载器工具
   *
   * @param needGetFileInfo {@code true} 需要获取文件信息。{@code false} 不需要获取文件信息
   */
  protected abstract AbsSubDLoadUtil createSubLoader(DTaskWrapper wrapper, boolean needGetFileInfo);

  protected IDGroupListener getListener() {
    return mListener;
  }

  protected DGTaskWrapper getWrapper() {
    return mGTWrapper;
  }

  protected GroupRunState getState() {
    return mState;
  }

  public Handler getScheduler() {
    return mScheduler;
  }

  /**
   * 初始化组合任务状态
   */
  private void initState(Looper looper) {
    mState = new GroupRunState(getWrapper().getKey(), mListener, mSubQueue);
    for (DTaskWrapper wrapper : mGTWrapper.getSubTaskWrapper()) {
      long fileLen = checkFileExists(wrapper.getEntity().getFilePath());
      if (wrapper.getEntity().getState() == IEntity.STATE_COMPLETE
          && fileLen > 0
          && fileLen == wrapper.getEntity().getFileSize()) {
        //mState.updateCompleteNum();
        mCurrentLocation += wrapper.getEntity().getFileSize();
      } else {
        if (fileLen <= 0) {
          wrapper.getEntity().setCurrentProgress(0);
        }
        wrapper.getEntity().setState(IEntity.STATE_POST_PRE);
        mCache.put(wrapper.getKey(), wrapper);
        mCurrentLocation += wrapper.getEntity().getCurrentProgress();
      }
    }
    if (getWrapper().getSubTaskWrapper().size() != mState.getCompleteNum()) {
      getWrapper().setState(IEntity.STATE_POST_PRE);
    }
    mState.updateProgress(mCurrentLocation);
    mScheduler = new Handler(looper, SimpleSchedulers.newInstance(mState, mGTWrapper.getKey()));
  }

  /**
   * 检查文件是否存在，需要检查普通任务和分块任务的
   *
   * @param filePath 文件路径
   * @return 文件存在返回文件长度，不存在返回-1
   */
  private long checkFileExists(String filePath) {
    File temp = new File(filePath);
    if (temp.exists()) {
      return temp.length();
    }
    File block = new File(String.format(IRecordHandler.SUB_PATH, filePath, 0));
    if (block.exists()) {
      return block.length();
    } else {
      return -1;
    }
  }

  @Override public String getKey() {
    return mGTWrapper.getKey();
  }

  /**
   * 启动子任务下载
   *
   * @param url 子任务下载地址
   */
  void startSubTask(String url) {
    if (!checkSubTask(url, "开始")) {
      return;
    }
    if (!mState.isRunning.get()) {
      startTimer();
    }
    AbsSubDLoadUtil d = getDownloader(url, false);
    if (d != null && !d.isRunning()) {
      mSubQueue.startTask(d);
    }
  }

  /**
   * 停止子任务下载
   *
   * @param url 子任务下载地址
   */
  void stopSubTask(String url) {
    if (!checkSubTask(url, "停止")) {
      return;
    }
    AbsSubDLoadUtil d = getDownloader(url, false);
    if (d != null && d.isRunning()) {
      mSubQueue.stopTask(d);
    }
  }

  /**
   * 检查子任务
   *
   * @param url 子任务url
   * @param type 任务类型
   * @return {@code true} 任务可以下载
   */
  private boolean checkSubTask(String url, String type) {
    DTaskWrapper wrapper = mCache.get(url);
    if (wrapper != null) {
      if (wrapper.getState() == IEntity.STATE_COMPLETE) {
        ALog.w(TAG, "任务【" + url + "】已完成，" + type + "失败");
        return false;
      }
    } else {
      ALog.w(TAG, "任务组中没有该任务【" + url + "】，" + type + "失败");
      return false;
    }
    return true;
  }

  /**
   * 通过地址获取下载器
   *
   * @param url 子任务下载地址
   */
  private AbsSubDLoadUtil getDownloader(String url, boolean needGetFileInfo) {
    AbsSubDLoadUtil d = mExeLoader.get(url);
    if (d == null) {
      return createSubLoader(mCache.get(url), needGetFileInfo);
    }
    return d;
  }

  @Override public boolean isRunning() {
    return mState != null && mState.isRunning.get();
  }

  @Override public void cancel() {
    isCancel = true;
    if (mInfoTask != null){
      mInfoTask.cancel();
    }
    closeTimer();
    mSubQueue.removeAllTask();
    mListener.onCancel();
  }

  @Override public void stop() {
    if (mInfoTask != null){
      mInfoTask.stop();
    }
    isStop = true;
    if (mSubQueue.getExecSize() == 0) {
      mListener.onStop(mGTWrapper.getEntity().getCurrentProgress());
    } else {
      mSubQueue.stopAllTask();
    }
    closeTimer();
  }

  @Override public void run() {
    checkComponent();
    if (isStop || isCancel) {
      closeTimer();
      return;
    }
    startRunningFlow();
  }

  /**
   * 开始进度流程
   */
  private void startRunningFlow() {
    closeTimer();
    Looper.prepare();
    Looper looper = Looper.myLooper();
    if (looper == Looper.getMainLooper()) {
      throw new IllegalThreadStateException("不能在主线程程序中调用Loader");
    }
    initState(looper);
    getState().setSubSize(getWrapper().getSubTaskWrapper().size());
    if (getState().getCompleteNum() != 0
        && getState().getCompleteNum() == getState().getSubSize()) {
      mListener.onComplete();
      return;
    }
    startTimer();
    handlerTask(looper);
    Looper.loop();
  }

  /**
   * 组合任务获取完成子任务的信息后调用
   */
  protected void onPostStart() {
    if (isBreak()) {
      return;
    }
    getListener().onPostPre(getWrapper().getEntity().getFileSize());
    if (getWrapper().getEntity().getFileSize() > 0) {
      getListener().onResume(getWrapper().getEntity().getCurrentProgress());
    } else {
      getListener().onStart(getWrapper().getEntity().getCurrentProgress());
    }
  }

  private synchronized void startTimer() {
    mState.isRunning.set(true);
    mTimer = new ScheduledThreadPoolExecutor(1);
    mTimer.scheduleWithFixedDelay(new Runnable() {
      @Override public void run() {
        if (!mState.isRunning.get()) {
          closeTimer();
        } else if (mCurrentLocation >= 0) {
          long t = 0;
          for (DTaskWrapper te : mGTWrapper.getSubTaskWrapper()) {
            if (te.getState() == IEntity.STATE_COMPLETE) {
              t += te.getEntity().getFileSize();
            } else {
              t += te.getEntity().getCurrentProgress();
            }
          }
          mCurrentLocation = t;
          mState.updateProgress(mCurrentLocation);
          mListener.onProgress(t);
        }
      }
    }, 0, mUpdateInterval, TimeUnit.MILLISECONDS);
  }

  /**
   * 启动子任务下载器
   */
  protected void startSubLoader(AbsSubDLoadUtil loader) {
    mExeLoader.put(loader.getKey(), loader);
    mSubQueue.startTask(loader);
  }

  @Override public boolean isBreak() {
    if (isCancel || isStop) {
      //ALog.d(TAG, "isCancel = " + isCancel + ", isStop = " + isStop);
      ALog.d(TAG, String.format("任务【%s】已停止或取消了", mGTWrapper.getKey()));
      return true;
    }
    return false;
  }

  private synchronized void closeTimer() {
    if (mTimer != null && !mTimer.isShutdown()) {
      mTimer.shutdown();
    }
  }

  protected void fail(AriaException e, boolean needRetry) {
    closeTimer();
    getListener().onFail(needRetry, e);
  }

  @Override public long getCurrentProgress() {
    return mCurrentLocation;
  }

  /**
   * @deprecated 组合任务不需要实现这个，记录交由其子任务处理
   */
  @Deprecated
  @Override public void addComponent(IRecordHandler recordHandler) {

  }

  /**
   * @deprecated 组合任务不需要实现这个，线程创建交有子任务处理
   */
  @Deprecated
  @Override public void addComponent(IThreadTaskBuilder builder) {

  }

  /**
   * @deprecated 组合任务不需要实现这个，其内部是一个子任务调度器，并不是线程状态管理器
   */
  @Deprecated
  @Override public void addComponent(IThreadStateManager threadState) {

  }

  /**
   * 检查组件:  {@link #mInfoTask}
   */
  private void checkComponent() {
    if (mInfoTask == null) {
      throw new NullPointerException(("文件信息组件为空"));
    }
  }
}
