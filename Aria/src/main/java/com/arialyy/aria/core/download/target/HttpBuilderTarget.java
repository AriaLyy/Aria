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

import com.arialyy.aria.core.common.AbsBuilderTarget;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.m3u8.M3U8LiveOption;
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;

public class HttpBuilderTarget extends AbsBuilderTarget<HttpBuilderTarget> {

  private DNormalConfigHandler<HttpBuilderTarget> mConfigHandler;

  HttpBuilderTarget(String url) {
    mConfigHandler = new DNormalConfigHandler<>(this, -1);
    mConfigHandler.setUrl(url);
    getTaskWrapper().setRequestType(ITaskWrapper.D_HTTP);
    getTaskWrapper().setNewTask(true);
    ((DownloadEntity)getEntity()).setTaskType(ITaskWrapper.D_HTTP);
  }

  public HttpBuilderTarget m3u8VodOption(M3U8VodOption m3U8VodOption) {
    if (m3U8VodOption == null) {
      throw new NullPointerException("m3u8任务设置为空");
    }
    getTaskWrapper().setRequestType(AbsTaskWrapper.M3U8_VOD);
    getTaskWrapper().getEntity().setFileSize(m3U8VodOption.getFileSize());
    ((DTaskWrapper) getTaskWrapper()).getM3U8Params().setParams(m3U8VodOption);
    return this;
  }

  public HttpBuilderTarget m3u8LiveOption(M3U8LiveOption m3U8LiveOption) {
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
  public HttpBuilderTarget option(HttpOption option) {
    if (option == null) {
      throw new NullPointerException("任务配置为空");
    }
    getTaskWrapper().getOptionParams().setParams(option);
    return this;
  }

  /**
   * 设置文件存储路径，如果需要修改新的文件名，修改路径便可。
   * 如：原文件路径 /mnt/sdcard/test.zip
   * 如果需要将test.zip改为game.zip，只需要重新设置文件路径为：/mnt/sdcard/game.zip
   *
   * @param filePath 路径必须为文件路径，不能为文件夹路径
   */
  public HttpBuilderTarget setFilePath(String filePath) {
    mConfigHandler.setTempFilePath(filePath);
    return this;
  }

  /**
   * 设置文件存储路径，如果需要修改新的文件名，修改路径便可。
   * 如：原文件路径 /mnt/sdcard/test.zip
   * 如果需要将test.zip改为game.zip，只需要重新设置文件路径为：/mnt/sdcard/game.zip
   *
   * @param filePath 路径必须为文件路径，不能为文件夹路径
   * @param forceDownload {@code true}强制下载，不考虑文件路径是否被占用
   * @deprecated 使用 {@link #ignoreFilePathOccupy()}
   */
  @Deprecated
  public HttpBuilderTarget setFilePath(String filePath, boolean forceDownload) {
    mConfigHandler.setTempFilePath(filePath);
    mConfigHandler.setForceDownload(forceDownload);
    return this;
  }
}
