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

import com.arialyy.aria.core.command.CmdHelper;
import com.arialyy.aria.core.command.NormalCmdFactory;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

/**
 * 创建任务时使用的控制器
 */
public final class BuilderController extends FeatureController implements IStartFeature {

  public BuilderController(AbsTaskWrapper wrapper) {
    super(wrapper);
  }

  /**
   * 添加任务，只添加任务不进行下载
   *
   * @return 正常添加，返回任务id，否则返回-1
   */
  public long add() {
    setAction(ACTION_ADD);
    if (checkConfig()) {
      EventMsgUtil.getDefault()
          .post(CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_CREATE,
              checkTaskType()));
      return getEntity().getId();
    }
    return -1;
  }

  /**
   * 开始任务
   *
   * @return 正常启动，返回任务id，否则返回-1
   */
  public long create() {
    setAction(ACTION_CREATE);
    if (checkConfig()) {
      EventMsgUtil.getDefault()
          .post(CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_START,
              checkTaskType()));
      return getEntity().getId();
    }
    return -1;
  }

  @Override public long setHighestPriority() {
    setAction(ACTION_PRIORITY);
    if (checkConfig()) {
      EventMsgUtil.getDefault()
          .post(CmdHelper.createNormalCmd(getTaskWrapper(), NormalCmdFactory.TASK_HIGHEST_PRIORITY,
              checkTaskType()));
      return getEntity().getId();
    }
    return -1;
  }
}
