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
package com.arialyy.aria.core.task;

import android.os.Bundle;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.exception.AriaException;

/**
 * 线程任务观察者
 *
 * @author lyy
 * Date: 2019-09-18
 */
public interface IThreadTaskObserver {

  /**
   * 更新所有状态
   *
   * @param state state {@link IThreadStateManager#STATE_STOP}..
   */
  void updateState(int state, Bundle bundle);

  /**
   * 更新完成的状态
   */
  void updateCompleteState();

  /**
   * 更新失败的状态
   *
   * @param needRetry 是否需要重试，一般是网络错误才需要重试
   */
  void updateFailState(AriaException e, boolean needRetry);

  /**
   * 更新进度
   *
   * @param len 新增的长度
   */
  void updateProgress(long len);

  /**
   * 获取线程当前进度
   */
  long getThreadProgress();
}
