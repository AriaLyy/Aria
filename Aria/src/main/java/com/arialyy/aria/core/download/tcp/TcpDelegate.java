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
package com.arialyy.aria.core.download.tcp;

import android.text.TextUtils;
import com.arialyy.aria.core.common.BaseOption;
import com.arialyy.aria.util.ALog;
import java.nio.charset.Charset;

/**
 * @author aria
 * Date: 2019-09-06
 */
public class TcpDelegate extends BaseOption {

  private String params;
  private String heartbeatInfo;
  private long heartbeat;
  private String charset;

  public TcpDelegate() {
    super();
  }

  /**
   * 上传给tcp服务的初始数据，一般是文件名、文件路径等信息
   */
  public TcpDelegate setParam(String params) {
    if (TextUtils.isEmpty(params)) {
      ALog.w(TAG, "tcp传输的数据不能为空");
      return this;
    }
    this.params = params;
    return this;
  }

  /**
   * 设置心跳包传输的数据
   *
   * @param heartbeatInfo 心跳包数据
   */
  public TcpDelegate setHeartbeatInfo(String heartbeatInfo) {
    if (TextUtils.isEmpty(heartbeatInfo)) {
      ALog.w(TAG, "心跳包传输的数据不能为空");
      return this;
    }
    this.heartbeatInfo = heartbeatInfo;
    return this;
  }

  /**
   * 心跳间隔，默认1s
   *
   * @param heartbeat 单位毫秒
   */
  public TcpDelegate setHeartbeatInterval(long heartbeat) {
    if (heartbeat <= 0) {
      ALog.w(TAG, "心跳间隔不能小于1毫秒");
      return this;
    }
    this.heartbeat = heartbeat;
    return this;
  }

  /**
   * 数据传输编码，默认"utf-8"
   */
  public TcpDelegate setCharset(String charset) {
    if (!Charset.isSupported(charset)) {
      ALog.w(TAG, "不支持的编码");
      return this;
    }

    this.charset = charset;
    return this;
  }
}
