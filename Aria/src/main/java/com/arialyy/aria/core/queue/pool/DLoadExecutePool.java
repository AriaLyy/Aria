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
package com.arialyy.aria.core.queue.pool;

import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.util.ALog;

/**
 * Created by AriaL on 2017/6/29.
 * 单个下载任务的执行池
 */
class DLoadExecutePool<TASK extends AbsTask> extends BaseExecutePool<TASK> {
  private final String TAG = "DownloadExecutePool";

  @Override protected int getMaxSize() {
    return AriaConfig.getInstance().getDConfig().getMaxTaskNum();
  }

  @Override public boolean putTask(TASK task) {
    synchronized (DLoadExecutePool.class) {
      if (task == null) {
        ALog.e(TAG, "任务不能为空！！");
        return false;
      }
      if (mExecuteQueue.contains(task)) {
        if (!task.isRunning()) return true;
        ALog.e(TAG, "任务【" + task.getTaskName() + "】进入执行队列失败，错误原因：已经在执行队列中");
        return false;
      } else {
        if (mExecuteQueue.size() >= mSize) {
          for (TASK temp : mExecuteQueue) {
            if (temp.isHighestPriorityTask()) {
              return false;
            }
          }
          if (pollFirstTask()) {
            return putNewTask(task);
          }
        } else {
          return putNewTask(task);
        }
      }
    }
    return false;
  }

  @Override boolean pollFirstTask() {
    TASK oldTask = mExecuteQueue.pollFirst();
    if (oldTask == null) {
      ALog.w(TAG, "移除任务失败，错误原因：任务为null");
      return false;
    }
    if (oldTask.isHighestPriorityTask()) {
      return false;
    }
    oldTask.stop();
    return true;
  }
}
