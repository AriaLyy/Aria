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
package com.arialyy.aria.core.listener;

import android.os.Handler;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.group.GroupSendParams;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.DeleteDGRecord;
import com.arialyy.aria.util.ErrorHelp;

import static com.arialyy.aria.core.task.AbsTask.ERROR_INFO_KEY;

/**
 * Created by Aria.Lao on 2017/7/20. 任务组下载事件
 */
public class DownloadGroupListener extends BaseListener implements IDGroupListener {
  private GroupSendParams<DownloadGroupTask, DownloadEntity> mSeedEntity;

  @Override public IEventListener setParams(AbsTask task, Handler outHandler) {
    IEventListener listener = super.setParams(task, outHandler);
    mSeedEntity = new GroupSendParams<>();
    mSeedEntity.groupTask = (DownloadGroupTask) task;
    return listener;
  }

  @Override
  public void onSubPre(DownloadEntity subEntity) {
    handleSubSpeed(subEntity, 0);
    saveSubState(IEntity.STATE_PRE, subEntity);
    sendInState2Target(ISchedulers.SUB_PRE, subEntity);
  }

  @Override
  public void supportBreakpoint(boolean support, DownloadEntity subEntity) {

  }

  @Override
  public void onSubStart(DownloadEntity subEntity) {
    handleSubSpeed(subEntity, 0);
    saveSubState(IEntity.STATE_RUNNING, subEntity);
    sendInState2Target(ISchedulers.SUB_START, subEntity);
  }

  @Override
  public void onSubStop(DownloadEntity subEntity, long stopLocation) {
    subEntity.setCurrentProgress(stopLocation);
    handleSubSpeed(subEntity, 0);
    saveSubState(IEntity.STATE_STOP, subEntity);
    saveCurrentLocation();
    sendInState2Target(ISchedulers.SUB_STOP, subEntity);
  }

  @Override
  public void onSubComplete(DownloadEntity subEntity) {
    handleSubSpeed(subEntity, 0);
    saveSubState(IEntity.STATE_COMPLETE, subEntity);
    saveCurrentLocation();
    sendInState2Target(ISchedulers.SUB_COMPLETE, subEntity);
  }

  @Override
  public void onSubFail(DownloadEntity subEntity, AriaException e) {
    handleSubSpeed(subEntity, 0);
    saveSubState(IEntity.STATE_FAIL, subEntity);
    saveCurrentLocation();
    mSeedEntity.groupTask.putExpand(ERROR_INFO_KEY, e);
    sendInState2Target(ISchedulers.SUB_FAIL, subEntity);
    if (e != null) {
      e.printStackTrace();
      ErrorHelp.saveError("", ALog.getExceptionString(e));
    }
  }

  @Override
  public void onSubCancel(DownloadEntity subEntity) {
    handleSubSpeed(subEntity, 0);
    saveSubState(IEntity.STATE_CANCEL, subEntity);
    saveCurrentLocation();
    sendInState2Target(ISchedulers.SUB_CANCEL, subEntity);
  }

  @Override
  public void onSubRunning(DownloadEntity subEntity, long currentProgress) {

    handleSubSpeed(subEntity, currentProgress);
    if (System.currentTimeMillis() - mLastSaveTime >= RUN_SAVE_INTERVAL) {
      saveSubState(IEntity.STATE_RUNNING, subEntity);
      mLastSaveTime = System.currentTimeMillis();
    }
    sendInState2Target(ISchedulers.SUB_RUNNING, subEntity);
  }

  private void handleSubSpeed(DownloadEntity subEntity, long currentProgress) {
    if (currentProgress == 0) {
      subEntity.setSpeed(0);
      subEntity.setConvertSpeed("0kb/s");
      return;
    }
    long speed = currentProgress - subEntity.getCurrentProgress();
    subEntity.setSpeed(speed);
    subEntity.setConvertSpeed(
        speed <= 0 ? "" : String.format("%s/s", CommonUtil.formatFileSize(speed)));
    subEntity.setPercent((int) (subEntity.getFileSize() <= 0 ? 0
        : subEntity.getCurrentProgress() * 100 / subEntity.getFileSize()));
    subEntity.setCurrentProgress(currentProgress);

    if (speed == 0) {
      subEntity.setTimeLeft(Integer.MAX_VALUE);
    } else {
      subEntity.setTimeLeft(
          (int) ((subEntity.getFileSize() - subEntity.getCurrentProgress()) / speed));
    }
  }

  /**
   * 将任务状态发送给下载器
   *
   * @param state {@link ISchedulers#START}
   */
  private void sendInState2Target(int state, DownloadEntity subEntity) {
    if (outHandler.get() != null) {
      mSeedEntity.entity = subEntity;
      outHandler.get().obtainMessage(state, ISchedulers.IS_SUB_TASK, 0, mSeedEntity).sendToTarget();
    }
  }

  private void saveSubState(int state, DownloadEntity subEntity) {
    subEntity.setState(state);
    if (state == IEntity.STATE_STOP) {
      subEntity.setStopTime(System.currentTimeMillis());
    } else if (state == IEntity.STATE_COMPLETE) {
      subEntity.setComplete(true);
      subEntity.setCompleteTime(System.currentTimeMillis());
      subEntity.setCurrentProgress(subEntity.getFileSize());
      subEntity.setPercent(100);
      subEntity.setConvertSpeed("0kb/s");
      subEntity.setSpeed(0);
    }
    subEntity.update();
  }

  private void saveCurrentLocation() {
    DownloadGroupEntity dgEntity = (DownloadGroupEntity) mEntity;
    if (dgEntity.getSubEntities() == null || dgEntity.getSubEntities().isEmpty()) {
      ALog.w(TAG, "保存进度失败，子任务为null");
      return;
    }
    long location = 0;
    for (DownloadEntity e : dgEntity.getSubEntities()) {
      location += e.getCurrentProgress();
    }
    if (location > mEntity.getFileSize()) {
      location = mEntity.getFileSize();
    }
    mEntity.setCurrentProgress(location);
    mEntity.update();
  }

  @Override
  public void onPostPre(long fileSize) {
    mEntity.setFileSize(fileSize);
    mEntity.setConvertFileSize(CommonUtil.formatFileSize(fileSize));
    saveData(IEntity.STATE_POST_PRE, -1);
    sendInState2Target(ISchedulers.POST_PRE);
  }

  @Override
  public void supportBreakpoint(boolean support) {

  }

  @Override protected void handleCancel() {
    int sType = getTask(DownloadGroupTask.class).getSchedulerType();
    if (sType == TaskSchedulerType.TYPE_CANCEL_AND_NOT_NOTIFY) {
      mEntity.setComplete(false);
      mEntity.setState(IEntity.STATE_WAIT);
      DeleteDGRecord.getInstance().deleteRecord(mEntity, mTaskWrapper.isRemoveFile(), false);
    } else {
      DeleteDGRecord.getInstance().deleteRecord(mEntity, mTaskWrapper.isRemoveFile(), true);
    }
  }
}
