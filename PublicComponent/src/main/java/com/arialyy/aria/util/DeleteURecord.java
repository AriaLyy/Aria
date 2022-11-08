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
package com.arialyy.aria.util;

import android.text.TextUtils;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.orm.DbEntity;

/**
 * 删除上传记录
 */
public class DeleteURecord implements IDeleteRecord {
  private String TAG = CommonUtil.getClassName(this);

  private static volatile DeleteURecord INSTANCE = null;

  private DeleteURecord() {

  }

  public static DeleteURecord getInstance() {
    if (INSTANCE == null) {
      synchronized (DeleteURecord.class) {
        if (INSTANCE == null) {
          INSTANCE = new DeleteURecord();
        }
      }
    }
    return INSTANCE;
  }

  /**
   * 删除上传记录
   *
   * @param filePath 上传文件的地址
   * @param needRemoveFile 上传完成后是否需要删除本地文件。true，上传完成后删除本地文件
   * @param needRemoveEntity 是否需要删除实体，true 删除实体
   */
  @Override public void deleteRecord(String filePath, boolean needRemoveFile,
      boolean needRemoveEntity) {
    if (TextUtils.isEmpty(filePath)) {
      throw new NullPointerException("删除记录失败，文件路径为空");
    }
    if (!filePath.startsWith("/")) {
      throw new IllegalArgumentException(String.format("文件路径错误，filePath：%s", filePath));
    }

    UploadEntity entity = DbEntity.findFirst(UploadEntity.class, "filePath=?", filePath);
    if (entity == null) {
      ALog.e(TAG, "删除上传记录失败，没有在数据库中找到对应的实体文件，filePath：" + filePath);
      return;
    }
    deleteRecord(entity, needRemoveFile, needRemoveEntity);
  }

  @Override
  public void deleteRecord(AbsEntity absEntity, boolean needRemoveFile, boolean needRemoveEntity) {
    if (absEntity == null) {
      ALog.e(TAG, "删除上传记录失败，实体为空");
      return;
    }

    UploadEntity entity = (UploadEntity) absEntity;

    // 删除下载的线程记录和任务记录
    DbEntity.deleteData(ThreadRecord.class, "taskKey=? AND threadType=?", entity.getFilePath(),
        String.valueOf(entity.getTaskType()));
    DbEntity.deleteData(TaskRecord.class, "filePath=? AND taskType=?", entity.getFilePath(),
        String.valueOf(entity.getTaskType()));

    if (needRemoveFile) {
      FileUtil.deleteFile(entity.getFilePath());
    }

    deleteEntity(needRemoveEntity, entity.getFilePath());
  }

  private void deleteEntity(boolean needRemoveEntity, String filePath) {
    if (needRemoveEntity) {
      DbEntity.deleteData(UploadEntity.class, "filePath=?", filePath);
    }
  }
}
