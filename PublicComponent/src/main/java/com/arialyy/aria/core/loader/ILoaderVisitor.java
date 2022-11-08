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

import com.arialyy.aria.core.inf.IThreadStateManager;

/**
 * 加载器访问者
 */
public interface ILoaderVisitor {

  /**
   * 处理任务记录
   */
  void addComponent(IRecordHandler recordHandler);

  /**
   * 处理任务的文件信息
   */
  void addComponent(IInfoTask infoTask);

  /**
   * 线程状态
   */
  void addComponent(IThreadStateManager threadState);

  /**
   * 构造线程任务
   */
  void addComponent(IThreadTaskBuilder builder);
}
