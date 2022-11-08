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
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.event.DGMaxNumEvent;
import com.arialyy.aria.core.event.Event;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.core.scheduler.TaskSchedulers;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by AriaL on 2017/6/29. 任务组下载队列
 */
public class DGroupTaskQueue
    extends AbsTaskQueue<DownloadGroupTask, DGTaskWrapper> {
  private static volatile DGroupTaskQueue INSTANCE = null;

  private final String TAG = CommonUtil.getClassName(this);

  public static DGroupTaskQueue getInstance() {
    if (INSTANCE == null) {
      synchronized (DGroupTaskQueue.class) {
        INSTANCE = new DGroupTaskQueue();
        EventMsgUtil.getDefault().register(INSTANCE);
      }
    }
    return INSTANCE;
  }

  private DGroupTaskQueue() {
  }

  @Override int getQueueType() {
    return TYPE_DG_QUEUE;
  }

  @Event
  public void maxTaskNum(DGMaxNumEvent event) {
    setMaxTaskNum(event.maxNum);
  }

  @Override public int getMaxTaskNum() {
    return AriaConfig.getInstance().getDGConfig().getMaxTaskNum();
  }

  @Override public DownloadGroupTask createTask(DGTaskWrapper wrapper) {
    super.createTask(wrapper);
    DownloadGroupTask task = null;
    if (!mCachePool.taskExits(wrapper.getKey()) && !mExecutePool.taskExits(wrapper.getKey())) {
      task = (DownloadGroupTask) TaskFactory.getInstance()
          .createTask(wrapper, TaskSchedulers.getInstance());
      addTask(task);
    } else {
      ALog.w(TAG, "任务已存在");
    }
    return task;
  }

  @Override public int getOldMaxNum() {
    return AriaConfig.getInstance().getDGConfig().oldMaxTaskNum;
  }
}
