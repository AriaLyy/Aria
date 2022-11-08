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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.text.TextUtils;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.ALog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyy on 2015/11/2.
 * sql帮助类
 */
final class SqlHelper extends SQLiteOpenHelper {
  private static final String TAG = "SqlHelper";
  private static volatile SqlHelper INSTANCE = null;
  private static boolean mainTmpDirSet = false;
  private Context mContext;

  synchronized static SqlHelper init(Context context) {
    if (INSTANCE == null) {
      synchronized (SqlHelper.class) {
        INSTANCE = new SqlHelper(context.getApplicationContext());
      }
    }
    return INSTANCE;
  }

  static SqlHelper getInstance() {
    return INSTANCE;
  }

  private SqlHelper(Context context) {
    super(DBConfig.SAVE_IN_SDCARD ? new DatabaseContext(context) : context, DBConfig.DB_NAME, null,
        DBConfig.VERSION);
    mContext = context;
  }

  @Override public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
  }

  @Override public void onConfigure(SQLiteDatabase db) {
    super.onConfigure(db);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      db.setForeignKeyConstraintsEnabled(true);
    } else {
      // SQLite在3.6.19版本中开始支持外键约束，
      // 而在Android中 2.1以前的版本使用的SQLite版本是3.5.9， 在2.2版本中使用的是3.6.22.
      // 但是为了兼容以前的程序，默认并没有启用该功能，如果要启用该功能
      // 需要使用如下语句：
      db.execSQL("PRAGMA foreign_keys=ON;");
    }
  }

  @Override public void onCreate(SQLiteDatabase db) {
    Set<String> tables = DBConfig.mapping.keySet();
    for (String tableName : tables) {
      Class clazz = DBConfig.mapping.get(tableName);
      if (!SqlUtil.tableExists(db, clazz)) {
        SqlUtil.createTable(db, clazz);
      }
    }
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < newVersion) {
      if (oldVersion < 31) {
        handleLowAriaUpdate(db);
      } else if (oldVersion < 45) {
        handle360AriaUpdate(db);
      } else if (oldVersion < 51) {
        handle365Update(db);
      } else if (oldVersion < 53) {
        handle366Update(db);
      } else {
        handleDbUpdate(db, null);
      }
      // 处理380版本TaskRecord 增加的记录类型判断
      if (newVersion == 57) {
        addTaskRecordType(db);
      }
    }
  }

  @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion > newVersion) {
      handleDbUpdate(db, null);
    }
  }

  /**
   * 获取数据库连接
   */
  SQLiteDatabase getDb() {
    SQLiteDatabase db;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      SQLiteDatabase.OpenParams params = new SQLiteDatabase.OpenParams.Builder().setOpenFlags(
          SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE |
              SQLiteDatabase.CREATE_IF_NECESSARY).build();
      setOpenParams(params);
      db = getWritableDatabase();
    } else {
      //SQLiteDatabase.openOrCreateDatabase()
      File dbFile = mContext.getDatabasePath(DBConfig.DB_NAME);
      if (!dbFile.exists()) {
        db = getWritableDatabase();
      } else {
        // 触发一次SQLiteOpenHelper的流程，再使用NO_LOCALIZED_COLLATORS标志打开数据库
        db = getReadableDatabase();
        db.close();
        db = SQLiteDatabase.openDatabase(dbFile.getPath(), null,
            SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE |
                SQLiteDatabase.CREATE_IF_NECESSARY);
      }
    }
    db.enableWriteAheadLogging();
    return db;
  }

  @Override public SQLiteDatabase getWritableDatabase() {
    if (!mainTmpDirSet) {
      createDbCacheDir();
      return super.getWritableDatabase();
    }
    return super.getWritableDatabase();
  }

  /**
   * 用于修复 Too many open files 的问题
   * https://github.com/AriaLyy/Aria/issues/664
   */
  @Override
  public SQLiteDatabase getReadableDatabase() {
    if (!mainTmpDirSet) {
      createDbCacheDir();
      return super.getReadableDatabase();
    }
    return super.getReadableDatabase();
  }

  private void createDbCacheDir() {
    String cacheDir = mContext.getCacheDir().getPath() + "/AriaDbCacheDir";
    File cacheFile = new File(cacheDir);
    if (!cacheFile.exists()){
      boolean rs = cacheFile.mkdirs();
      ALog.d(TAG, rs + "");
    }
    super.getReadableDatabase()
        .execSQL("PRAGMA temp_store_directory = '" + cacheDir + "'");
    mainTmpDirSet = true;
  }

  /**
   * 处理数据库升级
   *
   * @param modifyColumns 需要修改的表字段的映射，key为表名，
   * value{@code Map<String, String>}中的Map的key为老字段名称，value为该老字段对应的新字段名称
   */
  private void handleDbUpdate(SQLiteDatabase db, Map<String, Map<String, String>> modifyColumns) {
    if (db == null) {
      ALog.e("SqlHelper", "db 为 null");
      return;
    } else if (!db.isOpen()) {
      ALog.e("SqlHelper", "db已关闭");
      return;
    }

    try {
      db.beginTransaction();
      Set<String> tables = DBConfig.mapping.keySet();
      for (String tableName : tables) {
        Class<? extends DbEntity> clazz = DBConfig.mapping.get(tableName);
        if (SqlUtil.tableExists(db, clazz)) {
          // ----------- 1、获取旧表字段、新表字段
          Cursor columnC =
              db.rawQuery(String.format("PRAGMA table_info(%s)", tableName), null);

          // 获取新表的所有字段名称
          List<String> newTabColumns = SqlUtil.getColumns(clazz);
          // 获取旧表的所有字段名称
          List<String> oldTabColumns = new ArrayList<>();

          while (columnC.moveToNext()) {
            String columnName = columnC.getString(columnC.getColumnIndex("name"));
            oldTabColumns.add(columnName);
          }
          columnC.close();

          // ----------- 2、为防止字段增加失败的情况，先给旧表增加字段
          List<String> newAddColum = getNewColumn(newTabColumns, oldTabColumns);
          // 删除重命名的字段
          Map<String, String> modifyMap = null;
          if (modifyColumns != null) {
            modifyMap = modifyColumns.get(tableName);
            if (modifyMap != null) {
              Iterator<String> it = newAddColum.iterator();
              while (it.hasNext()) {
                String s = it.next();
                if (modifyMap.get(s) != null) {
                  it.remove();
                }
              }
            }
          }

          // 给旧表增加字段，防止新增字段失败
          if (newAddColum.size() > 0) {
            String sql = "ALTER TABLE %s ADD COLUMN %s %s";
            for (String nc : newAddColum) {
              String temp =
                  String.format(sql, tableName, nc, SqlUtil.getColumnTypeByFieldName(clazz, nc));
              ALog.d(TAG, "添加表字段的sql：" + temp);
              db.execSQL(temp);
            }
          }

          // ----------- 3、将旧表备份下，并创建新表
          String alertSql = String.format("ALTER TABLE %s RENAME TO %s_temp", tableName, tableName);
          db.execSQL(alertSql);

          //创建新表
          SqlUtil.createTable(db, clazz);

          String sql = String.format("SELECT COUNT(*) FROM %s_temp", tableName);
          Cursor cursor = db.rawQuery(sql, null);
          cursor.moveToFirst();
          long count = cursor.getLong(0);
          cursor.close();

          // ----------- 4、将旧表数据复制到新表
          if (count > 0) {

            // 旧表需要删除的字段，删除旧表有而新表没的字段
            List<String> diffTab = getDiffColumn(newTabColumns, oldTabColumns);
            StringBuilder params = new StringBuilder();

            // 需要修改的列名映射表
            //Map<String, String> modifyMap = null;
            if (modifyColumns != null) {
              modifyMap = modifyColumns.get(tableName);
            }

            for (String column : oldTabColumns) {
              if (!diffTab.isEmpty() && diffTab.contains(column)
                  // 如果旧表字段有修改，忽略这个删除
                  && !(modifyMap != null && modifyMap.containsKey(column))) {
                continue;
              }
              params.append(column).append(",");
            }

            String oldParamStr = params.toString();
            oldParamStr = oldParamStr.substring(0, oldParamStr.length() - 1);
            String newParamStr = oldParamStr;
            // 处理字段名称改变
            if (modifyMap != null && !modifyMap.isEmpty()) {
              Set<String> keys = modifyMap.keySet();
              for (String key : keys) {
                if (newParamStr.contains(key)) {
                  newParamStr = newParamStr.replace(key, modifyMap.get(key));
                }
              }
            }

            //恢复数据
            String insertSql =
                String.format("INSERT INTO %s (%s) SELECT %s FROM %s_temp", tableName, newParamStr,
                    oldParamStr, tableName);
            ALog.d(TAG, "恢复数据的sql：" + insertSql);

            db.execSQL(insertSql);
          }
          // ----------- 5、删除备份的表
          SqlUtil.dropTable(db, tableName + "_temp");
        } else {
          SqlUtil.createTable(db, clazz);
        }
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      db.endTransaction();
    }
  }

  /**
   * 取新表差值（需要删除的字段）：旧表有而新表没的字段
   *
   * @param newTab 新表字段
   * @param oldTab 旧表字段
   * @return 旧表有而新表没的字段
   */
  private List<String> getDiffColumn(List<String> newTab, List<String> oldTab) {
    List<String> temp = new ArrayList<>(oldTab); // 拷贝旧表字段
    temp.removeAll(newTab);
    return temp;
  }

  /**
   * 获取新增字段
   *
   * @param newTab 新表字段
   * @param oldTab 就表字段
   * @return 新表有而旧表没的字段
   */
  private List<String> getNewColumn(List<String> newTab, List<String> oldTab) {
    List<String> temp = new ArrayList<>(newTab);
    temp.removeAll(oldTab);
    return temp;
  }

  /**
   * 给TaskRecord 增加任务类型
   */
  private void addTaskRecordType(SQLiteDatabase db) {
    try {
      SqlUtil.checkOrCreateTable(db, ThreadRecord.class);
      SqlUtil.checkOrCreateTable(db, TaskRecord.class);
      SqlUtil.checkOrCreateTable(db, UploadEntity.class);
      SqlUtil.checkOrCreateTable(db, DownloadEntity.class);

      db.beginTransaction();
      /*
       * 增加下载实体的类型
       */
      String dSql = "SELECT downloadPath, url FROM DownloadEntity";
      Cursor c = db.rawQuery(dSql, null);
      while (c.moveToNext()) {
        int type;
        String filePath = c.getString(0);
        String url = c.getString(1);
        if (url.startsWith("ftp") || url.startsWith("sftp")) {
          type = ITaskWrapper.D_FTP;
        } else {
          if (SqlUtil.tableExists(db, M3U8Entity.class)) {
            Cursor m3u8c = db.rawQuery("SELECT isLive FROM M3U8Entity WHERE filePath=\""
                + SqlUtil.encodeStr(filePath)
                + "\"", null);
            if (m3u8c.moveToNext()) {
              String temp = m3u8c.getString(0);
              type =
                  (TextUtils.isEmpty(temp) ? false : Boolean.valueOf(temp)) ? ITaskWrapper.M3U8_LIVE
                      : ITaskWrapper.M3U8_VOD;
            } else {
              type = ITaskWrapper.D_HTTP;
            }
            m3u8c.close();
          } else {
            type = ITaskWrapper.D_HTTP;
          }
        }
        db.execSQL("UPDATE DownloadEntity SET taskType=? WHERE downloadPath=?",
            new Object[] { type, filePath });
        db.execSQL("UPDATE TaskRecord SET taskType=? WHERE filePath=?",
            new Object[] { type, filePath });
        db.execSQL("UPDATE ThreadRecord SET threadType=? WHERE taskKey=?",
            new Object[] { type, filePath });
      }
      c.close();

      /*
       * 增加上传实体的类型
       */
      String uSql = "SELECT filePath, url FROM UploadEntity";
      c = db.rawQuery(uSql, null);
      while (c.moveToNext()) {
        int type;
        String filePath = c.getString(c.getColumnIndex("filePath"));
        String url = c.getString(c.getColumnIndex("url"));
        if (url.startsWith("ftp") || url.startsWith("sftp")) {
          type = ITaskWrapper.D_FTP;
        } else {
          type = ITaskWrapper.D_HTTP;
        }
        db.execSQL("UPDATE UploadEntity SET taskType=? WHERE filePath=?",
            new Object[] { type, filePath });
        db.execSQL("UPDATE TaskRecord SET taskType=? WHERE filePath=?",
            new Object[] { type, filePath });
        db.execSQL("UPDATE ThreadRecord SET threadType=? WHERE taskKey=?",
            new Object[] { type, filePath });
      }
      c.close();

      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  /**
   * 删除重复的repeat数据
   */
  private void delRepeatThreadRecord(SQLiteDatabase db) {
    SqlUtil.checkOrCreateTable(db, ThreadRecord.class);
    String repeatSql = "DELETE FROM ThreadRecord WHERE (rowid) "
        + "IN (SELECT rowid FROM ThreadRecord GROUP BY taskKey, threadId, endLocation HAVING COUNT(*) > 1) "
        + "AND rowid NOT IN (SELECT MIN(rowid) FROM ThreadRecord GROUP BY taskKey, threadId, endLocation HAVING COUNT(*)> 1)";
    ALog.d(TAG, repeatSql);
    db.execSQL(repeatSql);
  }

  /**
   * 处理366版本以下的升级
   */
  private void handle366Update(SQLiteDatabase db) {
    Map<String, Map<String, String>> modifyMap = new HashMap<>();
    // 处理ThreadRecord的key字段名修改
    Map<String, String> threadRecordModify = new HashMap<>();
    threadRecordModify.put("key", "taskKey");
    modifyMap.put("ThreadRecord", threadRecordModify);

    // 执行升级操作
    handleDbUpdate(db, modifyMap);
    delRepeatThreadRecord(db);
  }

  /**
   * 处理365版本以下的升级
   */
  private void handle365Update(SQLiteDatabase db) {
    SqlUtil.checkOrCreateTable(db, ThreadRecord.class);
    db.execSQL("UPDATE ThreadRecord SET threadId=0 WHERE threadId=-1");

    Map<String, Map<String, String>> modifyMap = new HashMap<>();
    // 处理ThreadRecord的key字段名修改
    Map<String, String> threadRecordModify = new HashMap<>();
    threadRecordModify.put("key", "taskKey");
    modifyMap.put("ThreadRecord", threadRecordModify);

    // 执行升级操作
    handleDbUpdate(db, modifyMap);
    delRepeatThreadRecord(db);
  }

  /**
   * 处理3.6以下版本的数据库升级
   */
  private void handle360AriaUpdate(SQLiteDatabase db) {
    String[] taskTables =
        new String[] { "UploadTaskEntity", "DownloadTaskEntity", "DownloadGroupTaskEntity" };
    for (String taskTable : taskTables) {
      if (SqlUtil.tableExists(db, taskTable)) {
        SqlUtil.dropTable(db, taskTable);
      }
    }

    Map<String, Map<String, String>> modifyMap = new HashMap<>();
    // 处理DownloadEntity、DownloadGroupEntity的 groupName字段名的修改
    Map<String, String> entityModify = new HashMap<>();
    entityModify.put("groupName", "groupHash");
    modifyMap.put("DownloadEntity", entityModify);
    modifyMap.put("DownloadGroupEntity", entityModify);

    // 处理TaskRecord的dGroupName字段名的修改
    Map<String, String> taskRecordModify = new HashMap<>();
    taskRecordModify.put("dGroupName", "dGroupHash");
    modifyMap.put("TaskRecord", taskRecordModify);

    // 处理ThreadRecord的key字段名修改
    Map<String, String> threadRecordModify = new HashMap<>();
    threadRecordModify.put("key", "taskKey");
    modifyMap.put("ThreadRecord", threadRecordModify);

    // 执行升级操作
    handleDbUpdate(db, modifyMap);
    delRepeatThreadRecord(db);
  }

  /**
   * 处理低版本的数据库迁移，主要是修改子表外键字段对应的值
   */
  private void handleLowAriaUpdate(SQLiteDatabase db) {
    String[] taskTables =
        new String[] { "UploadTaskEntity", "DownloadTaskEntity", "DownloadGroupTaskEntity" };
    for (String taskTable : taskTables) {
      if (SqlUtil.tableExists(db, taskTable)) {
        SqlUtil.dropTable(db, taskTable);
      }
    }

    //删除所有主键为null和主键重复的数据
    String[] tables = new String[] { "DownloadEntity", "DownloadGroupEntity" };
    String[] keys = new String[] { "downloadPath", "groupName" };
    int i = 0;
    for (String tableName : tables) {
      if (!SqlUtil.tableExists(db, tableName)) {
        continue;
      }
      String pColumn = keys[i];
      String nullSql =
          String.format("DELETE FROM %s WHERE %s='' OR %s IS NULL", tableName, pColumn, pColumn);
      ALog.d(TAG, nullSql);
      db.execSQL(nullSql);

      //删除所有主键重复的数据
      String repeatSql =
          String.format(
              "DELETE FROM %s WHERE %s IN(SELECT %s FROM %s GROUP BY %s HAVING COUNT(%s) > 1)",
              tableName, pColumn, pColumn, tableName, pColumn, pColumn);

      ALog.d(TAG, repeatSql);
      db.execSQL(repeatSql);
      i++;
    }

    // 处理数据库版本小于3的字段改变
    Map<String, Map<String, String>> modifyMap = new HashMap<>();
    Map<String, String> dEntityModifyMap = new HashMap<>();
    dEntityModifyMap.put("groupName", "groupHash");
    dEntityModifyMap.put("downloadUrl", "url");
    dEntityModifyMap.put("isDownloadComplete", "isComplete");
    modifyMap.put("DownloadEntity", dEntityModifyMap);

    Map<String, String> dGEntityModifyMap = new HashMap<>();
    dGEntityModifyMap.put("groupName", "groupHash");
    modifyMap.put("DownloadGroupEntity", dGEntityModifyMap);

    handleDbUpdate(db, modifyMap);
  }
}