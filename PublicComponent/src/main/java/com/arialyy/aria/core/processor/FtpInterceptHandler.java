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

import android.text.TextUtils;

/**
 * Ftp上传拦截器处理，只针对新任务有效
 *
 * 如果使用者同时实现{@link Builder#resetFileName(String)}和{@link Builder#coverServerFile}，
 * 将默认使用{@link Builder#coverServerFile}
 */
public class FtpInterceptHandler {

  private boolean coverServerFile;

  private String newFileName;

  private FtpInterceptHandler() {
  }

  public boolean isCoverServerFile() {
    return coverServerFile;
  }

  public String getNewFileName() {
    return newFileName;
  }

  public static final class Builder {
    private boolean coverServerFile = false;

    private String newFileName;

    private boolean stopUpload = false;

    /**
     * 如果ftp服务器端已经有同名文件，控制是否覆盖远端的同名文件；
     * 如果你不希望覆盖远端文件，可以使用{@link #resetFileName(String)}
     *
     * @return {@code true} 如果ftp服务器端已经有同名文件，覆盖服务器端的同名文件
     */
    public Builder coverServerFile() {
      coverServerFile = true;
      return this;
    }

    /**
     * 如果ftp服务器端已经有同名文件，修改该文件上传到远端的文件名，该操作不会修改本地文件名
     * 如果你希望覆盖远端的同名文件，可以使用{@link #coverServerFile()}
     */
    public Builder resetFileName(String newFileName) {
      this.newFileName = newFileName;
      return this;
    }

    /**
     * 如果你希望停止上传任务，可以调用该方法
     */
    public Builder stopUpload() {
      stopUpload = true;
      return this;
    }

    /**
     * 如果使用者同时实现{@link Builder#resetFileName(String)}和{@link Builder#coverServerFile}，
     * 将默认使用{@link Builder#coverServerFile}
     */
    public FtpInterceptHandler build() {
      FtpInterceptHandler handler = new FtpInterceptHandler();
      if (coverServerFile) {
        handler.coverServerFile = true;
      } else if (!TextUtils.isEmpty(newFileName)) {
        handler.newFileName = newFileName;
      }
      return handler;
    }
  }
}