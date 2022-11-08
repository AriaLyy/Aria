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

import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.task.UploadTask;

/**
 * Created by Aria.Lao on 2017/6/7.
 * 普通任务事件{@link DownloadTask}、{@link UploadTask}、{@link DownloadGroupTask}回调类
 */
public class AptNormalTaskListener<TASK extends ITask> implements NormalTaskListenerInterface<TASK>, ISchedulerListener {

  /**
   * 队列已经满了，继续创建任务，将会回调该方法
   */
  @Override public void onWait(TASK task) {

  }

  /**
   * 预处理，有时有些地址链接比较慢，这时可以先在这个地方出来一些界面上的UI，如按钮的状态。
   * 在这个回调中，任务是获取不到文件大小，下载速度等参数
   */
  @Override public void onPre(TASK task) {

  }

  /**
   * 任务预加载完成
   */
  @Override public void onTaskPre(TASK task) {

  }

  /**
   * 任务恢复下载
   */
  @Override public void onTaskResume(TASK task) {

  }

  /**
   * 任务开始
   */
  @Override public void onTaskStart(TASK task) {

  }

  /**
   * 任务停止
   */
  @Override public void onTaskStop(TASK task) {

  }

  /**
   * 任务取消
   */
  @Override public void onTaskCancel(TASK task) {

  }

  /**
   * 任务失败
   *
   * @deprecated {@link #onTaskFail(ITask, Exception)}
   */
  public void onTaskFail(TASK task) {

  }

  /**
   * 任务失败
   */
  @Override public void onTaskFail(TASK task, Exception e) {

  }

  /**
   * 任务完成
   */
  @Override public void onTaskComplete(TASK task) {

  }

  /**
   * 任务执行中
   */
  @Override public void onTaskRunning(TASK task) {

  }

  @Override public void onNoSupportBreakPoint(TASK task) {

  }

  @Override public void setListener(Object obj) {

  }
}