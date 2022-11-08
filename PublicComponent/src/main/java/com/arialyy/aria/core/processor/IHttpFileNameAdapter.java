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

import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.IEventHandler;

import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Http文件名适配器
 */
public interface IHttpFileNameAdapter extends IEventHandler {

  /**
   * 根据header中的数据获取文件名字
   * @param headers header参数{@link URLConnection#getHeaderFields()}
   * @param key 这里如果传entity可能会导致entity属性变更而其他地方没有改变，所以就传了个key 减少可操作范围
   * @return 文件长度
   */
  String handleFileName(Map<String, List<String>> headers, String key);
}
