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
package com.arialyy.compiler;

/**
 * Created by lyy on 2019/6/25.
 * 实体信息
 */
enum EntityInfo {
  NORMAL("com.arialyy.aria.core.common", "AbsNormalEntity"),
  DOWNLOAD("com.arialyy.aria.core.download", "DownloadEntity"),
  UPLOAD("com.arialyy.aria.core.upload", "UploadEntity");
  String pkg, className;

  public String getClassName() {
    return className;
  }

  public String getPkg() {
    return pkg;
  }

  /**
   * @param pkg 包名
   * @param className 对应到任务类名
   */
  EntityInfo(String pkg, String className) {
    this.pkg = pkg;
    this.className = className;
  }
}
