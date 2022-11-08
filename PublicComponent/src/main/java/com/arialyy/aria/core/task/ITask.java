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
package com.arialyy.aria.core.task;

import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

/**
 * Created by lyy on 2017/2/13.
 */
public interface ITask<TASK_WRAPPER extends AbsTaskWrapper> {

  /**
   * 普通下载任务
   */
  int DOWNLOAD = 1;
  /**
   * 上传任务
   */
  int UPLOAD = 2;
  /**
   * 组合任务
   */
  int DOWNLOAD_GROUP = 3;
  /**
   * 组合任务的子任务
   */
  int DOWNLOAD_GROUP_SUB = 4;
  /**
   * 切片任务
   */
  int M3U8_PEER = 5;
  /**
   * 填充的
   */
  int TEMP = 6;
  /**
   * 未知
   */
  int OTHER = -1;

  /**
   * 获取任务类型
   *
   * @return {@link #DOWNLOAD}、{@link #UPLOAD}、{@link #DOWNLOAD_GROUP}
   */
  int getTaskType();

  /**
   * 获取下载状态
   */
  int getState();

  /**
   * 唯一标识符，DownloadTask 为下载地址，UploadTask 为文件路径
   */
  String getKey();

  /**
   * 任务是否正在执行
   *
   * @return true，正在执行；
   */
  boolean isRunning();

  /**
   * 获取信息实体
   */
  TASK_WRAPPER getTaskWrapper();

  /**
   * 启动任务
   */
  void start();

  /**
   * 启动任务
   *
   * @param type {@link TaskSchedulerType}
   */
  void start(int type);

  /**
   * 停止任务
   */
  void stop();

  /**
   * 停止任务
   *
   * @param type {@link TaskSchedulerType}
   */
  void stop(int type);

  /**
   * 删除任务
   */
  void cancel();

  /**
   * 停止任务
   *
   * @param type {@link TaskSchedulerType}
   */
  void cancel(int type);

  /**
   * 读取扩展数据
   */
  Object getExpand(String key);

  /**
   * 任务是否停止了
   *
   * @return {@code true}任务已经停止
   */
  boolean isStop();

  /**
   * 任务是否取消了
   *
   * @return {@code true}任务已经取消
   */
  boolean isCancel();

  /**
   * 任务是否需要重试
   *
   * @return {@code true}任务已经取消
   */
  boolean isNeedRetry();

  /**
   * 获取任务名，也就是文件名
   */
  String getTaskName();

  /**
   * 任务的调度类型
   * {@link TaskSchedulerType}
   */
  int getSchedulerType();
}
