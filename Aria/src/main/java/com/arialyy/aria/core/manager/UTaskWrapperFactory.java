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
package com.arialyy.aria.core.manager;

import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.util.ALog;

/**
 * Created by Aria.Lao on 2017/11/1. 任务实体工厂
 */
class UTaskWrapperFactory implements INormalTEFactory<UploadEntity, UTaskWrapper> {
  private static final String TAG = "UTaskWrapperFactory";
  private static volatile UTaskWrapperFactory INSTANCE = null;

  private UTaskWrapperFactory() {
  }

  public static UTaskWrapperFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (UTaskWrapperFactory.class) {
        INSTANCE = new UTaskWrapperFactory();
      }
    }
    return INSTANCE;
  }

  @Override public UTaskWrapper create(long taskId) {
    UTaskWrapper wrapper;
    if (taskId == -1) {
      wrapper = new UTaskWrapper(new UploadEntity());
    } else {
      wrapper = new UTaskWrapper(getUploadEntity(taskId));
    }

    wrapper.setRequestType(wrapper.getEntity().getTaskType());
    return wrapper;
  }

  /**
   * 从数据中读取上传实体，如果数据库查不到，则新创建一个上传实体
   */
  private UploadEntity getUploadEntity(long taskId) {
    UploadEntity entity =
        UploadEntity.findFirst(UploadEntity.class, "rowid=?", String.valueOf(taskId));
    if (entity == null) {
      entity = new UploadEntity();
    }
    return entity;
  }
}
