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

import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.sftp.AbsSFtpInfoTask;
import com.arialyy.aria.sftp.SFtpTaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.UnsupportedEncodingException;

final class SFtpUInfoTask extends AbsSFtpInfoTask<UTaskWrapper> {
  static final int ISCOMPLETE = 0xa1;

  SFtpUInfoTask(UTaskWrapper uTaskWrapper) {
    super(uTaskWrapper);
  }

  @Override protected void getFileInfo(Session session)
      throws JSchException, UnsupportedEncodingException, SftpException {
    SFtpTaskOption option = (SFtpTaskOption) getWrapper().getTaskOption();
    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
    channel.connect(1000);

    String remotePath = option.getUrlEntity().remotePath;
    String temp = CommonUtil.convertSFtpChar(getOption().getCharSet(), remotePath)
        + "/"
        + getWrapper().getEntity().getFileName();

    SftpATTRS attr = null;
    try {
      attr = channel.stat(temp);
    } catch (Exception e) {
      ALog.d(TAG, String.format("文件不存在，remotePath：%s", remotePath));
    }

    boolean isComplete = false;
    UploadEntity entity = getWrapper().getEntity();
    if (attr != null && attr.getSize() == entity.getFileSize()) {
      isComplete = true;
    }

    CompleteInfo info = new CompleteInfo();
    info.code = isComplete ? ISCOMPLETE : 200;
    info.obj = attr;
    channel.disconnect();
    onSucceed(info);
  }
}
