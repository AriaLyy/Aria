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
package com.arialyy.aria.sftp.upload;

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.RecordHandler;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.RecordUtil;
import com.jcraft.jsch.SftpATTRS;
import java.util.ArrayList;

/**
 * 上传任务记录处理器
 */
final class SFtpURecordHandler extends RecordHandler {
  private SftpATTRS ftpAttrs;

  SFtpURecordHandler(UTaskWrapper wrapper) {
    super(wrapper);
  }

  void setFtpAttrs(SftpATTRS ftpAttrs) {
    this.ftpAttrs = ftpAttrs;
  }

  @Override public void handlerTaskRecord(TaskRecord record) {
    if (record.threadRecords == null || record.threadRecords.isEmpty()) {
      record.threadRecords = new ArrayList<>();
      record.threadRecords.add(
          createThreadRecord(record, 0, ftpAttrs == null ? 0 : ftpAttrs.getSize(), getFileSize()));
    }

    if (ftpAttrs != null) {
      UploadEntity entity = (UploadEntity) getWrapper().getEntity();
      //远程文件已完成
      if (ftpAttrs.getSize() == getFileSize()) {
        record.threadRecords.get(0).isComplete = true;
        ALog.d(TAG, "FTP服务器上已存在该文件【" + entity.getFileName() + "】");
      } else if (ftpAttrs.getSize() == 0) {
        getWrapper().setNewTask(true);
        ALog.d(TAG, "FTP服务器上已存在该文件【" + entity.getFileName() + "】，但文件长度为0，重新上传该文件");
      } else {
        ALog.w(TAG, "FTP服务器已存在未完成的文件【"
            + entity.getFileName()
            + "，size: "
            + ftpAttrs.getSize()
            + "】"
            + "尝试从位置："
            + ftpAttrs.getSize()
            + "开始上传");
        getWrapper().setNewTask(false);

        // 修改记录
        ThreadRecord threadRecord = record.threadRecords.get(0);
        //修改本地保存的停止地址为服务器上对应文件的大小
        threadRecord.startLocation = ftpAttrs.getSize();
      }
    } else {
      ALog.d(TAG, "SFTP服务器上不存在该文件");
      getWrapper().setNewTask(true);
      ThreadRecord tr = record.threadRecords.get(0);
      tr.startLocation = 0;
      tr.endLocation = getFileSize();
      tr.isComplete = false;
    }
  }

  @Override
  public ThreadRecord createThreadRecord(TaskRecord record, int threadId, long startL, long endL) {
    ThreadRecord tr;
    tr = new ThreadRecord();
    tr.taskKey = record.filePath;
    tr.threadId = threadId;
    tr.startLocation = startL;
    tr.isComplete = false;
    tr.threadType = record.taskType;
    tr.endLocation = getFileSize();
    tr.blockLen = RecordUtil.getBlockLen(getFileSize(), threadId, record.threadNum);
    return tr;
  }

  @Override public TaskRecord createTaskRecord(int threadNum) {
    TaskRecord record = new TaskRecord();
    record.fileName = getEntity().getFileName();
    record.filePath = getEntity().getFilePath();
    record.threadRecords = new ArrayList<>();
    record.threadNum = threadNum;
    record.isBlock = false;
    record.taskType = ITaskWrapper.U_SFTP;
    record.isGroupRecord = getEntity().isGroupChild();

    return record;
  }

  @Override public int initTaskThreadNum() {
    return 1;
  }
}
