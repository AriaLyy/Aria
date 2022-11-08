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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2015/11/2. 所有数据库实体父类
 */
public abstract class DbEntity {
  private static final Object LOCK = new Object();
  public long rowID = -1;

  protected DbEntity() {

  }

  protected long getRowID() {
    return rowID;
  }

  /**
   * 查询关联数据
   * <code>
   * DbEntity.findRelationData(DGEntityWrapper.class, "downloadUrl=?", downloadUrl);
   * </code>
   *
   * @param expression 查询条件
   */
  public static <T extends AbsDbWrapper> List<T> findRelationData(Class<T> clazz,
      String... expression) {
    return DelegateWrapper.getInstance().findRelationData(clazz, expression);
  }

  /**
   * 分页查询关联数据
   *
   * <code>
   * DbEntity.findRelationData(DGEntityWrapper.class, 0, 10, "downloadUrl=?", downloadUrl);
   * </code>
   *
   * @param expression 查询条件
   * @param page 需要查询的页数，从1开始，如果page小于1 或 num 小于1，返回null
   * @param num 每页返回的数量
   * @return 没有数据返回null，如果页数大于总页数，返回null
   */
  public static <T extends AbsDbWrapper> List<T> findRelationData(Class<T> clazz, int page, int num,
      String... expression) {
    if (page < 1 || num < 1) {
      return null;
    }
    return DelegateWrapper.getInstance().findRelationData(clazz, page, num, expression);
  }

  /**
   * 检查某个字段的值是否存在
   *
   * @param expression 字段和值"downloadPath=?"
   * @return {@code true}该字段的对应的value已存在
   */
  public static boolean checkDataExist(Class clazz, String... expression) {
    return DelegateWrapper.getInstance().checkDataExist(clazz, expression);
  }

  /**
   * 清空表数据
   */
  public static <T extends DbEntity> void clean(Class<T> clazz) {
    DelegateWrapper.getInstance().clean(clazz);
  }

  /**
   * 直接执行sql语句
   */
  public static void exeSql(String sql) {
    DelegateWrapper.getInstance().exeSql(sql);
  }

  /**
   * 查询所有数据
   *
   * @return 没有数据返回null
   */
  public static <T extends DbEntity> List<T> findAllData(Class<T> clazz) {
    return DelegateWrapper.getInstance().findAllData(clazz);
  }

  /**
   * 查询第一条数据
   */
  public static <T extends DbEntity> T findFirst(Class<T> clazz) {
    List<T> list = findAllData(clazz);
    return (list == null || list.size() == 0) ? null : list.get(0);
  }

  /**
   * 查询一组数据
   * <code>
   * DbEntity.findFirst(DownloadEntity.class, "downloadUrl=?", downloadUrl);
   * </code>
   *
   * @return 没有数据返回null
   */
  public static <T extends DbEntity> List<T> findDatas(Class<T> clazz, String... expression) {
    return DelegateWrapper.getInstance().findData(clazz, expression);
  }

  /**
   * 分页查询数据
   * <code>
   * DbEntity.findFirst(DownloadEntity.class, 0, 10, "downloadUrl=?", downloadUrl);
   * </code>
   *
   * @param page 需要查询的页数，从1开始，如果page小于1 或 num 小于1，返回null
   * @param num 每页返回的数量
   * @return 没有数据返回null，如果页数大于总页数，返回null
   */
  public static <T extends DbEntity> List<T> findDatas(Class<T> clazz, int page, int num,
      String... expression) {
    if (page < 1 || num < 1) {
      return null;
    }
    return DelegateWrapper.getInstance().findData(clazz, page, num, expression);
  }

  /**
   * 模糊查询一组数据
   * <code>
   * DbEntity.findDataByFuzzy(DownloadEntity.class, "downloadUrl like http://");
   * </code>
   *
   * @return 没有数据返回null
   */
  public static <T extends DbEntity> List<T> findDataByFuzzy(Class<T> clazz, String conditions) {
    return DelegateWrapper.getInstance().findDataByFuzzy(clazz, conditions);
  }

