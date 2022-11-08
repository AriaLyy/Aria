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
package com.arialyy.aria.core.inf;

import com.arialyy.aria.orm.annotation.Ignore;

/**
 * Created by lyy on 2017/2/23.
 */
public interface IEntity {
  /**
   * 其它状态
   */
  @Ignore int STATE_OTHER = -1;
  /**
   * 失败状态
   */
  @Ignore int STATE_FAIL = 0;
  /**
   * 完成状态
   */
  @Ignore int STATE_COMPLETE = 1;
  /**
   * 停止状态
   */
  @Ignore int STATE_STOP = 2;
  /**
   * 等待状态
   */
  @Ignore int STATE_WAIT = 3;
  /**
   * 正在执行
   */
  @Ignore int STATE_RUNNING = 4;
  /**
   * 预处理
   */
  @Ignore int STATE_PRE = 5;
  /**
   * 预处理完成
   */
  @Ignore int STATE_POST_PRE = 6;
  /**
   * 删除任务
   */
  @Ignore int STATE_CANCEL = 7;
}
