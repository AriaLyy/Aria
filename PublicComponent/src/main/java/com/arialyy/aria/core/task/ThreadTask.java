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
package com.arialyy.aria.core.task;

import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.ErrorHelp;
import com.arialyy.aria.util.FileUtil;
import com.arialyy.aria.util.NetUtils;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lyy on 2017/1/18. 任务线程
 */
public class ThreadTask implements IThreadTask, IThreadTaskObserver {
  /**
   * 线程重试次数
   */
  private final int RETRY_NUM = 2;
  private final String TAG = CommonUtil.getClassName(getClass());
  private AbsEntity mEntity;
  protected AbsTaskWrapper mTaskWrapper;
  private int mFailTimes = 0;
  private long mLastSaveTime, mLastSendProgressTime;
  private boolean isNotNetRetry;  //断网情况是否重试
  private boolean taskBreak = false;  //任务跳出
  private boolean isDestroy = false;
  protected boolean isCancel = false, isStop = false;
  private ExecutorService mConfigThreadPool;
  private Handler mStateHandler;
  private SubThreadConfig mConfig;
  /**
   * 当前线程的下去区间的进度
   */
  private long mRangeProgress, mLastRangeProgress;
  private IThreadTaskAdapter mAdapter;
  private ThreadRecord mRecord;
  private String mThreadName;
  private long updateInterval; // 更新间隔

  private Thread mConfigThread = new Thread(new Runnable() {
    @Override public void run() {
      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
      final long currentTemp = mRangeProgress;
      writeConfig(false, currentTemp);
    }
  });

  public ThreadTask(SubThreadConfig config) {
    mConfig = config;
    mTaskWrapper = config.taskWrapper;
    mRecord = config.record;
    mStateHandler = config.stateHandler;
    mEntity = mTaskWrapper.getEntity();
    mLastSaveTime = System.currentTimeMillis();
    mConfigThreadPool = Executors.newCachedThreadPool();
    isNotNetRetry = AriaConfig.getInstance().getAConfig().isNotNetRetry();
    mRangeProgress = mRecord.startLocation;
    mLastRangeProgress = mRangeProgress;
    updateInterval = config.updateInterval;
    checkFileExist();
  }

  private void checkFileExist() {
    if (!getConfig().tempFile.exists()) {
      FileUtil.createFile(getConfig().tempFile);
    }
  }

  /**
   * 设置线程任务适配器
   */
  public void setAdapter(IThreadTaskAdapter adapter) {
    mAdapter = adapter;
    mAdapter.attach(this);
  }

  /**
   * 当前线程处理的文件名
   */
  protected String getFileName() {
    return mConfig.tempFile.getName();
  }

  public SubThreadConfig getConfig() {
    return mConfig;
  }

  /**
   * 设置线程是否中断
   */
  @Override
  public void destroy() {
    this.isDestroy = true;
  }

  /**
   * 线程是否存活
   *
   * @return {@code true}存活
   */
  @Override
  public boolean isLive() {
    return !Thread.currentThread().isInterrupted() && !isDestroy;
  }

  /**
   * 当前线程是否完成，对于不支持断点的任务，一律未完成 {@code true} 完成；{@code false} 未完成
   */
  @Override
  public boolean isThreadComplete() {
    return mRecord.isComplete;
  }

  /**
   * 获取实体
   */
  protected AbsEntity getEntity() {
    return mEntity;
  }

  /**
   * 获取任务驱动对象
   */
  protected ITaskWrapper getTaskWrapper() {
    return mTaskWrapper;
  }

  /**
   * 设置当前线程最大下载速度
   *
   * @param speed 单位为：kb
   */
  @Override
  public void setMaxSpeed(int speed) {
    if (mAdapter != null) {
      mAdapter.setMaxSpeed(speed);
    }
  }

  /**
   * 中断任务
   */
  @Override
  public void breakTask() {
    taskBreak = true;
    if (mTaskWrapper.isSupportBP()) {
      final long currentTemp = mRangeProgress;
      updateState(IThreadStateManager.STATE_STOP, null);
      ALog.d(TAG, String.format("任务【%s】thread__%s__中断【停止位置：%s】", getFileName(),
          mRecord.threadId, currentTemp));
      writeConfig(false, currentTemp);
    } else {
      ALog.i(TAG, String.format("任务【%s】已中断", getFileName()));
    }
  }

  @Override
  public boolean isDestroy() {
    return Thread.currentThread().isInterrupted();
  }

