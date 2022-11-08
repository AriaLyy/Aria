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
package com.arialyy.aria.core.download.m3u8;

import com.arialyy.aria.core.processor.IVodTsUrlConverter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;

/**
 * m3u8点播文件参数设置
 */
public class M3U8VodOption extends M3U8Option<M3U8VodOption> {
  private long fileSize;
  private int maxTsQueueNum;
  private int jumpIndex;
  private IVodTsUrlConverter vodUrlConverter;

  public M3U8VodOption() {
    super();
  }

  /**
   * M3U8 ts 文件url转换器，对于某些服务器，返回的ts地址可以是相对地址，也可能是处理过的
   * 对于这种情况，你需要使用url转换器将地址转换为可正常访问的http地址
   *
   * @param vodUrlConverter {@link IVodTsUrlConverter}
   */
  public M3U8VodOption setVodTsUrlConvert(IVodTsUrlConverter vodUrlConverter) {
    CheckUtil.checkMemberClass(vodUrlConverter.getClass());
    this.vodUrlConverter = vodUrlConverter;
    return this;
  }

  /**
   * 由于m3u8协议的特殊性质，无法有效快速获取到正确到文件长度，如果你需要显示文件中长度，你需要自行设置文件长度
   *
   * @param fileSize 文件长度
   */
  public M3U8VodOption setFileSize(long fileSize) {
    if (fileSize <= 0) {
      ALog.e(TAG, "文件长度错误");
      return this;
    }
    this.fileSize = fileSize;
    return this;
  }

  /**
   * 默认情况下，对于同一点播文件的下载，最多同时下载4个ts分片，如果你希望增加或减少同时下载的ts分片数量，可以使用该方法设置同时下载的ts分片数量
   *
   * @param maxTsQueueNum 同时下载的ts分片数量
   */
  public M3U8VodOption setMaxTsQueueNum(int maxTsQueueNum) {
    if (maxTsQueueNum < 1) {
      ALog.e(TAG, "同时下载的分片数量不能小于1");
      return this;
    }

    this.maxTsQueueNum = maxTsQueueNum;
    return this;
  }

  /**
   * 启动任务时初始化索引位置
   *
   * 优先下载指定索引后的切片
   * 如果指定的切片索引大于切片总数，则此操作无效
   * 如果指定的切片索引小于当前正在下载的切片索引，并且指定索引和当前索引区间内有未下载的切片，则优先下载该区间的切片；否则此操作无效
   * 如果指定索引后的切片已经全部下载完成，但是索引前有未下载的切片，间会自动下载未下载的切片
   *
   * @param jumpIndex 指定的切片位置
   */
  public M3U8VodOption setPeerIndex(int jumpIndex) {
    if (jumpIndex < 1) {
      ALog.e(TAG, "切片索引不能小于1");
      return this;
    }
    this.jumpIndex = jumpIndex;
    return this;
  }

  public long getFileSize() {
    return fileSize;
  }

  public int getJumpIndex() {
    return jumpIndex;
  }
}
