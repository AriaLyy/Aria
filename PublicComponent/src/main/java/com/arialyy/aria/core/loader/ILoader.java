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
package com.arialyy.aria.core.loader;

public interface ILoader extends Runnable {

  //void start();

  /**
   * 任务是否在执行
   *
   * @return true 任务执行中
   */
  boolean isRunning();

  void cancel();

  void stop();

  /**
   * 任务是否被中断（停止，取消）
   *
   * @return true 任务中断，false 任务没有中断
   */
  boolean isBreak();

  String getKey();

  long getCurrentProgress();
}
