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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.orm.annotation.Default;
import com.arialyy.aria.orm.annotation.Foreign;
import com.arialyy.aria.orm.annotation.Ignore;
import com.arialyy.aria.orm.annotation.Many;
import com.arialyy.aria.orm.annotation.NoNull;
import com.arialyy.aria.orm.annotation.One;
import com.arialyy.aria.orm.annotation.Primary;
import com.arialyy.aria.orm.annotation.Unique;
import com.arialyy.aria.orm.annotation.Wrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyy on 2017/7/24.
 * sql工具
 */
final class SqlUtil {
  private static final String TAG = CommonUtil.getClassName("SqlUtil");

  /**
   * 检查表是否存在，不存在则创建表
   */
  static void checkOrCreateTable(SQLiteDatabase db, Class<? extends DbEntity> clazz) {
    if (!tableExists(db, clazz)) {
      createTable(db, clazz);
    }
  }

  static void closeCursor(Cursor cursor) {
    synchronized (AbsDelegate.class) {
      if (cursor != null && !cursor.isClosed()) {
        try {
          cursor.close();
        } catch (android.database.SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 查找表是否存在
   *
   * @param clazz 数据库实体
   * @return true，该数据库实体对应的表存在；false，不存在
   */
  static boolean tableExists(SQLiteDatabase db, Class<? extends DbEntity> clazz) {
    return tableExists(db, CommonUtil.getClassName(clazz));
  }

  /**
   * 查找表是否存在
   *
   * @param tableName 表名
   * @return true，该数据库实体对应的表存在；false，不存在
   */
  static boolean tableExists(SQLiteDatabase db, String tableName) {
    db = checkDb(db);
    Cursor cursor = null;
    try {
      String sql =
          String.format("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='%s'",
              tableName);
      cursor = db.rawQuery(sql, null);
      if (cursor != null && cursor.moveToNext()) {
        int count = cursor.getInt(0);
        if (count > 0) {
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeCursor(cursor);
    }
    return false;
  }

  /**
   * 检查list参数是否合法，list只能是{@code List<String>}
   *
   * @return {@code true} 合法
   */
  static boolean checkList(Field list) {
    Class t = CommonUtil.getListParamType(list);
    if (t == String.class) {
      return true;
    } else {
      ALog.d(TAG, "map参数错误，支持List<String>的参数字段");
      return false;
    }
  }

  /**
   * 检查map参数是否合法，map只能是{@code Map<String, String>}
   *
   * @return {@code true} 合法
   */
  static boolean checkMap(Field map) {
    Class[] ts = CommonUtil.getMapParamType(map);
    if (ts != null
        && ts[0] != null
        && ts[1] != null
        && ts[0] == String.class
        && ts[1] == String.class) {
      return true;
    } else {
      ALog.d(TAG, "map参数错误，支持Map<String,String>的参数字段");
      return false;
    }
  }

  /**
   * 删除指定的表
   */
  static void dropTable(SQLiteDatabase db, String tableName) {
    db = checkDb(db);
    String deleteSQL = String.format("DROP TABLE IF EXISTS %s", tableName);
    //db.beginTransaction();
    db.execSQL(deleteSQL);
    //db.setTransactionSuccessful();
    //db.endTransaction();
  }

  /**
   * 清空表数据
   */
  static void clean(SQLiteDatabase db, Class<? extends DbEntity> clazz) {
    db = checkDb(db);
    String tableName = CommonUtil.getClassName(clazz);
    if (tableExists(db, clazz)) {
      String sql = "DELETE FROM " + tableName;
      db.execSQL(sql);
    }
  }

  /**
   * 检查某个字段的值是否存在
   *
   * @param expression 字段和值"url=xxx"
   * @return {@code true}该字段的对应的value已存在
   */
  static boolean checkDataExist(SQLiteDatabase db, Class<? extends DbEntity> clazz,
      String... expression) {
    db = checkDb(db);
    if (!CommonUtil.checkSqlExpression(expression)) {
      return false;
    }
    String sql = String.format("SELECT rowid, * FROM %s WHERE %s ", CommonUtil.getClassName(clazz),
        expression[0]);
    sql = sql.replace("?", "%s");
    Object[] params = new String[expression.length - 1];
    for (int i = 0, len = params.length; i < len; i++) {
      params[i] = String.format("'%s'", SqlUtil.encodeStr(expression[i + 1]));
    }
    sql = String.format(sql, params);
    Cursor cursor = db.rawQuery(sql, null);
    final boolean isExist = cursor.getCount() > 0;
    closeCursor(cursor);
    return isExist;
  }

  /**
   * 通过class 获取该class的表字段
   *
   * @return 表字段列表
   */
  static List<String> getColumns(Class<? extends DbEntity> clazz) {
    List<String> columns = new ArrayList<>();
    List<Field> fields = CommonUtil.getAllFields(clazz);
    for (Field field : fields) {
      field.setAccessible(true);
      if (SqlUtil.isIgnore(field)) {
        continue;
      }
      columns.add(field.getName());
    }
    return columns;
  }

  /**
   * 检查数据库是否关闭，已经关闭的话，打开数据库
   *
   * @return 返回数据库
   */
  static SQLiteDatabase checkDb(SQLiteDatabase db) {
    if (db == null || !db.isOpen()) {
      db = SqlHelper.getInstance().getDb();
    }
    return db;
  }

  /**
   * 创建表
   *
   * @param clazz 数据库实体
   */
  static void createTable(SQLiteDatabase db, Class<? extends DbEntity> clazz) {
    db = checkDb(db);
    List<Field> fields = CommonUtil.getAllFields(clazz);
    if (fields != null && fields.size() > 0) {
      //外键Map，在Sqlite3中foreign修饰的字段必须放在最后
      final List<Field> foreignArray = new ArrayList<>();
      StringBuilder sb = new StringBuilder();
      sb.append("CREATE TABLE IF NOT EXISTS ")
          .append(CommonUtil.getClassName(clazz))
          .append(" (");
      for (Field field : fields) {
        field.setAccessible(true);
        if (SqlUtil.isIgnore(field)) {
          continue;
        }
        Class<?> type = field.getType();
        String columnType = getColumnType(type);
        if (columnType == null) {
          continue;
        }
        sb.append(field.getName());
        sb.append(" ").append(columnType);

        if (SqlUtil.isPrimary(field)) {
          Primary pk = field.getAnnotation(Primary.class);
          sb.append(" PRIMARY KEY");
          if (pk.autoincrement() && (type == int.class || type == Integer.class)) {
            sb.append(" AUTOINCREMENT");
          }
        }

        if (SqlUtil.isForeign(field)) {
          foreignArray.add(field);
        }

        if (SqlUtil.isNoNull(field)) {
          sb.append(" NOT NULL");
        }

        if (SqlUtil.isDefault(field)) {
          Default d = field.getAnnotation(Default.class);
          if (!TextUtils.isEmpty(d.value())) {
            sb.append(" ERROR ").append("'").append(d.value()).append("'");
          }
        }

        if (SqlUtil.isUnique(field)) {
          sb.append(" UNIQUE");
        }

        sb.append(",");
      }

      for (Field field : foreignArray) {
        Foreign foreign = field.getAnnotation(Foreign.class);
        sb.append("FOREIGN KEY (")
            .append(field.getName())
            .append(") REFERENCES ")
            .append(CommonUtil.getClassName(foreign.parent()))
            .append("(")
            .append(foreign.column())
            .append(")");
        ActionPolicy update = foreign.onUpdate();
        ActionPolicy delete = foreign.onDelete();
        if (update != ActionPolicy.NO_ACTION) {
          sb.append(" ON UPDATE ").append(update.function);
        }

        if (delete != ActionPolicy.NO_ACTION) {
          sb.append(" ON DELETE ").append(update.function);
        }
        sb.append(",");
      }

      String str = sb.toString();
      str = str.substring(0, str.length() - 1) + ");";
      ALog.d(TAG, "创建表的sql：" + str);
      db.execSQL(str);
    }
  }

  /**
   * 根据字段名获取字段类型
   */
  static String getColumnTypeByFieldName(Class tabClass, String fieldName) {
    List<Field> fields = CommonUtil.getAllFields(tabClass);
    for (Field field : fields) {
      if (field.getName().equals(fieldName)) {
        return getColumnType(field.getType());
      }
    }
    return null;
  }

  /**
   * 获取字段类型
   */
  static String getColumnType(Class fieldtype) {
    if (fieldtype == String.class || fieldtype.isEnum()) {
      return "VARCHAR";
    } else if (fieldtype == int.class || fieldtype == Integer.class) {
      return "INTEGER";
    } else if (fieldtype == float.class || fieldtype == Float.class) {
      return "FLOAT";
    } else if (fieldtype == double.class || fieldtype == Double.class) {
      return "DOUBLE";
    } else if (fieldtype == long.class || fieldtype == Long.class) {
      return "BIGINT";
    } else if (fieldtype == boolean.class || fieldtype == Boolean.class) {
      return "BOOLEAN";
    } else if (fieldtype == java.util.Date.class || fieldtype == java.sql.Date.class) {
      return "DATA";
    } else if (fieldtype == byte.class || fieldtype == Byte.class) {
      return "BLOB";
    } else if (fieldtype == Map.class || fieldtype == List.class) {
      return "TEXT";
    } else {
      return null;
    }
  }

  /**
   * URL编码字符串
   *
   * @param str 原始字符串
   * @return 编码后的字符串
   */
  static String encodeStr(String str) {
    str = str.replaceAll("\\+", "%2B");
    return URLEncoder.encode(str);
  }

  /**
   * 获取主键字段名
   */
  static String getPrimaryName(Class<? extends DbEntity> clazz) {
    List<Field> fields = CommonUtil.getAllFields(clazz);
    String column;
    if (fields != null && !fields.isEmpty()) {

      for (Field field : fields) {
        field.setAccessible(true);
        if (isPrimary(field)) {
          column = field.getName();
          return column;
        }
      }
    }
    return null;
  }

  /**
   * 获取类中所有不被忽略的字段
   */
  static List<Field> getAllNotIgnoreField(Class clazz) {
    List<Field> fields = CommonUtil.getAllFields(clazz);
    List<Field> temp = new ArrayList<>();
    if (fields != null && fields.size() > 0) {
      for (Field f : fields) {
        f.setAccessible(true);
        if (!isIgnore(f)) {
          temp.add(f);
        }
      }
      return temp;
    } else {
      return null;
    }
  }

  /**
   * 列表数据转字符串
   *
   * @param field list反射字段
   */
  static String list2Str(DbEntity dbEntity, Field field) throws IllegalAccessException {
    List list = (List) field.get(dbEntity);
    if (list == null || list.isEmpty()) return "";
    StringBuilder sb = new StringBuilder();
    for (Object aList : list) {
      sb.append(aList).append("$$");
    }
    return sb.toString();
  }

  /**
   * 字符串转列表
   *
   * @param str 数据库中的字段
   * @return 如果str为null，则返回null
   */
  static List str2List(String str, Field field) {
    if (TextUtils.isEmpty(str)) return null;
    String[] datas = str.split("\\$\\$");
    List list = new ArrayList();
    Class clazz = CommonUtil.getListParamType(field);
    if (clazz != null) {
      String type = clazz.getName();
      for (String data : datas) {
        list.add(checkData(type, data));
      }
    }

    return list;
  }

  /**
   * 字符串转Map，只支持
   * <pre>
   *   {@code Map<String, String>}
   * </pre>
   */
  static Map<String, String> str2Map(String str) {
    Map<String, String> map = new HashMap<>();
    if (TextUtils.isEmpty(str)) {
      return map;
    }
    boolean isDecode = false;
    if (str.endsWith("_&_decode_&_")) {
      isDecode = true;
      str = str.substring(0, str.length() - 12);
    }
    String[] element = str.split(",");
    for (String data : element) {
      String[] s = data.split("\\$");
      if (isDecode) {
        map.put(CommonUtil.decryptBASE64(s[0]), CommonUtil.decryptBASE64(s[1]));
      } else {
        map.put(s[0], s[1]);
      }
    }
    return map;
  }

  /**
   * Map转字符串，只支持
   * <pre>
   *   {@code Map<String, String>}
   * </pre>
   */
  static String map2Str(Map<String, String> map) {
    StringBuilder sb = new StringBuilder();
    Set<String> keys = map.keySet();
    for (String key : keys) {
      sb.append(CommonUtil.encryptBASE64(key))
          .append("$")
          .append(CommonUtil.encryptBASE64(map.get(key)))
          .append(",");
    }
    String str = sb.toString();
    str = TextUtils.isEmpty(str) ? str : str.substring(0, str.length() - 1);
    //3.3.10版本之前没有decode，需要加标志
    if (map.size() != 0) {
      str += "_&_decode_&_";
    }
    return str;
  }

  /**
   * shadow$_klass_、shadow$_monitor_、{@link Ignore}、rowID、{@link Field#isSynthetic()}、{@link
   * Modifier#isFinal(int)}、{@link Modifier#isStatic(int)}将被忽略
   *
   * @return true 忽略该字段
   */
  static boolean isIgnore(Field field) {
    // field.isSynthetic(), 使用as热启动App时，AS会自动给你的class添加change字段
    Ignore ignore = field.getAnnotation(Ignore.class);
    int modifiers = field.getModifiers();
    String fieldName = field.getName();
    return (ignore != null && ignore.value()) || fieldName.equals("rowID") || fieldName.equals(
        AriaConfig.IGNORE_CLASS_KLASS) || fieldName.equals(AriaConfig.IGNORE_CLASS_MONITOR)
        || field.isSynthetic() || Modifier
        .isStatic(modifiers) || Modifier.isFinal(modifiers);
  }

  /**
   * 判断是否是Wrapper注解
   *
   * @return {@code true} 是
   */
  static boolean isWrapper(Class<? extends AbsDbWrapper> clazz) {
    Wrapper w = clazz.getAnnotation(Wrapper.class);
    return w != null;
  }

  /**
   * 判断是否一对多注解
   */
  static boolean isMany(Field field) {
    Many oneToMany = field.getAnnotation(Many.class);
    return oneToMany != null;
  }

  /**
   * 判断是否是一对一注解
   */
  static boolean isOne(Field field) {
    One oneToOne = field.getAnnotation(One.class);
    return oneToOne != null;
  }

  /**
   * 判断是否是主键约束
   *
   * @return {@code true}主键约束
   */
  static boolean isPrimary(Field field) {
    Primary pk = field.getAnnotation(Primary.class);
    return pk != null;
  }

  /**
   * 判断是否是外键约束
   *
   * @return {@code true}外键约束
   */
  static boolean isForeign(Field field) {
    Foreign fk = field.getAnnotation(Foreign.class);
    return fk != null;
  }

  /**
   * 判断是否是非空约束
   *
   * @return {@code true}为非空约束
   */
  static boolean isNoNull(Field field) {
    NoNull nn = field.getAnnotation(NoNull.class);
    return nn != null;
  }

  /**
   * 判断是否是default
   *
   * @return {@code true}为default
   */
  static boolean isDefault(Field field) {
    Default nn = field.getAnnotation(Default.class);
    return nn != null;
  }

  /**
   * 判断是否是Unique
   *
   * @return {@code true}为Unique
   */
  static boolean isUnique(Field field) {
    Unique nn = field.getAnnotation(Unique.class);
    return nn != null;
  }

  private static Object checkData(String type, String data) {
    if (type.equalsIgnoreCase("java.lang.String")) {
      return data;
    } else if (type.equalsIgnoreCase("int") || type.equals("java.lang.Integer")) {
      return Integer.parseInt(data);
    } else if (type.equalsIgnoreCase("double") || type.equals("java.lang.Double")) {
      return Double.parseDouble(data);
    } else if (type.equalsIgnoreCase("float") || type.equals("java.lang.Float")) {
      return Float.parseFloat(data);
    }
    return null;
  }
}
