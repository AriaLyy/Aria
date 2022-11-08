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

/**
 * Created by laoyuyu on 2018/5/8.
 * 任务的线程记录
 */
public class ThreadRecord extends DbEntity {

  /**
   * 任务的文件路径，不是当前线程记录的的分块文件路径
   */
  public String taskKey;

  /**
   * 开始位置
   */
  public long startLocation;

  /**
   * 结束位置
   */
  public long endLocation;

  /**
   * 线程是否完成
   * {@code true}完成，{@code false}未完成
   */
  public boolean isComplete = false;

  /**
   * 线程id
   */
  public int threadId = 0;

  /**
   * 分块长度
   */
  public long blockLen = 0;

  /**
   * 线程类型
   * {@link ITaskWrapper}
   */
  public int threadType = 0;

  /**
   * ts文件的下载地址
   */
  public String tsUrl;
}