  /**
   * 模糊查询一组数据
   * <code>
   * DbEntity.findDataByFuzzy(DownloadEntity.class, 0, 10, "downloadUrl like http://");
   * </code>
   *
   * @param page 需要查询的页数，从1开始，如果page小于1 或 num 小于1，返回null
   * @param num 每页返回的数量
   * @return 没有数据返回null，如果页数大于总页数，返回null
   */
  public static <T extends DbEntity> List<T> findDataByFuzzy(Class<T> clazz, int page, int num,
      String conditions) {
    return DelegateWrapper.getInstance().findDataByFuzzy(clazz, page, num, conditions);
  }

  /**
   * 查询一行数据
   * <code>
   * DbEntity.findFirst(DownloadEntity.class, "downloadUrl=?", downloadUrl);
   * </code>
   *
   * @return 没有数据返回null
   */
  public static <T extends DbEntity> T findFirst(Class<T> clazz, String... expression) {
    DelegateWrapper util = DelegateWrapper.getInstance();
    List<T> datas = util.findData(clazz, expression);
    return datas == null ? null : datas.size() > 0 ? datas.get(0) : null;
  }

  /**
   * 插入多条数据
   */
  public static <T extends DbEntity> void insertManyData(List<T> entities) {
    checkListData(entities);
    DelegateWrapper.getInstance().insertManyData(entities);
  }

  /**
   * 修改多条数据
   */
  public static <T extends DbEntity> void updateManyData(List<T> entities) {
    checkListData(entities);
    DelegateWrapper.getInstance().updateManyData(entities);
  }

  /**
   * 保存多条数据，通过rowID来判断记录存在以否，如果数据库已有记录，则更新该记录；如果数据库中没有记录，则保存该记录
   */
  public static <T extends DbEntity> void saveAll(List<T> entities) {
    checkListData(entities);
    List<T> insertD = new ArrayList<>();
    List<T> updateD = new ArrayList<>();
    DelegateWrapper wrapper = DelegateWrapper.getInstance();
    for (T entity : entities) {
      if (entity.rowID == -1) {
        insertD.add(entity);
        continue;
      }
      if (wrapper.isExist(entity.getClass(), entity.rowID)) {
        updateD.add(entity);
      } else {
        insertD.add(entity);
      }
    }
    if (!insertD.isEmpty()) {
      wrapper.insertManyData(insertD);
    }
    if (!updateD.isEmpty()) {
      wrapper.updateManyData(updateD);
    }
  }

  /**
   * 检查批量操作的列表数据，如果数据为空，抛出{@link NullPointerException}
   */
  private static <T extends DbEntity> void checkListData(List<T> entities) {
    if (entities == null || entities.isEmpty()) {
      throw new NullPointerException("列表数据为空");
    }
  }

  /**
   * 删除当前数据
   */
  public void deleteData() {
    deleteData(getClass(), "rowid=?", rowID + "");
  }

  /**
   * 根据条件删除数据
   * <code>
   * DbEntity.deleteData(DownloadEntity.class, "downloadUrl=?", downloadUrl);
   * </code>
   */
  public static <T extends DbEntity> void deleteData(Class<T> clazz, String... expression) {
    DelegateWrapper util = DelegateWrapper.getInstance();
    util.delData(clazz, expression);
  }

  /**
   * 修改数据
   */
  public void update() {
    DelegateWrapper.getInstance().updateData(this);
  }

  /**
   * 保存自身，如果表中已经有数据，则更新数据，否则插入数据 只有 target中checkEntity成功后才能保存，创建实体部分也不允许保存
   */
  public void save() {
    synchronized (LOCK) {
      if (thisIsExist()) {
        update();
      } else {
        insert();
      }
    }
  }

  /**
   * 查找数据在表中是否存在
   */
  private boolean thisIsExist() {
    DelegateWrapper util = DelegateWrapper.getInstance();
    return rowID != -1 && util.isExist(getClass(), rowID);
  }

  /**
   * 表是否存在
   *
   * @return {@code true} 存在
   */
  public static boolean tableExists(Class<DbEntity> clazz) {
    return DelegateWrapper.getInstance().tableExists(clazz);
  }

  /**
   * 插入数据，只有 target中checkEntity成功后才能插入，创建实体部分也不允许操作
   */
  public void insert() {
    DelegateWrapper.getInstance().insertData(this);
  }
}