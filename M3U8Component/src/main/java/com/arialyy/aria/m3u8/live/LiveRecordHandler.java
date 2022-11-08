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
package com.arialyy.aria.m3u8.live;

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.RecordHandler;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.m3u8.M3U8TaskOption;
import com.arialyy.aria.util.DeleteM3u8Record;
import com.arialyy.aria.util.RecordUtil;
import java.util.ArrayList;

/**
 * 直播m3u8文件处理器
 */
final class LiveRecordHandler extends RecordHandler {
  private M3U8TaskOption mOption;

  LiveRecordHandler(AbsTaskWrapper wrapper) {
    super(wrapper);
  }

  public void setOption(M3U8TaskOption option) {
    mOption = option;
  }

  @Override public void onPre() {
    super.onPre();
    DeleteM3u8Record.getInstance().deleteRecord(getEntity().getFilePath(), true, true);
  }

  /**
   * @deprecated 直播文件不需要处理任务记录
   */
  @Deprecated
  @Override public void handlerTaskRecord(TaskRecord record) {
    if (record.threadRecords == null) {
      record.threadRecords = new ArrayList<>();
    }
  }

  /**
   * @deprecated 交由{@link #createThreadRecord(TaskRecord, String, int)} 处理
   */
  @Override
  @Deprecated
  public ThreadRecord createThreadRecord(TaskRecord record, int threadId, long startL, long endL) {
    return null;
  }

  /**
   * 创建线程记录
   *
   * @param taskRecord 任务记录
   * @param tsUrl ts下载地址
   * @param threadId 线程id
   */
  ThreadRecord createThreadRecord(TaskRecord taskRecord, String tsUrl, int threadId) {
    ThreadRecord tr = new ThreadRecord();
    tr.taskKey = taskRecord.filePath;
    tr.isComplete = false;
    tr.tsUrl = tsUrl;
    tr.threadType = taskRecord.taskType;
    tr.threadId = threadId;
    tr.startLocation = 0;
    taskRecord.threadRecords.add(tr);
    return tr;
  }

  @Override public TaskRecord createTaskRecord(int threadNum) {
    TaskRecord record = new TaskRecord();
    record.fileName = getEntity().getFileName();
    record.filePath = getEntity().getFilePath();
    record.threadRecords = new ArrayList<>();
    record.threadNum = threadNum;
    record.isBlock = true;
    record.taskType = ITaskWrapper.M3U8_LIVE;
    record.bandWidth = mOption.getBandWidth();
    return record;
  }

  @Override public int initTaskThreadNum() {
    return 1;
  }
}
