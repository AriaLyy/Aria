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

import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.util.ALog;

/**
 * Created by lyy on 2016/9/20.
 * 停止命令
 */
final class StopCmd<T extends AbsTaskWrapper> extends AbsNormalCmd<T> {

  StopCmd(T entity, int taskType) {
    super(entity, taskType);
  }

  @Override public void executeCmd() {
    if (!canExeCmd) return;
    AbsTask task = getTask();
    if (task == null) {
      if (mTaskWrapper.getEntity().getState() == IEntity.STATE_RUNNING) {
        stopTask();
      } else {
        ALog.w(TAG, "停止命令执行失败，【调度器中没有该任务】");
      }
    } else {
      stopTask();
    }
  }
}