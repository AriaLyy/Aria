/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aria.Lao on 2017/10/25.
 * 程序异常日志捕获器
 */
public class AriaCrashHandler implements Thread.UncaughtExceptionHandler {
  private Thread.UncaughtExceptionHandler mDefaultHandler;
  private ExecutorService mExecutorService;

  public AriaCrashHandler() {
    mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    mExecutorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    ex.printStackTrace();
    //ALog.d(thread.getName(), ex.getLocalizedMessage());
    handleException(thread.getName(), ex);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      mDefaultHandler.uncaughtException(thread, ex);
      exit();
    }
  }

  /**
   * 处理异常
   */
  private void handleException(final String name, final Throwable ex) {
    if (ex == null) {
      return;
    }

    mExecutorService.execute(new Runnable() {
      @Override
      public void run() {
        ErrorHelp.saveError("", ALog.getExceptionString(ex));
      }
    });
  }

  /**
   * 退出当前应用
   */
  private void exit() {
    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(1);
  }
}