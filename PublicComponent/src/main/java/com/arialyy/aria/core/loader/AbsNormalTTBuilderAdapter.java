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
package com.arialyy.aria.core.loader;

import android.os.Handler;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.AbsNormalEntity;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.task.IThreadTaskAdapter;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;

public abstract class AbsNormalTTBuilderAdapter {
  protected String TAG = CommonUtil.getClassName(this);
  protected AbsTaskWrapper wrapper;
  private File tempFile;

  public AbsNormalTTBuilderAdapter() {
  }

  protected void setWrapper(AbsTaskWrapper wrapper) {
    this.wrapper = wrapper;
    tempFile = new File(((AbsNormalEntity) wrapper.getEntity()).getFilePath());
  }

  /**
   * 创建线程任务适配器
   */
  public abstract IThreadTaskAdapter getAdapter(SubThreadConfig config);

  /**
   * 处理新任务
   *
   * @param record 任务记录
   * @param totalThreadNum 任务的线程总数
   * @return {@code true}创建新任务成功
   */
  public abstract boolean handleNewTask(TaskRecord record, int totalThreadNum);

  /**
   * SubThreadConfig 模版，如果不使用该方法创建配置，则默认使用{@link #createNormalSubThreadConfig(Handler, ThreadRecord,
   * boolean, int)}创建配置
   */
  protected SubThreadConfig getSubThreadConfig(Handler stateHandler, ThreadRecord threadRecord,
      boolean isBlock, int startNum) {
    return createNormalSubThreadConfig(stateHandler, threadRecord, isBlock, startNum);
  }

  private SubThreadConfig createNormalSubThreadConfig(Handler stateHandler,
      ThreadRecord threadRecord,
      boolean isBlock, int startNum) {
    SubThreadConfig config = new SubThreadConfig();
    config.url = getEntity().isRedirect() ? getEntity().getRedirectUrl() : getEntity().getUrl();
    config.tempFile =
        isBlock ? new File(
            String.format(IRecordHandler.SUB_PATH, tempFile.getPath(), threadRecord.threadId))
            : tempFile;
    config.isBlock = isBlock;
    config.startThreadNum = startNum;
    config.taskWrapper = wrapper;
    config.record = threadRecord;
    config.stateHandler = stateHandler;
    config.threadType = SubThreadConfig.getThreadType(wrapper.getRequestType());
    config.updateInterval = SubThreadConfig.getUpdateInterval(wrapper.getRequestType());
    return config;
  }

  protected AbsNormalEntity getEntity() {
    return (AbsNormalEntity) wrapper.getEntity();
  }

  protected File getTempFile() {
    return tempFile;
  }
}
