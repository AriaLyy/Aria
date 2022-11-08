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

import android.os.Looper;
import android.util.Log;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by AriaL on 2017/7/1.
 * 任务执行器，用于处理任务的开始，停止
 * 流程：
 * 1、获取任务记录
 * 2、创建任务状态管理器，用于管理任务的状态
 * 3、创建文件信息获取器，获取文件信息，根据文件信息执行任务
 * 4、创建线程任务执行下载、上传操作
 */
public abstract class AbsNormalLoader<T extends AbsTaskWrapper> implements ILoaderVisitor, ILoader {
  protected final String TAG = CommonUtil.getClassName(getClass());
  private IEventListener mListener;
  protected T mTaskWrapper;
  protected File mTempFile;

  private List<IThreadTask> mTask = new ArrayList<>();
  private ScheduledThreadPoolExecutor mTimer;

  /**
   * 进度刷新间隔
   */
  private long mUpdateInterval = 1000;
  protected TaskRecord mRecord;
  protected boolean isCancel = false, isStop = false;
  private boolean isRuning = false;

  protected IRecordHandler mRecordHandler;
  protected IThreadStateManager mStateManager;
  protected IInfoTask mInfoTask;
  protected IThreadTaskBuilder mTTBuilder;

  protected AbsNormalLoader(T wrapper, IEventListener listener) {
    mListener = listener;
    mTaskWrapper = wrapper;
  }

  /**
   * 启动线程任务
   */
  protected abstract void handleTask(Looper looper);

  /**
   * 获取文件长度
   */
  public abstract long getFileSize();

  protected IEventListener getListener() {
    return mListener;
  }

  protected IThreadStateManager getStateManager() {
    return mStateManager;
  }

  public String getKey() {
    return mTaskWrapper.getKey();
  }

  public List<IThreadTask> getTaskList() {
    return mTask;
  }

  /**
   * 重置任务状态
   */
  private void resetState() {
    closeTimer();
    if (mTask != null && mTask.size() != 0) {
      for (int i = 0; i < mTask.size(); i++) {
        mTask.get(i).breakTask();
      }
      mTask.clear();
    }
  }

  @Override public void run() {
    checkComponent();
    if (isRunning()) {
      ALog.d(TAG, String.format("任务【%s】正在执行，启动任务失败", mTaskWrapper.getKey()));
      return;
    }
    startFlow();
  }

  /**
   * 开始流程
   */
  private void startFlow() {
    if (isBreak()) {
      return;
    }
    Looper.prepare();
    Looper looper = Looper.myLooper();
    if (looper == Looper.getMainLooper()) {
      throw new IllegalThreadStateException("不能在主线程程序中调用Loader");
    }
    isRuning = true;
    resetState();
    onPostPre();
    handleTask(looper);
    Looper.loop();
  }

  /**
   * 预处理完成
   */
  protected void onPostPre() {

  }

  /**
   * 延迟启动定时器
   */
  protected long delayTimer() {
    return 1000;
  }

