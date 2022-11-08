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

import com.arialyy.aria.core.download.AbsGroupTaskWrapper;

/**
 * Created by AriaL on 2017/6/29. 任务组子任务控制命令
 */
public class GroupCmdFactory {
  /**
   * 启动子任务
   */
  public static final int SUB_TASK_START = 0xa1;
  /**
   * 停止子任务
   */
  public static final int SUB_TASK_STOP = 0xa2;

  private static volatile GroupCmdFactory INSTANCE = null;

  private GroupCmdFactory() {

  }

  public static GroupCmdFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (GroupCmdFactory.class) {
        INSTANCE = new GroupCmdFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * @param wrapper 参数信息
   * @param type 命令类型{@link #SUB_TASK_START}、{@link #SUB_TASK_STOP}
   * @param childUrl 需要控制的子任务url
   */
  public AbsGroupCmd createCmd(AbsGroupTaskWrapper wrapper, int type, String childUrl) {
    AbsGroupCmd cmd = null;
    switch (type) {
      case SUB_TASK_START:
        cmd = new DGSubStartCmd<>(wrapper);
        break;
      case SUB_TASK_STOP:
        cmd = new DGSubStopCmd<>(wrapper);
        break;
    }
    if (cmd != null) {
      cmd.childUrl = childUrl;
    }
    return cmd;
  }
}
