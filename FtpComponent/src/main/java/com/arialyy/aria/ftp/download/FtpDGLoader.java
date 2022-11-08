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

import android.os.Looper;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.group.AbsGroupLoader;
import com.arialyy.aria.core.group.AbsSubDLoadUtil;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.ALog;

/**
 * ftp 组合任务加载器
 */
final class FtpDGLoader extends AbsGroupLoader {
  FtpDGLoader(AbsTaskWrapper groupWrapper, IEventListener listener) {
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
    FtpSubDLoaderUtil subUtil = new FtpSubDLoaderUtil(getScheduler(), needGetFileInfo, getKey());
    subUtil.setParams(wrapper, null);
    return subUtil;
  }

  /**
   * 启动子任务下载
   */
  private void startSub() {
    if (isBreak()) {
      return;
    }
    onPostStart();

    // ftp需要获取完成只任务信息才更新只任务数量
    getState().setSubSize(getWrapper().getSubTaskWrapper().size());

    for (DTaskWrapper wrapper : getWrapper().getSubTaskWrapper()) {
      if (wrapper.getState() != IEntity.STATE_COMPLETE) {
        startSubLoader(createSubLoader(wrapper, false));
      }
    }
  }

  @Override public void addComponent(IInfoTask infoTask) {
    mInfoTask = infoTask;
    infoTask.setCallback(new IInfoTask.Callback() {
      @Override public void onSucceed(String key, CompleteInfo info) {
        if (info.code >= 200 && info.code < 300) {
          startSub();
        } else {
          ALog.e(TAG, "获取任务信息失败，code：" + info.code);
          getListener().onFail(false, null);
        }
      }

      @Override public void onFail(AbsEntity entity, AriaException e, boolean needRetry) {
        //getListener().onFail(needRetry, e);
        fail(e, needRetry);
      }
    });
  }
}
