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
package com.arialyy.aria.core.command;

import com.arialyy.aria.core.event.Event;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.util.CommonUtil;

/**
 * 命令处理器
 */
public class CommandManager {

  private static CommandManager instance;

  private CommandManager() {
    EventMsgUtil.getDefault().register(this);
  }

  public static void init() {
    if (instance == null) {
      synchronized (CommandManager.class) {
        if (instance == null) {
          instance = new CommandManager();
        }
      }
    }
  }

  @Event
  public void add(AddCmd cmd) {
    if (CommonUtil.isFastDoubleClick()) {
      return;
    }
    cmd.executeCmd();
  }

  @Event
  public void start(StartCmd cmd) {
    cmd.executeCmd();
  }

  @Event
  public void stop(StopCmd cmd) {
    cmd.executeCmd();
  }

  @Event
  public void cancel(CancelCmd cmd) {
    cmd.executeCmd();
  }

  @Event
  public void stopAll(StopAllCmd cmd) {
    if (CommonUtil.isFastDoubleClick()) {
      return;
    }
    cmd.executeCmd();
  }

  @Event
  public void cancelAll(CancelAllCmd cmd) {
    if (CommonUtil.isFastDoubleClick()) {
      return;
    }
    cmd.executeCmd();
  }

  @Event
  public void reStart(ReStartCmd cmd) {
    if (CommonUtil.isFastDoubleClick()) {
      return;
    }
    cmd.executeCmd();
  }

  @Event
  public void highestPriority(HighestPriorityCmd cmd) {
    if (CommonUtil.isFastDoubleClick()) {
      return;
    }
    cmd.executeCmd();
  }

  @Event
  public void resumeAll(ResumeAllCmd cmd) {
    if (CommonUtil.isFastDoubleClick()) {
      return;
    }
    cmd.executeCmd();
  }

  @Event
  public void subStart(DGSubStartCmd cmd) {
    if (CommonUtil.isFastDoubleClick()) {
      return;
    }
    cmd.executeCmd();
  }

  @Event
  public void subStop(DGSubStopCmd cmd) {
    cmd.executeCmd();
  }
}
