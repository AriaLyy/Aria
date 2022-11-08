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
import com.arialyy.aria.util.AriaCrashHandler;
import java.io.Serializable;

/**
 * 应用配置
 */
public class AppConfig extends BaseConfig implements Serializable {
  /**
   * 是否使用{@link AriaCrashHandler}来捕获异常 {@code true} 使用；{@code false} 不使用
   */
  boolean useAriaCrashHandler;

  /**
   * 设置Aria的日志级别
   *
   * {@link ALog#LOG_LEVEL_VERBOSE}
   */
  int logLevel;

  /**
   * 是否检查网络，{@code true}检查网络
   */
  boolean netCheck = true;

  /**
   * 是否使用广播 除非无法使用注解，否则不建议使用广播来接受任务 {@code true} 使用广播，{@code false} 不适用广播
   */
  boolean useBroadcast = false;

  /**
   * 断网的时候是否重试，{@code true}断网也重试；{@code false}断网不重试，直接走失败的回调
   */
  boolean notNetRetry = false;

  public boolean isNotNetRetry() {
    return notNetRetry;
  }

  public AppConfig setNotNetRetry(boolean notNetRetry) {
    this.notNetRetry = notNetRetry;
    save();
    return this;
  }

  public boolean isUseBroadcast() {
    return useBroadcast;
  }

  public AppConfig setUseBroadcast(boolean useBroadcast) {
    this.useBroadcast = useBroadcast;
    save();
    return this;
  }

  public boolean isNetCheck() {
    return netCheck;
  }

  public AppConfig setNetCheck(boolean netCheck) {
    this.netCheck = netCheck;
    save();
    return this;
  }

  public AppConfig setLogLevel(int level) {
    this.logLevel = level;
    ALog.LOG_LEVEL = level;
    save();
    return this;
  }

  public int getLogLevel() {
    return logLevel;
  }

  public boolean getUseAriaCrashHandler() {
    return useAriaCrashHandler;
  }

  public AppConfig setUseAriaCrashHandler(boolean useAriaCrashHandler) {
    this.useAriaCrashHandler = useAriaCrashHandler;
    if (useAriaCrashHandler) {
      Thread.setDefaultUncaughtExceptionHandler(new AriaCrashHandler());
    } else {
      Thread.setDefaultUncaughtExceptionHandler(null);
    }
    save();
    return this;
  }

  @Override int getType() {
    return TYPE_APP;
  }
}