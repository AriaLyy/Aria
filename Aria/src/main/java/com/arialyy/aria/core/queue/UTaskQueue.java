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

package com.arialyy.aria.core.queue;

import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.event.Event;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.core.event.UMaxNumEvent;
import com.arialyy.aria.core.scheduler.TaskSchedulers;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.task.UploadTask;
import com.arialyy.aria.util.ALog;

/**
 * Created by lyy on 2017/2/27. 上传任务队列
 */
public class UTaskQueue extends AbsTaskQueue<UploadTask, UTaskWrapper> {
  private static final String TAG = "UploadTaskQueue";
  private static volatile UTaskQueue INSTANCE = null;

  public static UTaskQueue getInstance() {
    if (INSTANCE == null) {
      synchronized (UTaskQueue.class) {
        INSTANCE = new UTaskQueue();
        EventMsgUtil.getDefault().register(INSTANCE);
      }
    }
    return INSTANCE;
  }

  private UTaskQueue() {
  }

  @Event
  public void maxTaskNum(UMaxNumEvent event){
    setMaxTaskNum(event.maxNum);
  }

  @Override int getQueueType() {
    return TYPE_U_QUEUE;
  }

  @Override public int getOldMaxNum() {
    return AriaConfig.getInstance().getUConfig().oldMaxTaskNum;
  }

  @Override public int getMaxTaskNum() {
    return AriaConfig.getInstance().getUConfig().getMaxTaskNum();
  }

  @Override public UploadTask createTask(UTaskWrapper wrapper) {
    super.createTask(wrapper);
    UploadTask task = null;
    if (!mCachePool.taskExits(wrapper.getKey()) && !mExecutePool.taskExits(wrapper.getKey())) {
      task = (UploadTask) TaskFactory.getInstance()
          .createTask(wrapper, TaskSchedulers.getInstance());
      addTask(task);
    } else {
      ALog.w(TAG, "任务已存在");
    }
    return task;
  }
}
