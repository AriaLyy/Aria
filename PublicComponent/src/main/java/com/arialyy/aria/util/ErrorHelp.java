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
package com.arialyy.aria.util;

import android.annotation.SuppressLint;
import com.arialyy.aria.core.AriaConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aria.Lao on 2017/8/29.
 * 错误帮助类
 */
public class ErrorHelp {

  /**
   * 保存错误信息
   *
   * @param msg 错误提示
   * @param ex 异常
   */
  public static void saveError(String msg, String ex) {
    writeLogToFile(String.format("\nmsg【%s】\nException：%s", msg, ex));
  }

  /**
   * 返回日志路径
   *
   * @return "/mnt/sdcard/Android/data/{package_name}/files/log/*"
   */
  private static String getLogPath() {
    String path = String.format("%slog/AriaCrash_%s.log",
        CommonUtil.getAppPath(AriaConfig.getInstance().getAPP()),
        getData("yyyy-MM-dd_HH_mm_ss"));

    FileUtil.createFile(path);
    return path;
  }

  /**
   * 把日志记录到文件
   */
  private static int writeLogToFile(String message) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(getData("yyyy-MM-dd HH:mm:ss"));
    stringBuffer.append("    ");
    stringBuffer.append(message);
    stringBuffer.append("\n\n");
    PrintWriter writer = null;
    try {
      File file = new File(getLogPath());
      if (!file.exists()) {
        FileUtil.createFile(file);
      }
      writer = new PrintWriter(new FileWriter(file.getPath(), true));
      writer.append(stringBuffer);
      writer.flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
    return 0;
  }

  @SuppressLint("SimpleDateFormat")
  private static String getData(String format) {
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(date);
  }
}
