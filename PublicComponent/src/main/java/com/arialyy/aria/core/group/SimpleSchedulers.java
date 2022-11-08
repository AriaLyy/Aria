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

package com.arialyy.aria.core.group;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.config.Configuration;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.exception.ExceptionFactory;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.NetUtils;
import java.io.File;

/**
 * 组合任务子任务调度器，用于调度任务的开始、停止、失败、完成等情况
 * 该调度器生命周期和{@link AbsGroupLoaderUtil}生命周期一致
 */
final class SimpleSchedulers implements Handler.Callback {
  private final String TAG = CommonUtil.getClassName(this);
  private SimpleSubQueue mQueue;
  private GroupRunState mGState;
  private String mKey; // 组合任务的key

  private SimpleSchedulers(GroupRunState state, String key) {
    mQueue = state.queue;
    mGState = state;
    mKey = key;
  }

  static SimpleSchedulers newInstance(GroupRunState state, String key) {
    return new SimpleSchedulers(state, key);
  }

  @Override public boolean handleMessage(Message msg) {
    Bundle b = msg.getData();
    if (b == null) {
      ALog.w(TAG, "组合任务子任务调度数据为空");
      return true;
    }
    String threadName = b.getString(IThreadStateManager.DATA_THREAD_NAME);
    AbsSubDLoadUtil loaderUtil = mQueue.getLoaderUtil(threadName);
    if (loaderUtil == null) {
      ALog.e(TAG, String.format("子任务loader不存在，state：%s，key：%s", msg.what, threadName));
      return true;
    }
    long curLocation = b.getLong(IThreadStateManager.DATA_THREAD_LOCATION,
        loaderUtil.getLoader().getWrapper().getEntity().getCurrentProgress());
    // 处理状态
    switch (msg.what) {
      case IThreadStateManager.STATE_RUNNING:
        long range = (long) msg.obj;
        mGState.listener.onSubRunning(loaderUtil.getEntity(), range);
        break;
      case IThreadStateManager.STATE_PRE:
        mGState.listener.onSubPre(loaderUtil.getEntity());
        mGState.updateCount(loaderUtil.getKey());
        break;
      case IThreadStateManager.STATE_START:
        mGState.listener.onSubStart(loaderUtil.getEntity());
        break;
      case IThreadStateManager.STATE_STOP:
        handleStop(loaderUtil, curLocation);
        ThreadTaskManager.getInstance().removeSingleTaskThread(mKey, threadName);
        break;
      case IThreadStateManager.STATE_COMPLETE:
        handleComplete(loaderUtil);
        ThreadTaskManager.getInstance().removeSingleTaskThread(mKey, threadName);
        break;
      case IThreadStateManager.STATE_FAIL:
        boolean needRetry = b.getBoolean(IThreadStateManager.DATA_RETRY, false);
        handleFail(loaderUtil, needRetry);
        ThreadTaskManager.getInstance().removeSingleTaskThread(mKey, threadName);
        break;
    }
    return true;
  }

  /**
   * 处理子任务失败的情况
   * 1、子任务失败次数大于等于配置的重试次数，才能认为子任务停止
   * 2、stopNum + failNum + completeNum + cacheNum == subSize，则认为组合任务停止
   * 3、failNum == subSize，只有全部的子任务都失败了，才能任务组合任务失败
   *
   * @param needRetry true 需要重试，false 不需要重试
   */
  private synchronized void handleFail(final AbsSubDLoadUtil loaderUtil, boolean needRetry) {
    Log.d(TAG, String.format("handleFail, size = %s, completeNum = %s, failNum = %s, stopNum = %s",
        mGState.getSubSize(), mGState.getCompleteNum(), mGState.getFailNum(),
        mGState.getSubSize()));

    Configuration config = Configuration.getInstance();
    int num = config.dGroupCfg.getSubReTryNum();
    boolean isNotNetRetry = config.appCfg.isNotNetRetry();

    if (!needRetry
        || (!NetUtils.isConnected(AriaConfig.getInstance().getAPP()) && !isNotNetRetry)
        || loaderUtil.getLoader() == null // 如果获取不到文件信息，loader为空
        || loaderUtil.getEntity().getFailNum() > num) {
      mQueue.removeTaskFromExecQ(loaderUtil);
      mGState.listener.onSubFail(loaderUtil.getEntity(),
          ExceptionFactory.getException(ExceptionFactory.TYPE_GROUP,
              String.format("任务组子任务【%s】下载失败，下载地址【%s】", loaderUtil.getEntity().getFileName(),
                  loaderUtil.getEntity().getUrl()), null));
      mGState.countFailNum(loaderUtil.getKey());
      if (mGState.getFailNum() == mGState.getSubSize()
          || mGState.getStopNum() + mGState.getFailNum() + mGState.getCompleteNum()
          == mGState.getSubSize()) {
        mGState.isRunning.set(false);
        if (mGState.getCompleteNum() > 0
            && Configuration.getInstance().dGroupCfg.isSubFailAsStop()) {
          ALog.e(TAG, String.format("任务组【%s】停止", mGState.getGroupHash()));
          mGState.listener.onStop(mGState.getProgress());
          return;
        }
        mGState.listener.onFail(false,
            ExceptionFactory.getException(ExceptionFactory.TYPE_GROUP,
                String.format("任务组【%s】下载失败", mGState.getGroupHash()), null));
        return;
      }
      startNext();
      return;
    }
    SimpleSubRetryQueue.getInstance().offer(loaderUtil);
  }

