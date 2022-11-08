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

package com.arialyy.aria.core.listener;

/**
 * 下载监听
 */
public interface IDLoadListener extends IEventListener {

  /**
   * 预处理完成,准备下载---开始下载之间
   */
  void onPostPre(long fileSize);

  /**
   * 支持断点回调
   *
   * @param support true,支持；false 不支持
   */
  void supportBreakpoint(boolean support);
}