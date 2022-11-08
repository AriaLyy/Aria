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
package com.arialyy.aria.ftp.upload;

import android.os.Handler;
import aria.apache.commons.net.ftp.FTPFile;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.loader.NormalLoader;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.exception.AriaException;

final class FtpULoader extends NormalLoader<UTaskWrapper> {
  private FTPFile ftpFile;

  FtpULoader(UTaskWrapper wrapper, IEventListener listener) {
    super(wrapper, listener);
  }

  @Override
  protected void startThreadTask() {
    if (isBreak()) {
      return;
    }
    // 检查记录
    ((FtpURecordHandler) mRecordHandler).setFtpFile(ftpFile);
    if (mRecordHandler.checkTaskCompleted()) {
      mRecord.deleteData();
      isComplete = true;
      getListener().onComplete();
      return;
    }
    mRecord = mRecordHandler.getRecord(getFileSize());

    // 初始化线程状态管理器
    mStateManager.setLooper(mRecord, getLooper());
    getTaskList().addAll(mTTBuilder.buildThreadTask(mRecord,
        new Handler(getLooper(), mStateManager.getHandlerCallback())));
    mStateManager.updateCurrentProgress(getEntity().getCurrentProgress());

    if (mStateManager.getCurrentProgress() > 0) {
      getListener().onResume(mStateManager.getCurrentProgress());
    } else {
      getListener().onStart(mStateManager.getCurrentProgress());
    }

    startTimer();

    // 启动线程任务
    for (IThreadTask threadTask : getTaskList()) {
      ThreadTaskManager.getInstance().startThread(mTaskWrapper.getKey(), threadTask);
    }
  }

  @Override public void addComponent(IInfoTask infoTask) {
    mInfoTask = infoTask;
    infoTask.setCallback(new IInfoTask.Callback() {
      @Override public void onSucceed(String key, CompleteInfo info) {
        if (info.code == FtpUFileInfoTask.CODE_COMPLETE) {
          getListener().onComplete();
        } else {
          ftpFile = (FTPFile) info.obj;
          startThreadTask();
        }
      }

      @Override public void onFail(AbsEntity entity, AriaException e, boolean needRetry) {
        getListener().onFail(needRetry, e);
      }
    });
  }

  @Override public void addComponent(IRecordHandler recordHandler) {
    mRecordHandler = recordHandler;
  }
}