  /**
   * 启动进度获取定时器
   */
  protected synchronized void startTimer() {
    if (isBreak()) {
      return;
    }
    ALog.d(TAG, String.format("启动定时器，delayTimer = %s, updateInterval = %s", delayTimer(),
        mUpdateInterval));
    closeTimer();
    try {
      mTimer = new ScheduledThreadPoolExecutor(1);
      mTimer.scheduleWithFixedDelay(new Runnable() {
        @Override public void run() {
          // 线程池中是不抛异常的，没有日志，很难定位问题，需要手动try-catch
          try {
            if (mStateManager == null) {
              ALog.e(TAG, "stateManager is null");
            } else if (mStateManager.isComplete()
                || mStateManager.isFail()
                || !isRunning()
                || isBreak()) {
              //ALog.d(TAG, "isComplete = " + mStateManager.isComplete()
              //    + "; isFail = " + mStateManager.isFail()
              //    + "; isRunning = " + isRunning()
              //    + "; isBreak = " + isBreak());
              ThreadTaskManager.getInstance().removeTaskThread(mTaskWrapper.getKey());
              closeTimer();
              onDestroy();
            } else if (mStateManager.getCurrentProgress() >= 0) {
              Log.d(TAG, "running...");
              mListener.onProgress(mStateManager.getCurrentProgress());
            } else {
              Log.d(TAG, "未知状态");
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }, delayTimer(), mUpdateInterval, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      ALog.e(TAG, "启动定时器失败");
      e.printStackTrace();
    }
  }

  private synchronized void closeTimer() {
    if (mTimer != null && !mTimer.isShutdown()) {
      mTimer.shutdown();
    }
  }

  public void onDestroy() {
    isRuning = false;
  }

  /**
   * 设置定时器更新间隔
   *
   * @param interval 单位毫秒，不能小于0
   */
  protected void setUpdateInterval(long interval) {
    if (interval < 0) {
      ALog.w(TAG, "更新间隔不能小于0，默认为1000毫秒");
      return;
    }
    mUpdateInterval = interval;
  }

  @Override
  public synchronized boolean isRunning() {
    boolean b = ThreadTaskManager.getInstance().taskIsRunning(mTaskWrapper.getKey());
    //ALog.d(TAG, "isRunning = " + b);
    return b && isRuning;
  }

  @Override final public synchronized void cancel() {
    if (isCancel) {
      ALog.d(TAG, String.format("任务【%s】正在删除，删除任务失败", mTaskWrapper.getKey()));
      return;
    }
    if (mInfoTask != null){
      mInfoTask.cancel();
    }
    closeTimer();
    isCancel = true;
    onCancel();
    for (int i = 0; i < mTask.size(); i++) {
      IThreadTask task = mTask.get(i);
      if (task != null && !task.isThreadComplete()) {
        task.cancel();
      }
    }
    ThreadTaskManager.getInstance().removeTaskThread(mTaskWrapper.getKey());
    onPostCancel();
    onDestroy();
    mListener.onCancel();
  }

  /**
   * 删除线程任务前的操作
   */
  protected void onCancel() {

  }

  /**
   * 删除操作处理完成
   */
  protected void onPostCancel() {

  }

  final public synchronized void stop() {
    if (isStop) {
      return;
    }
    if (mInfoTask != null){
      mInfoTask.stop();
    }
    closeTimer();
    isStop = true;
    onStop();
    for (int i = 0; i < mTask.size(); i++) {
      IThreadTask task = mTask.get(i);
      if (task != null && !task.isThreadComplete()) {
        task.stop();
      }
    }
    ThreadTaskManager.getInstance().removeTaskThread(mTaskWrapper.getKey());
    onPostStop();
    onDestroy();
    mListener.onStop(getCurrentProgress());
  }

  /**
   * 停止线程任务前的操作
   */
  protected void onStop() {

  }

  /**
   * 停止操作完成
   */
  protected void onPostStop() {

  }

  /**
   * 重试任务
   */
  public void retryTask() {
    ALog.w(TAG, String.format("任务【%s】开始重试", mTaskWrapper.getKey()));
    startFlow();
  }

  /**
   * 任务是否已经中断
   *
   * @return {@code true}中断
   */
  public boolean isBreak() {
    if (isCancel || isStop) {
      //closeTimer();
      ALog.d(TAG, "isCancel = " + isCancel + ", isStop = " + isStop);
      ALog.d(TAG, String.format("任务【%s】已停止或取消了", mTaskWrapper.getKey()));
      return true;
    }
    return false;
  }

  /**
   * 检查组件:  {@link #mRecordHandler}、{@link #mInfoTask}、{@link #mStateManager}、{@link #mTTBuilder}
   */
  protected void checkComponent() {
    if (mRecordHandler == null) {
      throw new NullPointerException("任务记录组件为空");
    }
    if (mInfoTask == null) {
      throw new NullPointerException(("文件信息组件为空"));
    }
    if (mStateManager == null) {
      throw new NullPointerException("任务状态管理组件为空");
    }
    if (mTTBuilder == null) {
      throw new NullPointerException("线程任务组件为空");
    }
  }
}
