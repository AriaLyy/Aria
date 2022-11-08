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
public interface NormalTaskListenerInterface<TASK extends ITask> extends
    TaskInternalListenerInterface {

  /**
   * 队列已经满了，继续创建任务，将会回调该方法
   */
  public void onWait(TASK task);

  /**
   * 预处理，有时有些地址链接比较慢，这时可以先在这个地方出来一些界面上的UI，如按钮的状态。
   * 在这个回调中，任务是获取不到文件大小，下载速度等参数
   */
  public void onPre(TASK task);

  /**
   * 任务预加载完成
   */
  public void onTaskPre(TASK task);

  /**
   * 任务恢复下载
   */
  public void onTaskResume(TASK task);

  /**
   * 任务开始
   */
  public void onTaskStart(TASK task);

  /**
   * 任务停止
   */
  public void onTaskStop(TASK task);

  /**
   * 任务取消
   */
  public void onTaskCancel(TASK task);

  /**
   * 任务失败
   */
  public void onTaskFail(TASK task, Exception e);

  /**
   * 任务完成
   */
  public void onTaskComplete(TASK task);

  /**
   * 任务执行中
   */
  public void onTaskRunning(TASK task);

  public void onNoSupportBreakPoint(TASK task);
}