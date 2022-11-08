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

package com.arialyy.aria.core.task;

import android.os.Handler;
import android.os.Looper;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.util.ComponentUtil;

/**
 * Created by lyy on 2016/8/11.
 * 下载任务类
 */
public class DownloadTask extends AbsTask<DTaskWrapper> {

  private DownloadTask(DTaskWrapper taskWrapper, Handler outHandler) {
    mTaskWrapper = taskWrapper;
    mOutHandler = outHandler;
    mContext = AriaConfig.getInstance().getAPP();
    mListener =
        ComponentUtil.getInstance().buildListener(taskWrapper.getRequestType(), this, mOutHandler);
  }

  /**
   * 获取文件保存路径
   */
  public String getFilePath() {
    return mTaskWrapper.getEntity().getFilePath();
  }

  public DownloadEntity getEntity() {
    return mTaskWrapper.getEntity();
  }

  /**
   * 获取当前下载任务的下载地址
   *
   * @see DownloadTask#getKey()
   */
  @Deprecated public String getDownloadUrl() {
    return mTaskWrapper.getEntity().getUrl();
  }

  @Override public int getTaskType() {
    return ITask.DOWNLOAD;
  }

  @Override public String getKey() {
    return mTaskWrapper.getEntity().getKey();
  }

  public DownloadEntity getDownloadEntity() {
    return mTaskWrapper.getEntity();
  }

  @Override public String getTaskName() {
    return mTaskWrapper.getEntity().getFileName();
  }

  public static class Builder {
    DTaskWrapper taskEntity;
    Handler outHandler;

    public Builder(DTaskWrapper taskEntity) {
      this.taskEntity = taskEntity;
    }

    /**
     * 设置自定义Handler处理下载状态时间
     *
     * @param schedulers {@link ISchedulers}
     */
    public Builder setOutHandler(ISchedulers schedulers) {
      outHandler = new Handler(Looper.getMainLooper(), schedulers);
      return this;
    }

    public DownloadTask build() {
      return new DownloadTask(taskEntity, outHandler);
    }
  }
}