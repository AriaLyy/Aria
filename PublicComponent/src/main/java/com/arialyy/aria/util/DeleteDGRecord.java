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
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.wrapper.RecordWrapper;
import com.arialyy.aria.orm.DbEntity;
import java.util.List;

/**
 * 删除组合任务记录
 */
public class DeleteDGRecord implements IDeleteRecord {
  private String TAG = CommonUtil.getClassName(this);

  private static volatile DeleteDGRecord INSTANCE = null;

  private DeleteDGRecord() {

  }

  public static DeleteDGRecord getInstance() {
    if (INSTANCE == null) {
      synchronized (DeleteDGRecord.class) {
        if (INSTANCE == null) {
          INSTANCE = new DeleteDGRecord();
        }
      }
    }
    return INSTANCE;
  }

  /**
   * @param dirPath 组合任务保存路径
   * @param needRemoveFile true，无论下载成功以否，都将删除下载下来的文件。false，只会删除下载任务没有完成的文件
   * @param needRemoveEntity 是否需要删除实体，true 删除实体
   */
  @Override
  public void deleteRecord(String dirPath, boolean needRemoveFile, boolean needRemoveEntity) {
    if (TextUtils.isEmpty(dirPath)) {
      ALog.e(TAG, "删除下载任务组记录失败，组合任务路径为空");
      return;
    }
    deleteRecord(DbDataHelper.getDGEntityByPath(dirPath), needRemoveFile, needRemoveEntity);
  }

  @Override
  public void deleteRecord(AbsEntity absEntity, boolean needRemoveFile, boolean needRemoveEntity) {
    if (absEntity == null) {
      ALog.e(TAG, "删除组合任务记录失败，组合任务实体为空");
      return;
    }
    DownloadGroupEntity groupEntity = (DownloadGroupEntity) absEntity;

    List<RecordWrapper> records =
        DbEntity.findRelationData(RecordWrapper.class, "dGroupHash=?", groupEntity.getGroupHash());

    // 删除子任务记录
    if (records == null || records.isEmpty()) {
      ALog.w(TAG, "组任务记录已删除");
    } else {
      for (RecordWrapper record : records) {
        if (record == null || record.taskRecord == null) {
          continue;
        }
        // 删除分块文件
        if (record.taskRecord.isBlock) {
          removeBlockFile(record.taskRecord);
        }
        DbEntity.deleteData(ThreadRecord.class, "taskKey=?", record.taskRecord.filePath);
        record.taskRecord.deleteData();
      }
    }

    // 删除组合任务子任务的文件
    List<DownloadEntity> subs = groupEntity.getSubEntities();
    if (subs != null) {
      for (DownloadEntity sub : subs) {
        if (needRemoveFile || !groupEntity.isComplete()) {
          FileUtil.deleteFile(sub.getFilePath());
        }
      }
    }

    // 删除文件夹
    if (!TextUtils.isEmpty(groupEntity.getDirPath())) {
      if (needRemoveFile || !groupEntity.isComplete()) {
        FileUtil.deleteFile(groupEntity.getDirPath());
      }
    }

    deleteEntity(needRemoveEntity, groupEntity.getGroupHash());
  }

  private void deleteEntity(boolean needRemoveEntity, String groupHash) {
    if (needRemoveEntity) {
      DbEntity.deleteData(DownloadEntity.class, "groupHash=?", groupHash);
      DbEntity.deleteData(DownloadGroupEntity.class, "groupHash=?", groupHash);
    }
  }

  /**
   * 删除多线程分块下载的分块文件
   */
  private void removeBlockFile(TaskRecord record) {
    for (int i = 0, len = record.threadNum; i < len; i++) {
      FileUtil.deleteFile(String.format(IRecordHandler.SUB_PATH, record.filePath, i));
    }
  }
}
