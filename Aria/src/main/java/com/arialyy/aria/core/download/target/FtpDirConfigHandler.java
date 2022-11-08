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
package com.arialyy.aria.core.download.target;

import com.arialyy.aria.core.common.FtpOption;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.CommonUtil;
import java.util.List;

/**
 * Created by lyy on 2017/4/9.
 * ftp文件夹下载功能代理
 */
class FtpDirConfigHandler<TARGET extends AbsTarget> extends AbsGroupConfigHandler<TARGET> {

  FtpDirConfigHandler(TARGET target, long taskId) {
    super(target, taskId);
    init();
  }

  private void init() {
    getTaskWrapper().setRequestType(ITaskWrapper.D_FTP_DIR);
    List<DTaskWrapper> wrappers = getTaskWrapper().getSubTaskWrapper();
    if (!wrappers.isEmpty()) {
      for (DTaskWrapper subWrapper : wrappers) {
        subWrapper.setRequestType(ITaskWrapper.D_FTP);
        subWrapper.getEntity().setTaskType(ITaskWrapper.D_FTP);
      }
    }
  }
}
