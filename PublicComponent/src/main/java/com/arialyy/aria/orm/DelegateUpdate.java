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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.arialyy.aria.orm.annotation.Primary;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by laoyuyu on 2018/3/22. 增加数据、更新数据
 */
class DelegateUpdate extends AbsDelegate {
  private DelegateUpdate() {
  }

  /**
   * 删除某条数据
   */
  synchronized <T extends DbEntity> void delData(SQLiteDatabase db, Class<T> clazz,
      String... expression) {
    SqlUtil.checkOrCreateTable(db, clazz);
    db = checkDb(db);
    if (!CommonUtil.checkSqlExpression(expression)) {
      return;
    }

    String sql = "DELETE FROM " + CommonUtil.getClassName(clazz) + " WHERE " + expression[0] + " ";
    sql = sql.replace("?", "%s");
    Object[] params = new String[expression.length - 1];
    for (int i = 0, len = params.length; i < len; i++) {
      params[i] = String.format("'%s'", SqlUtil.encodeStr(expression[i + 1]));
    }
    sql = String.format(sql, params);
    db.execSQL(sql);
  }

  /**
   * 修改某行数据
   */
  synchronized void updateData(SQLiteDatabase db, DbEntity dbEntity) {
    SqlUtil.checkOrCreateTable(db, dbEntity.getClass());
    db = checkDb(db);
    ContentValues values = createValues(dbEntity);
    if (values != null) {
      db.update(CommonUtil.getClassName(dbEntity), values, "rowid=?",
          new String[] { String.valueOf(dbEntity.rowID) });
    } else {
      ALog.e(TAG, "更新记录失败，记录没有属性字段");
    }
  }

  /**
   * 更新多条记录
   */
  synchronized <T extends DbEntity> void updateManyData(SQLiteDatabase db, List<T> dbEntities) {
    db = checkDb(db);
    db.beginTransaction();
    try {
      Class oldClazz = null;
      String table = null;
      for (DbEntity entity : dbEntities) {
        if (oldClazz == null || oldClazz != entity.getClass() || table == null) {
          oldClazz = entity.getClass();
          table = CommonUtil.getClassName(oldClazz);
        }
        ContentValues value = createValues(entity);
        if (value == null) {
          ALog.e(TAG, "更新记录失败，记录没有属性字段");
        } else {
          db.update(table, value, "rowid=?", new String[] { String.valueOf(entity.rowID) });
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
   * 插入多条记录
   */
  synchronized <T extends DbEntity> void insertManyData(SQLiteDatabase db, List<T> dbEntities) {
    db = checkDb(db);
    db.beginTransaction();
    try {
      Class oldClazz = null;
      String table = null;
      for (DbEntity entity : dbEntities) {
        if (oldClazz == null || oldClazz != entity.getClass() || table == null) {
          oldClazz = entity.getClass();
          table = CommonUtil.getClassName(oldClazz);
          SqlUtil.checkOrCreateTable(db, oldClazz);
        }

        ContentValues value = createValues(entity);
        if (value == null) {
          ALog.e(TAG, "保存记录失败，记录没有属性字段");
        } else {
          entity.rowID = db.insert(table, null, value);
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
   * 插入数据
   */
  synchronized void insertData(SQLiteDatabase db, DbEntity dbEntity) {
    SqlUtil.checkOrCreateTable(db, dbEntity.getClass());
    db = checkDb(db);
    ContentValues values = createValues(dbEntity);
    if (values != null) {
      dbEntity.rowID = db.insert(CommonUtil.getClassName(dbEntity), null, values);
    } else {
      ALog.e(TAG, "保存记录失败，记录没有属性字段");
    }
  }

  /**
   * 创建存储数据\更新数据时使用的ContentValues
   *
   * @return 如果没有字段属性，返回null
   */
  private ContentValues createValues(DbEntity dbEntity) {
    List<Field> fields = CommonUtil.getAllFields(dbEntity.getClass());
    if (fields.size() > 0) {
      ContentValues values = new ContentValues();
      try {
        for (Field field : fields) {
          field.setAccessible(true);
          if (isIgnore(dbEntity, field)) {
            continue;
          }
          String value = null;
          Type type = field.getType();
          if (type == Map.class && SqlUtil.checkMap(field)) {
            value = SqlUtil.map2Str((Map<String, String>) field.get(dbEntity));
          } else if (type == List.class && SqlUtil.checkList(field)) {
            value = SqlUtil.list2Str(dbEntity, field);
          } else {
            Object obj = field.get(dbEntity);
            if (obj != null) {
              value = field.get(dbEntity).toString();
            }
          }
          values.put(field.getName(), SqlUtil.encodeStr(value));
        }
        return values;
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  /**
   * {@code true}自动增长的主键和需要忽略的字段
   */
  private boolean isIgnore(Object obj, Field field) throws IllegalAccessException {
    if (SqlUtil.isIgnore(field)) {
      return true;
    }
    Object value = field.get(obj);
    if (value == null) {  // 忽略为空的字段
      return true;
    }
    if (value instanceof String) {
      if (TextUtils.isEmpty(String.valueOf(value))) {
        return true;
      }
    }
    if (value instanceof List) {
      if (((List) value).size() == 0) {
        return true;
      }
    }
    if (value instanceof Map) {
      if (((Map) value).size() == 0) {
        return true;
      }
    }

    if (SqlUtil.isPrimary(field)) {   //忽略自动增长的主键
      Primary p = field.getAnnotation(Primary.class);
      return p.autoincrement();
    }

    return false;
  }
}
