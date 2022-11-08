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
package com.arialyy.aria.http.upload;

import android.os.Handler;
import android.os.Looper;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.loader.AbsNormalLoader;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.loader.IThreadTaskBuilder;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.exception.AriaHTTPException;
import com.arialyy.aria.util.ALog;
import java.util.List;

final class HttpULoader extends AbsNormalLoader<UTaskWrapper> {
  HttpULoader(UTaskWrapper wrapper, IEventListener listener) {
    super(wrapper, listener);
  }

  @Override public void addComponent(IRecordHandler recordHandler) {
    mRecordHandler = recordHandler;
  }

  /**
   * @deprecated http 上传任务不需要设置这个
   */
  @Deprecated
  @Override public void addComponent(IInfoTask infoTask) {

  }

  @Override public void addComponent(IThreadStateManager threadState) {
    mStateManager = threadState;
  }

  @Override public void addComponent(IThreadTaskBuilder builder) {
    mTTBuilder = builder;
  }

  @Override protected void handleTask(Looper looper) {
    mRecord = mRecordHandler.getRecord(getFileSize());
    mStateManager.setLooper(mRecord, looper);
    List<IThreadTask> tt = mTTBuilder.buildThreadTask(mRecord,
        new Handler(looper, mStateManager.getHandlerCallback()));
    if (tt == null || tt.isEmpty()) {
      ALog.e(TAG, "创建线程任务失败");
      getListener().onFail(false, new AriaHTTPException("创建线程任务失败"));
      return;
    }

    getListener().onStart(0);
    ThreadTaskManager.getInstance().startThread(mTaskWrapper.getKey(), tt.get(0));

    startTimer();
  }

  @Override public long getFileSize() {
    return mTaskWrapper.getEntity().getFileSize();
  }

  @Override protected void checkComponent() {
    if (mRecordHandler == null) {
      throw new NullPointerException("任务记录组件为空");
    }
    if (mStateManager == null) {
      throw new NullPointerException("任务状态管理组件为空");
    }
    if (mTTBuilder == null) {
      throw new NullPointerException("线程任务组件为空");
    }
  }

  @Override public long getCurrentProgress() {
    return mStateManager.getCurrentProgress();
  }
}
