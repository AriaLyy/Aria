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
package com.arialyy.aria.core.common;

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.core.wrapper.RecordWrapper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.DbDataHelper;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 处理任务记录，分配线程区间
 */
public abstract class RecordHandler implements IRecordHandler {
  protected final String TAG = CommonUtil.getClassName(this);

  @Deprecated private File mConfigFile;
  private TaskRecord mTaskRecord;
  private AbsTaskWrapper mTaskWrapper;
  private AbsNormalEntity mEntity;
  protected String mFilePath;
  protected long mFileSize;

  public RecordHandler(AbsTaskWrapper wrapper) {
    mTaskWrapper = wrapper;
    mEntity = (AbsNormalEntity) mTaskWrapper.getEntity();
  }

  public AbsTaskWrapper getWrapper() {
    return mTaskWrapper;
  }

  public AbsNormalEntity getEntity() {
    return mEntity;
  }

  @Override public void onPre() {

  }

  /**
   * 获取任务记录，如果任务记录存在，检查任务记录
   * 检查记录 对于分块任务： 子分块不存在或被删除，子线程将重新下载
   * 对于普通任务： 预下载文件不存在，则任务任务呗删除
   * 如果任务记录不存在或线程记录不存在，初始化记录
   *
   * @return 任务记录
   */
  @Override
  public TaskRecord getRecord(long fileSize) {
    mFileSize = fileSize;
    mConfigFile = new File(CommonUtil.getFileConfigPath(false, mEntity.getFileName()));
    if (mConfigFile.exists()) {
      convertDb();
    } else {
      onPre();
      mTaskRecord = DbDataHelper.getTaskRecord(getFilePath(), mEntity.getTaskType());
      if (mTaskRecord == null) {
        initRecord(true);
      }else if (mTaskRecord.threadRecords == null || mTaskRecord.threadRecords.size() == 0){
        if (mTaskRecord.threadRecords == null){
          mTaskRecord.threadRecords = new ArrayList<>();
        }
        initRecord(false);
      }
      handlerTaskRecord(mTaskRecord);
    }
    saveRecord();
    return mTaskRecord;
  }

  /**
   * convertDb 是兼容性代码 从3.4.1开始，线程配置信息将存储在数据库中。 将配置文件的内容复制到数据库中，并将配置文件删除
   */
  private void convertDb() {
    List<RecordWrapper> records =
        DbEntity.findRelationData(RecordWrapper.class, "TaskRecord.filePath=?",
            getFilePath());
    if (records == null || records.size() == 0) {
      Properties pro = FileUtil.loadConfig(mConfigFile);
      if (pro.isEmpty()) {
        ALog.d(TAG, "老版本的线程记录为空，任务为新任务");
        initRecord(true);
        return;
      }

      Set<Object> keys = pro.keySet();
      // 老版本记录是5s存一次，但是5s中内，如果线程执行完成，record记录是没有的，只有state记录...
      // 第一步应该是record 和 state去重取正确的线程数
      Set<Integer> set = new HashSet<>();
      for (Object key : keys) {
        String str = String.valueOf(key);
        int i = Integer.parseInt(str.substring(str.length() - 1));
        set.add(i);
      }
      int threadNum = set.size();
      if (threadNum == 0) {
        ALog.d(TAG, "线程数为空，任务为新任务");
        initRecord(true);
        return;
      }
      mTaskWrapper.setNewTask(false);
      mTaskRecord = createTaskRecord(threadNum);
      mTaskRecord.isBlock = false;
      File tempFile = new File(getFilePath());
      for (int i = 0; i < threadNum; i++) {
        ThreadRecord tRecord = new ThreadRecord();
        tRecord.taskKey = mTaskRecord.filePath;
        Object state = pro.getProperty(tempFile.getName() + STATE + i);
        Object record = pro.getProperty(tempFile.getName() + RECORD + i);
        if (state != null && Integer.parseInt(String.valueOf(state)) == 1) {
          tRecord.isComplete = true;
          continue;
        }
        if (record != null) {
          long temp = Long.parseLong(String.valueOf(record));
          tRecord.startLocation = temp > 0 ? temp : 0;
        } else {
          tRecord.startLocation = 0;
        }
        mTaskRecord.threadRecords.add(tRecord);
      }
      FileUtil.deleteFile(mConfigFile);
    }
  }

  /**
   * 初始化任务记录，分配线程区间，如果任务记录不存在，则创建新的任务记录
   *
   * @param newRecord {@code true} 需要创建新{@link TaskRecord}
   */
  private void initRecord(boolean newRecord) {
    if (newRecord) {
      mTaskRecord = createTaskRecord(initTaskThreadNum());
    }
    mTaskWrapper.setNewTask(true);
    int requestType = mTaskWrapper.getRequestType();
    if (requestType == ITaskWrapper.M3U8_LIVE) {
      return;
    }
    long blockSize = getFileSize() / mTaskRecord.threadNum;
    // 处理线程区间记录
    for (int i = 0; i < mTaskRecord.threadNum; i++) {
      long startL = i * blockSize, endL = (i + 1) * blockSize;
      ThreadRecord tr = createThreadRecord(mTaskRecord, i, startL, endL);
      mTaskRecord.threadRecords.add(tr);
    }
  }

  /**
   * 保存任务记录
   */
  private void saveRecord() {
    mTaskRecord.threadNum = mTaskRecord.threadRecords.size();
    mTaskRecord.save();
    if (mTaskRecord.threadRecords != null && !mTaskRecord.threadRecords.isEmpty()) {
      DbEntity.saveAll(mTaskRecord.threadRecords);
    }
    ALog.d(TAG, String.format("保存记录，线程记录数：%s", mTaskRecord.threadRecords.size()));
  }

  protected long getFileSize() {
    return mFileSize;
  }

  /**
   * 获取任务路径
   *
   * @return 任务文件路径
   */
  private String getFilePath() {
    if (mEntity instanceof DownloadEntity) {
      return ((DownloadEntity) mTaskWrapper.getEntity()).getFilePath();
    } else {
      return ((UploadEntity) mTaskWrapper.getEntity()).getFilePath();
    }
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }

  @Override public boolean checkTaskCompleted() {
    if (mTaskRecord == null
        || mTaskRecord.threadRecords == null
        || mTaskRecord.threadRecords.isEmpty()) {
      return false;
    }
    int completeNum = 0;
    for (ThreadRecord tr : mTaskRecord.threadRecords) {
      if (tr.isComplete) {
        completeNum++;
      }
    }
    return completeNum != 0 && completeNum == mTaskRecord.threadNum;
  }
}
