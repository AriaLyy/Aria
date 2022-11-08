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
package com.arialyy.aria.core.common.controller;

/**
 * 恢复、停止、删除、等功能控制器
 */
public interface INormalFeature {

  /**
   * 停止任务
   */
  void stop();

  /**
   * 恢复任务
   */
  void resume();

  /**
   * 正常来说，当执行队列满时，调用恢复任务接口，只能将任务放到缓存队列中。
   * 如果希望调用恢复接口，马上进入执行队列，需要使用该方法
   *
   * @param newStart true 立即将任务恢复到执行队列中
   */
  void resume(boolean newStart);

  /**
   * 删除任务
   */
  void cancel();

  /**
   * 任务重试
   */
  void reTry();

  /**
   * 删除任务
   *
   * @param removeFile {@code true} 不仅删除任务数据库记录，还会删除已经删除完成的文件
   * {@code false}如果任务已经完成，只删除任务数据库记录，
   */
  void cancel(boolean removeFile);

  /**
   * 重新下载
   */
  long reStart();

  /**
   * 保存数据
   */
  void save();
}
