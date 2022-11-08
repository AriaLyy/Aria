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

import com.arialyy.aria.core.common.AbsBuilderTarget;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;

/**
 * Created by lyy on 2017/2/28.
 * http 单文件上传
 */
public class HttpBuilderTarget extends AbsBuilderTarget<HttpBuilderTarget> {
  private UNormalConfigHandler<HttpBuilderTarget> mConfigHandler;

  HttpBuilderTarget(String filePath) {
    mConfigHandler = new UNormalConfigHandler<>(this, -1);
    mConfigHandler.setFilePath(filePath);
    //http暂时不支持断点上传
    getTaskWrapper().setSupportBP(false);
    getTaskWrapper().setRequestType(ITaskWrapper.U_HTTP);
    ((UploadEntity) getEntity()).setTaskType(ITaskWrapper.U_HTTP);
    getTaskWrapper().setNewTask(true);
  }

  /**
   * 设置上传路径
   *
   * @param tempUrl 上传路径
   */
  public HttpBuilderTarget setUploadUrl(String tempUrl) {
    mConfigHandler.setTempUrl(tempUrl);
    return this;
  }

  /**
   * 设置http请求参数，header等信息
   */
  public HttpBuilderTarget option(HttpOption option) {

    if (option == null) {
      throw new NullPointerException("任务配置为空");
    }
    getTaskWrapper().getOptionParams().setParams(option);
    return this;
  }

  /**
   * 如果文件路径被其它任务占用，删除其它任务
   *
   * @deprecated 使用 {@link #ignoreFilePathOccupy()}
   */
  @Deprecated
  public HttpBuilderTarget forceUpload() {
    getTaskWrapper().setIgnoreFilePathOccupy(true);
    return this;
  }
}
