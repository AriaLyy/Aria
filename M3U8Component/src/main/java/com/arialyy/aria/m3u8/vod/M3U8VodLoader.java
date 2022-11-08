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

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.event.Event;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.core.event.PeerIndexEvent;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.loader.IThreadTaskBuilder;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.processor.IVodTsUrlConverter;
import com.arialyy.aria.core.task.ThreadTask;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.exception.AriaM3U8Exception;
import com.arialyy.aria.m3u8.BaseM3U8Loader;
import com.arialyy.aria.m3u8.M3U8Listener;
import com.arialyy.aria.m3u8.M3U8TaskOption;
import com.arialyy.aria.m3u8.M3U8ThreadTaskAdapter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * M3U8点播文件下载器
 */
final class M3U8VodLoader extends BaseM3U8Loader {
  /**
   * 最大执行数
   */
  private int EXEC_MAX_NUM;
  private Handler mStateHandler;
  private ArrayBlockingQueue<TempFlag> mFlagQueue;
  private ArrayBlockingQueue<PeerIndexEvent> mJumpQueue;
  private ReentrantLock LOCK = new ReentrantLock();
  private ReentrantLock EVENT_LOCK = new ReentrantLock();
  private ReentrantLock JUMP_LOCK = new ReentrantLock();
  private Condition mWaitCondition = LOCK.newCondition();
  private Condition mEventQueueCondition = EVENT_LOCK.newCondition();
  private Condition mJumpCondition = JUMP_LOCK.newCondition();
  private SparseArray<ThreadRecord> mBeforePeer = new SparseArray<>();
  private SparseArray<ThreadRecord> mAfterPeer = new SparseArray<>();
  private PeerIndexEvent mCurrentEvent;
  private String mCacheDir;
  private AtomicInteger afterPeerIndex = new AtomicInteger();
  private AtomicInteger beforePeerIndex = new AtomicInteger();
  private AtomicInteger mCompleteNum = new AtomicInteger();
  private AtomicInteger mCurrentFlagSize = new AtomicInteger();
  private boolean isJump = false, isDestroy = false;
  private ExecutorService mJumpThreadPool;
  private Thread jumpThread = null;
  private M3U8TaskOption mM3U8Option;
  private Looper mLooper;

  M3U8VodLoader(DTaskWrapper wrapper, M3U8Listener listener) {
    super(wrapper, listener);
    mM3U8Option = (M3U8TaskOption) wrapper.getM3u8Option();
    mFlagQueue = new ArrayBlockingQueue<>(mM3U8Option.getMaxTsQueueNum());
    EXEC_MAX_NUM = mM3U8Option.getMaxTsQueueNum();
    mJumpQueue = new ArrayBlockingQueue<>(10);
    EventMsgUtil.getDefault().register(this);
  }

  @Override protected M3U8Listener getListener() {
    return (M3U8Listener) super.getListener();
  }

  SparseArray<ThreadRecord> getBeforePeer() {
    return mBeforePeer;
  }

  int getCompleteNum() {
    return mCompleteNum.get();
  }

  void setCompleteNum(int completeNum) {
    mCompleteNum.set(completeNum);
  }

  int getCurrentFlagSize() {
    mCurrentFlagSize.set(mFlagQueue.size());
    return mCurrentFlagSize.get();
  }

  void setCurrentFlagSize(int currentFlagSize) {
    mCurrentFlagSize.set(currentFlagSize);
  }

  boolean isJump() {
    return isJump;
  }

  File getTempFile() {
    return mTempFile;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    isDestroy = true;
    EventMsgUtil.getDefault().unRegister(this);
    if (mJumpThreadPool != null && !mJumpThreadPool.isShutdown()) {
      mJumpThreadPool.shutdown();
    }
  }

  @Override protected void handleTask(Looper looper) {
    if (isBreak()) {
      return;
    }
    mLooper = looper;
    mInfoTask.run();
  }

