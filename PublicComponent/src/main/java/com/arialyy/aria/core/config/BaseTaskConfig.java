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

package com.arialyy.aria.core.config;

import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.io.Serializable;

/**
 * 通用任务配置
 */
public abstract class BaseTaskConfig extends BaseConfig implements Serializable {
  protected String TAG = CommonUtil.getClassName(getClass());

  /**
   * 设置写文件buff大小，该数值大小不能小于2048，数值变小，下载速度会变慢
   */
  int buffSize = 8192;

  /**
   * 进度刷新间隔，默认1秒
   */
  long updateInterval = 1000;

  /**
   * 旧任务数
   */
  public int oldMaxTaskNum = 2;

  /**
   * 任务队列最大任务数， 默认为2
   */
  int maxTaskNum = 2;
  /**
   * 下载失败，重试次数，默认为10
   */
  int reTryNum = 10;
  /**
   * 设置重试间隔，单位为毫秒，默认2000毫秒
   */
  int reTryInterval = 2000;
  /**
   * 设置url连接超时时间，单位为毫秒，默认5000毫秒
   */
  int connectTimeOut = 5000;

  /**
   * 是否需要转换速度单位，转换完成后为：1b/s、1k/s、1m/s、1g/s、1t/s，如果不需要将返回byte长度
   */
  boolean isConvertSpeed = false;

  /**
   * 执行队列类型
   */
  String queueMod = "wait";

  /**
   * 设置IO流读取时间，单位为毫秒，默认20000毫秒，该时间不能少于10000毫秒
   */
  int iOTimeOut = 20 * 1000;

  /**
   * 设置最大下载/上传速度，单位：kb, 为0表示不限速
   */
  int maxSpeed = 0;

  /**
   * 设置https ca 证书信息；path 为assets目录下的CA证书完整路径
   */
  String caPath;
  /**
   * name 为CA证书名
   */
  String caName;

  public String getCaPath() {
    return caPath;
  }

  public BaseConfig setCaPath(String caPath) {
    this.caPath = caPath;
    save();
    return this;
  }

  public String getCaName() {
    return caName;
  }

  public BaseConfig setCaName(String caName) {
    this.caName = caName;
    save();
    return this;
  }

  public BaseTaskConfig setMaxTaskNum(int maxTaskNum) {
    oldMaxTaskNum = this.maxTaskNum;
    this.maxTaskNum = maxTaskNum;
    save();
    return this;
  }

  public int getMaxSpeed() {
    return maxSpeed;
  }

  public BaseTaskConfig setMaxSpeed(int maxSpeed) {
    this.maxSpeed = maxSpeed;
    save();
    return this;
  }

  public long getUpdateInterval() {
    return updateInterval;
  }

  /**
   * 设置进度更新间隔，该设置对正在运行的任务无效，默认为1000毫秒
   *
   * @param updateInterval 不能小于0
   */
  public BaseTaskConfig setUpdateInterval(long updateInterval) {
    if (updateInterval <= 0) {
      ALog.w("Configuration", "进度更新间隔不能小于0");
      return this;
    }
    this.updateInterval = updateInterval;
    save();
    return this;
  }

  public String getQueueMod() {
    return queueMod;
  }

  public BaseTaskConfig setQueueMod(String queueMod) {
    this.queueMod = queueMod;
    save();
    return this;
  }

  public int getMaxTaskNum() {
    return maxTaskNum;
  }

  public int getReTryNum() {
    return reTryNum;
  }

  public BaseTaskConfig setReTryNum(int reTryNum) {
    this.reTryNum = reTryNum;
    save();
    return this;
  }

  public int getReTryInterval() {
    return reTryInterval;
  }

  public BaseTaskConfig setReTryInterval(int reTryInterval) {
    this.reTryInterval = reTryInterval;
    save();
    return this;
  }

  public boolean isConvertSpeed() {
    return isConvertSpeed;
  }

  public BaseTaskConfig setConvertSpeed(boolean convertSpeed) {
    isConvertSpeed = convertSpeed;
    save();
    return this;
  }

  public int getConnectTimeOut() {
    return connectTimeOut;
  }

  public BaseTaskConfig setConnectTimeOut(int connectTimeOut) {
    this.connectTimeOut = connectTimeOut;
    save();
    return this;
  }

  public int getIOTimeOut() {
    return iOTimeOut;
  }

  public BaseTaskConfig setIOTimeOut(int iOTimeOut) {
    this.iOTimeOut = iOTimeOut;
    save();
    return this;
  }

  public int getBuffSize() {
    return buffSize;
  }

  public BaseTaskConfig setBuffSize(int buffSize) {
    this.buffSize = buffSize;
    save();
    return this;
  }
}