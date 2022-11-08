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

import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

/**
 * 重新开始任务命令
 */
final class ReStartCmd<T extends AbsTaskWrapper> extends AbsNormalCmd<T> {

  ReStartCmd(T entity, int taskType) {
    super(entity, taskType);
  }

  @Override public void executeCmd() {
    AbsTask task = getTask();
    if (task == null) {
      task = createTask();
    }
    if (task != null) {
      mQueue.cancelTask(task, TaskSchedulerType.TYPE_CANCEL_AND_NOT_NOTIFY);
      mQueue.startTask(task, TaskSchedulerType.TYPE_START_AND_RESET_STATE);
    }
  }
}
