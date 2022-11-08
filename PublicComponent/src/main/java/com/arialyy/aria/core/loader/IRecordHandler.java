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

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;

/**
 * @author lyy
 * Date: 2019-09-18
 */
public interface IRecordHandler extends ILoaderComponent {

  int TYPE_DOWNLOAD = 1;
  int TYPE_UPLOAD = 2;
  int TYPE_M3U8_VOD = 3;
  int TYPE_M3U8_LIVE = 4;

  String STATE = "_state_";
  String RECORD = "_record_";
  /**
   * 小于1m的文件不启用多线程
   */
  long SUB_LEN = 1024 * 1024;

  /**
   * 分块文件路径，文件路径.线程id.part
   */
  String SUB_PATH = "%s.%s.part";

  /**
   * 获取任务记录
   */
  TaskRecord getRecord(long fileSize);

  /**
   * 记录处理前的操作，可用来删除任务记录
   */
  void onPre();

  /**
   * 处理任务记录
   */
  void handlerTaskRecord(TaskRecord record);

  /**
   * 处理线程任务
   *
   * @param record 任务记录
   * @param threadId 线程id
   * @param startL 线程开始位置
   * @param endL 线程结束位置
   */
  ThreadRecord createThreadRecord(TaskRecord record, int threadId, long startL, long endL);

  /**
   * 新任务创建任务记录
   */
  TaskRecord createTaskRecord(int threadNum);

  /**
   * 配置新任务的线程数
   *
   * @return 新任务的线程数
   */
  int initTaskThreadNum();

  /**
   * 检查任务是否已完成
   *
   * @return true 任务已完成
   */
  boolean checkTaskCompleted();
}
