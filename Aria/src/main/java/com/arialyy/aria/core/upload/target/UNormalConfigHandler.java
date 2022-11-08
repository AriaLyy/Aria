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
package com.arialyy.aria.core.upload.target;

import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.AbsNormalTarget;
import com.arialyy.aria.core.common.ErrorCode;
import com.arialyy.aria.core.event.ErrorEvent;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.inf.IConfigHandler;
import com.arialyy.aria.core.manager.TaskWrapperManager;
import com.arialyy.aria.core.queue.UTaskQueue;
import com.arialyy.aria.core.task.UploadTask;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;

/**
 * Created by Aria.Lao on 2019/4/5.
 * 普通上传任务通用功能处理
 */
class UNormalConfigHandler<TARGET extends AbsTarget> implements IConfigHandler {
  private UploadEntity mEntity;
  private TARGET mTarget;
  private UTaskWrapper mWrapper;

  UNormalConfigHandler(TARGET target, long taskId) {
    mTarget = target;
    initTarget(taskId);
  }

  private void initTarget(long taskId) {
    mWrapper = TaskWrapperManager.getInstance().getNormalTaskWrapper(UTaskWrapper.class, taskId);
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
    getTaskWrapper().setTempUrl(mEntity.getUrl());
  }

  void setFilePath(String filePath) {
    File file = new File(filePath);
    mEntity.setFilePath(filePath);
    mEntity.setFileName(file.getName());
    mEntity.setFileSize(file.length());
  }

  @Override public AbsEntity getEntity() {
    return mEntity;
  }

  @Override public boolean taskExists() {
    return DbEntity.checkDataExist(UploadEntity.class, "key=?", mEntity.getFilePath());
  }

  @Override public boolean isRunning() {
    UploadTask task = UTaskQueue.getInstance().getTask(mEntity.getKey());
    return task != null && task.isRunning();
  }

  void setTempUrl(String tempUrl) {
    getTaskWrapper().setTempUrl(tempUrl);
  }

  private UTaskWrapper getTaskWrapper() {
    return mWrapper;
  }
}
