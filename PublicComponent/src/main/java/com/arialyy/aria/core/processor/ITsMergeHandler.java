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
package com.arialyy.aria.core.processor;

import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.inf.IEventHandler;
import java.util.List;

/**
 * Ts文件合并处理，如果你希望使用自行处理ts文件的合并，你可以实现该接口
 */
public interface ITsMergeHandler extends IEventHandler {

  /**
   * 合并ts文件
   *
   * @param m3U8Entity m3u8信息，可通过{@link M3U8Entity#keyPath}、{@link M3U8Entity#keyUrl}、
   * {@link M3U8Entity#iv}、{@link M3U8Entity#method} 获取相应的加密信息
   * @param tsPath ts文件列表
   * @return {@code true} 合并成功
   */
  boolean merge(M3U8Entity m3U8Entity, List<String> tsPath);
}