  private void startThreadTask() {
    // 处理任务记录
    ((VodRecordHandler) mRecordHandler).setOption(mM3U8Option);
    mRecord = mRecordHandler.getRecord(0);

    // 处理任务管理器
    mStateHandler = new Handler(mLooper, getStateManager().getHandlerCallback());
    getStateManager().setVodLoader(this);
    getStateManager().setLooper(mRecord, mLooper);

    // 初始化ts数据
    initData();

    // 启动定时器
    startTimer();
    if (getStateManager().isComplete()){
      Log.d(TAG, "任务已完成");
      getStateManager().handleTaskComplete();
      return;
    }

    // 启动线程开始下载ts切片
    Thread th = new Thread(new Runnable() {
      @Override public void run() {
        while (!isBreak()) {
          try {
            JUMP_LOCK.lock();
            if (isJump) {
              mJumpCondition.await(5, TimeUnit.SECONDS);
              isJump = false;
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            JUMP_LOCK.unlock();
          }

          try {
            LOCK.lock();
            while (mFlagQueue.size() < EXEC_MAX_NUM && !isBreak()) {
              if (mCompleteNum.get() == mRecord.threadRecords.size()) {
                break;
              }

              ThreadRecord tr = getThreadRecord();
              if (tr == null || tr.isComplete) {
                ALog.d(TAG, "记录为空或记录已完成");
                break;
              }
              addTaskToQueue(tr);
            }
            if (mFlagQueue.size() > 0) {
              mWaitCondition.await();
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            LOCK.unlock();
          }
        }
      }
    });
    th.start();
  }

  @Override public long getFileSize() {
    return getEntity().getFileSize();
  }

  /**
   * 获取线程记录
   */
  private ThreadRecord getThreadRecord() {
    ThreadRecord tr = null;
    try {
      // 优先下载peer指针之后的数据
      if (beforePeerIndex.get() == 0 && afterPeerIndex.get() < mAfterPeer.size()) {
        //ALog.d(TAG, String.format("afterArray size:%s, index:%s", mAfterPeer.size(), aIndex));
        tr = mAfterPeer.valueAt(afterPeerIndex.get());
        afterPeerIndex.getAndIncrement();
      }

      // 如果指针之后的数组没有切片了，则重新初始化指针位置，并获取指针之前的数组获取切片进行下载
      if (mBeforePeer.size() > 0
          && (tr == null || beforePeerIndex.get() != 0)
          && beforePeerIndex.get() < mBeforePeer.size()) {
        tr = mBeforePeer.valueAt(beforePeerIndex.get());
        beforePeerIndex.getAndIncrement();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return tr;
  }

  /**
   * 启动线程任务
   */
  private void addTaskToQueue(ThreadRecord tr) throws InterruptedException {
    ThreadTask task = createThreadTask(mCacheDir, tr, tr.threadId);
    getTaskList().add(task);
    getEntity().getM3U8Entity().setPeerIndex(tr.threadId);
    TempFlag flag = startThreadTask(task, tr.threadId);
    if (flag != null) {
      mFlagQueue.put(flag);
    }
  }

  /**
   * 初始化数据
   */
  private void initData() {
    mCacheDir = getCacheDir();
    if (mM3U8Option.getJumpIndex() != 0) {
      mCurrentEvent = new PeerIndexEvent(mTaskWrapper.getKey(), mM3U8Option.getJumpIndex());
      resumeTask();
      return;
    }
    // 设置需要下载的切片
    mCompleteNum.set(0);
    for (ThreadRecord tr : mRecord.threadRecords) {
      if (!tr.isComplete) {
        mAfterPeer.put(tr.threadId, tr);
      } else {
        mCompleteNum.getAndIncrement();
      }
    }
    getStateManager().updateStateCount();
    if (mCompleteNum.get() <= 0) {
      getListener().onStart(0);
    } else {
      int percent = mCompleteNum.get() * 100 / mRecord.threadRecords.size();
      getListener().onResume(percent);
    }
  }

  /**
   * 每隔几秒钟检查jump队列，取最新的事件处理
   */
  private synchronized void startJumpThread() {
    jumpThread = new Thread(new Runnable() {
      @Override public void run() {
        try {
          PeerIndexEvent event;
          while (!isBreak()) {
            try {
              EVENT_LOCK.lock();
              PeerIndexEvent temp = null;
              // 取最新的事件
              while ((event = mJumpQueue.poll(1, TimeUnit.SECONDS)) != null) {
                temp = event;
              }

              if (temp != null) {
                handleJump(temp);
              }
              mEventQueueCondition.await();
            } finally {
              EVENT_LOCK.unlock();
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    jumpThread.start();
  }

  /**
   * 处理跳转
   */
  private void handleJump(PeerIndexEvent event) {
    if (isBreak()) {
      ALog.e(TAG, "任务已停止，处理跳转失败");
      return;
    }
    mCurrentEvent = event;
    if (mRecord == null || mRecord.threadRecords == null) {
      ALog.e(TAG, "跳到指定位置失败，记录为空");
      return;
    }
    if (event.peerIndex >= mRecord.threadRecords.size()) {
      ALog.e(TAG,
          String.format("切片索引设置错误，切片最大索引为：%s，当前设置的索引为：%s", mRecord.threadRecords.size(),
              event.peerIndex));
      return;
    }
    ALog.i(TAG, String.format("将优先下载索引【%s】之后的切片", event.peerIndex));

    isJump = true;
    notifyWaitLock(false);
    mCurrentFlagSize.set(mFlagQueue.size());
    // 停止所有正在执行的线程任务
    try {
      TempFlag flag;
      while ((flag = mFlagQueue.poll()) != null) {
        flag.threadTask.stop();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    ALog.d(TAG, "完成停止队列中的切片任务");
  }

  /**
   * 下载指定索引后面的切片
   * 如果指定的切片索引大于切片总数，则此操作无效
   * 如果指定的切片索引小于当前正在下载的切片索引，并且指定索引和当前索引区间内有未下载的切片，则优先下载该区间的切片；否则此操作无效
   * 如果指定索引后的切片已经全部下载完成，但是索引前有未下载的切片，间会自动下载未下载的切片
   */
  @Event
  public synchronized void jumpPeer(PeerIndexEvent event) {
    if (!event.key.equals(mTaskWrapper.getKey())) {
      return;
    }
    if (isBreak()) {
      ALog.e(TAG, "任务已停止，发送跳转事件失败");
      return;
    }
    if (jumpThread == null) {
      mJumpThreadPool = Executors.newSingleThreadExecutor();
      startJumpThread();
    }
    mJumpQueue.offer(event);
    mJumpThreadPool.submit(new Runnable() {
      @Override public void run() {
        try {
          Thread.sleep(1000);
          notifyJumpQueue();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void notifyJumpQueue() {
    try {
      EVENT_LOCK.lock();
      mEventQueueCondition.signalAll();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      EVENT_LOCK.unlock();
    }
  }

  /**
   * 从指定位置恢复任务
   */
  synchronized void resumeTask() {
    if (isBreak()) {
      ALog.e(TAG, "任务已停止，恢复任务失败");
      return;
    }
    if (mJumpQueue.size() > 0) {
      ALog.d(TAG, "有新定位，取消上一次操作");
      notifyJumpQueue();
      return;
    }
    ALog.d(TAG, "恢复切片任务");
    // 重新初始化需要下载的分片
    mBeforePeer.clear();
    mAfterPeer.clear();
    mFlagQueue.clear();
    afterPeerIndex.set(0);
    beforePeerIndex.set(0);
    mCompleteNum.set(0);
    for (ThreadRecord tr : mRecord.threadRecords) {
      if (tr.isComplete) {
        mCompleteNum.getAndIncrement();
        continue;
      }
      if (tr.threadId < mCurrentEvent.peerIndex) {
        mBeforePeer.put(tr.threadId, tr);
      } else {
        mAfterPeer.put(tr.threadId, tr);
      }
    }

    ALog.i(TAG,
        String.format("beforeSize = %s, afterSize = %s, mCompleteNum = %s", mBeforePeer.size(),
            mAfterPeer.size(), mCompleteNum));
    ALog.i(TAG, String.format("完成处理数据的操作，将优先下载【%s】之后的切片", mCurrentEvent.peerIndex));
    getStateManager().updateStateCount();

    try {
      JUMP_LOCK.lock();
      mJumpCondition.signalAll();
    } finally {
      JUMP_LOCK.unlock();
    }
  }

  void notifyWaitLock(boolean isComplete) {
    try {
      LOCK.lock();
      if (isComplete) {
        TempFlag flag = mFlagQueue.poll(1, TimeUnit.SECONDS);
        if (flag != null) {
          ALog.d(TAG, String.format("切片【%s】完成", flag.threadId));
        }
      }
      mWaitCondition.signalAll();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * 启动线程任务
   *
   * @return 线程唯一id标志
   */
  private TempFlag startThreadTask(ThreadTask task, int peerIndex) {
    if (isBreak()) {
      ALog.w(TAG, "任务已停止，启动线程任务失败");
      return null;
    }
    ThreadTaskManager.getInstance().startThread(mTaskWrapper.getKey(), task);
    getListener().onPeerStart(mTaskWrapper.getKey(), task.getConfig().tempFile.getPath(),
        peerIndex);
    TempFlag flag = new TempFlag();
    flag.threadTask = task;
    flag.threadId = peerIndex;
    return flag;
  }

  /**
   * 配置config
   */
  private ThreadTask createThreadTask(String cacheDir, ThreadRecord record, int index) {
    SubThreadConfig config = new SubThreadConfig();
    config.url = record.tsUrl;
    config.tempFile = new File(BaseM3U8Loader.getTsFilePath(cacheDir, record.threadId));
    config.isBlock = mRecord.isBlock;
    config.taskWrapper = mTaskWrapper;
    config.record = record;
    config.stateHandler = mStateHandler;
    config.peerIndex = index;
    config.threadType = SubThreadConfig.getThreadType(ITaskWrapper.M3U8_LIVE);
    config.updateInterval = SubThreadConfig.getUpdateInterval(ITaskWrapper.M3U8_LIVE);
    config.ignoreFailure = mM3U8Option.isIgnoreFailureTs();
    if (!config.tempFile.exists()) {
      FileUtil.createFile(config.tempFile);
    }
    ThreadTask threadTask = new ThreadTask(config);
    M3U8ThreadTaskAdapter adapter = new M3U8ThreadTaskAdapter(config);
    threadTask.setAdapter(adapter);
    return threadTask;
  }

  @Override public void addComponent(IRecordHandler recordHandler) {
    mRecordHandler = recordHandler;
  }

  @Override public void addComponent(IInfoTask infoTask) {
    mInfoTask = infoTask;
    final List<String> urls = new ArrayList<>();
    mInfoTask.setCallback(new IInfoTask.Callback() {
      @Override public void onSucceed(String key, CompleteInfo info) {
        IVodTsUrlConverter converter = mM3U8Option.isUseDefConvert() ?
            new VodTsDefConverter() :
            mM3U8Option.getVodUrlConverter();
        if (converter != null) {
          if (TextUtils.isEmpty(mM3U8Option.getBandWidthUrl())) {
            urls.addAll(
                converter.convert(getEntity().getUrl(), (List<String>) info.obj));
          } else {
            urls.addAll(
                converter.convert(mM3U8Option.getBandWidthUrl(), (List<String>) info.obj));
          }
        } else {
          urls.addAll((Collection<? extends String>) info.obj);
        }
        if (urls.isEmpty()) {
          fail(new AriaM3U8Exception("获取地址失败"), false);
          return;
        } else if (!urls.get(0).startsWith("http")) {
          fail(new AriaM3U8Exception("地址错误，请使用IVodTsUrlConverter处理你的url信息"), false);
          return;
        }
        mM3U8Option.setUrls(urls);

        if (isStop) {
          getListener().onStop(getEntity().getCurrentProgress());
        } else if (isCancel) {
          getListener().onCancel();
        } else {
          startThreadTask();
        }
      }

      @Override public void onFail(AbsEntity entity, AriaException e, boolean needRetry) {
        fail(e, needRetry);
      }
    });
  }

  protected void fail(AriaException e, boolean needRetry) {
    if (isBreak()) {
      return;
    }
    getListener().onFail(needRetry, e);
    onDestroy();
  }

  /**
   * 需要在 {@link #addComponent(IRecordHandler)}后调用
   */
  @Override public void addComponent(IThreadStateManager threadState) {
    mStateManager = threadState;
  }

  /**
   * m3u8 不需要实现这个
   */
  @Deprecated
  @Override public void addComponent(IThreadTaskBuilder builder) {

  }

  @Override
  protected VodStateManager getStateManager() {
    return (VodStateManager) mStateManager;
  }

  @Override protected void checkComponent() {
    if (mRecordHandler == null) {
      throw new NullPointerException("任务记录组件为空");
    }
    if (mInfoTask == null) {
      throw new NullPointerException(("文件信息组件为空"));
    }
    if (getStateManager() == null) {
      throw new NullPointerException("任务状态管理组件为空");
    }
  }

  private static class TempFlag {
    ThreadTask threadTask;
    int threadId;
  }
}
