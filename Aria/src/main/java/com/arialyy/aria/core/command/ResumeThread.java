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
package com.arialyy.aria.core.command;

import android.text.TextUtils;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.IOptionConstant;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.manager.TaskWrapperManager;
import com.arialyy.aria.core.queue.AbsTaskQueue;
import com.arialyy.aria.core.queue.DGroupTaskQueue;
import com.arialyy.aria.core.queue.DTaskQueue;
import com.arialyy.aria.core.queue.UTaskQueue;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * 恢复任务工具
 */
public class ResumeThread implements Runnable {
  private String TAG = CommonUtil.getClassName(getClass());
  private List<AbsTaskWrapper> mWaitList = new ArrayList<>();
  private boolean isDownloadCmd;
  private String sqlCondition;

  ResumeThread(boolean isDownload, String sqlCondition) {
    this.isDownloadCmd = isDownload;
    this.sqlCondition = sqlCondition;
  }

  /**
   * 查找数据库中的所有任务数据
   *
   * @param type {@code 1}单任务下载任务；{@code 2}任务组下载任务；{@code 3} 单任务上传任务
   */
  private void findTaskData(int type) {
    if (type == 1) {
      List<DownloadEntity> entities =
          DbEntity.findDatas(DownloadEntity.class,
              String.format("NOT(isGroupChild) AND NOT(isComplete) AND %s ORDER BY stopTime DESC",
                  sqlCondition));
      if (entities != null && !entities.isEmpty()) {
        for (DownloadEntity entity : entities) {
          addResumeEntity(TaskWrapperManager.getInstance()
              .getNormalTaskWrapper(DTaskWrapper.class, entity.getId()));
        }
      }
    } else if (type == 2) {
      List<DownloadGroupEntity> entities =
          DbEntity.findDatas(DownloadGroupEntity.class,
              String.format("NOT(isComplete) AND %s ORDER BY stopTime DESC",
                  sqlCondition));
      if (entities != null && !entities.isEmpty()) {
        for (DownloadGroupEntity entity : entities) {
          addResumeEntity(
              TaskWrapperManager.getInstance()
                  .getGroupWrapper(DGTaskWrapper.class, entity.getId()));
        }
      }
    } else if (type == 3) {
      List<UploadEntity> entities =
          DbEntity.findDatas(UploadEntity.class,
              String.format("NOT(isComplete) AND %s ORDER BY stopTime DESC",
                  sqlCondition));
      if (entities != null && !entities.isEmpty()) {
        for (UploadEntity entity : entities) {
          addResumeEntity(TaskWrapperManager.getInstance()
              .getNormalTaskWrapper(UTaskWrapper.class, entity.getId()));
        }
      }
    }
  }

  /**
   * 添加恢复实体
   */
  private void addResumeEntity(AbsTaskWrapper te) {
    if (te == null || te.getEntity() == null || TextUtils.isEmpty(te.getKey())) {
      return;
    }
    mWaitList.add(te);
  }

  /**
   * 处理等待状态的任务
   */
  private void resumeWaitTask() {

    if (mWaitList == null || mWaitList.isEmpty()) {
      return;
    }
    List<AbsEntity> resumeEntities = new ArrayList<>();

    for (AbsTaskWrapper wrapper : mWaitList) {
      AbsTaskQueue queue = null;
      if (wrapper instanceof DTaskWrapper) {
        queue = DTaskQueue.getInstance();
      } else if (wrapper instanceof UTaskWrapper) {
        queue = UTaskQueue.getInstance();
      } else if (wrapper instanceof DGTaskWrapper) {
        queue = DGroupTaskQueue.getInstance();
      }

      if (queue == null) {
        ALog.e(TAG, "任务类型错误");
        continue;
      }

      if (wrapper.getEntity() == null || TextUtils.isEmpty(wrapper.getKey())) {
        ALog.e(TAG, "任务实体为空或key为空");
        continue;
      }

      AbsTask task = queue.getTask(wrapper.getKey());
      if (task != null) {
        ALog.w(TAG, "任务已存在");
        continue;
      }

      int maxTaskNum = queue.getMaxTaskNum();
      task = queue.createTask(wrapper);
      if (task == null) {
        continue;
      }

      handleWrapper(wrapper);

      if (queue.getCurrentExePoolNum() < maxTaskNum) {
        queue.startTask(task);
      } else {
        wrapper.getEntity().setState(IEntity.STATE_WAIT);
        sendWaitState(task);
        resumeEntities.add(wrapper.getEntity());
      }
    }
    if (!resumeEntities.isEmpty()) {
      DbEntity.updateManyData(resumeEntities);
    }
  }

  /**
   * 处理ftp的wrapper
   */
  private void handleWrapper(AbsTaskWrapper wrapper) {
    int requestType = wrapper.getRequestType();
    if (requestType == ITaskWrapper.D_FTP
        || requestType == ITaskWrapper.U_FTP
        || requestType == ITaskWrapper.D_FTP_DIR) {
      wrapper.getOptionParams()
          .setParams(IOptionConstant.ftpUrlEntity,
              CommonUtil.getFtpUrlInfo(wrapper.getEntity().getKey()));
    }
  }

  /**
   * 发送等待状态
   */
  private void sendWaitState(AbsTask task) {
    if (task != null) {
      task.getTaskWrapper().setState(IEntity.STATE_WAIT);
      task.getOutHandler().obtainMessage(ISchedulers.WAIT, task).sendToTarget();
    }
  }

  @Override public void run() {
    if (isDownloadCmd) {
      findTaskData(1);
      findTaskData(2);
    } else {
      findTaskData(3);
    }
    resumeWaitTask();
  }
}
