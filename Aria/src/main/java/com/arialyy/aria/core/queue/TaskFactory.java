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

package com.arialyy.aria.core.queue;

import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.task.UploadTask;

/**
 * Created by lyy on 2016/8/18.
 * 任务工厂
 */
class TaskFactory {

  private static volatile TaskFactory INSTANCE = null;

  private TaskFactory() {

  }

  public static TaskFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (TaskFactory.class) {
        INSTANCE = new TaskFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * 创建任务
   *
   * @param entity 下载实体
   * @param schedulers 对应的任务调度器
   * @param <TASK_ENTITY> {@link DTaskWrapper}、{@link UTaskWrapper}、{@link
   * DGTaskWrapper}
   * @return {@link DownloadTask}、{@link UploadTask}、{@link DownloadGroupTask}
   */
  <TASK_ENTITY extends AbsTaskWrapper, SCHEDULER extends ISchedulers> ITask createTask(
      TASK_ENTITY entity, SCHEDULER schedulers) {
    if (entity instanceof DTaskWrapper) {
      return createDownloadTask((DTaskWrapper) entity, schedulers);
    } else if (entity instanceof UTaskWrapper) {
      return createUploadTask((UTaskWrapper) entity, schedulers);
    } else if (entity instanceof DGTaskWrapper) {
      return createDownloadGroupTask((DGTaskWrapper) entity, schedulers);
    }
    return null;
  }

  /**
   * 创建下载任务主任务
   *
   * @param entity 下载任务实体{@link DownloadGroupTask}
   * @param schedulers {@link ISchedulers}
   */
  private DownloadGroupTask createDownloadGroupTask(DGTaskWrapper entity,
      ISchedulers schedulers) {
    DownloadGroupTask.Builder builder = new DownloadGroupTask.Builder(entity);
    builder.setOutHandler(schedulers);
    return builder.build();
  }

  /**
   * @param entity 上传任务实体{@link UTaskWrapper}
   * @param schedulers {@link ISchedulers}
   */
  private UploadTask createUploadTask(UTaskWrapper entity, ISchedulers schedulers) {
    UploadTask.Builder builder = new UploadTask.Builder();
    builder.setUploadTaskEntity(entity);
    builder.setOutHandler(schedulers);
    return builder.build();
  }

  /**
   * @param entity 下载任务实体{@link DTaskWrapper}
   * @param schedulers {@link ISchedulers}
   */
  private DownloadTask createDownloadTask(DTaskWrapper entity, ISchedulers schedulers) {
    DownloadTask.Builder builder = new DownloadTask.Builder(entity);
    builder.setOutHandler(schedulers);
    return builder.build();
  }
}