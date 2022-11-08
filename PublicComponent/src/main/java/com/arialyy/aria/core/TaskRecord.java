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
package com.arialyy.aria.core;

import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.annotation.Ignore;
import com.arialyy.aria.orm.annotation.NoNull;
import com.arialyy.aria.orm.annotation.Unique;
import java.util.List;

/**
 * Created by laoyuyu on 2018/3/21.
 * 任务上传或下载的任务记录
 */
public class TaskRecord extends DbEntity {
  //public static final int TYPE_HTTP_FTP = 0;
  //public static final int TYPE_M3U8_VOD = 1;
  //public static final int TYPE_M3U8_LIVE = 2;

  @Ignore
  public List<ThreadRecord> threadRecords;

  /**
   * 任务线程数
   */
  public int threadNum;

  /**
   * 任务文件路径
   */
  public String filePath;

  /**
   * 文件长度
   */
  public long fileLength;

  /**
   * 任务文件名
   */
  @NoNull
  public String fileName;

  /**
   * 是否是任务组的子任务记录
   * {@code true}是
   */
  public boolean isGroupRecord = false;

  /**
   * 下载任务组名
   */
  public String dGroupHash;

  /**
   * 上传组任务名，暂时没有用
   */
  @Ignore
  @Deprecated
  public String uGroupHash;

  /**
   * 是否是分块{@code true}是，{@code false} 不是
   */
  public boolean isBlock = false;

  /**
   * 任务类型
   * {@link ITaskWrapper}
   */
  public int taskType = 0;

  /**
   * m3u8文件码率
   */
  public long bandWidth = 0;
}
