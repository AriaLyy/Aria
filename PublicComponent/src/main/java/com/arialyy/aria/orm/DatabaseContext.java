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
package com.arialyy.aria.orm;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;

/**
 * 保存在sd卡的数据库使用的Context
 */
class DatabaseContext extends ContextWrapper {
  public DatabaseContext(Context context) {
    super(context);
  }

  /**
   * 获得数据库路径，如果不存在，则创建对象对象
   */
  @Override
  public File getDatabasePath(String name) {
    String dbDir = CommonUtil.getAppPath(getBaseContext());

    dbDir += "DB";//数据库所在目录
    String dbPath = dbDir + "/" + name;//数据库路径
    //判断目录是否存在，不存在则创建该目录
    File dirFile = new File(dbDir);
    if (!dirFile.exists()) {
      dirFile.mkdirs();
    }

    //数据库文件是否创建成功
    boolean isFileCreateSuccess = false;
    //判断文件是否存在，不存在则创建该文件
    File dbFile = new File(dbPath);
    if (!dbFile.exists()) {
      try {
        isFileCreateSuccess = dbFile.createNewFile();//创建文件
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      isFileCreateSuccess = true;
    }

    //返回数据库文件对象
    if (isFileCreateSuccess) {
      return dbFile;
    } else {
      return null;
    }
  }

  /**
   * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
   */
  @Override
  public SQLiteDatabase openOrCreateDatabase(String name, int mode,
      SQLiteDatabase.CursorFactory factory) {
    return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
  }

  /**
   * Android 4.0会调用此方法获取数据库。
   *
   * @see android.content.ContextWrapper#openOrCreateDatabase(java.lang.String, int,
   * android.database.sqlite.SQLiteDatabase.CursorFactory,
   * android.database.DatabaseErrorHandler)
   */
  @Override
  public SQLiteDatabase openOrCreateDatabase(String name, int mode,
      SQLiteDatabase.CursorFactory factory,
      DatabaseErrorHandler errorHandler) {
    return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
  }
}