  @Override protected void finalize() throws Throwable {
    super.finalize();
    if (mConfigThreadPool != null) {
      mConfigThreadPool.shutdown();
    }
  }

  /**
   * 任务是否中断，中断条件：
   * 1、任务取消
   * 2、任务停止
   * 3、手动中断 {@link #taskBreak}
   *
   * @return {@code true} 中断，{@code false} 不是中断
   */
  @Override
  public boolean isBreak() {
    return isCancel || isStop || taskBreak;
  }

  /**
   * 检查下载完成的分块大小，如果下载完成的分块大小大于或小于分配的大小，则需要重新下载该分块 如果是非分块任务，直接返回{@code true}
   *
   * @return {@code true} 分块分大小正常，{@code false} 分块大小错误
   */
  @Override
  public boolean checkBlock() {
    if (!mConfig.isBlock) {
      return true;
    }
    File blockFile = mConfig.tempFile;
    if (!blockFile.exists() || blockFile.length() != mRecord.blockLen) {
      ALog.i(TAG,
          String.format("分块【%s】错误，blockFileLen: %s, threadRect: %s; 即将重新下载该分块, 开始位置：%s，结束位置：%s",
              blockFile.getName(), blockFile.length(), mRecord.blockLen, mRecord.startLocation,
              mRecord.endLocation));
      if (blockFile.exists()) {
        FileUtil.deleteFile(blockFile);
        ALog.i(TAG, String.format("删除分块【%s】成功", blockFile.getName()));
      }
      retryBlockTask(isBreak());
      return false;
    }
    return true;
  }

  @Override public int getThreadId() {
    return mRecord.threadId;
  }

  @Override public String getThreadName() {
    return mThreadName == null ? (mThreadName =
        CommonUtil.getThreadName(getEntity().getKey(), getThreadId())) : mThreadName;
  }

  /**
   * 停止任务
   */
  @Override
  public void stop() {
    isStop = true;
    final long stopLocation = mRangeProgress;
    updateState(IThreadStateManager.STATE_STOP, null);
    if (mTaskWrapper.getRequestType() == ITaskWrapper.M3U8_VOD) {
      writeConfig(false, getConfig().tempFile.length());
      ALog.i(TAG, String.format("任务【%s】已停止", getFileName()));
      return;
    }
    if (mTaskWrapper.isSupportBP()) {
      ALog.d(TAG, String.format("任务【%s】thread__%s__停止【当前线程停止位置：%s】", getFileName(),
          mRecord.threadId, stopLocation));
      writeConfig(false, stopLocation);
      return;
    }
    ALog.i(TAG, String.format("任务【%s】已停止", getFileName()));
  }

  /**
   * 发送状态给状态处理器
   *
   * @param state {@link IThreadStateManager#STATE_STOP}..
   * @param bundle 而外数据
   */
  @Override
  public synchronized void updateState(int state, Bundle bundle) {
    Message msg = mStateHandler.obtainMessage();
    if (bundle == null) {
      bundle = new Bundle();
    }
    msg.setData(bundle);
    bundle.putString(IThreadStateManager.DATA_THREAD_NAME, getThreadName());
    bundle.putLong(IThreadStateManager.DATA_THREAD_LOCATION, mRangeProgress);
    msg.what = state;
    int reqType = getConfig().threadType;
    if (reqType == SubThreadConfig.TYPE_M3U8_PEER) {
      sendM3U8Info(state, msg);
    }

    Thread loopThread = mStateHandler.getLooper().getThread();
    if (!loopThread.isAlive() || loopThread.isInterrupted()) {
      return;
    }
    msg.sendToTarget();
  }

  private void sendM3U8Info(int state, Message msg) {
    Bundle bundle = msg.getData();
    if (state != IThreadStateManager.STATE_UPDATE_PROGRESS) {
      msg.obj = this;
    }
    if ((state == IThreadStateManager.STATE_COMPLETE || state == IThreadStateManager.STATE_FAIL)) {
      bundle.putString(ISchedulers.DATA_M3U8_URL, getConfig().url);
      bundle.putString(ISchedulers.DATA_M3U8_PEER_PATH, getConfig().tempFile.getPath());
      bundle.putInt(ISchedulers.DATA_M3U8_PEER_INDEX, getConfig().peerIndex);
    }
  }

