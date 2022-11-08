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
package com.arialyy.aria.ftp.download;

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.RecordHandler;
import com.arialyy.aria.core.common.RecordHelper;
import com.arialyy.aria.core.config.Configuration;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.RecordUtil;
import java.util.ArrayList;

/**
 * @author lyy
 * Date: 2019-09-19
 */
final class FtpDRecordHandler extends RecordHandler {

  FtpDRecordHandler(DTaskWrapper wrapper) {
    super(wrapper);
  }

  @Override public void handlerTaskRecord(TaskRecord record) {
    RecordHelper helper = new RecordHelper(getWrapper(), record);
    if (record.threadNum == 1) {
      helper.handleSingleThreadRecord();
    } else {
      if (getWrapper().isSupportBP()) {
        if (record.isBlock) {
          helper.handleBlockRecord();
        } else {
          helper.handleMultiRecord();
        }
      }
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
    //最后一个线程的结束位置即为文件的总长度
    if (threadId == (record.threadNum - 1)) {
      endL = getFileSize();
    }
    tr.endLocation = endL;
    tr.blockLen = RecordUtil.getBlockLen(getFileSize(), threadId, record.threadNum);
    return tr;
  }

  @Override public TaskRecord createTaskRecord(int threadNum) {
    TaskRecord record = new TaskRecord();
    record.fileName = getEntity().getFileName();
    record.filePath = getEntity().getFilePath();
    record.threadRecords = new ArrayList<>();
    record.threadNum = threadNum;
    record.isBlock = Configuration.getInstance().downloadCfg.isUseBlock();
    record.taskType = ITaskWrapper.D_FTP;
    record.isGroupRecord = false;

    return record;
  }

  @Override public int initTaskThreadNum() {
    int threadNum = Configuration.getInstance().downloadCfg.getThreadNum();
    return getFileSize() <= IRecordHandler.SUB_LEN
        || threadNum == 1
        ? 1
        : threadNum;
  }
}
