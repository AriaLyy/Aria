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

import com.arialyy.aria.core.common.AbsNormalTarget;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.m3u8.M3U8LiveOption;
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

/**
 * Created by lyy on 2016/12/5.
 * https://github.com/AriaLyy/Aria
 */
public class HttpNormalTarget extends AbsNormalTarget<HttpNormalTarget> {
  private DNormalConfigHandler<HttpNormalTarget> mConfigHandler;

  HttpNormalTarget(long taskId) {
    mConfigHandler = new DNormalConfigHandler<>(this, taskId);
    getTaskWrapper().setRequestType(getTaskWrapper().getEntity().getTaskType());
    getTaskWrapper().setNewTask(false);
  }

  public M3U8NormalTarget m3u8VodOption(M3U8VodOption m3U8VodOption) {
    if (m3U8VodOption == null) {
      throw new NullPointerException("m3u8任务设置为空");
    }
    getTaskWrapper().setRequestType(AbsTaskWrapper.M3U8_VOD);
    getTaskWrapper().getEntity().setFileSize(m3U8VodOption.getFileSize());
    ((DTaskWrapper) getTaskWrapper()).getM3U8Params().setParams(m3U8VodOption);
    return new M3U8NormalTarget((DTaskWrapper) getTaskWrapper());
  }

  public M3U8NormalTarget m3u8VodOption() {
    return new M3U8NormalTarget((DTaskWrapper) getTaskWrapper());
  }

  public HttpNormalTarget m3u8LiveOption(M3U8LiveOption m3U8LiveOption) {
    if (m3U8LiveOption == null) {
      throw new NullPointerException("m3u8任务设置为空");
    }
    getTaskWrapper().setRequestType(AbsTaskWrapper.M3U8_LIVE);
    ((DTaskWrapper) getTaskWrapper()).getM3U8Params().setParams(m3U8LiveOption);
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

  /**
   * 更新文件保存路径
   * 如：原文件路径 /mnt/sdcard/test.zip
   * 如果需要将test.zip改为game.zip，只需要重新设置文件路径为：/mnt/sdcard/game.zip
   */
  public HttpNormalTarget modifyFilePath(String filePath) {
    mConfigHandler.setTempFilePath(filePath);
    return this;
  }

  /**
   * 从header中获取文件描述信息
   */
  public String getContentDisposition() {
    return getEntity().getDisposition();
  }

  /**
   * 更新下载地址
   */
  public HttpNormalTarget updateUrl(String newUrl) {
    return mConfigHandler.updateUrl(newUrl);
  }

  @Override public DownloadEntity getEntity() {
    return (DownloadEntity) super.getEntity();
  }

  @Override public boolean isRunning() {
    return mConfigHandler.isRunning();
  }

  @Override public boolean taskExists() {
    return mConfigHandler.taskExists();
  }
}
