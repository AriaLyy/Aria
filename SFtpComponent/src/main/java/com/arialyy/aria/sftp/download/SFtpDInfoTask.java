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
package com.arialyy.aria.sftp.download;

import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.exception.AriaSFTPException;
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

/**
 * 进行登录，获取session，获取文件信息
 */
final class SFtpDInfoTask extends AbsSFtpInfoTask<DTaskWrapper> {

  SFtpDInfoTask(DTaskWrapper wrapper) {
    super(wrapper);
  }

  @Override protected void getFileInfo(Session session) throws JSchException,
      UnsupportedEncodingException {
    SFtpTaskOption option = (SFtpTaskOption) getWrapper().getTaskOption();
    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
    channel.connect(1000);

    //channel.setFilenameEncoding(option.getCharSet());
    //channel.setFilenameEncoding("gbk");

    String remotePath = option.getUrlEntity().remotePath;
    String temp = CommonUtil.convertSFtpChar(option.getCharSet(), remotePath);
    SftpATTRS attr = null;
    try {
      attr = channel.stat(temp);
    } catch (Exception e) {
      ALog.e(TAG, String.format("文件不存在，remotePath：%s", remotePath));
    }

    if (attr != null) {
      getWrapper().getEntity().setFileSize(attr.getSize());
      CompleteInfo info = new CompleteInfo();
      info.code = 200;
      onSucceed(info);
    } else {
      handleFail(new AriaSFTPException(String.format("文件不存在，remotePath：%s", remotePath)), false);
    }
    channel.disconnect();
  }
}
