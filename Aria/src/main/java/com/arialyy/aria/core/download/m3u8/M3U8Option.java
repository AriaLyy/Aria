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

import android.text.TextUtils;
import com.arialyy.aria.core.common.BaseOption;
import com.arialyy.aria.core.processor.IBandWidthUrlConverter;
import com.arialyy.aria.core.processor.IKeyUrlConverter;
import com.arialyy.aria.core.processor.ITsMergeHandler;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.ComponentUtil;

/**
 * m3u8任务设置
 */
public class M3U8Option<OP extends M3U8Option> extends BaseOption {

  private boolean generateIndexFile = false;
  private boolean mergeFile = true;
  private int bandWidth;
  private ITsMergeHandler mergeHandler;
  private IBandWidthUrlConverter bandWidthUrlConverter;
  private IKeyUrlConverter keyUrlConverter;
  private boolean ignoreFailureTs = false;
  private String keyPath;
  private boolean useDefConvert = true;

  M3U8Option() {
    super();
    ComponentUtil.getInstance().checkComponentExist(ComponentUtil.COMPONENT_TYPE_M3U8);
  }

  /**
   * 设置使用默认的码率转换器和TS转换器，默认打开
   * @param useDefConvert true 使用默认的转换器，false 关闭默认的转换器
   */
  public OP setUseDefConvert(boolean useDefConvert) {
    this.useDefConvert = useDefConvert;
    ALog.d(TAG, "使用默认的码率转换器和TS转换器，如果无法下载，请参考：https://github.com/AriaLyy/Aria/issues/597 定制转换器");
    return (OP) this;
  }

  /**
   * 设置密钥文件的保存路径
   *
   * @param keyPath 密钥文件的保存路径
   */
  public OP setKeyPath(String keyPath) {
    if (TextUtils.isEmpty(keyPath)) {
      ALog.e(TAG, "密钥文件保存路径为空");
      return (OP) this;
    }
    this.keyPath = keyPath;
    return (OP) this;
  }

  /**
   * 忽略下载失败的ts切片，即使有失败的切片，下载完成后也要合并所有切片，并进入complete回调
   */
  public OP ignoreFailureTs() {
    this.ignoreFailureTs = true;
    return (OP) this;
  }

  /**
   * 生成m3u8索引文件
   * 注意：创建索引文件，{@link #merge(boolean)}方法设置与否都不再合并文件
   * 如果是直播文件下载，创建索引文件的操作将导致只能同时下载一个切片！！
   */
  public OP generateIndexFile() {
    this.generateIndexFile = true;
    return (OP) this;
  }

  /**
   * 下载完成后，将所有的切片合并为一个文件
   *
   * @param mergeFile {@code true}合并所有ts文件为一个
   */
  public OP merge(boolean mergeFile) {
    this.mergeFile = mergeFile;
    return (OP) this;
  }

  /**
   * 如果你希望使用自行处理ts文件的合并，可以使用{@link ITsMergeHandler}处理ts文件的合并
   * 需要注意的是：只有{@link #merge(boolean)}设置合并ts文件，该方法才会生效
   */
  public OP setMergeHandler(ITsMergeHandler mergeHandler) {
    CheckUtil.checkMemberClass(mergeHandler.getClass());
    this.mergeHandler = mergeHandler;
    return (OP) this;
  }

  /**
   * 选择需要下载的码率，默认下载的码率
   *
   * @param bandWidth 指定的码率
   */
  public OP setBandWidth(int bandWidth) {
    this.bandWidth = bandWidth;
    return (OP) this;
  }

  /**
   * M3U8 bandWidth 码率url转换器，对于某些服务器，返回的ts地址可以是相对地址，也可能是处理过的，
   * 对于这种情况，你需要使用url转换器将地址转换为可正常访问的http地址
   *
   * @param bandWidthUrlConverter {@link IBandWidthUrlConverter}
   */
  public OP setBandWidthUrlConverter(IBandWidthUrlConverter bandWidthUrlConverter) {
    CheckUtil.checkMemberClass(bandWidthUrlConverter.getClass());
    this.bandWidthUrlConverter = bandWidthUrlConverter;
    return (OP) this;
  }

  /**
   * M3U8 密钥url转换器，对于某些服务器，密钥的下载地址是被加密的，因此需要使用该方法将被加密的密钥解密成可被识别的http地址
   *
   * @param keyUrlConverter {@link IKeyUrlConverter}
   */
  public OP setKeyUrlConverter(IKeyUrlConverter keyUrlConverter) {
    CheckUtil.checkMemberClass(keyUrlConverter.getClass());
    this.keyUrlConverter = keyUrlConverter;
    return (OP) this;
  }
}
