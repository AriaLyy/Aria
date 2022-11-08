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
package com.arialyy.aria.core.upload.target;

import com.arialyy.aria.core.common.AbsNormalTarget;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;

/**
 * Created by lyy on 2017/2/28.
 * http 单文件上传
 */
public class HttpNormalTarget extends AbsNormalTarget<HttpNormalTarget> {
  private UNormalConfigHandler<HttpNormalTarget> mConfigHandler;

  HttpNormalTarget(long taskId) {
    mConfigHandler = new UNormalConfigHandler<>(this, taskId);
    getTaskWrapper().setSupportBP(false);
    getTaskWrapper().setRequestType(AbsTaskWrapper.U_HTTP);
    getTaskWrapper().setNewTask(false);
  }

  /**
   * 设置上传路径
   *
   * @param tempUrl 上传路径
   */
  public HttpNormalTarget setUploadUrl(String tempUrl) {
    mConfigHandler.setTempUrl(tempUrl);
    return this;
  }

  /**
   * 设置http请求参数，header等信息
   */
  public HttpNormalTarget option(HttpOption option) {
    if (option == null) {
      throw new NullPointerException("任务配置为空");
    }
    getTaskWrapper().getOptionParams().setParams(option);
    return this;
  }

  @Override public void resume() {
    ALog.e(TAG, "http上传任务没有恢复功能");
  }

  @Override public boolean isRunning() {
    return mConfigHandler.isRunning();
  }

  @Override public boolean taskExists() {
    return mConfigHandler.taskExists();
  }
}
