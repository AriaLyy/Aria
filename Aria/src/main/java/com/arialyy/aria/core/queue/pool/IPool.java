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

package com.arialyy.aria.core.queue.pool;

import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.task.AbsTask;

/**
 * Created by lyy on 2016/8/14. 任务池
 */
interface IPool<T extends AbsTask> {
  /**
   * 将下载任务添加到任务池中
   */
  boolean putTask(T task);

  /**
   * 按照队列原则取出下载任务
   *
   * @return 返回null或者下载任务
   */
  T pollTask();

  /**
   * 通过key获取任务，当任务不为空时，队列将删除该下载任务
   *
   * @param key {@link AbsEntity#getKey()}
   * @return 返回null或者下载任务
   */
  T getTask(String key);

  /**
   * 任务是在存在
   *
   * @param key {@link AbsEntity#getKey()}
   * @return {@code true} 任务存在
   */
  boolean taskExits(String key);

  /**
   * 删除任务池中的下载任务
   *
   * @param task {@link AbsTask}
   * @return true:移除成功
   */
  boolean removeTask(T task);

  /**
   * 通过key除下载任务
   *
   * @param key 下载链接
   * @return true:移除成功
   */
  boolean removeTask(String key);

  /**
   * 池子大小
   *
   * @return 返回缓存池或者执行池大小
   */
  int size();
}