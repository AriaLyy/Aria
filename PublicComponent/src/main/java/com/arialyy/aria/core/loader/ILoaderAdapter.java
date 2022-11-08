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

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.common.SubThreadConfig;

/**
 * @author lyy
 * Date: 2019-09-18
 */
public interface ILoaderAdapter {

  /**
   * 处理新任务
   *
   * @param record 任务记录
   * @param totalThreadNum 任务的线程总数
   * @return {@code true}创建新任务成功
   */
  boolean handleNewTask(TaskRecord record, int totalThreadNum);

  /**
   * 创建线程任务
   */
  IThreadTask createThreadTask(SubThreadConfig config);

  /**
   * 处理任务记录
   */
  IRecordHandler recordHandler(AbsTaskWrapper wrapper);
}
