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

/**
 * tcp任务配置
 *
 * @author aria
 * Date: 2019-09-06
 */
public class TcpTaskConfig {

  /**
   * 上传给tcp服务的初始数据，一般是文件名等信息
   */
  private String params;

  /**
   * 心跳包传输的数据
   */
  private String heartbeat;

  /**
   * 心跳间隔，单位毫秒，默认1s
   */
  private long heartbeatInterval = 1000;

  /**
   * 数据传输编码，默认"utf-8"
   */
  private String charset = "utf-8";

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public String getHeartbeat() {
    return heartbeat;
  }

  public void setHeartbeat(String heartbeat) {
    this.heartbeat = heartbeat;
  }

  public long getHeartbeatInterval() {
    return heartbeatInterval;
  }

  public void setHeartbeatInterval(long heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }
}
