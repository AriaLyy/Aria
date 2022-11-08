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
import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;

/**
 * 删除m3u8记录
 */
public class DeleteM3u8Record implements IDeleteRecord {
  private String TAG = CommonUtil.getClassName(this);

  private static volatile DeleteM3u8Record INSTANCE = null;

  private DeleteM3u8Record() {

  }

  public static DeleteM3u8Record getInstance() {
    if (INSTANCE == null) {
      synchronized (DeleteM3u8Record.class) {
        if (INSTANCE == null) {
          INSTANCE = new DeleteM3u8Record();
        }
      }
    }
    return INSTANCE;
  }

  /**
   * @param filePath 文件保存路径
   * @param removeTarget true，无论下载成功以否，都将删除下载下来的文件。false，只会删除下载任务没有完成的文件
   * @param needRemoveEntity 是否需要删除实体
   */
  @Override public void deleteRecord(String filePath, boolean removeTarget,
      boolean needRemoveEntity) {
    if (TextUtils.isEmpty(filePath)) {
      throw new NullPointerException("删除记录失败，文件路径为空");
    }
    if (!filePath.startsWith("/")) {
      throw new IllegalArgumentException(String.format("文件路径错误，filePath：%s", filePath));
    }
    DownloadEntity entity = DbEntity.findFirst(DownloadEntity.class, "downloadPath=?", filePath);
    if (entity == null) {
      ALog.e(TAG, "删除下载记录失败，没有在数据库中找到对应的实体文件，filePath：" + filePath);
      return;
    }
    deleteRecord(entity, removeTarget, needRemoveEntity);
  }

  @Override
  public void deleteRecord(AbsEntity absEntity, boolean needRemoveFile, boolean needRemoveEntity) {
    if (absEntity == null) {
      ALog.e(TAG, "删除下载记录失败，实体为空");
      return;
    }

    DownloadEntity entity = (DownloadEntity) absEntity;
    final String filePath = entity.getFilePath();
    TaskRecord record = DbDataHelper.getTaskRecord(filePath, entity.getTaskType());
    if (record == null) {
      ALog.e(TAG, "删除下载记录失败，记录为空，将删除实体记录，filePath：" + entity.getFilePath());
      deleteEntity(entity.getTaskType(), needRemoveEntity, filePath);
      return;
    }

    if (needRemoveFile || !entity.isComplete()) {
      removeTsCache(new File(filePath), record.bandWidth);
      FileUtil.deleteFile(filePath);
    }

    deleteEntity(entity.getTaskType(), needRemoveEntity, filePath);
  }

  private void deleteEntity(int taskType, boolean needRemoveEntity, String filePath){
    // 删除下载的线程记录和任务记录
    DbEntity.deleteData(ThreadRecord.class, "taskKey=? AND threadType=?", filePath,
        String.valueOf(taskType));
    DbEntity.deleteData(TaskRecord.class, "filePath=? AND taskType=?", filePath,
        String.valueOf(taskType));
    DbEntity.deleteData(M3U8Entity.class, "filePath=?", filePath);

    if (needRemoveEntity) {
      DbEntity.deleteData(DownloadEntity.class, "downloadPath=?", filePath);
    }
  }

  /**
   * 删除ts文件，和索引文件（如果有的话）
   */
  private static void removeTsCache(File targetFile, long bandWidth) {

    // 删除key
    M3U8Entity entity = DbEntity.findFirst(M3U8Entity.class, "filePath=?", targetFile.getPath());
    if (entity != null && !TextUtils.isEmpty(entity.keyPath)){
      File keyFile = new File(entity.keyPath);
      FileUtil.deleteFile(keyFile);
    }

    // 删除ts
    String cacheDir = null;
    if (!targetFile.isDirectory()) {
      cacheDir =
          String.format("%s/.%s_%s", targetFile.getParent(), targetFile.getName(), bandWidth);
    }

    if (!TextUtils.isEmpty(cacheDir)) {
      File cacheDirF = new File(cacheDir);
      if (!cacheDirF.exists()) {
        return;
      }
      File[] files = cacheDirF.listFiles();
      for (File f : files) {
        if (f.exists()) {
          f.delete();
        }
      }
      File cDir = new File(cacheDir);
      if (cDir.exists()) {
        cDir.delete();
      }
    }

    File indexFile = new File(String.format("%s.index", targetFile.getPath()));

    if (indexFile.exists()) {
      indexFile.delete();
    }

  }
}
