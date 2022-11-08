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
package com.arialyy.annotations;

/**
 * Created by lyy on 2017/7/10.
 * 任务类型枚举
 */
public enum TaskEnum {
  DOWNLOAD("com.arialyy.aria.core.task", "DownloadTask", "$$DownloadListenerProxy",
      "AptNormalTaskListener"),
  DOWNLOAD_GROUP("com.arialyy.aria.core.task", "DownloadGroupTask",
      "$$DownloadGroupListenerProxy", "AptNormalTaskListener"),
  DOWNLOAD_GROUP_SUB("com.arialyy.aria.core.task", "DownloadGroupTask",
      "$$DGSubListenerProxy", "AptSubTaskListener"),
  UPLOAD("com.arialyy.aria.core.task", "UploadTask", "$$UploadListenerProxy",
      "AptNormalTaskListener"),
  M3U8_PEER("com.arialyy.aria.core.task", "DownloadTask", "$$M3U8PeerListenerProxy",
      "AptM3U8PeerTaskListener");

  public String pkg, className, proxySuffix, proxySuperClass;

  /**
   * @param pkg 包名
   * @param className 对应到任务类名
   * @param proxySuffix 事件代理后缀
   * @param proxySuperClass 代理类的父类
   */
  TaskEnum(String pkg, String className, String proxySuffix, String proxySuperClass) {
    this.pkg = pkg;
    this.className = className;
    this.proxySuffix = proxySuffix;
    this.proxySuperClass = proxySuperClass;
  }
}
