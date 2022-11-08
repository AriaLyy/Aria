/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
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

package com.arialyy.aria.core.queue.pool;

import android.text.TextUtils;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by lyy on 2016/8/14. 任务缓存池，所有下载任务最先缓存在这个池中
 */
public class BaseCachePool<TASK extends AbsTask> implements IPool<TASK> {
  private final String TAG = CommonUtil.getClassName(this);
  private static final int MAX_NUM = Integer.MAX_VALUE;  //最大下载任务数
  private static final Object LOCK = new Object();
  private Deque<TASK> mCacheQueue;

  BaseCachePool() {
    mCacheQueue = new LinkedBlockingDeque<>(MAX_NUM);
  }

  /**
   * 获取被缓存的任务
   */
  public List<TASK> getAllTask() {
    return new ArrayList<>(mCacheQueue);
  }

  /**
   * 清除所有缓存的任务
   */
  public void clear() {
    mCacheQueue.clear();
  }

  /**
   * 将任务放在队首
   */
  public boolean putTaskToFirst(TASK task) {
    return mCacheQueue.offerFirst(task);
  }

  @Override public boolean putTask(TASK task) {
    synchronized (LOCK) {
      if (task == null) {
        ALog.e(TAG, "任务不能为空！！");
        return false;
      }
      if (mCacheQueue.contains(task)) {
        ALog.w(TAG, "任务【" + task.getTaskName() + "】进入缓存队列失败，原因：已经在缓存队列中");
        return false;
      } else {
        boolean s = mCacheQueue.offer(task);
        ALog.d(TAG, "任务【" + task.getTaskName() + "】进入缓存队列" + (s ? "成功" : "失败"));
        return s;
      }
    }
  }

  @Override public TASK pollTask() {
    synchronized (LOCK) {
      return mCacheQueue.pollFirst();
    }
  }

  @Override public TASK getTask(String key) {
    synchronized (LOCK) {
      if (TextUtils.isEmpty(key)) {
        ALog.e(TAG, "key 为null");
        return null;
      }
      for (TASK task : mCacheQueue) {
        if (task.getKey().equals(key)) {
          return task;
        }
      }
    }
    return null;
  }

  @Override public boolean taskExits(String key) {
    return getTask(key) != null;
  }

  @Override public boolean removeTask(TASK task) {
    synchronized (LOCK) {
      if (task == null) {
        ALog.e(TAG, "任务不能为空");
        return false;
      } else {
        return mCacheQueue.remove(task);
      }
    }
  }

  @Override public boolean removeTask(String key) {
    synchronized (LOCK) {
      if (TextUtils.isEmpty(key)) {
        ALog.e(TAG, "请传入有效的下载链接");
        return false;
      }
      return mCacheQueue.remove(getTask(key));
    }
  }

  @Override public int size() {
    return mCacheQueue.size();
  }
}