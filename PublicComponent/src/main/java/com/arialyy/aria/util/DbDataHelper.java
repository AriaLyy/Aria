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

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.download.DGEntityWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库帮助类
 */
public class DbDataHelper {

  /**
   * 获取任务记录
   *
   * @param filePath 文件地址
   * @param taskType 任务类型{@link ITaskWrapper}
   * @return 没有记录返回null，有记录则返回任务记录
   */
  public static TaskRecord getTaskRecord(String filePath, int taskType) {
    TaskRecord taskRecord =
        DbEntity.findFirst(TaskRecord.class, "filePath=? AND taskType=?", filePath,
            String.valueOf(taskType));
    if (taskRecord != null) {
      taskRecord.threadRecords =
          DbEntity.findDatas(ThreadRecord.class, "taskKey=? AND threadType=?", filePath,
              String.valueOf(taskType));
    }

    return taskRecord;
  }

  /**
   * 获取组合任务实体、ftpDir任务实体
   *
   * @param groupHash 组合任务Hash
   * @return 实体不存在，返回null
   */
  public static DownloadGroupEntity getDGEntityByHash(String groupHash) {
    List<DGEntityWrapper> wrapper =
        DbEntity.findRelationData(DGEntityWrapper.class, "DownloadGroupEntity.groupHash=?",
            groupHash);

    return wrapper == null || wrapper.size() == 0 ? null : wrapper.get(0).groupEntity;
  }

  /**
   * 获取组合任务实体、ftpDir任务实体
   *
   * @param dirPath 组合任务Hash
   * @return 实体不存在，返回null
   */
  public static DownloadGroupEntity getDGEntityByPath(String dirPath) {
    List<DGEntityWrapper> wrapper =
        DbEntity.findRelationData(DGEntityWrapper.class, "DownloadGroupEntity.dirPath=?",
            dirPath);

    return wrapper == null || wrapper.size() == 0 ? null : wrapper.get(0).groupEntity;
  }

  /**
   * 获取组合任务实体、ftpDir任务实体
   *
   * @param taskId 组合任务id
   * @return 实体不存在，返回null
   */
  public static DownloadGroupEntity getDGEntity(long taskId) {
    List<DGEntityWrapper> wrapper =
        DbEntity.findRelationData(DGEntityWrapper.class, "DownloadGroupEntity.rowid=?",
            String.valueOf(taskId));

    return wrapper == null || wrapper.size() == 0 ? null : wrapper.get(0).groupEntity;
  }

  /**
   * 创建HTTP子任务实体
   */
  public static List<DownloadEntity> createHttpSubTask(String groupHash, List<String> urls) {
    List<DownloadEntity> list = new ArrayList<>();
    for (int i = 0, len = urls.size(); i < len; i++) {
      String url = urls.get(i);
      DownloadEntity entity = new DownloadEntity();
      entity.setUrl(url);
      entity.setFilePath(groupHash + "_" + i);
      int lastIndex = url.lastIndexOf(File.separator);
      //去除url末尾携带的的参数
      int endIndex = url.lastIndexOf("?");

      if(endIndex<0||endIndex<lastIndex)endIndex=url.length();
      entity.setFileName(url.substring(lastIndex + 1,endIndex));
      entity.setGroupHash(groupHash);
      entity.setGroupChild(true);
      list.add(entity);
    }
    return list;
  }

  /**
   * 通过Ftp下载地址获取组合任务实体
   *
   * @param ftpUrl ftp下载地址
   */
  public static DownloadGroupEntity getOrCreateFtpDGEntity(String ftpUrl) {
    List<DGEntityWrapper> wrapper =
        DbEntity.findRelationData(DGEntityWrapper.class, "DownloadGroupEntity.groupHash=?",
            ftpUrl);
    DownloadGroupEntity groupEntity;
    if (wrapper != null && !wrapper.isEmpty()) {
      groupEntity = wrapper.get(0).groupEntity;
      if (groupEntity == null) {
        groupEntity = new DownloadGroupEntity();
      }
    } else {
      groupEntity = new DownloadGroupEntity();
    }
    groupEntity.setGroupHash(ftpUrl);
    return groupEntity;
  }

  /**
   * 创建任务组子任务的任务实体
   */
  public static List<DTaskWrapper> createDGSubTaskWrapper(DownloadGroupEntity dge) {
    List<DTaskWrapper> list = new ArrayList<>();
    for (DownloadEntity entity : dge.getSubEntities()) {
      DTaskWrapper taskEntity = new DTaskWrapper(entity);
      taskEntity.setGroupHash(dge.getKey());
      taskEntity.setGroupTask(true);
      list.add(taskEntity);
    }
    return list;
  }
}
