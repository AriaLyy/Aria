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

package com.arialyy.aria.core.queue;

import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.task.UploadTask;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

/**
 * Created by lyy on 2016/8/16. 任务功能接口
 */
public interface ITaskQueue<TASK extends ITask, TASK_WRAPPER extends AbsTaskWrapper> {

  /**
   * 通过key跑断任务是在存在
   *
   * @param key 下载链接，或上传文件的路径
   * @return {@code true} 任务存在
   */
  boolean taskExists(String key);

  /**
   * 通过key判断任务是否正在执行
   *
   * @param key 下载链接，或上传文件的路径
   * @return {@code true} 任务正在运行
   */
  boolean taskIsRunning(String key);

  /**
   * 恢复任务 如果执行队列任务未满，则直接启动任务。 如果执行队列已经满了，则暂停执行队列队首任务，并恢复指定任务
   *
   * @param task 需要恢复飞任务
   */
  void resumeTask(TASK task);

  /**
   * 停止所有任务
   */
  void stopAllTask();

  /**
   * 开始任务
   *
   * @param task {@link DownloadTask}、{@link UploadTask}、{@link DownloadGroupTask}
   */
  void startTask(TASK task);

  /**
   * 开始任务
   *
   * @param action {@link TaskSchedulerType}
   */
  void startTask(TASK task, int action);

  /**
   * 停止任务
   *
   * @param task {@link DownloadTask}、{@link UploadTask}、{@link DownloadGroupTask}
   */
  void stopTask(TASK task);

  /**
   * 取消任务
   *
   * @param task {@link DownloadTask}、{@link UploadTask}、{@link DownloadGroupTask}
   */
  void cancelTask(TASK task);

  /**
   * 取消任务
   *
   * @param action {@link TaskSchedulerType}
   */
  void cancelTask(TASK task, int action);

  /**
   * 通过key从队列中删除任务
   *
   * @param key 如果是下载，则为下载链接；如果是上传，为文件保存路径；如果是下载任务组，则为任务组名
   */
  void removeTaskFormQueue(String key);

  /**
   * 重试下载
   *
   * @param task {@link DownloadTask}、{@link UploadTask}、{@link DownloadGroupTask}
   */
  void reTryStart(TASK task);

  /**
   * 获取当前执行池中的任务数量
   */
  int getCurrentExePoolNum();

  /**
   * 获取当前任务缓存池中的任务数量
   */
  int getCurrentCachePoolNum();

  /**
   * 设置执行池可执行的最大任务数
   *
   * @param newMaxNum 最大任务数
   */
  void setMaxTaskNum(int newMaxNum);

  /**
   * 获取执行池可执行的最大任务数
   */
  int getMaxTaskNum();

  /**
   * 创建一个缓存任务，创建时只是将新任务存储到缓存池
   *
   * @param wrapper 任务实体{@link DTaskWrapper}、{@link UTaskWrapper}、{@link DGTaskWrapper}
   * @return {@link DownloadTask}、{@link UploadTask}、{@link DGTaskWrapper}
   */
  TASK createTask(TASK_WRAPPER wrapper);

  /**
   * 通过工作实体缓存池或任务池搜索下载任务，如果缓存池或任务池都没有任务，则创建新任务
   *
   * @param key 如果是下载，则为下载链接；如果是上传，为文件保存路径；如果是下载任务组，则为任务组名
   * @return {@link DownloadTask}、{@link UploadTask}、{@link DownloadGroupTask}
   */
  TASK getTask(String key);

  /**
   * 获取缓存池的下一个任务
   *
   * @return 下载任务 or null
   */
  TASK getNextTask();
}