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

import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

/**
 * Created by Lyy on 2016/9/23.
 * 命令工厂
 */
public class NormalCmdFactory extends AbsCmdFactory<AbsTaskWrapper, AbsNormalCmd> {
  /**
   * 创建任务
   */
  public static final int TASK_CREATE = 0xb1;
  /**
   * 启动任务
   */
  public static final int TASK_START = 0xb2;
  /**
   * 恢复任务
   */
  public static final int TASK_RESUME = 0xb3;
  /**
   * 取消任务
   */
  public static final int TASK_CANCEL = 0xb4;
  /**
   * 停止任务
   */
  public static final int TASK_STOP = 0xb5;
  /**
   * 设置任务为最高优先级
   */
  public static final int TASK_HIGHEST_PRIORITY = 0xb6;
  /**
   * 停止所有任务
   */
  public static final int TASK_STOP_ALL = 0xb7;
  /**
   * 恢复所有停止的任务
   */
  public static final int TASK_RESUME_ALL = 0xb8;
  /**
   * 删除所有任务，
   */
  public static final int TASK_CANCEL_ALL = 0xb9;
  /**
   * 重启任务
   */
  public static final int TASK_RESTART = 0xba;

  private static volatile NormalCmdFactory INSTANCE = null;

  private NormalCmdFactory() {

  }

  public static NormalCmdFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (NormalCmdFactory.class) {
        INSTANCE = new NormalCmdFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * @param entity 下载实体
   * @param type 命令类型{@link #TASK_CREATE}、{@link #TASK_START}、{@link #TASK_CANCEL}、{@link
   * #TASK_STOP}、{@link #TASK_HIGHEST_PRIORITY}、{@link #TASK_STOP_ALL}、{@link #TASK_RESUME_ALL}
   * @param taskType {@link ITask#DOWNLOAD}、{@link ITask#DOWNLOAD_GROUP}、{@link ITask#UPLOAD}
   */
  public AbsNormalCmd createCmd(AbsTaskWrapper entity, int type, int taskType) {
    switch (type) {
      case TASK_CREATE:
        return new AddCmd<>(entity, taskType);
      case TASK_RESUME:
      case TASK_START:
        return new StartCmd<>(entity, taskType);
      case TASK_CANCEL:
        return new CancelCmd<>(entity, taskType);
      case TASK_STOP:
        return new StopCmd<>(entity, taskType);
      case TASK_HIGHEST_PRIORITY:
        return new HighestPriorityCmd<>(entity, taskType);
      case TASK_STOP_ALL:
        return new StopAllCmd<>(entity, taskType);
      case TASK_RESUME_ALL:
        return new ResumeAllCmd<>(entity, taskType);
      case TASK_CANCEL_ALL:
        return new CancelAllCmd<>(entity, taskType);
      case TASK_RESTART:
        return new ReStartCmd<>(entity, taskType);
      default:
        return null;
    }
  }
}