  @Override public synchronized void updateCompleteState() {
    ALog.i(TAG, String.format("任务【%s】线程__%s__完成, blockSize = %s", getTaskWrapper().getKey(),
        mRecord.threadId, mConfig.tempFile.length()));
    writeConfig(true, mRecord.endLocation);
    // 进度发送不是实时的，发送完成任务前，需要更新一次进度
    sendRunningState();
    updateState(IThreadStateManager.STATE_COMPLETE, null);
  }

  /**
   * 更新失败的状态
   *
   * @param needRetry 是否需要重试，一般是网络错误才需要重试
   */
  @Override public synchronized void updateFailState(AriaException e, boolean needRetry) {
    fail(mRangeProgress, e, needRetry);
  }

  /**
   * @param len 新增的长度
   */
  @Override
  public synchronized void updateProgress(long len) {
    mRangeProgress += len;
    Thread loopThread = mStateHandler.getLooper().getThread();
    if (!loopThread.isAlive() || loopThread.isInterrupted()) {
      return;
    }
    // 不需要太频繁发送，以减少消息队列的压力
    if (System.currentTimeMillis() - mLastSendProgressTime > updateInterval) {
      sendRunningState();
      mLastRangeProgress = mRangeProgress;
      mLastSendProgressTime = System.currentTimeMillis();
    }

    if (System.currentTimeMillis() - mLastSaveTime > 5000
        && mRangeProgress < mRecord.endLocation) {
      mLastSaveTime = System.currentTimeMillis();
      if (!mConfigThreadPool.isShutdown()) {
        mConfigThreadPool.execute(mConfigThread);
      }
    }
  }

  /**
   * 发送执行中的数据
   */
  private void sendRunningState() {
    Message msg = mStateHandler.obtainMessage();
    Bundle b = msg.getData();
    if (b == null) {
      b = new Bundle();
      msg.setData(b);
    }
    b.putString(IThreadStateManager.DATA_THREAD_NAME, getThreadName());
    b.putLong(IThreadStateManager.DATA_ADD_LEN, mRangeProgress - mLastRangeProgress);
    msg.what = IThreadStateManager.STATE_RUNNING;
    msg.obj = mRangeProgress;

    Thread loopThread = mStateHandler.getLooper().getThread();
    if (!loopThread.isAlive() || loopThread.isInterrupted()) {
      return;
    }
    msg.sendToTarget();
  }

  @Override
  public long getThreadProgress() {
    return mRangeProgress;
  }

  /**
   * 取消任务
   */
  @Override
  public void cancel() {
    isCancel = true;
    updateState(IThreadStateManager.STATE_CANCEL, null);
    ALog.d(TAG,
        String.format("任务【%s】thread__%s__取消", getFileName(), mRecord.threadId));
  }

  /**
   * 任务失败
   *
   * @param subCurrentLocation 当前子线程进度
   */
  protected void fail(final long subCurrentLocation, AriaException ex, boolean needRetry) {
    if (ex != null) {
      ex.printStackTrace();
    }
    if (mTaskWrapper.getRequestType() == ITaskWrapper.M3U8_VOD) {
      writeConfig(false, 0);
      retryM3U8Peer(needRetry);
      return;
    }

    if (mTaskWrapper.isSupportBP()) {
      writeConfig(false, subCurrentLocation);
      retryBlockTask(needRetry);
      return;
    }

    ALog.e(TAG, String.format("任务【%s】执行失败", getFileName()));
    ErrorHelp.saveError("", ALog.getExceptionString(ex));
    sendFailMsg(null, needRetry);
  }

  /**
   * 重试ts分片
   */
  private void retryM3U8Peer(boolean needRetry) {
    if (mConfig.ignoreFailure) {
      ALog.d(TAG, "忽略失败的切片");
      sendFailMsg(null, false);
      return;
    }
    boolean isConnected = NetUtils.isConnected(AriaConfig.getInstance().getAPP());
    if (!isConnected && !isNotNetRetry) {
      ALog.w(TAG, String.format("ts切片【%s】重试失败，网络未连接", getFileName()));
      sendFailMsg(null, false);
      return;
    }
    if (mFailTimes < RETRY_NUM
        && needRetry
        && (NetUtils.isConnected(AriaConfig.getInstance().getAPP()) || isNotNetRetry)
        && !isBreak()) {
      ALog.w(TAG, String.format("ts切片【%s】第%s重试", getFileName(), String.valueOf(mFailTimes)));
      mFailTimes++;
      FileUtil.deleteFile(mConfig.tempFile);
      FileUtil.createFile(mConfig.tempFile);
      ThreadTaskManager.getInstance().retryThread(this);
      return;
    }
    sendFailMsg(null, false);
  }

