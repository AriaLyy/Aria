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
package com.arialyy.aria.http.download;

import android.os.Handler;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.group.AbsSubDLoadUtil;
import com.arialyy.aria.core.group.SubRecordHandler;
import com.arialyy.aria.core.loader.GroupSubThreadStateManager;
import com.arialyy.aria.core.loader.LoaderStructure;
import com.arialyy.aria.core.loader.NormalTTBuilder;
import com.arialyy.aria.core.loader.SubLoader;

/**
 * @author lyy
 * Date: 2019-09-28
 */
final class HttpSubDLoaderUtil extends AbsSubDLoadUtil {
  /**
   * @param schedulers 调度器
   * @param needGetInfo {@code true} 需要获取文件信息。{@code false} 不需要获取文件信息
   */
  HttpSubDLoaderUtil( Handler schedulers, boolean needGetInfo, String parentKey) {
    super(schedulers, needGetInfo, parentKey);
  }

  @Override protected SubLoader getLoader() {
    if (mDLoader == null) {
      mDLoader = new SubLoader(getWrapper(), getSchedulers());
      mDLoader.setNeedGetInfo(isNeedGetInfo());
      mDLoader.setParentKey(getParentKey());
    }
    return mDLoader;
  }

  @Override protected LoaderStructure buildLoaderStructure() {
    LoaderStructure structure = new LoaderStructure();
    structure.addComponent(new SubRecordHandler(getWrapper()))
            .addComponent(new GroupSubThreadStateManager(getSchedulers(),getKey()))
            .addComponent(new NormalTTBuilder(getWrapper(), new HttpDTTBuilderAdapter()))
            .addComponent(new HttpDFileInfoTask(getWrapper()));
    structure.accept(getLoader());
    return structure;
  }
}

