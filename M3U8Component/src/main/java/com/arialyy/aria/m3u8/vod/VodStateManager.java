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
package com.arialyy.aria.m3u8.vod;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.processor.ITsMergeHandler;
import com.arialyy.aria.core.task.ThreadTask;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.exception.AriaM3U8Exception;
import com.arialyy.aria.m3u8.BaseM3U8Loader;
import com.arialyy.aria.m3u8.M3U8Listener;
import com.arialyy.aria.m3u8.M3U8TaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * m3u8 点播下载状态管理器
 */
public final class VodStateManager implements IThreadStateManager {
  private final String TAG = CommonUtil.getClassName(getClass());

  private M3U8Listener listener;
  private int startThreadNum;    // 启动的线程总数
  private AtomicInteger cancelNum = new AtomicInteger(0); // 已经取消的线程的数
  private AtomicInteger stopNum = new AtomicInteger(0);  // 已经停止的线程数
  private AtomicInteger failNum = new AtomicInteger(0);  // 失败的线程数
  private long progress;
  private TaskRecord taskRecord; // 任务记录
  private Looper looper;
  private DTaskWrapper wrapper;
  private M3U8TaskOption m3U8Option;
  private M3U8VodLoader loader;

  /**
   * @param listener 任务事件
   */
  VodStateManager(DTaskWrapper wrapper, M3U8Listener listener) {
    this.wrapper = wrapper;
    this.listener = listener;
    m3U8Option = (M3U8TaskOption) wrapper.getM3u8Option();
    progress = wrapper.getEntity().getCurrentProgress();
  }

