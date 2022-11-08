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
package com.arialyy.aria.core.upload;

import com.arialyy.aria.core.config.Configuration;
import com.arialyy.aria.core.config.UploadConfig;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

/**
 * Created by lyy on 2017/2/9. 上传任务实体
 */
public class UTaskWrapper extends AbsTaskWrapper<UploadEntity> {

  /**
   * 保存临时设置的上传路径
   */
  private String tempUrl;

  public UTaskWrapper(UploadEntity entity) {
    super(entity);
  }

  public String getTempUrl() {
    return tempUrl;
  }

  public void setTempUrl(String tempUrl) {
    this.tempUrl = tempUrl;
  }

  /**
   * 文件保存路径
   */
  @Override public String getKey() {
    return getEntity().getKey();
  }

  @Override public UploadConfig getConfig() {
    return Configuration.getInstance().uploadCfg;
  }
}
