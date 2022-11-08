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

import android.text.TextUtils;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程任务管理器
 */
public class ThreadTaskManager {
  private final String TAG = CommonUtil.getClassName(this);
  private static volatile ThreadTaskManager INSTANCE = null;
  private static final int CORE_POOL_NUM = 20;
  private static final ReentrantLock LOCK = new ReentrantLock();
  private ThreadPoolExecutor mExePool;
  private Map<String, Set<FutureContainer>> mThreadTasks = new ConcurrentHashMap<>();

  public static synchronized ThreadTaskManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ThreadTaskManager();
    }
    return INSTANCE;
  }

  private ThreadTaskManager() {
    mExePool = new ThreadPoolExecutor(CORE_POOL_NUM, Integer.MAX_VALUE,
        60L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>());
    mExePool.allowsCoreThreadTimeOut();
  }

  /**
   * 删除所有线程任务
   */
  public void removeAllThreadTask() {
    if (mThreadTasks.isEmpty()) {
      return;
    }
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      for (Set<FutureContainer> threads : mThreadTasks.values()) {
        for (FutureContainer container : threads) {
          if (container.future.isDone() || container.future.isCancelled()) {
            continue;
          }
          container.threadTask.destroy();
        }
        threads.clear();
      }
      mThreadTasks.clear();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * 启动线程任务
   *
   * @param key 任务对应的key{@link AbsTaskWrapper#getKey()}
   * @param threadTask 线程任务{@link IThreadTask}
   */
  public void startThread(String key, IThreadTask threadTask) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return;
      }
      key = getKey(key);
      Set<FutureContainer> temp = mThreadTasks.get(key);
      if (temp == null) {
        temp = new HashSet<>();
        mThreadTasks.put(key, temp);
      }
      FutureContainer container = new FutureContainer();
      container.threadTask = threadTask;
      container.future = mExePool.submit(threadTask);
      temp.add(container);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * 任务是否在执行
   *
   * @param key 任务的key
   * @return {@code true} 任务正在运行
   */
  public boolean taskIsRunning(String key) {
    return mThreadTasks.get(getKey(key)) != null;
  }

  /**
   * 停止任务的所有线程
   *
   * @param key 任务对应的key{@link AbsTaskWrapper#getKey()}
   */
  public void removeTaskThread(String key) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return;
      }
      key = getKey(key);
      Set<FutureContainer> temp = mThreadTasks.get(key);
      if (temp != null && temp.size() > 0) {
        for (FutureContainer container : temp) {
          if (container.future.isDone() || container.future.isCancelled()) {
            continue;
          }
          container.threadTask.destroy();
        }
        temp.clear();
        mThreadTasks.remove(key);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * 根据线程名删除任务的中的线程
   *
   * @param key 任务的key，如果是组合任务，则为组合任务的key
   * @param threadName 线程名
   * @return true 删除线程成功；false 删除线程失败
   */
  public boolean removeSingleTaskThread(String key, String threadName) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return false;
      }
      if (TextUtils.isEmpty(threadName)) {
        ALog.e(TAG, "线程名为空");
        return false;
      }

      key = getKey(key);
      Set<FutureContainer> temp = mThreadTasks.get(key);
      if (temp != null && temp.size() > 0) {
        FutureContainer tempC = null;
        for (FutureContainer container : temp) {
          if (container.threadTask.getThreadName().equals(threadName)) {
            tempC = container;
            break;
          }
        }
        if (tempC != null) {
          tempC.threadTask.destroy();
          temp.remove(tempC);
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
    return false;
  }

  /**
   * 删除单个线程任务
   *
   * @param key 任务的key
   * @param task 线程任务
   */
  public boolean removeSingleTaskThread(String key, IThreadTask task) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return false;
      }
      if (task == null) {
        ALog.e(TAG, "线程任务为空");
        return false;
      }
      key = getKey(key);
      Set<FutureContainer> temp = mThreadTasks.get(key);
      if (temp != null && temp.size() > 0) {
        FutureContainer tempC = null;
        for (FutureContainer container : temp) {
          if (container.threadTask == task) {
            tempC = container;
            break;
          }
        }
        if (tempC != null) {
          task.destroy();
          temp.remove(tempC);
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
    return false;
  }

  /**
   * 重试线程任务
   *
   * @param task 线程任务
   */
  public void retryThread(IThreadTask task) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return;
      }
      try {
        if (task == null || task.isDestroy()) {
          ALog.e(TAG, "线程为空或线程已经中断");
          return;
        }
      } catch (Exception e) {
        ALog.e(TAG, "", e);
        return;
      }
      mExePool.submit(task);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * map中的key
   *
   * @param key 任务的key{@link AbsTaskWrapper#getKey()}
   * @return 转换后的map中的key
   */
  private String getKey(String key) {
    return CommonUtil.getStrMd5(key);
  }

  private class FutureContainer {
    Future future;
    IThreadTask threadTask;
  }
}
