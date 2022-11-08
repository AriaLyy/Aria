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
 * M3U8 密钥下载地址处理器，可用于解密被加密的密钥的下载地址
 */
public interface IKeyUrlConverter extends IEventHandler {

  /**
   * 将被加密的密钥下载地址转换为可使用的http下载地址
   *
   * @param m3u8Url 主m3u8的url地址
   * @param tsListUrl m3u8切片列表url地址
   * @param keyUrl 加密的url地址
   * @return 可正常访问的http地址
   */
  String convert(String m3u8Url, String tsListUrl, String keyUrl);
}
