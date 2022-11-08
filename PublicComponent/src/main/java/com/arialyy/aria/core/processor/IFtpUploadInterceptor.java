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
package com.arialyy.aria.core.processor;

import com.arialyy.aria.core.inf.IEventHandler;
import com.arialyy.aria.core.upload.UploadEntity;
import java.util.List;

/**
 * FTP文件上传拦截器，如果远端已有同名文件，可使用该拦截器控制覆盖文件或修改该文件上传到服务器端端文件名
 */
public interface IFtpUploadInterceptor extends IEventHandler {

  /**
   * 处理拦截事件
   *
   * @param entity 上传信息实体
   * @param fileList ftp服务器端remotePath下的文件列表
   */
  FtpInterceptHandler onIntercept(UploadEntity entity, List<String> fileList);
}
