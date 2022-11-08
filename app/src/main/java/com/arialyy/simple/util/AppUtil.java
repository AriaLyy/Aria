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

package com.arialyy.simple.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import androidx.core.content.FileProvider;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import com.arialyy.simple.BuildConfig;
import java.io.File;
import java.io.IOException;

public class AppUtil {
  private static final String TAG = "AppUtil";
  private static final String ARIA_SHARE_PRE_KEY = "ARIA_SHARE_PRE_KEY";

  /**
   * 检查实体是否有效
   *
   * @return true 实体有效
   */
  public static boolean chekEntityValid(AbsEntity entity) {
    return entity != null && entity.getId() != -1;
  }

  /**
   * http下载示例代码
   */
  public static File getHelpCode(Context context, String fileName) throws IOException {
    String path = String.format("%s/code/%s", context.getFilesDir().getPath(), fileName);
    File ftpCode = new File(path);
    if (!ftpCode.exists()) {
      FileUtil.createFile(ftpCode);
      FileUtil.createFileFormInputStream(context.getAssets()
              .open(String.format("help_code/%s", fileName)),
          path);
    }
    return ftpCode;
  }

  /**
   * 读取配置文件字段
   *
   * @param key key
   * @param defStr 默认字符串
   */
  public static String getConfigValue(Context context, String key, String defStr) {
    SharedPreferences preferences =
        context.getSharedPreferences(ARIA_SHARE_PRE_KEY, Context.MODE_PRIVATE);
    return preferences.getString(key, defStr);
  }

  /**
   * set配置文件字段
   *
   * @param key key
   * @param value 需要保存的字符串
   */
  public static void setConfigValue(Context context, String key, String value) {
    SharedPreferences preferences =
        context.getSharedPreferences(ARIA_SHARE_PRE_KEY, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(key, value);
    editor.apply();
  }

  /**
   * 调用系统文件管理器选择文件
   *
   * @param file 不能是文件夹
   * @param mineType android 可用的minetype
   * @param requestCode 请求码
   */
  public static void chooseFile(Activity activity, File file, String mineType, int requestCode) {
    if (file.isDirectory()) {
      ALog.e(TAG, "不能选择文件夹");
      return;
    }
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    Uri uri;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      uri = FileProvider.getUriForFile(activity.getApplicationContext(),
          BuildConfig.APPLICATION_ID + ".provider",
          file);
    } else {
      uri = Uri.fromFile(file);
    }

    intent.setDataAndType(uri, TextUtils.isEmpty(mineType) ? "*/*" : mineType);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    activity.startActivityForResult(intent, requestCode);
  }
}
