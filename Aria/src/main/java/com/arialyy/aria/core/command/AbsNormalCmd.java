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

package com.arialyy.aria.core.command;

import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.queue.DGroupTaskQueue;
import com.arialyy.aria.core.queue.DTaskQueue;
import com.arialyy.aria.core.queue.UTaskQueue;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2016/8/22. 下载命令
 */
public abstract class AbsNormalCmd<T extends AbsTaskWrapper> extends AbsCmd<T> {
  /**
   * 能否执行命令
   */
  boolean canExeCmd = true;

  int taskType;

  /**
   * @param taskType 下载任务类型{@link ITask#DOWNLOAD}、{@link ITask#DOWNLOAD_GROUP}、{@link
   * ITask#UPLOAD}
   */
  AbsNormalCmd(T entity, int taskType) {
    this.taskType = taskType;
    mTaskWrapper = entity;
    TAG = CommonUtil.getClassName(this);
    if (taskType == ITask.DOWNLOAD) {
      if (!(entity instanceof DTaskWrapper)) {
        ALog.e(TAG, "任务类型错误，任务类型应该为ICM.TASK_TYPE_DOWNLOAD");
        return;
      }
      mQueue = DTaskQueue.getInstance();
    } else if (taskType == ITask.DOWNLOAD_GROUP) {
      if (!(entity instanceof DGTaskWrapper)) {
        ALog.e(TAG, "任务类型错误，任务类型应该为ICM.TASK_TYPE_DOWNLOAD_GROUP");
        return;
      }
      mQueue = DGroupTaskQueue.getInstance();
    } else if (taskType == ITask.UPLOAD) {
      if (!(entity instanceof UTaskWrapper)) {
        ALog.e(TAG, "任务类型错误，任务类型应该为ICM.TASK_TYPE_UPLOAD");
        return;
      }
      mQueue = UTaskQueue.getInstance();
    } else {
      ALog.e(TAG, "任务类型错误，任务类型应该为ICM.TASK_TYPE_DOWNLOAD、TASK_TYPE_DOWNLOAD_GROUP、TASK_TYPE_UPLOAD");
      return;
    }
    isDownloadCmd = taskType == ITask.DOWNLOAD || taskType == ITask.DOWNLOAD_GROUP;
  }

  /**
   * 发送等待状态
   */
  void sendWaitState() {
    AbsTask task = getTask();
    if (task == null) {
      task = createTask();
    }
    sendWaitState(task);
  }

  /**
   * 发送等待状态
   */
  void sendWaitState(AbsTask task) {
    if (task != null) {
      task.getTaskWrapper().setState(IEntity.STATE_WAIT);
      task.getOutHandler().obtainMessage(ISchedulers.WAIT, task).sendToTarget();
    }
  }

  /**
   * 停止所有任务
   */
  void stopAll() {
    mQueue.stopAllTask();
  }

  /**
   * 停止任务
   */
  void stopTask() {
    AbsTask task = getTask();
    if (task == null) {
      task = createTask();
    }
    mQueue.stopTask(task);
  }

  /**
   * 删除任务
   */
  void removeTask() {
    AbsTask task = getTask();
    if (task == null) {
      task = createTask();
    }
    mQueue.cancelTask(task);
  }

  /**
   * 删除任务
   */
  void removeTask(AbsTaskWrapper wrapper) {
    AbsTask tempTask = getTask(wrapper.getKey());
    if (tempTask == null) {
      tempTask = createTask(wrapper);
    }
    mQueue.cancelTask(tempTask);
  }

  /**
   * 启动任务
   */
  void startTask() {
    AbsTask task = getTask();
    if (task == null) {
      task = createTask();
    }
    mQueue.startTask(task);
  }

  /**
   * 恢复任务
   */
  void resumeTask() {
    AbsTask task = getTask();
    if (task == null) {
      task = createTask();
    }
    mQueue.resumeTask(task);
  }


  /**
   * 从队列中获取任务
   *
   * @return 执行任务
   */
  AbsTask getTask() {
    return mQueue.getTask(mTaskWrapper.getEntity().getKey());
  }

  /**
   * 从队列中获取任务
   *
   * @return 执行任务
   */
  AbsTask getTask(String key) {
    return mQueue.getTask(key);
  }

  /**
   * 创建任务
   *
   * @return 创建的任务
   */
  AbsTask createTask() {
    return mQueue.createTask(mTaskWrapper);
  }

  /**
   * 创建指定实体的任务
   *
   * @param wrapper 特定的任务实体
   * @return 创建的任务
   */
  AbsTask createTask(AbsTaskWrapper wrapper) {

    return mQueue.createTask(wrapper);
  }
}