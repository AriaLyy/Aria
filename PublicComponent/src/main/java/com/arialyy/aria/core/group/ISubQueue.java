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
package com.arialyy.aria.core.group;

import com.arialyy.aria.core.loader.AbsNormalLoader;
import com.arialyy.aria.core.inf.IUtil;
import com.arialyy.aria.core.config.DGroupConfig;

/**
 * 组合任务子任务队列
 *
 * @param <Fileer> {@link AbsNormalLoader}下载器
 */
interface ISubQueue<Fileer extends IUtil> {

  /**
   * 添加任务
   */

  void addTask(Fileer fileer);

  /**
   * 开始任务
   * 如果执行队列没有达到上限，则启动任务。
   * 如果执行队列已经到达上限，则将任务添加到等待队列总。
   * 队列上限配置{@link DGroupConfig#setSubMaxTaskNum(int)}
   */
  void startTask(Fileer fileer);

  /**
   * 停止单个任务，如果缓存队列中有等待中的任务，则启动等待中的任务
   */
  void stopTask(Fileer fileer);

  /**
   * 停止全部任务，停止所有正在执行的任务，并清空所有等待中的端服务
   */
  void stopAllTask();

  /**
   * 修改最大任务数
   *
   * @param num 任务数不能小于1
   */
  void modifyMaxExecNum(int num);

  /**
   * 从执行队列中移除任务，一般用于任务完成的情况
   */
  void removeTaskFromExecQ(Fileer fileer);

  /**
   * 删除任务，如果缓存队列中有等待中的任务，则启动等待中的任务
   */
  void removeTask(Fileer fileer);

  /**
   * 停止全部任务，停止所有正在执行的任务，并清空所有等待中的端服务
   */
  void removeAllTask();


  /**
   * 获取下一个任务
   */
  Fileer getNextTask();

  /**
   * 清空缓存队列和执行队列
   */
  void clear();
}
