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
package com.arialyy.aria.core.listener;

import android.os.Handler;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.exception.AriaException;

/**
 * Created by Aria.Lao on 2017/7/18.
 * 基础事件
 */
public interface IEventListener {

  IEventListener setParams(AbsTask task, Handler outHandler);

  /**
   * 预处理，有时有些地址链接比较慢，这时可以先在这个地方出来一些界面上的UI，如按钮的状态
   */
  void onPre();

  /**
   * 开始
   */
  void onStart(long startLocation);

  /**
   * 恢复位置
   */
  void onResume(long resumeLocation);

  /**
   * 下载监听
   */
  void onProgress(long currentLocation);

  /**
   * 停止
   */
  void onStop(long stopLocation);

  /**
   * 下载完成
   */
  void onComplete();

  /**
   * 取消下载
   */
  void onCancel();

  /**
   * 下载失败
   *
   * @param needRetry 是否需要重试{@code true} 需要
   * @param e 失败信息
   */
  void onFail(boolean needRetry, AriaException e);
}
