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
package com.arialyy.aria.sftp.upload;

import android.os.Handler;
import android.os.Looper;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.listener.IDLoadListener;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.loader.AbsNormalLoader;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.loader.IThreadTaskBuilder;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.FileUtil;
import com.jcraft.jsch.SftpATTRS;
import java.io.File;

final class SFtpULoader extends AbsNormalLoader<UTaskWrapper> {

  private Looper looper;

  SFtpULoader(UTaskWrapper wrapper, IEventListener listener) {
    super(wrapper, listener);
    mTempFile = new File(getEntity().getFilePath());
    EventMsgUtil.getDefault().register(this);
    setUpdateInterval(wrapper.getConfig().getUpdateInterval());
  }

  private UploadEntity getEntity() {
    return mTaskWrapper.getEntity();
  }

  @Override public long getFileSize() {
    return getEntity().getFileSize();
  }

  /**
   * 设置最大下载/上传速度AbsFtpInfoThread
   *
   * @param maxSpeed 单位为：kb
   */
  protected void setMaxSpeed(int maxSpeed) {
    for (IThreadTask threadTask : getTaskList()) {
      if (threadTask != null) {
        threadTask.setMaxSpeed(maxSpeed);
      }
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    EventMsgUtil.getDefault().unRegister(this);
  }

  /**
   * 启动单线程任务
   */
  @Override
  public void handleTask(Looper looper) {
    if (isBreak()) {
      return;
    }
    this.looper = looper;
    mInfoTask.run();
  }

  private void startThreadTask(SftpATTRS attrs) {
    if (isBreak()) {
      return;
    }

    if (getListener() instanceof IDLoadListener) {
      ((IDLoadListener) getListener()).onPostPre(getEntity().getFileSize());
    }
    File file = new File(getEntity().getFilePath());
    if (file.getParentFile() != null && !file.getParentFile().exists()) {
      FileUtil.createDir(file.getPath());
    }
    // 处理记录、初始化状态管理器
    SFtpURecordHandler recordHandler = (SFtpURecordHandler) mRecordHandler;
    recordHandler.setFtpAttrs(attrs);
    mRecord = recordHandler.getRecord(getFileSize());
    mStateManager.setLooper(mRecord, looper);

    // 创建线程任务
    getTaskList().addAll(mTTBuilder.buildThreadTask(mRecord,
        new Handler(looper, mStateManager.getHandlerCallback())));

    mStateManager.updateCurrentProgress(getEntity().getCurrentProgress());
    if (mStateManager.getCurrentProgress() > 0) {
      getListener().onResume(mStateManager.getCurrentProgress());
    } else {
      getListener().onStart(mStateManager.getCurrentProgress());
    }

    // 启动线程任务
    for (IThreadTask threadTask : getTaskList()) {
      ThreadTaskManager.getInstance().startThread(mTaskWrapper.getKey(), threadTask);
    }

    // 启动定时器
    startTimer();
  }

  @Override public long getCurrentProgress() {
    return isRunning() ? mStateManager.getCurrentProgress() : getEntity().getCurrentProgress();
  }

  @Override public void addComponent(IRecordHandler recordHandler) {
    mRecordHandler = recordHandler;
  }

  @Override public void addComponent(IInfoTask infoTask) {
    mInfoTask = infoTask;
    infoTask.setCallback(new IInfoTask.Callback() {
      @Override public void onSucceed(String key, CompleteInfo info) {
        if (info.code == SFtpUInfoTask.ISCOMPLETE) {
          getListener().onComplete();
        } else {
          startThreadTask((SftpATTRS) info.obj);
        }
      }

      @Override public void onFail(AbsEntity entity, AriaException e, boolean needRetry) {
        getListener().onFail(needRetry, e);
      }
    });
  }

  @Override public void addComponent(IThreadStateManager threadState) {
    mStateManager = threadState;
  }

  @Override public void addComponent(IThreadTaskBuilder builder) {
    mTTBuilder = builder;
  }
}
