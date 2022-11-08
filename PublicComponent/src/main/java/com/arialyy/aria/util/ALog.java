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

import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aria.Lao on 2017/10/25.
 * Aria日志工具
 */
public class ALog {
  public static final boolean DEBUG = true;
  public static final int LOG_LEVEL_VERBOSE = 2;
  public static final int LOG_LEVEL_DEBUG = 3;
  public static final int LOG_LEVEL_INFO = 4;
  public static final int LOG_LEVEL_WARN = 5;
  public static final int LOG_LEVEL_ERROR = 6;
  public static final int LOG_LEVEL_ASSERT = 7;
  public static final int LOG_CLOSE = 8;
  public static final int LOG_DEFAULT = LOG_LEVEL_DEBUG;

  public static int LOG_LEVEL = LOG_DEFAULT;

  public static int v(String tag, String msg) {
    return println(Log.VERBOSE, tag, msg);
  }

  public static int d(String tag, String msg) {
    return println(Log.DEBUG, tag, msg);
  }

  public static int i(String tag, String msg) {
    return println(Log.INFO, tag, msg);
  }

  public static int w(String tag, String msg) {
    return println(Log.WARN, tag, msg);
  }

  public static int e(String tag, String msg) {
    return println(Log.ERROR, tag, msg);
  }

  public static void e(String tag, String msg, Throwable e) {
    Log.e(tag, msg, e);
  }

  /**
   * 打印MAp，debug级别日志
   */
  public static void m(String tag, Map map) {
    if (LOG_LEVEL <= Log.DEBUG) {
      Set set = map.entrySet();
      if (set.size() < 1) {
        d(tag, "[]");
        return;
      }
      int i = 0;
      String[] s = new String[set.size()];
      for (Object aSet : set) {
        Map.Entry entry = (Map.Entry) aSet;
        s[i] = entry.getKey() + " = " + entry.getValue() + ",\n";
        i++;
      }
      println(Log.DEBUG, tag, Arrays.toString(s));
    }
  }

  /**
   * 打印JSON，debug级别日志
   */
  public static void j(String tag, String jsonStr) {
    if (LOG_LEVEL <= Log.DEBUG) {
      String message;
      try {
        if (jsonStr.startsWith("{")) {
          JSONObject jsonObject = new JSONObject(jsonStr);
          message = jsonObject.toString(4); //这个是核心方法
        } else if (jsonStr.startsWith("[")) {
          JSONArray jsonArray = new JSONArray(jsonStr);
          message = jsonArray.toString(4);
        } else {
          message = jsonStr;
        }
      } catch (JSONException e) {
        message = jsonStr;
      }
      println(Log.DEBUG, tag, message);
    }
  }

  /**
   * 将异常信息转换为字符串
   */
  public static String getExceptionString(Throwable ex) {
    if (ex == null) {
      return "";
    }
    StringBuilder err = new StringBuilder();
    err.append("ExceptionDetailed:\n");
    err.append("====================Exception Info====================\n");
    err.append(ex.toString());
    err.append("\n");
    StackTraceElement[] stack = ex.getStackTrace();
    for (StackTraceElement stackTraceElement : stack) {
      err.append(stackTraceElement.toString()).append("\n");
    }
    Throwable cause = ex.getCause();
    if (cause != null) {
      err.append("【Caused by】: ");
      err.append(cause.toString());
      err.append("\n");
      StackTraceElement[] stackTrace = cause.getStackTrace();
      for (StackTraceElement stackTraceElement : stackTrace) {
        err.append(stackTraceElement.toString()).append("\n");
      }
    }
    err.append("===================================================");
    return err.toString();
  }

  private static int println(int level, String tag, String msg) {
    if (LOG_LEVEL <= level) {
      return Log.println(level, tag, TextUtils.isEmpty(msg) ? "null" : msg);
    } else {
      return -1;
    }
  }
}
