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
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.event.ErrorEvent;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.inf.IConfigHandler;
import com.arialyy.aria.core.manager.SubTaskManager;
import com.arialyy.aria.core.manager.TaskWrapperManager;
import com.arialyy.aria.core.queue.DGroupTaskQueue;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2019/4/9.
 * 下载组合任务功能
 */
abstract class AbsGroupConfigHandler<TARGET extends AbsTarget> implements IConfigHandler {
  protected String TAG;
  private TARGET mTarget;
  private DGTaskWrapper mWrapper;

  private SubTaskManager mSubTaskManager;

  AbsGroupConfigHandler(TARGET target, long taskId) {
    TAG = CommonUtil.getClassName(getClass());
    mTarget = target;
    mWrapper = TaskWrapperManager.getInstance().getGroupWrapper(DGTaskWrapper.class, taskId);
    // 判断已存在的任务
    if (mTarget instanceof AbsNormalTarget) {
      if (taskId < 0) {
        mWrapper.setErrorEvent(new ErrorEvent(taskId, "任务id为空"));
      } else if (mWrapper.getEntity().getId() < 0) {
        mWrapper.setErrorEvent(new ErrorEvent(taskId, "任务信息不存在"));
      }
    }

    mTarget.setTaskWrapper(mWrapper);
    if (getEntity() != null) {
      getTaskWrapper().setDirPathTemp(getEntity().getDirPath());
    }
  }

  /**
   * 获取子任务管理器
   *
   * @return 子任务管理器
   */
  SubTaskManager getSubTaskManager() {
    if (mSubTaskManager == null) {
      mSubTaskManager = new SubTaskManager(getTaskWrapper());
    }
    return mSubTaskManager;
  }

  /**
   * 设置任务组别名
   */
  void setGroupAlias(String alias) {
    if (TextUtils.isEmpty(alias)) {
      return;
    }
    getEntity().setAlias(alias);
  }

  @Override public boolean isRunning() {
    DownloadGroupTask task = DGroupTaskQueue.getInstance().getTask(getEntity().getKey());
    return task != null && task.isRunning();
  }

  TARGET setDirPath(String dirPath) {
    mWrapper.setDirPathTemp(dirPath);
    return mTarget;
  }

  @Override public DownloadGroupEntity getEntity() {
    return mWrapper.getEntity();
  }

  @Override public boolean taskExists() {
    return getEntity().getId() != -1 && DbEntity.checkDataExist(DownloadGroupEntity.class,
        "rowid=?", String.valueOf(getEntity().getId()));
  }

  DGTaskWrapper getTaskWrapper() {
    return mWrapper;
  }

  TARGET getTarget() {
    return mTarget;
  }
}
