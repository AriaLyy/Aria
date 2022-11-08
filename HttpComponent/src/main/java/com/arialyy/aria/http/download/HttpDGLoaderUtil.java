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

import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.group.AbsGroupLoader;
import com.arialyy.aria.core.group.AbsGroupLoaderUtil;
import com.arialyy.aria.core.listener.DownloadGroupListener;
import com.arialyy.aria.core.loader.LoaderStructure;
import com.arialyy.aria.http.HttpTaskOption;

/**
 * Created by AriaL on 2017/6/30.
 * 任务组下载工具
 */
public final class HttpDGLoaderUtil extends AbsGroupLoaderUtil {

  @Override protected AbsGroupLoader getLoader() {
    if (mLoader == null) {
      getTaskWrapper().generateTaskOption(HttpTaskOption.class);
      mLoader = new HttpDGLoader(getTaskWrapper(), (DownloadGroupListener) getListener());
    }
    return mLoader;
  }

  @Override protected LoaderStructure buildLoaderStructure() {
    LoaderStructure structure = new LoaderStructure();
    structure.addComponent(new HttpDGInfoTask((DGTaskWrapper) getTaskWrapper()));
    structure.accept(getLoader());
    return structure;
  }
}