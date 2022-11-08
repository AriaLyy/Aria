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
package com.arialyy.aria.sftp.upload;

import android.os.Handler;
import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.loader.AbsNormalTTBuilderAdapter;
import com.arialyy.aria.core.task.IThreadTaskAdapter;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.sftp.SFtpSessionManager;
import com.arialyy.aria.sftp.SFtpTaskOption;
import com.arialyy.aria.sftp.SFtpUtil;
import com.arialyy.aria.util.CommonUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.UnsupportedEncodingException;

final class SFtpUTTBuilderAdapter extends AbsNormalTTBuilderAdapter {
  private SFtpTaskOption option;

  SFtpUTTBuilderAdapter(UTaskWrapper wrapper) {
    option = (SFtpTaskOption) wrapper.getTaskOption();
  }

  @Override public IThreadTaskAdapter getAdapter(SubThreadConfig config) {
    return new SFtpUThreadTaskAdapter(config);
  }

  @Override
  protected SubThreadConfig getSubThreadConfig(Handler stateHandler, ThreadRecord threadRecord,
      boolean isBlock, int startNum) {
    SubThreadConfig config =
        super.getSubThreadConfig(stateHandler, threadRecord, isBlock, startNum);

    FtpUrlEntity entity = option.getUrlEntity();
    String key =
        CommonUtil.getStrMd5(entity.hostName + entity.port + entity.user + threadRecord.threadId);
    Session session = SFtpSessionManager.getInstance().getSession(key);
    if (session == null) {
      try {
        session = SFtpUtil.getInstance().getSession(entity, threadRecord.threadId);
      } catch (JSchException e) {
        e.printStackTrace();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    config.obj = session;

    return config;
  }

  @Override public boolean handleNewTask(TaskRecord record, int totalThreadNum) {

    return true;
  }
}
