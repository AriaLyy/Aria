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
package com.arialyy.aria.m3u8.live;

import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.loader.AbsNormalLoaderUtil;
import com.arialyy.aria.core.loader.LoaderStructure;
import com.arialyy.aria.http.HttpTaskOption;
import com.arialyy.aria.m3u8.M3U8InfoTask;
import com.arialyy.aria.m3u8.M3U8Listener;
import com.arialyy.aria.m3u8.M3U8TaskOption;

/**
 * M3U8直播文件下载工具，对于直播来说，需要定时更新m3u8文件
 * 工作流程：
 * 1、持续获取切片信息，直到调用停止|取消才停止获取切片信息
 * 2、完成所有分片下载后，合并ts文件
 * 3、删除该隐藏文件夹
 * 4、对于直播来说是没有停止的，停止就代表完成
 * 5、不处理直播切片下载失败的状态
 */
public class M3U8LiveUtil extends AbsNormalLoaderUtil {

  public M3U8LiveUtil() {
  }

  @Override public DTaskWrapper getTaskWrapper() {
    return (DTaskWrapper) super.getTaskWrapper();
  }

  @Override public M3U8LiveLoader getLoader() {
    if (mLoader == null) {
      getTaskWrapper().generateM3u8Option(M3U8TaskOption.class);
      getTaskWrapper().generateTaskOption(HttpTaskOption.class);
      mLoader = new M3U8LiveLoader(getTaskWrapper(), (M3U8Listener) getListener());
    }
    return (M3U8LiveLoader) mLoader;
  }

  @Override public LoaderStructure BuildLoaderStructure() {
    LoaderStructure structure = new LoaderStructure();
    structure.addComponent(new LiveRecordHandler(getTaskWrapper()))
        .addComponent(new M3U8InfoTask(getTaskWrapper()))
        .addComponent(new LiveStateManager(getTaskWrapper(), getListener()));
    structure.accept(getLoader());
    return structure;
  }
}
