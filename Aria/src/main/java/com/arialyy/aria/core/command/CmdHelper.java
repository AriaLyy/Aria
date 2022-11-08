/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.command;

import com.arialyy.aria.core.download.AbsGroupTaskWrapper;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

public class CmdHelper {
  /**
   * 创建任务命令
   *
   * @param taskType {@link ITask#DOWNLOAD}、{@link ITask#DOWNLOAD_GROUP}、{@link ITask#UPLOAD}
   */
  public static <T extends AbsTaskWrapper> AbsNormalCmd createNormalCmd(T entity, int cmd,
      int taskType) {
    return NormalCmdFactory.getInstance().createCmd(entity, cmd, taskType);
  }

  /**
   * 创建任务组命令
   *
   * @param childUrl 子任务url
   */
  public static <T extends AbsGroupTaskWrapper> AbsGroupCmd createGroupCmd(T entity, int cmd,
      String childUrl) {
    return GroupCmdFactory.getInstance().createCmd(entity, cmd, childUrl);
  }
}
