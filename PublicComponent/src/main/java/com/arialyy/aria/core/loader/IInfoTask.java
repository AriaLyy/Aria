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

import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.exception.AriaException;

/**
 * 任务信息采集
 */
public interface IInfoTask extends ILoaderComponent {

  /**
   * 执行任务
   */
  void run();

  /**
   * 设置回调
   */
  void setCallback(Callback callback);

  /**
   * 任务停止
   */
  void stop();

  /**
   * 任务取消
   */
  void cancel();

  interface Callback {
    /**
     * 处理完成
     *
     * @param info 一些回调的信息
     */
    void onSucceed(String key, CompleteInfo info);

    /**
     * 请求失败
     *
     * @param e 错误信息
     */
    void onFail(AbsEntity entity, AriaException e, boolean needRetry);
  }
}
