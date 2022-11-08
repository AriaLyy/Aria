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
package com.arialyy.aria.m3u8.vod;

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.RecordHandler;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.m3u8.BaseM3U8Loader;
import com.arialyy.aria.m3u8.M3U8InfoTask;
import com.arialyy.aria.m3u8.M3U8TaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.util.ArrayList;

/**
 * @author lyy
 * Date: 2019-09-24
 */
final class VodRecordHandler extends RecordHandler {
  private M3U8TaskOption mOption;

  VodRecordHandler(DTaskWrapper wrapper) {
    super(wrapper);
  }

  public void setOption(M3U8TaskOption option) {
    mOption = option;
  }

  /**
   * 不处理live的记录
   */
  @Override public void handlerTaskRecord(TaskRecord mTaskRecord) {
    String cacheDir = mOption.getCacheDir();
    long currentProgress = 0;
    int completeNum = 0;
    File targetFile = new File(mTaskRecord.filePath);
    if (!targetFile.exists()) {
      FileUtil.createFile(targetFile);
    }

    M3U8Entity m3U8Entity = ((DownloadEntity) getEntity()).getM3U8Entity();
    // 重新下载所有切片
    boolean reDownload =
        (m3U8Entity.getPeerNum() <= 0 || (mOption.isGenerateIndexFile() && !new File(
            String.format(M3U8InfoTask.M3U8_INDEX_FORMAT, getEntity().getFilePath())).exists()));

    for (ThreadRecord record : mTaskRecord.threadRecords) {
      File temp = new File(BaseM3U8Loader.getTsFilePath(cacheDir, record.threadId));
      if (!record.isComplete || reDownload) {
        if (temp.exists()) {
          FileUtil.deleteFile(temp);
        }
        record.startLocation = 0;
        //ALog.d(TAG, String.format("分片【%s】未完成，将重新下载该分片", record.threadId));
      } else {
        if (!temp.exists()) {
          record.startLocation = 0;
          record.isComplete = false;
          ALog.w(TAG, String.format("分片【%s】不存在，将重新下载该分片", record.threadId));
        } else {
          completeNum++;
          currentProgress += temp.length();
        }
      }
    }
    mOption.setCompleteNum(completeNum);
    getEntity().setCurrentProgress(currentProgress);
    mTaskRecord.bandWidth = mOption.getBandWidth();
  }

  /**
   * 不处理live的记录
   *
   * @param record 任务记录
   * @param threadId 线程id
   * @param startL 线程开始位置
   * @param endL 线程结束位置
   */
  @Override
  public ThreadRecord createThreadRecord(TaskRecord record, int threadId, long startL, long endL) {
    ThreadRecord tr;
    tr = new ThreadRecord();
    tr.taskKey = record.filePath;
    tr.threadId = threadId;
    tr.isComplete = false;
    tr.startLocation = 0;
    tr.threadType = record.taskType;
    tr.tsUrl = mOption.getUrls().get(threadId);
    return tr;
  }

  @Override public TaskRecord createTaskRecord(int threadNum) {
    TaskRecord record = new TaskRecord();
    record.fileName = getEntity().getFileName();
    record.filePath = getEntity().getFilePath();
    record.threadRecords = new ArrayList<>();
    record.threadNum = threadNum;
    record.isBlock = true;
    record.taskType = ITaskWrapper.M3U8_VOD;
    record.bandWidth = mOption.getBandWidth();
    return record;
  }

  @Override public int initTaskThreadNum() {
    if (getWrapper().getRequestType() == ITaskWrapper.M3U8_VOD) {
      return
          mOption.getUrls() == null || mOption.getUrls().isEmpty() ? 1 : mOption.getUrls().size();
    }
    if (getWrapper().getRequestType() == ITaskWrapper.M3U8_LIVE) {
      return 1;
    }
    return 0;
  }
}
