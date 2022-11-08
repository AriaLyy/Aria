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

import java.util.concurrent.Callable;

/**
 * @author lyy
 * Date: 2019-09-18
 */
public interface IThreadTask extends Callable<IThreadTask> {

  /**
   * 销毁任务
   */
  void destroy();

  /**
   * 判断线程任务是否销毁
   *
   * @return true 已经销毁
   */
  boolean isDestroy();

  /**
   * 中断任务
   */
  void breakTask();

  /**
   * 当前线程是否完成，对于不支持断点的任务，一律未完成
   *
   * @return {@code true} 完成；{@code false} 未完成
   */
  boolean isThreadComplete();

  /**
   * 取消任务
   */
  void cancel();

  /**
   * 停止任务
   */
  void stop();

  /**
   * 设置当前线程最大下载速度
   *
   * @param speed 单位为：kb
   */
  void setMaxSpeed(int speed);

  /**
   * 线程是否存活
   *
   * @return {@code true}存活
   */
  boolean isLive();

  /**
   * 任务是否中断，中断条件：
   * 1、任务取消
   * 2、任务停止
   * 3、手动中断
   *
   * @return {@code true} 中断，{@code false} 不是中断
   */
  boolean isBreak();

  /**
   * 检查下载完成的分块大小，如果下载完成的分块大小大于或小于分配的大小，则需要重新下载该分块 如果是非分块任务，直接返回{@code true}
   *
   * @return {@code true} 分块分大小正常，{@code false} 分块大小错误
   */
  boolean checkBlock();

  /**
   * 获取线程id
   */
  int getThreadId();

  /**
   * 线程名字，命名规则：md5(任务地址 + 线程id)
   */
  String getThreadName();
}
