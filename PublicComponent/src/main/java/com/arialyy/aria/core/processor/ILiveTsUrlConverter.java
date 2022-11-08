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

import com.arialyy.aria.core.inf.IEventHandler;

/**
 * M3U8 直播下载，ts url转换器，对于某些服务器，返回的ts地址可以是相对地址，也可能是处理过的
 * 对于这种情况，你需要使用url转换器将地址转换为可正常访问的http地址
 */
public interface ILiveTsUrlConverter extends IEventHandler {

  /**
   * 处理#EXTINF信息，对于某些服务器，返回的切片信息有可能是相对地址，因此，你需要自行转换为可下载http连接
   *
   * @param m3u8Url m3u8文件下载地址
   * @param tsUrl ts文件下载地址
   * @return 转换后的http地址
   */
  String convert(String m3u8Url, String tsUrl);
}
