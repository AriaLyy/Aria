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

import com.arialyy.aria.core.common.AbsNormalEntity;
import com.arialyy.aria.core.task.ITask;

/**
 * Created by Aria.Lao on 2019/6/26.
 * 子任务事件回调类
 */
public class AptSubTaskListener<TASK extends ITask, SUB_ENTITY extends AbsNormalEntity>
    implements SubTaskListener<TASK, SUB_ENTITY>, ISchedulerListener {

  @Override public void onNoSupportBreakPoint(TASK task) {

  }

  @Override public void onSubTaskPre(TASK task, SUB_ENTITY subTask) {

  }

  @Override public void onSubTaskStart(TASK task, SUB_ENTITY subTask) {

  }

  @Override public void onSubTaskStop(TASK task, SUB_ENTITY subTask) {

  }

  @Override public void onSubTaskCancel(TASK task, SUB_ENTITY subTask) {

  }

  @Override public void onSubTaskComplete(TASK task, SUB_ENTITY subTask) {

  }

  @Deprecated
  public void onSubTaskFail(TASK task, SUB_ENTITY subTask) {

  }

  @Override public void onSubTaskFail(TASK task, SUB_ENTITY subTask, Exception e) {

  }

  @Override public void onSubTaskRunning(TASK task, SUB_ENTITY subTask) {

  }

  @Override public void setListener(Object obj) {

  }
}