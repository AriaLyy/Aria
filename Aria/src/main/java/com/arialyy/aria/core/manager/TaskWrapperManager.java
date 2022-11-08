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
package com.arialyy.aria.core.manager;

import android.util.LruCache;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Aria.Lao on 2017/11/1. 任务实体管理器
 */
public class TaskWrapperManager {
  private static final String TAG = "TaskWrapperManager";
  private static volatile TaskWrapperManager INSTANCE = null;
  private LruCache<String, AbsTaskWrapper> cache = new LruCache<>(1024);
  private Lock lock;

  public static TaskWrapperManager getInstance() {
    if (INSTANCE == null) {
      synchronized (TaskWrapperManager.class) {
        if (INSTANCE == null) {
          INSTANCE = new TaskWrapperManager();
        }
      }
    }
    return INSTANCE;
  }

  private TaskWrapperManager() {
    lock = new ReentrantLock();
  }

  private IGroupWrapperFactory chooseGroupFactory(Class clazz) {
    if (clazz == DGTaskWrapper.class) {
      return DGTaskWrapperFactory.getInstance();
    }
    return null;
  }

  private INormalTEFactory chooseNormalFactory(Class clazz) {
    if (clazz == DTaskWrapper.class) {
      return DTaskWrapperFactory.getInstance();
    } else if (clazz == UTaskWrapper.class) {
      return UTaskWrapperFactory.getInstance();
    }
    return null;
  }

  /**
   * 获取普通任务的Wrapper
   *
   * @return 创建失败，返回null；成功返回{@link DTaskWrapper}或者{@link UTaskWrapper}
   */
  public <TW extends AbsTaskWrapper> TW getNormalTaskWrapper(Class<TW> clazz, long taskId) {
    final Lock lock = this.lock;
    lock.lock();
    try {

      AbsTaskWrapper wrapper = cache.get(convertKey(clazz, taskId));
      if (wrapper == null || wrapper.getClass() != clazz) {
        INormalTEFactory factory = chooseNormalFactory(clazz);
        if (factory == null) {
          ALog.e(TAG, "任务实体创建失败");
          return null;
        }
        wrapper = factory.create(taskId);
        putTaskWrapper(wrapper);
      }
      return (TW) wrapper;
    } finally {
      lock.unlock();
    }
  }

  /**
   * 从缓存中获取HTTP任务组的任务实体，如果任务实体不存在，则创建任务实体 获取{}
   *
   * @param taskId 任务ID
   * @return 地址列表为null或创建实体失败，返回null；成功返回{@link DGTaskWrapper}
   */
  public <TW extends AbsTaskWrapper> TW getGroupWrapper(Class<TW> clazz, long taskId) {
    final Lock lock = this.lock;
    lock.lock();
    try {
      AbsTaskWrapper tWrapper = cache.get(convertKey(clazz, taskId));
      if (tWrapper == null || tWrapper.getClass() != clazz) {
        IGroupWrapperFactory factory = chooseGroupFactory(clazz);
        if (factory == null) {
          ALog.e(TAG, "任务实体创建失败");
          return null;
        }
        tWrapper = factory.getGroupWrapper(taskId);
        putTaskWrapper(tWrapper);
      }
      return (TW) tWrapper;
    } finally {
      lock.unlock();
    }
  }

  /**
   * 更新任务Wrapper
   */
  public void putTaskWrapper(AbsTaskWrapper wrapper) {
    if (wrapper == null) {
      ALog.e(TAG, "任务实体添加失败");
      return;
    }
    if (wrapper.getEntity() == null || wrapper.getEntity().getId() == -1) {
      return;
    }
    final Lock lock = this.lock;
    lock.lock();
    try {
      cache.put(convertKey(wrapper.getClass(), wrapper.getEntity().getId()), wrapper);
    } finally {
      lock.unlock();
    }
  }

  /**
   * 通过key删除任务实体 当任务complete或删除记录时将删除缓存
   */
  public void removeTaskWrapper(AbsTaskWrapper wrapper) {
    final Lock lock = this.lock;
    lock.lock();
    try {
      cache.remove(convertKey(wrapper.getClass(), wrapper.getEntity().getId()));
    } finally {
      lock.unlock();
    }
  }

  private String convertKey(Class clazz, long taskId) {
    return CommonUtil.keyToHashKey(clazz.getName() + taskId);
  }
}
