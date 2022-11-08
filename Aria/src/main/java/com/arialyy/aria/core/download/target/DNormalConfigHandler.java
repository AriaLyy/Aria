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
package com.arialyy.aria.core.download.target;

import android.text.TextUtils;
import com.arialyy.aria.core.common.AbsNormalTarget;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.event.ErrorEvent;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.inf.IConfigHandler;
import com.arialyy.aria.core.manager.TaskWrapperManager;
import com.arialyy.aria.core.queue.DTaskQueue;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;

/**
 * Created by AriaL on 2019/4/5.
 * 普通下载任务通用功能处理
 */
class DNormalConfigHandler<TARGET extends AbsTarget> implements IConfigHandler {
  private final String TAG = "DNormalDelegate";
  private DownloadEntity mEntity;

  private TARGET mTarget;
  private DTaskWrapper mWrapper;

  /**
   * @param taskId 第一次下载，taskId为-1
   */
  DNormalConfigHandler(TARGET target, long taskId) {
    this.mTarget = target;
    initTarget(taskId);
  }

  private void initTarget(long taskId) {
    mWrapper = TaskWrapperManager.getInstance().getNormalTaskWrapper(DTaskWrapper.class, taskId);
    // 判断已存在的任务
    if (mTarget instanceof AbsNormalTarget) {
      if (taskId < 0) {
        mWrapper.setErrorEvent(new ErrorEvent(taskId, "任务id为空"));
      } else if (mWrapper.getEntity().getId() < 0) {
        mWrapper.setErrorEvent(new ErrorEvent(taskId, "任务信息不存在"));
      }
    }

    mEntity = mWrapper.getEntity();
    mTarget.setTaskWrapper(mWrapper);
    if (mEntity != null) {
      getWrapper().setTempFilePath(mEntity.getFilePath());
    }
  }

  TARGET updateUrl(String newUrl) {
    if (TextUtils.isEmpty(newUrl)) {
      ALog.e(TAG, "url更新失败，newUrl为null");
      return mTarget;
    }
    if (mEntity.getUrl().equals(newUrl)) {
      ALog.e(TAG, "url更新失败，新的下载url和旧的url一致");
      return mTarget;
    }
    getWrapper().setRefreshInfo(true);
    getWrapper().setTempUrl(newUrl);
    ALog.d(TAG, "更新url成功");
    return mTarget;
  }

  @Override public DownloadEntity getEntity() {
    return (DownloadEntity) mTarget.getEntity();
  }

  @Override public boolean taskExists() {
    return DbEntity.checkDataExist(DownloadEntity.class, "rowid=?",
        String.valueOf(mEntity.getId()));
  }

  @Override public boolean isRunning() {
    return DTaskQueue.getInstance().taskIsRunning(mEntity.getKey());
  }

  void setForceDownload(boolean forceDownload) {
    getWrapper().setIgnoreFilePathOccupy(forceDownload);
  }

  void setUrl(String url) {
    mEntity.setUrl(url);
    mWrapper.setTempUrl(url);
  }

  String getUrl() {
    return mEntity.getUrl();
  }

  void setTempFilePath(String tempFilePath) {
    getWrapper().setTempFilePath(tempFilePath);
  }

  private DTaskWrapper getWrapper() {
    return mWrapper;
  }
}
