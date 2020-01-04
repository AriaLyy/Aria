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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.core.task.ThreadTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.exception.BaseException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.util.List;

/**
 * 子任务加载器
 */
public final class SubLoader implements ILoader, ILoaderVisitor {
  private String TAG = CommonUtil.getClassName(this);
  // 是否需要获取信息
  private boolean needGetInfo = true;
  private Handler schedulers;
  private boolean isCancel = false, isStop = false;
  private AbsTaskWrapper wrapper;
  private IInfoTask infoTask;
  private IThreadTaskBuilder ttBuild;
  private IRecordHandler recordHandler;
  private IThreadTask threadTask;
  private String parentKey;

  public SubLoader(AbsTaskWrapper wrapper, Handler schedulers) {
    this.wrapper = wrapper;
    this.schedulers = schedulers;
  }

  public AbsTaskWrapper getWrapper() {
    return wrapper;
  }

  /**
   * 发送状态到调度器
   *
   * @param state {@link IThreadStateManager}
   */
  private void sendNormalState(int state) {
    Message msg = schedulers.obtainMessage();
    Bundle b = msg.getData();
    if (b == null) {
      b = new Bundle();
    }
    b.putString(IThreadStateManager.DATA_THREAD_NAME, getKey());
    msg.what = state;
    msg.setData(b);
    msg.sendToTarget();
  }

  /**
   * 发送失败的状态
   */
  private void sendFailState(boolean needRetry) {
    Message msg = schedulers.obtainMessage();
    Bundle b = msg.getData();
    if (b == null) {
      b = new Bundle();
    }
    b.putString(IThreadStateManager.DATA_THREAD_NAME, getKey());
    b.putBoolean(IThreadStateManager.DATA_RETRY, needRetry);
    msg.what = IThreadStateManager.STATE_FAIL;
    msg.setData(b);
    msg.sendToTarget();
  }

  private void handlerTask() {
    TaskRecord record = recordHandler.getRecord(wrapper.getEntity().getFileSize());
    if (record.threadRecords != null
        && !TextUtils.isEmpty(record.filePath)
        && new File(record.filePath).exists()
        && !record.threadRecords.isEmpty()
        && record.threadRecords.get(0).isComplete) {
      ALog.d(TAG, "子任务已完成，key：" + wrapper.getKey());
      sendNormalState(IThreadStateManager.STATE_COMPLETE);
      return;
    }
    List<IThreadTask> task = ttBuild.buildThreadTask(record, schedulers);
    if (task == null || task.isEmpty()) {
      ALog.e(TAG, "创建子任务的线程任务失败，key：" + wrapper.getKey());
      sendFailState(false);
      return;
    }
    if (TextUtils.isEmpty(parentKey)) {
      ALog.e(TAG, "parentKey为空");
      sendFailState(false);
      return;
    }
    sendNormalState(IThreadStateManager.STATE_PRE);
    threadTask = task.get(0);
    try {
      ThreadTaskManager.getInstance().startThread(parentKey, threadTask);
      sendNormalState(IThreadStateManager.STATE_START);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setParentKey(String parentKey) {
    this.parentKey = parentKey;
  }

  public void setNeedGetInfo(boolean needGetInfo) {
    this.needGetInfo = needGetInfo;
  }

  public void retryTask() {
    try {
      if (threadTask != null) {
        threadTask.call();
      } else {
        ALog.e(TAG, "子任务的线程任务为空");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override public void stop() {
    if (isStop) {
      ALog.w(TAG, "子任务已停止");
      return;
    }
    isStop = true;
    threadTask.stop();
  }

  @Override public boolean isRunning() {
    return threadTask != null && !threadTask.isBreak();
  }

  @Override public void cancel() {
    if (isCancel) {
      ALog.w(TAG, "子任务已取消");
      return;
    }
    isCancel = true;
    threadTask.cancel();
  }

  @Override public boolean isBreak() {
    if (isCancel || isStop) {
      ALog.d(TAG, "isCancel = " + isCancel + ", isStop = " + isStop);
      ALog.d(TAG, String.format("任务【%s】已停止或取消了", wrapper.getKey()));
      return true;
    }
    return false;
  }

  /**
   * 线程名，一个子任务的loader只有一个线程，使用线程名标示key
   *
   * @return {@link ThreadTask#getThreadName()}
   */
  @Override public String getKey() {
    return CommonUtil.getThreadName(wrapper.getKey(), 0);
  }

  /**
   * @deprecated 子任务不需要实现这个
   */
  @Deprecated
  @Override public long getCurrentProgress() {
    return 0;
  }

  @Override public void addComponent(IRecordHandler recordHandler) {
    this.recordHandler = recordHandler;
  }

  @Override public void addComponent(IInfoTask infoTask) {
    this.infoTask = infoTask;
    infoTask.setCallback(new IInfoTask.Callback() {
      @Override public void onSucceed(String key, CompleteInfo info) {
        handlerTask();
      }

      @Override public void onFail(AbsEntity entity, BaseException e, boolean needRetry) {
        sendFailState(needRetry);
      }
    });
  }

  /**
   * @deprecated 子任务不需要实现这个
   */
  @Override public void addComponent(IThreadStateManager threadState) {
    // 子任务不需要实现这个
  }

  @Override public void addComponent(IThreadTaskBuilder builder) {
    ttBuild = builder;
  }

  @Override public void run() {
    checkComponent();
    if (isBreak()) {
      return;
    }
    if (needGetInfo) {
      infoTask.run();
    } else {
      handlerTask();
    }
  }

  /**
   * 检查组件:  {@link #recordHandler}、{@link #infoTask}、{@link #ttBuild}
   */
  private void checkComponent() {
    if (recordHandler == null) {
      throw new NullPointerException("任务记录组件为空");
    }
    if (infoTask == null) {
      throw new NullPointerException(("文件信息组件为空"));
    }
    if (ttBuild == null) {
      throw new NullPointerException("线程任务组件为空");
    }
  }
}
