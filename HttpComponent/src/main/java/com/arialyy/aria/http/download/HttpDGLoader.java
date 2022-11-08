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

import android.os.Looper;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.group.AbsGroupLoader;
import com.arialyy.aria.core.group.AbsSubDLoadUtil;
import com.arialyy.aria.core.listener.DownloadGroupListener;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.exception.AriaHTTPException;
import java.io.File;

/**
 * http 组合任务加载器
 */
final class HttpDGLoader extends AbsGroupLoader {
  HttpDGLoader(AbsTaskWrapper groupWrapper, DownloadGroupListener listener) {
    super(groupWrapper, listener);
  }

  @Override protected void handlerTask(Looper looper) {
    if (isBreak()) {
      return;
    }
    mInfoTask.run();
  }

  @Override
  protected AbsSubDLoadUtil createSubLoader(DTaskWrapper wrapper, boolean needGetFileInfo) {
    HttpSubDLoaderUtil subUtil = new HttpSubDLoaderUtil(getScheduler(), needGetFileInfo, getKey());
    subUtil.setParams(wrapper, null);
    return subUtil;
  }

  private void startSub() {
    if (isBreak()) {
      return;
    }
    onPostStart();
    for (DTaskWrapper wrapper : getWrapper().getSubTaskWrapper()) {
      DownloadEntity dEntity = wrapper.getEntity();

      startSubLoader(createSubLoader(wrapper, dEntity.getFileSize() < 0));
    }
  }

  @Override public void addComponent(IInfoTask infoTask) {
    mInfoTask = infoTask;
    mInfoTask.setCallback(new HttpDGInfoTask.DGInfoCallback() {

      @Override
      public void onSubFail(DownloadEntity subEntity, AriaHTTPException e, boolean needRetry) {
        getState().countFailNum(subEntity.getKey());
      }

      @Override public void onStop(long len) {
        getListener().onStop(len);
      }

      @Override public void onSucceed(String key, CompleteInfo info) {
        startSub();
      }

      @Override public void onFail(AbsEntity entity, AriaException e, boolean needRetry) {
        fail(e, needRetry);
      }
    });
  }
}
