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
package com.arialyy.aria.core.listener;

import android.os.Handler;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.ErrorHelp;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public abstract class BaseListener implements IEventListener {
  protected String TAG = getClass().getSimpleName();
  static final int RUN_SAVE_INTERVAL = 5 * 1000;  //5s保存一次下载中的进度
  protected SoftReference<Handler> outHandler;
  private long mLastLen;   //上一次发送长度
  private boolean isFirst = true;
  private AbsTask mTask;
  long mLastSaveTime;
  protected AbsEntity mEntity;
  protected AbsTaskWrapper mTaskWrapper;
  private boolean isConvertSpeed;
  private long mUpdateInterval;

  @Override public IEventListener setParams(AbsTask task, Handler outHandler) {
    this.outHandler = new SoftReference<>(outHandler);
    mTask = new WeakReference<>(task).get();
    mEntity = mTask.getTaskWrapper().getEntity();
    mTaskWrapper = mTask.getTaskWrapper();
    isConvertSpeed = mTaskWrapper.getConfig().isConvertSpeed();
    mUpdateInterval = mTaskWrapper.getConfig().getUpdateInterval();
    mLastLen = mEntity.getCurrentProgress();
    mLastSaveTime = System.currentTimeMillis();
    TAG = CommonUtil.getClassName(getClass());
    return this;
  }

  protected <TASK extends AbsTask> TASK getTask(Class<TASK> clazz) {
    return (TASK) mTask;
  }

  @Override public void onPre() {
    saveData(IEntity.STATE_PRE, -1);
    sendInState2Target(ISchedulers.PRE);
  }

  @Override public void onStart(long startLocation) {
    saveData(IEntity.STATE_RUNNING, startLocation);
    sendInState2Target(ISchedulers.START);
  }

  @Override public void onResume(long resumeLocation) {
    saveData(IEntity.STATE_RUNNING, resumeLocation);
    sendInState2Target(ISchedulers.RESUME);
  }

  @Override public void onProgress(long currentLocation) {
    mEntity.setCurrentProgress(currentLocation);
    long speed = currentLocation - mLastLen;
    if (isFirst) {
      speed = 0;
      isFirst = false;
    }
    handleSpeed(speed);
    sendInState2Target(ISchedulers.RUNNING);
    if (System.currentTimeMillis() - mLastSaveTime >= RUN_SAVE_INTERVAL) {
      saveData(IEntity.STATE_RUNNING, currentLocation);
      mLastSaveTime = System.currentTimeMillis();
    }

    mLastLen = currentLocation;
  }

  @Override public void onStop(long stopLocation) {
    saveData(mTask.getSchedulerType() == TaskSchedulerType.TYPE_STOP_AND_WAIT ? IEntity.STATE_WAIT
        : IEntity.STATE_STOP, stopLocation);
    handleSpeed(0);
    sendInState2Target(ISchedulers.STOP);
  }

  @Override public void onComplete() {
    saveData(IEntity.STATE_COMPLETE, mEntity.getFileSize());
    handleSpeed(0);
    sendInState2Target(ISchedulers.COMPLETE);
  }

  @Override public void onCancel() {
    saveData(IEntity.STATE_CANCEL, -1);
    handleSpeed(0);
    if (mTask.getSchedulerType() != TaskSchedulerType.TYPE_CANCEL_AND_NOT_NOTIFY) {
      ALog.d(TAG, "删除任务完成");
      sendInState2Target(ISchedulers.CANCEL);
    }
  }

  @Override public void onFail(boolean needRetry, AriaException e) {
    mEntity.setFailNum(mEntity.getFailNum() + 1);
    saveData(IEntity.STATE_FAIL, mEntity.getCurrentProgress());
    handleSpeed(0);
    mTask.setNeedRetry(needRetry);
    mTask.putExpand(AbsTask.ERROR_INFO_KEY, e);
    sendInState2Target(ISchedulers.FAIL);
    if (e != null) {
      String error = ALog.getExceptionString(e);
      ALog.e(TAG, error);
      ErrorHelp.saveError(e.getMessage(), error);
    }
  }

  private void handleSpeed(long speed) {
    if (mUpdateInterval != 1000) {
      speed = speed * 1000 / mUpdateInterval;
    }
    if (isConvertSpeed) {
      mEntity.setConvertSpeed(CommonUtil.formatFileSize(speed < 0 ? 0 : speed) + "/s");
    }
    mEntity.setSpeed(speed < 0 ? 0 : speed);
    int taskType = mTaskWrapper.getRequestType();
    if (taskType != ITaskWrapper.M3U8_VOD && taskType != ITaskWrapper.M3U8_LIVE) {
      mEntity.setPercent((int) (mEntity.getFileSize() <= 0 ? 0
          : mEntity.getCurrentProgress() * 100 / mEntity.getFileSize()));
    }
    if (mEntity.getFileSize() != 0) {
      if (speed == 0) {
        mEntity.setTimeLeft(Integer.MAX_VALUE);
      } else {
        mEntity.setTimeLeft((int) ((mEntity.getFileSize() - mEntity.getCurrentProgress()) / speed));
      }
    }
  }

  /**
   * 处理任务完成后的情况
   */
  private void handleComplete() {
    mEntity.setComplete(true);
    mEntity.setCompleteTime(System.currentTimeMillis());
    mEntity.setCurrentProgress(mEntity.getFileSize());
    mEntity.setPercent(100);
    handleSpeed(0);
  }

  /**
   * 处理任务取消
   */
  protected abstract void handleCancel();

  /**
   * 将任务状态发送给下载器
   *
   * @param state {@link ISchedulers#START}
   */
  protected void sendInState2Target(int state) {
    if (outHandler.get() != null) {
      outHandler.get().obtainMessage(state, mTask).sendToTarget();
    }
  }

  protected void saveData(int state, long location) {
    mEntity.setState(state);

    if (state == IEntity.STATE_CANCEL) {
      handleCancel();
      return;
    } else if (state == IEntity.STATE_STOP) {
      mEntity.setStopTime(System.currentTimeMillis());
    } else if (state == IEntity.STATE_COMPLETE) {
      handleComplete();
    }
    if (location > 0) {
      mEntity.setCurrentProgress(location);
    }
    mEntity.update();
  }
}
