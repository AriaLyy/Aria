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
package com.arialyy.aria.core.wrapper;

import com.arialyy.aria.core.inf.IEntity;

/**
 * 组合任务实体包裹器，用于加载和任务相关的参数
 */
public interface ITaskWrapper {

  int ERROR = 0;

  /**
   * HTTP单任务载
   */
  int D_HTTP = 1;

  /**
   * HTTP任务组下载
   */
  int DG_HTTP = 2;

  /**
   * FTP单文件下载
   */
  int D_FTP = 3;

  /**
   * FTP文件夹下载，为避免登录过多，子任务由单线程进行处理
   */
  int D_FTP_DIR = 4;

  /**
   * HTTP单文件上传
   */
  int U_HTTP = 5;

  /**
   * FTP单文件上传
   */
  int U_FTP = 6;

  /**
   * M3u8点播
   */
  int M3U8_VOD = 7;

  /**
   * m3u8直播
   */
  int M3U8_LIVE = 8;

  /**
   * TCP 文件下载
   */
  int D_TCP = 9;

  /**
   * TCP 文件上传
   */
  int U_TCP = 10;

  /**
   * TCP 分片上传
   */
  int U_TCP_PEER = 11;

  /**
   * SFTP 下载
   */
  int D_SFTP = 12;

  /**
   * SFTP 上传
   */
  int U_SFTP = 13;

  /**
   * 获取任务类型
   *
   * @return {@link ITaskWrapper}
   */
  int getRequestType();

  /**
   * 获取任务实体
   */
  IEntity getEntity();

  /**
   * 获取任务唯一标志：
   * 下载任务，对应文件的下载地址
   * 上传任务，对应需要上传的文件路径
   * 组合任务，对应组合任务的groupHash
   */
  String getKey();
}
