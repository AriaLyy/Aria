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
package com.arialyy.aria.core.common.controller;

import com.arialyy.aria.core.command.CancelCmd;
import com.arialyy.aria.core.command.CmdHelper;
import com.arialyy.aria.core.command.NormalCmdFactory;
import com.arialyy.aria.core.command.StartCmd;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;

/**
 * 启动控制器
 */
public final class NormalController extends FeatureController implements INormalFeature {
  private String TAG = "NormalController";

  public NormalController(AbsTaskWrapper wrapper) {
    super(wrapper);
  }

  /**
   * 停止任务
   */
  @Override
  public void stop() {
    setAction(ACTION_STOP);
    if (checkConfig()) {
      EventMsgUtil.getDefault()
          .post(CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_STOP,
              checkTaskType()));
    }
  }

  /**
   * 恢复任务
   */
  @Override
  public void resume() {
    resume(false);
  }

  /**
   * 正常来说，当执行队列满时，调用恢复任务接口，只能将任务放到缓存队列中。
   * 如果希望调用恢复接口，马上进入执行队列，需要使用该方法
   *
   * @param newStart true 立即将任务恢复到执行队列中
   */
  @Override public void resume(boolean newStart) {
    setAction(ACTION_RESUME);
    if (checkConfig()) {
      StartCmd cmd =
          (StartCmd) CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_START,
              checkTaskType());
      cmd.setNowStart(newStart);
      EventMsgUtil.getDefault()
          .post(cmd);
    }
  }

  /**
   * 删除任务
   */
  @Override
  public void cancel() {
    cancel(false);
  }

  /**
   * 任务重试
   */
  @Override
  public void reTry() {
    setAction(ACTION_RETRY);
    if (checkConfig()) {
      int taskType = checkTaskType();
      EventMsgUtil.getDefault()
          .post(CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_STOP, taskType));
      EventMsgUtil.getDefault()
          .post(
              CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_START, taskType));
    }
  }

  /**
   * 删除任务
   *
   * @param removeFile {@code true} 不仅删除任务数据库记录，还会删除已经删除完成的文件
   * {@code false}如果任务已经完成，只删除任务数据库记录，
   */
  @Override
  public void cancel(boolean removeFile) {
    setAction(ACTION_CANCEL);
    if (checkConfig()) {
      CancelCmd cancelCmd =
          (CancelCmd) CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_CANCEL,
              checkTaskType());
      cancelCmd.removeFile = removeFile;
      EventMsgUtil.getDefault().post(cancelCmd);
    }
  }

  /**
   * 重新下载
   */
  @Override
  public long reStart() {
    setAction(ACTION_RESTART);
    if (checkConfig()) {
      EventMsgUtil.getDefault()
          .post(CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_RESTART,
              checkTaskType()));
      return getEntity().getId();
    }
    return -1;
  }

  @Override public void save() {
    setAction(ACTION_SAVE);
    if (!checkConfig()) {
      ALog.e(TAG, "保存修改失败");
    } else {
      ALog.i(TAG, "保存成功");
    }
  }
}
