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
import com.arialyy.aria.core.common.AbsNormalEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;

/**
 * 任务记录处理工具
 */
public class RecordUtil {
  private static final String TAG = "RecordUtil";

  /**
   * 删除任务组记录
   *
   * @param removeFile {@code true} 无论任务是否完成，都会删除记录和文件；
   * {@code false} 如果任务已经完成，则只删除记录，不删除文件；任务未完成，记录和文件都会删除。
   */
  public static void delGroupTaskRecordByHash(String groupHash, boolean removeFile) {
    if (TextUtils.isEmpty(groupHash)) {
      ALog.e(TAG, "删除下载任务组记录失败，groupHash为null");
      return;
    }
    DownloadGroupEntity groupEntity = DbDataHelper.getDGEntityByHash(groupHash);
    DeleteDGRecord.getInstance().deleteRecord(groupEntity, removeFile, true);
  }

  /**
   * 删除任务记录，默认删除文件
   *
   * @param removeFile {@code true} 无论任务是否完成，都会删除记录和文件；
   * {@code false} 如果是下载任务，并且任务已经完成，则只删除记录，不删除文件；任务未完成，记录和文件都会删除。
   * 如果是上传任务，无论任务是否完成，都只删除记录
   */
  public static void delNormalTaskRecord(AbsNormalEntity entity, boolean removeFile) {
    switch (entity.getTaskType()) {
      case ITaskWrapper.D_FTP:
      case ITaskWrapper.D_HTTP:
      case ITaskWrapper.D_SFTP:
      case ITaskWrapper.D_TCP:
        DeleteDRecord.getInstance().deleteRecord(entity, removeFile, true);
        break;
      case ITaskWrapper.U_FTP:
      case ITaskWrapper.U_HTTP:
      case ITaskWrapper.U_SFTP:
        DeleteURecord.getInstance().deleteRecord(entity, removeFile, true);
        break;
      case ITaskWrapper.M3U8_LIVE:
      case ITaskWrapper.M3U8_VOD:
        DeleteM3u8Record.getInstance().deleteRecord(entity, removeFile, true);
        break;
    }
  }

  /**
   * 删除任务记录，默认删除文件，删除任务实体
   *
   * @param filePath 文件路径
   * @param removeFile {@code true} 无论任务是否完成，都会删除记录和文件；
   * @param type {@link AbsTaskWrapper#getRequestType()}
   * {@code false} 如果是下载任务，并且任务已经完成，则只删除记录，不删除文件；任务未完成，记录和文件都会删除。
   * 如果是上传任务，无论任务是否完成，都只删除记录
   * 上传任务的记录
   */
  public static void delTaskRecord(String filePath, int type, boolean removeFile,
      boolean removeEntity) {
    switch (type) {
      case ITaskWrapper.D_FTP:
      case ITaskWrapper.D_HTTP:
      case ITaskWrapper.D_SFTP:
      case ITaskWrapper.D_TCP:
        DeleteDRecord.getInstance().deleteRecord(filePath, removeFile, removeEntity);
        break;
      case ITaskWrapper.U_FTP:
      case ITaskWrapper.U_HTTP:
      case ITaskWrapper.U_SFTP:
        DeleteURecord.getInstance().deleteRecord(filePath, removeFile, removeEntity);
        break;
      case ITaskWrapper.M3U8_LIVE:
      case ITaskWrapper.M3U8_VOD:
        DeleteM3u8Record.getInstance().deleteRecord(filePath, removeFile, removeEntity);
        break;
    }
  }

  /**
   * 修改任务路径，修改文件路径和任务记录信息。如果是分块任务，则修改分块文件的路径。
   *
   * @param oldPath 旧的文件路径
   * @param newPath 新的文件路径
   * @param taskType 任务类型{@link ITaskWrapper}
   */
  public static void modifyTaskRecord(String oldPath, String newPath, int taskType) {
    if (oldPath.equals(newPath)) {
      ALog.w(TAG, "修改任务记录失败，新文件路径和旧文件路径一致");
      return;
    }
    TaskRecord record = DbDataHelper.getTaskRecord(oldPath, taskType);
    if (record == null) {
      if (new File(oldPath).exists()) {
        ALog.w(TAG, "修改任务记录失败，文件【" + oldPath + "】对应的任务记录不存在");
      }
      return;
    }
    if (!record.isBlock) {
      File oldFile = new File(oldPath);
      if (oldFile.exists()) {
        oldFile.renameTo(new File(newPath));
      }
    }

    record.filePath = newPath;
    record.update();
    // 修改线程记录
    if (record.threadRecords != null && !record.threadRecords.isEmpty()) {
      for (ThreadRecord tr : record.threadRecords) {
        tr.taskKey = newPath;
        File blockFile = new File(String.format(IRecordHandler.SUB_PATH, oldPath, tr.threadId));
        if (blockFile.exists()) {
          blockFile.renameTo(
              new File(String.format(IRecordHandler.SUB_PATH, newPath, tr.threadId)));
        }
      }
      DbEntity.updateManyData(record.threadRecords);
    }
  }

  /**
   * 检查分块任务是否存在
   *
   * @param filePath 文件保存路径
   * @return {@code true} 分块文件存在
   */
  public static boolean blockTaskExists(String filePath) {
    return new File(String.format(IRecordHandler.SUB_PATH, filePath, 0)).exists();
  }

  /**
   * 获取分块文件的快大小
   *
   * @param fileLen 文件总长度
   * @param blockId 分块id
   * @param blockNum 分块数量
   * @return 分块长度
   */
  public static long getBlockLen(long fileLen, int blockId, int blockNum) {
    final long averageLen = fileLen / blockNum;
    return blockId == blockNum - 1 ? (fileLen - blockId * averageLen) : averageLen;
  }
}
