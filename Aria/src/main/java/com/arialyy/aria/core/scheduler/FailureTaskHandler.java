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
package com.arialyy.aria.core.scheduler;

import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.manager.TaskWrapperManager;
import com.arialyy.aria.core.queue.ITaskQueue;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 处理失败的任务
 */
class FailureTaskHandler<TASK extends ITask> {
  private final String TAG = CommonUtil.getClassName(getClass());

  private static volatile FailureTaskHandler INSTANCE;
  private final int MAX_EXE_NUM = 5; // 同时处理的失败任务数量
  private ArrayBlockingQueue<TASK> mQueue = new ArrayBlockingQueue<>(100);
  private List<TASK> mExeQueue = new ArrayList<>(MAX_EXE_NUM);
  private List<Integer> mHashList = new ArrayList<>();
  private TaskSchedulers mSchedulers;
  private final ReentrantLock LOCK = new ReentrantLock();
  private Condition mCondition = LOCK.newCondition();

  static FailureTaskHandler init(TaskSchedulers schedulers) {
    if (INSTANCE == null) {
      synchronized (FailureTaskHandler.class) {
        if (INSTANCE == null) {
          INSTANCE = new FailureTaskHandler(schedulers);
        }
      }
    }
    return INSTANCE;
  }

  private FailureTaskHandler(TaskSchedulers schedulers) {
    mSchedulers = schedulers;
    new Thread(new Runnable() {
      @Override public void run() {
        while (true) {
          try {
            TASK task = mQueue.take();
            if (mExeQueue.size() >= MAX_EXE_NUM) {
              try {
                LOCK.lock();
                mCondition.await();
              } finally {
                LOCK.unlock();
              }
            } else {
              retryTask(task);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }).start();
  }

  private void retryTask(final TASK task) {
    final ITaskQueue queue = mSchedulers.getQueue(task.getTaskType());
    if (task.isNeedRetry()){
      long interval = task.getTaskWrapper().getConfig().getReTryInterval();
      final int num = task.getTaskWrapper().getConfig().getReTryNum();
      AriaConfig.getInstance().getAriaHandler().postDelayed(new Runnable() {
        @Override public void run() {
          AbsEntity entity = task.getTaskWrapper().getEntity();
          if (entity.getFailNum() <= num) {
            ALog.d(TAG, String.format("任务【%s】开始重试", task.getTaskName()));
            queue.reTryStart(task);
          } else {
            queue.removeTaskFormQueue(task.getKey());
            mSchedulers.startNextTask(queue, task.getSchedulerType());
            TaskWrapperManager.getInstance().removeTaskWrapper(task.getTaskWrapper());
          }
          next(task);
        }
      }, interval);
    }else {
      queue.removeTaskFormQueue(task.getKey());
      mSchedulers.startNextTask(queue, task.getSchedulerType());
      TaskWrapperManager.getInstance().removeTaskWrapper(task.getTaskWrapper());
      next(task);
    }
  }

  private void next(TASK task){
    mExeQueue.remove(task);
    int index = mHashList.indexOf(task.hashCode());
    if (index != -1) {
      mHashList.remove(index);
    }
    if (LOCK.isLocked()) {
      try {
        LOCK.lock();
        mCondition.signalAll();
      } finally {
        LOCK.unlock();
      }
    }
  }

  void offer(TASK task) {
    int hashCode = task.hashCode();
    if (mHashList.contains(hashCode)) {
      return;
    }
    mQueue.offer(task);
    mHashList.add(hashCode);
  }
}
