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

import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.common.QueueMod;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.NetUtils;

/**
 * Created by lyy on 2016/8/22. 开始命令 队列模型{@link QueueMod#NOW}、{@link QueueMod#WAIT}
 */
final public class StartCmd<T extends AbsTaskWrapper> extends AbsNormalCmd<T> {

  private boolean nowStart = false;

  StartCmd(T entity, int taskType) {
    super(entity, taskType);
  }

  /**
   * 立即执行任务
   *
   * @param nowStart true 立即执行任务，无论执行队列是否满了
   */
  public void setNowStart(boolean nowStart) {
    this.nowStart = nowStart;
  }

  @Override public void executeCmd() {
    if (!canExeCmd) return;
    if (!NetUtils.isConnected(AriaConfig.getInstance().getAPP())) {
      ALog.e(TAG, "启动任务失败，网络未连接");
      return;
    }
    String mod;
    int maxTaskNum = mQueue.getMaxTaskNum();
    AriaConfig config = AriaConfig.getInstance();
    if (isDownloadCmd) {
      mod = config.getDConfig().getQueueMod();
    } else {
      mod = config.getUConfig().getQueueMod();
    }

    AbsTask task = getTask();
    if (task == null) {
      task = createTask();
      // 任务不存在时，根据配置不同，对任务执行操作
      if (mod.equals(QueueMod.NOW.getTag())) {
        startTask();
      } else if (mod.equals(QueueMod.WAIT.getTag())) {
        int state = task.getState();
        if (mQueue.getCurrentExePoolNum() < maxTaskNum) {
          if (state == IEntity.STATE_STOP
              || state == IEntity.STATE_FAIL
              || state == IEntity.STATE_OTHER
              || state == IEntity.STATE_PRE
              || state == IEntity.STATE_POST_PRE
              || state == IEntity.STATE_COMPLETE) {
            resumeTask();
          } else if (state == IEntity.STATE_RUNNING) {
            ALog.w(TAG, String.format("任务【%s】已经在运行", task.getTaskName()));
          } else {
            ALog.d(TAG, String.format("开始新任务, 任务状态：%s", state));
            startTask();
          }
        } else {
          if (nowStart) {
            startTask();
          } else {
            sendWaitState(task);
          }
        }
      }
    } else {
      //任务没执行并且执行队列中没有该任务，才认为任务没有运行中
      if (!mQueue.taskIsRunning(task.getKey())) {
        if (mod.equals(QueueMod.NOW.getTag())) {
          resumeTask();
        } else {
          if (mQueue.getCurrentExePoolNum() < maxTaskNum) {
            resumeTask();
          } else {
            if (nowStart) {
              resumeTask();
            } else {
              sendWaitState(task);
            }
          }
        }
      } else {
        ALog.w(TAG, String.format("任务【%s】已经在运行", task.getTaskName()));
      }
    }
    if (mQueue.getCurrentCachePoolNum() == 0) {
      findAllWaitTask();
    }
  }

  /**
   * 当缓冲队列为null时，查找数据库中所有等待中的任务
   */
  private void findAllWaitTask() {
    new Thread(
        new ResumeThread(isDownloadCmd, String.format("state=%s", IEntity.STATE_WAIT))).start();
  }
}