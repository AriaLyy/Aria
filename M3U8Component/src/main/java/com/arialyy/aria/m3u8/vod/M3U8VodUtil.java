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
package com.arialyy.aria.m3u8.vod;

import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.loader.AbsNormalLoader;
import com.arialyy.aria.core.loader.AbsNormalLoaderUtil;
import com.arialyy.aria.core.loader.LoaderStructure;
import com.arialyy.aria.http.HttpTaskOption;
import com.arialyy.aria.m3u8.M3U8InfoTask;
import com.arialyy.aria.m3u8.M3U8Listener;
import com.arialyy.aria.m3u8.M3U8TaskOption;

/**
 * M3U8点播文件下载工具
 * 工作流程：
 * 1、创建一个和文件同父路径并且同名隐藏文件夹
 * 2、将所有m3u8的ts文件下载到该文件夹中
 * 3、完成所有分片下载后，合并ts文件
 * 4、删除该隐藏文件夹
 */
public final class M3U8VodUtil extends AbsNormalLoaderUtil {

  public M3U8VodUtil() {
  }

  @Override public DTaskWrapper getTaskWrapper() {
    return (DTaskWrapper) super.getTaskWrapper();
  }

  @Override public AbsNormalLoader getLoader() {
    if (mLoader == null) {
      getTaskWrapper().generateM3u8Option(M3U8TaskOption.class);
      getTaskWrapper().generateTaskOption(HttpTaskOption.class);
      mLoader = new M3U8VodLoader(getTaskWrapper(), (M3U8Listener) getListener());
    }
    return mLoader;
  }

  @Override public LoaderStructure BuildLoaderStructure() {
    LoaderStructure structure = new LoaderStructure();
    structure.addComponent(new VodRecordHandler(getTaskWrapper()))
        .addComponent(new M3U8InfoTask(getTaskWrapper()))
        .addComponent(new VodStateManager(getTaskWrapper(), (M3U8Listener) getListener()));
    structure.accept(getLoader());
    return structure;
  }
}
