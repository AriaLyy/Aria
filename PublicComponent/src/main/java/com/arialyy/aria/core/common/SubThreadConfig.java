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
package com.arialyy.aria.core.common;

import android.os.Handler;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import java.io.File;

/**
 * 子线程下载信息类
 */
public class SubThreadConfig {
  public static final int TYPE_HTTP = 1;
  public static final int TYPE_FTP = 2;
  public static final int TYPE_M3U8_PEER = 3;
  public static final int TYPE_HTTP_DG_SUB = 4;
  public static final int TYPE_FTP_DG_SUB = 5;

  public AbsTaskWrapper taskWrapper;
  public boolean isBlock = false;
  // 启动的线程
  public int startThreadNum;
  // 真正的下载地址，如果是30x，则是30x后的地址
  public String url;
  public File tempFile;
  // 线程记录
  public ThreadRecord record;
  // 状态处理器
  public Handler stateHandler;
  // m3u8切片索引
  public int peerIndex;
  // 线程任务类型
  public int threadType = TYPE_HTTP;
  // 进度更新间隔，单位：毫秒
  public long updateInterval = 1000;
  // 扩展数据
  public Object obj;
  // 忽略失败
  public boolean ignoreFailure = false;

  /**
   * 转换线程任务类型
   *
   * @param requestType {@link AbsTaskWrapper#getRequestType()}
   * @return {@link #threadType}
   */
  public static int getThreadType(int requestType) {
    int threadType = SubThreadConfig.TYPE_HTTP;
    switch (requestType) {
      case ITaskWrapper.D_HTTP:
      case ITaskWrapper.U_HTTP:
        threadType = SubThreadConfig.TYPE_HTTP;
        break;
      case ITaskWrapper.D_FTP:
      case ITaskWrapper.U_FTP:
        threadType = SubThreadConfig.TYPE_FTP;
        break;
      case ITaskWrapper.D_FTP_DIR:
        threadType = SubThreadConfig.TYPE_FTP_DG_SUB;
        break;
      case ITaskWrapper.DG_HTTP:
        threadType = SubThreadConfig.TYPE_HTTP_DG_SUB;
        break;
      case ITaskWrapper.M3U8_LIVE:
      case ITaskWrapper.M3U8_VOD:
        threadType = SubThreadConfig.TYPE_M3U8_PEER;
        break;
    }
    return threadType;
  }

  /**
   * 根据配置肚脐更新间隔
   *
   * @param requestType {@link AbsTaskWrapper#getRequestType()}
   * @return {@link #updateInterval}
   */
  public static long getUpdateInterval(int requestType) {
    long updateInterval = 1000;
    switch (requestType) {
      case ITaskWrapper.D_HTTP:
      case ITaskWrapper.D_FTP:
      case ITaskWrapper.M3U8_LIVE:
      case ITaskWrapper.M3U8_VOD:
        updateInterval = AriaConfig.getInstance().getDConfig().getUpdateInterval();
        break;
      case ITaskWrapper.D_FTP_DIR:
      case ITaskWrapper.DG_HTTP:
        updateInterval = AriaConfig.getInstance().getDGConfig().getUpdateInterval();
        break;
      case ITaskWrapper.U_HTTP:
      case ITaskWrapper.U_FTP:
        updateInterval = AriaConfig.getInstance().getUConfig().getUpdateInterval();
    }
    return updateInterval;
  }
}