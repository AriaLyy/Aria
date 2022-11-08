package com.arialyy.aria.core.loader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.AbsNormalEntity;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.core.task.ThreadTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;

public final class NormalTTBuilder implements IThreadTaskBuilder {
  protected String TAG = CommonUtil.getClassName(this);

  private Handler mStateHandler;
  private AbsTaskWrapper mWrapper;
  private TaskRecord mRecord;
  private int mTotalThreadNum;
  private int mStartThreadNum;
  private AbsNormalTTBuilderAdapter mAdapter;

  public NormalTTBuilder(AbsTaskWrapper wrapper, AbsNormalTTBuilderAdapter adapter) {
    if (wrapper instanceof DGTaskWrapper) {
      throw new AssertionError("NormalTTBuilder 不适用于组合任务");
    }
    mWrapper = wrapper;
    mAdapter = adapter;
    mAdapter.setWrapper(wrapper);
  }

  protected AbsNormalEntity getEntity() {
    return (AbsNormalEntity) mWrapper.getEntity();
  }

  public AbsNormalTTBuilderAdapter getAdapter() {
    return mAdapter;
  }

  /**
   * 创建线程任务
   */
  private IThreadTask createThreadTask(SubThreadConfig config) {
    ThreadTask task = new ThreadTask(config);
    task.setAdapter(mAdapter.getAdapter(config));
    return task;
  }

  /**
   * 启动断点任务时，创建单线程任务
   *
   * @param record 线程记录
   * @param startNum 启动的线程数
   */
  private IThreadTask createSingThreadTask(ThreadRecord record, int startNum) {
    return createThreadTask(
        mAdapter.getSubThreadConfig(mStateHandler, record, mRecord.isBlock, startNum));
  }

  /**
   * 处理不支持断点的任务
   */
  private List<IThreadTask> handleNoSupportBP() {
    List<IThreadTask> list = new ArrayList<>();
    mStartThreadNum = 1;
    mRecord.isBlock = false;
    mRecord.update();
    IThreadTask task = createSingThreadTask(mRecord.threadRecords.get(0), 1);
    list.add(task);
    return list;
  }

  /**
   * 处理支持断点的任务
   */
  private List<IThreadTask> handleBreakpoint() {
    long fileLength = getEntity().getFileSize();
    long blockSize = fileLength / mTotalThreadNum;
    long currentProgress = 0;
    List<IThreadTask> threadTasks = new ArrayList<>(mTotalThreadNum);

    mRecord.fileLength = fileLength;
    if (mWrapper.isNewTask() && !mAdapter.handleNewTask(mRecord, mTotalThreadNum)) {
      ALog.e(TAG, "初始化线程任务失败");
      return null;
    }

    for (ThreadRecord tr : mRecord.threadRecords) {
      if (!tr.isComplete) {
        mStartThreadNum++;
      }
    }

    for (int i = 0; i < mTotalThreadNum; i++) {
      long startL = i * blockSize, endL = (i + 1) * blockSize;
      ThreadRecord tr = mRecord.threadRecords.get(i);

      if (tr.isComplete) {//该线程已经完成
        currentProgress += endL - startL;
        ALog.d(TAG, String.format("任务【%s】线程__%s__已完成", mWrapper.getKey(), i));
        Message msg = mStateHandler.obtainMessage();
        msg.what = IThreadStateManager.STATE_COMPLETE;
        Bundle b = msg.getData();
        if (b == null) {
          b = new Bundle();
        }
        b.putString(IThreadStateManager.DATA_THREAD_NAME,
            CommonUtil.getThreadName(getEntity().getKey(), tr.threadId));
        msg.setData(b);
        msg.sendToTarget();
        continue;
      }

      //如果有记录，则恢复任务
      long r = tr.startLocation;
      //记录的位置需要在线程区间中
      if (startL < r && r <= (i == (mTotalThreadNum - 1) ? fileLength : endL)) {
        currentProgress += r - startL;
      }
      ALog.d(TAG, String.format("任务【%s】线程__%s__恢复任务", getEntity().getFileName(), i));

      IThreadTask task = createSingThreadTask(tr, mStartThreadNum);
      if (task == null) {
        ALog.e(TAG, "创建线程任务失败");
        return null;
      }
      threadTasks.add(task);
    }
    if (currentProgress != getEntity().getCurrentProgress()) {
      ALog.d(TAG, String.format("进度修正，当前进度：%s", currentProgress));
      getEntity().setCurrentProgress(currentProgress);
    }
    //mStateManager.updateProgress(currentProgress);
    return threadTasks;
  }

  private List<IThreadTask> handleTask() {
    if (mWrapper.isSupportBP()) {
      return handleBreakpoint();
    } else {
      return handleNoSupportBP();
    }
  }

  @Override public List<IThreadTask> buildThreadTask(TaskRecord record, Handler stateHandler) {
    mRecord = record;
    mStateHandler = stateHandler;
    mTotalThreadNum = mRecord.threadNum;
    return handleTask();
  }

  @Override public int getCreatedThreadNum() {
    return mStartThreadNum;
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }
}
