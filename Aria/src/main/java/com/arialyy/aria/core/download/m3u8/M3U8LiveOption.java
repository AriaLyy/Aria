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

import com.arialyy.aria.core.processor.ILiveTsUrlConverter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;

/**
 * m3u8直播参数设置
 */
public class M3U8LiveOption extends M3U8Option<M3U8LiveOption> {

  private ILiveTsUrlConverter liveTsUrlConverter;
  private long liveUpdateInterval;

  public M3U8LiveOption() {
    super();
  }

  /**
   * M3U8 ts 文件url转换器，对于某些服务器，返回的ts地址可以是相对地址，也可能是处理过的
   * 对于这种情况，你需要使用url转换器将地址转换为可正常访问的http地址
   *
   * @param liveTsUrlConverter {@link ILiveTsUrlConverter}
   */
  public M3U8LiveOption setLiveTsUrlConvert(ILiveTsUrlConverter liveTsUrlConverter) {
    CheckUtil.checkMemberClass(liveTsUrlConverter.getClass());
    this.liveTsUrlConverter = liveTsUrlConverter;
    return this;
  }

  /**
   * 设置直播的m3u8文件更新间隔，默认10000微秒。
   *
   * @param liveUpdateInterval 更新间隔，单位微秒
   */
  public M3U8LiveOption setM3U8FileUpdateInterval(long liveUpdateInterval) {
    if (liveUpdateInterval <= 1) {
      ALog.e(TAG, "间隔时间错误");
      return this;
    }

    this.liveUpdateInterval = liveUpdateInterval;
    return this;
  }
}
