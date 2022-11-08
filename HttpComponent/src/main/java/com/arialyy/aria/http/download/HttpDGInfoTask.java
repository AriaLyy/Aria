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
package com.arialyy.aria.http.download;

import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.exception.AriaHTTPException;
import com.arialyy.aria.http.HttpTaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 组合任务文件信息，用于获取长度未知时，组合任务的长度
 */
public final class HttpDGInfoTask implements IInfoTask {
  private String TAG = CommonUtil.getClassName(this);
  private Callback callback;
  private DGTaskWrapper wrapper;
  private final Object LOCK = new Object();
  private ExecutorService mPool = null;
  private boolean getLenComplete = false;
  private AtomicInteger count = new AtomicInteger();
  private AtomicInteger failCount = new AtomicInteger();
  private boolean isStop = false, isCancel = false;

  public interface DGInfoCallback extends Callback {

    /**
     * 子任务失败
     */
    void onSubFail(DownloadEntity subEntity, AriaHTTPException e, boolean needRetry);

    /**
     * 组合任务停止
     */
    void onStop(long len);
  }

  /**
   * 子任务回调
   */
  private Callback subCallback = new Callback() {
    @Override public void onSucceed(String url, CompleteInfo info) {
      count.getAndIncrement();
      checkGetSizeComplete(count.get(), failCount.get());
      ALog.d(TAG, "获取子任务信息完成");
    }

    @Override public void onFail(AbsEntity entity, AriaException e, boolean needRetry) {
      ALog.e(TAG, String.format("获取文件信息失败，url：%s", ((DownloadEntity) entity).getUrl()));
      count.getAndIncrement();
      failCount.getAndIncrement();
      ((DGInfoCallback) callback).onSubFail((DownloadEntity) entity, new AriaHTTPException(
          String.format("子任务获取文件长度失败，url：%s", ((DownloadEntity) entity).getUrl())), needRetry);
      checkGetSizeComplete(count.get(), failCount.get());
    }
  };

  HttpDGInfoTask(DGTaskWrapper wrapper) {
    this.wrapper = wrapper;
  }

  /**
   * 停止
   */
  @Override
  public void stop() {
    isStop = true;
    if (mPool != null) {
      mPool.shutdown();
    }
  }

  @Override public void cancel() {
    isCancel = true;
    if (mPool != null) {
      mPool.shutdown();
    }
  }

  @Override public void run() {
    // 如果是isUnknownSize()标志，并且获取大小没有完成，则直接回调onStop
    if (mPool != null && !getLenComplete) {
      ALog.d(TAG, "获取长度未完成的情况下，停止组合任务");
      mPool.shutdown();
      ((DGInfoCallback)callback).onStop(0);
      return;
    }
    // 处理组合任务大小未知的情况
    if (wrapper.isUnknownSize()) {
      mPool = Executors.newCachedThreadPool();
      getGroupSize();
      try {
        synchronized (LOCK) {
          LOCK.wait();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (!mPool.isShutdown()) {
        mPool.shutdown();
      }
    } else {
      for (DTaskWrapper wrapper : wrapper.getSubTaskWrapper()) {
        cloneHeader(wrapper);
      }
      callback.onSucceed(wrapper.getKey(), new CompleteInfo());
    }
  }

  /*
   * 获取组合任务大小，使用该方式获取到的组合任务大小，子任务不需要再重新获取文件大小
   */
  private void getGroupSize() {
    new Thread(new Runnable() {
      @Override public void run() {
        for (DTaskWrapper dTaskWrapper : wrapper.getSubTaskWrapper()) {
          DownloadEntity subEntity = dTaskWrapper.getEntity();
          if (subEntity.getFileSize() > 0) {
            count.getAndIncrement();
            if (subEntity.getCurrentProgress() < subEntity.getFileSize()) {
              // 如果没有完成需要拷贝一份数据
              cloneHeader(dTaskWrapper);
            }
            checkGetSizeComplete(count.get(), failCount.get());
            continue;
          }
          cloneHeader(dTaskWrapper);
          HttpDFileInfoTask infoTask = new HttpDFileInfoTask(dTaskWrapper);
          infoTask.setCallback(subCallback);
          mPool.execute(infoTask);
        }
      }
    }).start();
  }

  /**
   * 检查组合任务大小是否获取完成，获取完成后取消阻塞，并设置组合任务大小
   */
  private void checkGetSizeComplete(int count, int failCount) {
    if (isStop || isCancel) {
      ALog.w(TAG, "任务已停止或已取消，isStop = " + isStop + ", isCancel = " + isCancel);
      notifyLock();
      return;
    }
    if (failCount == wrapper.getSubTaskWrapper().size()) {
      callback.onFail(wrapper.getEntity(), new AriaHTTPException("获取子任务长度失败"), false);
      notifyLock();
      return;
    }
    if (count == wrapper.getSubTaskWrapper().size()) {
      long size = 0;
      for (DTaskWrapper wrapper : wrapper.getSubTaskWrapper()) {
        size += wrapper.getEntity().getFileSize();
      }
      wrapper.getEntity().setConvertFileSize(CommonUtil.formatFileSize(size));
      wrapper.getEntity().setFileSize(size);
      wrapper.getEntity().update();
      getLenComplete = true;
      ALog.d(TAG, String.format("获取组合任务长度完成，组合任务总长度：%s，失败的子任务数：%s", size, failCount));
      callback.onSucceed(wrapper.getKey(), new CompleteInfo());
      notifyLock();
    }
  }

  private void notifyLock() {
    synchronized (LOCK) {
      LOCK.notifyAll();
    }
  }

  /**
   * 子任务使用父包裹器的属性
   */
  private void cloneHeader(DTaskWrapper taskWrapper) {
    HttpTaskOption groupOption = (HttpTaskOption) wrapper.getTaskOption();
    HttpTaskOption subOption = new HttpTaskOption();

    // 设置属性
    subOption.setFileLenAdapter(groupOption.getFileLenAdapter());
    subOption.setFileNameAdapter(groupOption.getFileNameAdapter());
    subOption.setUseServerFileName(groupOption.isUseServerFileName());

    subOption.setFileNameAdapter(groupOption.getFileNameAdapter());
    subOption.setRequestEnum(groupOption.getRequestEnum());
    subOption.setHeaders(groupOption.getHeaders());
    subOption.setProxy(groupOption.getProxy());
    subOption.setParams(groupOption.getParams());
    taskWrapper.setTaskOption(subOption);
  }

  @Override public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }
}
