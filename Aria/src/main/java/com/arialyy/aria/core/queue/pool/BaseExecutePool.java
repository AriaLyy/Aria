/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
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

import android.text.TextUtils;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by lyy on 2016/8/15. 任务执行池，所有当前下载任务都该任务池中，默认下载大小为2
 */
public class BaseExecutePool<TASK extends AbsTask> implements IPool<TASK> {
  private final String TAG = CommonUtil.getClassName(this);
  private static final Object LOCK = new Object();
  Deque<TASK> mExecuteQueue;
  int mSize;

  BaseExecutePool() {
    mSize = getMaxSize();
    mExecuteQueue = new LinkedBlockingDeque<>(mSize);
  }

  /**
   * 获取最大任务数配置
   *
   * @return {@link AriaManager#getDownloadConfig()} {@link AriaManager#getUploadConfig()}，如果不设置，默认返回2
   */
  protected int getMaxSize() {
    return 2;
  }

  /**
   * 获取所有正在执行的任务
   */
  public List<TASK> getAllTask() {
    return new ArrayList<>(mExecuteQueue);
  }

  @Override public boolean putTask(TASK task) {
    synchronized (LOCK) {
      if (task == null) {
        ALog.e(TAG, "任务不能为空！！");
        return false;
      }
      if (mExecuteQueue.contains(task)) {
        ALog.e(TAG, "任务【" + task.getTaskName() + "】进入执行队列失败，原因：已经在执行队列中");
        return false;
      } else {
        if (mExecuteQueue.size() >= mSize) {
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

  /**
   * 设置执行队列最大任务数
   *
   * @param maxNum 下载数
   */
  public void setMaxNum(int maxNum) {
    synchronized (LOCK) {
      Deque<TASK> temp = new LinkedBlockingDeque<>(maxNum);
      TASK task;
      while ((task = mExecuteQueue.poll()) != null) {
        temp.offer(task);
      }
      mExecuteQueue = temp;
      mSize = maxNum;
    }
  }

  /**
   * 添加新任务
   *
   * @param newTask 新任务
   */
  boolean putNewTask(TASK newTask) {
    synchronized (LOCK) {
      boolean s = mExecuteQueue.offer(newTask);
      ALog.d(TAG, "任务【" + newTask.getTaskName() + "】进入执行队列" + (s ? "成功" : "失败"));
      return s;
    }
  }

  /**
   * 队列满时，将移除下载队列中的第一个任务
   */
  boolean pollFirstTask() {
    synchronized (LOCK) {
      TASK oldTask = mExecuteQueue.pollFirst();
      if (oldTask == null) {
        ALog.w(TAG, "移除任务失败，原因：任务为null");
        return false;
      }
      oldTask.stop();
      return true;
    }
  }

  @Override public TASK pollTask() {
    synchronized (LOCK) {
      return mExecuteQueue.poll();
    }
  }

  @Override public TASK getTask(String key) {
    synchronized (LOCK) {
      if (TextUtils.isEmpty(key)) {
        ALog.e(TAG, "key为null");
        return null;
      }
      for (TASK task : mExecuteQueue) {
        if (task.getKey().equals(key)) {
          return task;
        }
      }

      return null;
    }
  }

  @Override public boolean taskExits(String key) {
    return getTask(key) != null;
  }

  @Override public boolean removeTask(TASK task) {
    synchronized (LOCK) {
      if (task == null) {
        ALog.e(TAG, "任务不能为空");
        return false;
      } else {
        return removeTask(task.getKey());
      }
    }
  }

  @Override public boolean removeTask(String key) {
    synchronized (LOCK) {
      if (TextUtils.isEmpty(key)) {
        ALog.e(TAG, "key 为null");
        return false;
      }

      return mExecuteQueue.remove(getTask(key));
    }
  }

  @Override public int size() {
    return mExecuteQueue.size();
  }
}