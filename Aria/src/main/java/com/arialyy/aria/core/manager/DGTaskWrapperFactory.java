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

import com.arialyy.aria.core.download.DGEntityWrapper;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.DbDataHelper;
import java.util.List;

/**
 * Created by Aria.Lao on 2017/11/1. 组合任务wrapper
 */
class DGTaskWrapperFactory implements IGroupWrapperFactory<DownloadGroupEntity, DGTaskWrapper> {
  private final String TAG = CommonUtil.getClassName(this);
  private static volatile DGTaskWrapperFactory INSTANCE = null;

  private DGTaskWrapperFactory() {
  }

  public static DGTaskWrapperFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (DGTaskWrapperFactory.class) {
        INSTANCE = new DGTaskWrapperFactory();
      }
    }
    return INSTANCE;
  }

  @Override public DGTaskWrapper getGroupWrapper(long taskId) {
    DGTaskWrapper wrapper;
    if (taskId == -1) {
      wrapper = new DGTaskWrapper(new DownloadGroupEntity());
    } else {
      DownloadGroupEntity entity = getOrCreateHttpDGEntity(taskId);
      wrapper = new DGTaskWrapper(entity);
      if (entity.getSubEntities() != null && !entity.getSubEntities().isEmpty()) {
        wrapper.setSubTaskWrapper(DbDataHelper.createDGSubTaskWrapper(entity));
      }
    }
    wrapper.setRequestType(wrapper.getEntity().getTaskType());
    return wrapper;
  }

  /**
   * 获取组合任务实体 如果数据库不存在该实体，则新创建一个新的任务组实体
   */
  private DownloadGroupEntity getOrCreateHttpDGEntity(long taskId) {
    List<DGEntityWrapper> wrapper =
        DbEntity.findRelationData(DGEntityWrapper.class, "DownloadGroupEntity.rowid=?",
            String.valueOf(taskId));

    DownloadGroupEntity groupEntity;
    if (wrapper != null && !wrapper.isEmpty()) {
      groupEntity = wrapper.get(0).groupEntity;
      if (groupEntity == null) {
        groupEntity = new DownloadGroupEntity();
      }
    } else {
      groupEntity = new DownloadGroupEntity();
    }
    return groupEntity;
  }
}