  /**
   * 重试分块线程，如果其中一条线程已经下载失败，则任务该任务下载失败，并且停止该任务的所有线程
   *
   * @param needRetry 是否可以重试
   */
  private void retryBlockTask(boolean needRetry) {
    if (!NetUtils.isConnected(AriaConfig.getInstance().getAPP()) && !isNotNetRetry) {
      ALog.w(TAG, String.format("分块【%s】重试失败，网络未连接", getFileName()));
      sendFailMsg(null, false);
      return;
    }
    if (mFailTimes < RETRY_NUM
        && needRetry
        && (NetUtils.isConnected(AriaConfig.getInstance().getAPP()) || isNotNetRetry)
        && !isBreak()) {
      ALog.w(TAG, String.format("分块【%s】第%s次重试", getFileName(), String.valueOf(mFailTimes)));
      mFailTimes++;
      handleBlockRecord();
      ThreadTaskManager.getInstance().retryThread(this);
      return;
    }
    ALog.e(TAG,
        String.format("重试分开【%s】失败，failTimes = %s, needRetry = %s, isBreak = %s",
            getFileName(), mFailTimes, needRetry, isBreak()));
    sendFailMsg(null, needRetry);
  }

  /**
   * 处理线程重试的分块记录，只有多线程任务才会执行
   * 如果是以前版本{@link BufferedRandomAccessFile}创建的下载，那么 record.startLocation不用修改
   */
  private void handleBlockRecord() {
    if (mConfig.isBlock) {
      // 默认线程分块长度
      File temp = mConfig.tempFile;

      long blockFileLen = temp.length(); // 磁盘中的分块文件长度
      long threadRect = mRecord.blockLen;     // 当前线程的区间

      if (!temp.exists()) {
        ALog.i(TAG, String.format("分块文件【%s】不存在，该分块将重新开始", temp.getName()));
        mRecord.isComplete = false;
        mRecord.startLocation = mRecord.endLocation - threadRect;
      } else {
        /*
         * 检查磁盘中的分块文件
         */
        if (blockFileLen > threadRect) {
          ALog.i(TAG, String.format("分块【%s】错误，将重新下载该分块", temp.getName()));
          FileUtil.deleteFile(temp);
          mRecord.startLocation = mRecord.endLocation - mRecord.blockLen;
          mRecord.isComplete = false;
        } else if (blockFileLen < mRecord.blockLen) {
          mRecord.startLocation = mRecord.endLocation - mRecord.blockLen + blockFileLen;
          mRecord.isComplete = false;
          updateState(IThreadStateManager.STATE_UPDATE_PROGRESS, null);
          ALog.i(TAG,
              String.format("修正分块【%s】记录，开始位置：%s，结束位置：%s", temp.getName(), mRecord.startLocation,
                  mRecord.endLocation));
        } else {
          ALog.i(TAG, String.format("分块【%s】已完成，更新记录", temp.getName()));
          mRecord.isComplete = true;
        }
      }
      mRecord.update();
    }
  }

  /**
   * 发送失败信息
   */
  private void sendFailMsg(AriaException e, boolean needRetry) {
    Bundle b = new Bundle();
    b.putBoolean(IThreadStateManager.DATA_RETRY, needRetry);
    if (e != null) {
      b.putSerializable(IThreadStateManager.DATA_ERROR_INFO, e);
    }
    updateState(IThreadStateManager.STATE_FAIL, b);
  }

  /**
   * 将记录写入到配置文件
   *
   * @param isComplete 当前线程是否完成 {@code true}完成
   * @param record 当前进度
   */
  private void writeConfig(boolean isComplete, final long record) {
    if (mRecord != null) {
      mRecord.isComplete = isComplete;
      if (mConfig.isBlock) {
        mRecord.startLocation = mConfig.tempFile.length();
      } else {
        if (0 < record && record < mRecord.endLocation) {
          mRecord.startLocation = record;
        }
      }
      mRecord.update();
    }
  }

  @Override public ThreadTask call() throws Exception {
    isDestroy = false;
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    TrafficStats.setThreadStatsTag(UUID.randomUUID().toString().hashCode());
    mAdapter.call(this);
    return this;
  }
}
