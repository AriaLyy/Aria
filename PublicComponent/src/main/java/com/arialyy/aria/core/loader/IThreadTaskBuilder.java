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

import android.os.Handler;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.task.IThreadTask;
import java.util.List;

/**
 * 线程任务构造器
 */
public interface IThreadTaskBuilder extends ILoaderComponent {

  /**
   * 构造线程任务
   */
  List<IThreadTask> buildThreadTask(TaskRecord record, Handler stateHandler);

  /**
   * 获取创建的线程任务数，需要先调用{@link #buildThreadTask(TaskRecord, Handler)}方法才能获取创建的线程任务数
   */
  int getCreatedThreadNum();
}
