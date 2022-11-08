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
package com.arialyy.aria.ftp.download;

import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.group.AbsGroupLoader;
import com.arialyy.aria.core.group.AbsGroupLoaderUtil;
import com.arialyy.aria.core.loader.LoaderStructure;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.ftp.FtpTaskOption;

/**
 * Created by Aria.Lao on 2017/7/27.
 * ftp文件夹下载工具
 */
public final class FtpDGLoaderUtil extends AbsGroupLoaderUtil {

  @Override protected AbsGroupLoader getLoader() {
    if (mLoader == null) {
      ((AbsTaskWrapper) getTaskWrapper()).generateTaskOption(FtpTaskOption.class);
      mLoader = new FtpDGLoader((AbsTaskWrapper) getTaskWrapper(), getListener());
    }
    return mLoader;
  }

  @Override protected LoaderStructure buildLoaderStructure() {
    LoaderStructure structure = new LoaderStructure();
    structure.addComponent(new FtpDGInfoTask((DGTaskWrapper) getTaskWrapper()));
    structure.accept(getLoader());
    return structure;
  }
}