  private Handler.Callback callback = new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      int peerIndex = msg.getData().getInt(ISchedulers.DATA_M3U8_PEER_INDEX);
      switch (msg.what) {
        case STATE_STOP:
          stopNum.getAndIncrement();
          removeSignThread((ThreadTask) msg.obj);
          // 处理跳转位置后，恢复任务
          if (loader.isJump()
              && (stopNum.get() == loader.getCurrentFlagSize() || loader.getCurrentFlagSize() == 0)
              && !loader.isBreak()) {
            loader.resumeTask();
            return true;
          }

          if (loader.isBreak()) {
            ALog.d(TAG, String.format("vod任务【%s】停止", loader.getTempFile().getName()));
            quitLooper();
          }
          break;
        case STATE_CANCEL:
          cancelNum.getAndIncrement();
          removeSignThread((ThreadTask) msg.obj);

          if (loader.isBreak()) {
            ALog.d(TAG, String.format("vod任务【%s】取消", loader.getTempFile().getName()));
            quitLooper();
          }
          break;
        case STATE_FAIL:
          failNum.getAndIncrement();
          for (ThreadRecord tr : taskRecord.threadRecords) {
            if (tr.threadId == peerIndex) {
              loader.getBeforePeer().put(peerIndex, tr);
              break;
            }
          }

          getListener().onPeerFail(wrapper.getKey(),
              msg.getData().getString(ISchedulers.DATA_M3U8_PEER_PATH), peerIndex);
          if (isFail()) {
            ALog.d(TAG, String.format("vod任务【%s】失败", loader.getTempFile().getName()));
            Bundle b = msg.getData();
            listener.onFail(b.getBoolean(DATA_RETRY, true),
                (AriaException) b.getSerializable(DATA_ERROR_INFO));
            quitLooper();
          }
          break;
        case STATE_COMPLETE:
          if (loader.isBreak()) {
            quitLooper();
          }
          loader.setCompleteNum(loader.getCompleteNum() + 1);
          // 正在切换位置时，切片完成，队列减小
          if (loader.isJump()) {
            loader.setCurrentFlagSize(loader.getCurrentFlagSize() - 1);
            if (loader.getCurrentFlagSize() < 0) {
              loader.setCurrentFlagSize(0);
            }
          }

          removeSignThread((ThreadTask) msg.obj);
          getListener().onPeerComplete(wrapper.getKey(),
              msg.getData().getString(ISchedulers.DATA_M3U8_PEER_PATH), peerIndex);
          handlerPercent();
          if (!loader.isJump()) {
            loader.notifyWaitLock(true);
          }
          if (isComplete()) {
            handleTaskComplete();
          }
          break;
        case STATE_RUNNING:
          Bundle b = msg.getData();
          if (b != null) {
            long len = b.getLong(IThreadStateManager.DATA_ADD_LEN, 0);
            progress += len;
          }
          break;
      }
      return true;
    }
  };

  /**
   * 处理m3u8以完成
   */
  void handleTaskComplete() {
    ALog.d(TAG, String.format(
        "startThreadNum = %s, stopNum = %s, cancelNum = %s, failNum = %s, completeNum = %s, flagQueueSize = %s",
        startThreadNum, stopNum, cancelNum, failNum, loader.getCompleteNum(),
        loader.getCurrentFlagSize()));
    ALog.d(TAG, String.format("vod任务【%s】完成", loader.getTempFile().getName()));

    if (m3U8Option.isGenerateIndexFile()) {
      if (loader.generateIndexFile(false)) {
        listener.onComplete();
      } else {
        listener.onFail(false, new AriaM3U8Exception("创建索引文件失败"));
      }
    } else if (m3U8Option.isMergeFile()) {
      if (mergeFile()) {
        listener.onComplete();
      } else {
        listener.onFail(false, null);
      }
    } else {
      listener.onComplete();
    }
    quitLooper();
  }

  void updateStateCount() {
    cancelNum.set(0);
    stopNum.set(0);
    failNum.set(0);
  }

  @Override public void setLooper(TaskRecord taskRecord, Looper looper) {
    this.looper = looper;
    this.taskRecord = taskRecord;
    for (ThreadRecord record : taskRecord.threadRecords) {
      if (!record.isComplete) {
        startThreadNum++;
      }
    }
  }

  @Override public Handler.Callback getHandlerCallback() {
    return callback;
  }

  private DownloadEntity getEntity() {
    return wrapper.getEntity();
  }

  private M3U8Listener getListener() {
    return listener;
  }

  void setVodLoader(M3U8VodLoader loader) {
    this.loader = loader;
  }

  /**
   * 退出looper循环
   */
  private void quitLooper() {
    ALog.d(TAG, "quitLooper");
    looper.quit();
  }

  private void removeSignThread(ThreadTask threadTask) {
    loader.getTaskList().remove(threadTask);
    ThreadTaskManager.getInstance().removeSingleTaskThread(wrapper.getKey(), threadTask);
  }

  /**
   * 设置进度
   */
  private void handlerPercent() {
    int completeNum = m3U8Option.getCompleteNum();
    completeNum++;
    m3U8Option.setCompleteNum(completeNum);
    int percent = completeNum * 100 / taskRecord.threadRecords.size();
    getEntity().setPercent(percent);
    getEntity().update();
  }

  @Override public boolean isFail() {
    printInfo("isFail");
    return failNum.get() != 0 && failNum.get() == loader.getCurrentFlagSize() && !loader.isJump();
  }

  @Override public boolean isComplete() {
    if (m3U8Option.isIgnoreFailureTs()) {
      return loader.getCompleteNum() + failNum.get() >= taskRecord.threadRecords.size()
          && !loader.isJump();
    } else {
      return loader.getCompleteNum() == taskRecord.threadRecords.size() && !loader.isJump();
    }
  }

  @Override public long getCurrentProgress() {
    return progress;
  }

  @Override public void updateCurrentProgress(long currentProgress) {
    progress = currentProgress;
  }

  private void printInfo(String tag) {
    if (false) {
      ALog.d(tag, String.format(
          "startThreadNum = %s, stopNum = %s, cancelNum = %s, failNum = %s, completeNum = %s, flagQueueSize = %s",
          startThreadNum, stopNum, cancelNum, failNum, loader.getCompleteNum(),
          loader.getCurrentFlagSize()));
    }
  }

  /**
   * 合并文件
   *
   * @return {@code true} 合并成功，{@code false}合并失败
   */
  private boolean mergeFile() {
    ITsMergeHandler mergeHandler = m3U8Option.getMergeHandler();
    String cacheDir = loader.getCacheDir();
    List<String> partPath = new ArrayList<>();
    for (ThreadRecord tr : taskRecord.threadRecords) {
      partPath.add(BaseM3U8Loader.getTsFilePath(cacheDir, tr.threadId));
    }
    boolean isSuccess;
    if (mergeHandler != null) {
      isSuccess = mergeHandler.merge(getEntity().getM3U8Entity(), partPath);

      if (mergeHandler.getClass().isAnonymousClass()) {
        m3U8Option.setMergeHandler(null);
      }
    } else {
      isSuccess = FileUtil.mergeFile(taskRecord.filePath, partPath);
    }
    if (isSuccess) {
      // 合并成功，删除缓存文件
      File[] files = new File(cacheDir).listFiles();
      for (File f : files) {
        if (f.exists()) {
          f.delete();
        }
      }
      File cDir = new File(cacheDir);
      if (cDir.exists()) {
        cDir.delete();
      }
      return true;
    } else {
      ALog.e(TAG, "合并失败");
      return false;
    }
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }
}
