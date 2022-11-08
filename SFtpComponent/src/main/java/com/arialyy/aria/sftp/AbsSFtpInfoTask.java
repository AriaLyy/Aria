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
package com.arialyy.aria.sftp;

import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.exception.AriaSFTPException;
import com.arialyy.aria.util.CommonUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.UnsupportedEncodingException;

/**
 * 进行登录，获取session，获取文件信息
 */
public abstract class AbsSFtpInfoTask<WP extends AbsTaskWrapper> implements IInfoTask {
  protected String TAG = CommonUtil.getClassName(this);
  private Callback callback;
  private WP wrapper;
  private SFtpTaskOption option;
  private boolean isStop = false, isCancel = false;

  public AbsSFtpInfoTask(WP wp) {
    this.wrapper = wp;
    this.option = (SFtpTaskOption) wrapper.getTaskOption();
  }

  protected abstract void getFileInfo(Session session)
      throws JSchException, UnsupportedEncodingException, SftpException;

  @Override public void stop() {
    isStop = true;
  }

  @Override public void cancel() {
    isCancel = true;
  }

  protected void handleFail(AriaException e, boolean needRetry) {
    if (isStop || isCancel){
      return;
    }
    callback.onFail(getWrapper().getEntity(), e, needRetry);
  }

  protected void onSucceed(CompleteInfo info){
    if (isStop || isCancel){
      return;
    }
    callback.onSucceed(getWrapper().getKey(), info);
  }

  @Override public void run() {
    try {
      FtpUrlEntity entity = option.getUrlEntity();
      String key = CommonUtil.getStrMd5(entity.hostName + entity.port + entity.user + 0);
      Session session = SFtpSessionManager.getInstance().getSession(key);
      if (session == null) {
        session = SFtpUtil.getInstance().getSession(entity, 0);
      }
      getFileInfo(session);
    } catch (JSchException e) {
      fail(new AriaSFTPException("jsch错误", e), false);
    } catch (UnsupportedEncodingException e) {
      fail(new AriaSFTPException("字符编码错误", e), false);
    } catch (SftpException e) {
      fail(new AriaSFTPException("sftp错误，错误类型：" + e.id, e), false);
    }
  }

  protected SFtpTaskOption getOption() {
    return option;
  }

  protected WP getWrapper() {
    return wrapper;
  }

  protected void fail(AriaException e, boolean needRetry) {
    callback.onFail(getWrapper().getEntity(), e, needRetry);
  }

  @Override public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }
}
