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
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;

/**
 * @author aria
 * Date: 2019-09-06
 */
public class TcpBuilderTarget extends AbsBuilderTarget<TcpBuilderTarget> {

  private DNormalConfigHandler mConfigHandler;

  TcpBuilderTarget(String ip, int port) {
    mConfigHandler = new DNormalConfigHandler<>(this, -1);
    getTaskWrapper().setRequestType(ITaskWrapper.D_TCP);
    ((DownloadEntity) getEntity()).setTaskType(ITaskWrapper.D_TCP);
    getTaskWrapper().setNewTask(true);
  }
  //
  ///**
  // * 设置tcp相应信息
  // */
  //@CheckResult(suggest = Suggest.TASK_CONTROLLER)
  //public TcpBuilderTarget option(T) {
  //  return new TcpDelegate<>(this, getTaskWrapper());
  //}

  /**
   * 设置文件存储路径，如果需要修改新的文件名，修改路径便可。
   * 如：原文件路径 /mnt/sdcard/test.zip
   * 如果需要将test.zip改为game.zip，只需要重新设置文件路径为：/mnt/sdcard/game.zip
   *
   * @param filePath 路径必须为文件路径，不能为文件夹路径
   */
  public TcpBuilderTarget setFilePath(String filePath) {
    mConfigHandler.setTempFilePath(filePath);
    return this;
  }
}