  /**
   * 处理子任务停止的情况
   * 1、所有的子任务已经停止，则认为组合任务停止
   * 2、completeNum + failNum + stopNum = subSize，则认为组合任务停止
   */
  private synchronized void handleStop(AbsSubDLoadUtil loadUtil, long curLocation) {
    Log.d(TAG, String.format("handleStop, size = %s, completeNum = %s, failNum = %s, stopNum = %s",
        mGState.getSubSize(), mGState.getCompleteNum(), mGState.getFailNum(),
        mGState.getSubSize()));

    mGState.listener.onSubStop(loadUtil.getEntity(), curLocation);
    mGState.countStopNum(loadUtil.getKey());
    if (mGState.getStopNum() == mGState.getSubSize()
        || mGState.getStopNum()
        + mGState.getCompleteNum()
        + mGState.getFailNum()
        + mQueue.getCacheSize()
        == mGState.getSubSize()) {
      mGState.isRunning.set(false);
      mGState.listener.onStop(mGState.getProgress());
      return;
    }
    startNext();
  }

  /**
   * 处理子任务完成的情况，有以下三种情况
   * 1、已经没有缓存的子任务，并且停止的子任务是数{@link GroupRunState#getStopNum()} ()}为0，失败的子任数{@link
   * GroupRunState#getFailNum()}为0，则认为组合任务已经完成
   * 2、已经没有缓存的子任务，并且停止的子任务是数{@link GroupRunState#getCompleteNum()}不为0，或者失败的子任数{@link
   * GroupRunState#getFailNum()}不为0，则认为组合任务被停止
   * 3、只有有缓存的子任务，则任务组合任务没有完成
   */
  private synchronized void handleComplete(AbsSubDLoadUtil loader) {
    ALog.d(TAG, String.format("子任务【%s】完成", loader.getEntity().getFileName()));
    Log.d(TAG,
        String.format("handleComplete, size = %s, completeNum = %s, failNum = %s, stopNum = %s",
            mGState.getSubSize(), mGState.getCompleteNum(), mGState.getFailNum(),
            mGState.getStopNum()));

    TaskRecord record = loader.getRecord();
    if (record != null && record.isBlock) {
      File partFile =
          new File(String.format(IRecordHandler.SUB_PATH, record.filePath, 0));
      partFile.renameTo(new File(record.filePath));
    }
    ThreadTaskManager.getInstance().removeTaskThread(loader.getKey());
    mGState.listener.onSubComplete(loader.getEntity());
    mQueue.removeTaskFromExecQ(loader);
    mGState.updateCompleteNum();
    if (mGState.getCompleteNum() + mGState.getFailNum() + mGState.getStopNum()
        == mGState.getSubSize()) {
      if (mGState.getStopNum() == 0 && mGState.getFailNum() == 0) {
        mGState.listener.onComplete();
      } else if (mGState.getStopNum() == 0
          && !Configuration.getInstance().dGroupCfg.isSubFailAsStop()) {
        mGState.listener.onFail(false,
            ExceptionFactory.getException(ExceptionFactory.TYPE_GROUP,
                String.format("任务组【%s】下载失败", mGState.getGroupHash()), null));
      } else {
        mGState.listener.onStop(mGState.getProgress());
      }
      mGState.isRunning.set(false);
      return;
    }
    startNext();
  }

  /**
   * 如果有等待中的任务，则启动下一任务
   */
  private void startNext() {
    if (mQueue.isStopAll()) {
      return;
    }
    AbsSubDLoadUtil next = mQueue.getNextTask();
    if (next != null) {
      ALog.d(TAG, String.format("启动任务：%s", next.getEntity().getFileName()));
      mQueue.startTask(next);
      return;
    }
    ALog.i(TAG, "没有下一子任务");
  }
}
