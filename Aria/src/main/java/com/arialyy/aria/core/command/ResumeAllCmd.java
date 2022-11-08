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

import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.NetUtils;

/**
 * Created by AriaL on 2017/6/13.
 * 恢复所有停止的任务
 * 1.如果执行队列没有满，则开始下载任务，直到执行队列满
 * 2.如果队列执行队列已经满了，则将所有任务添加到等待队列中
 * 3.如果队列中只有等待状态的任务，如果执行队列没有满，则会启动等待状态的任务，如果执行队列已经满了，则会将所有等待状态的任务加载到缓存队列中
 * 4.恢复下载的任务规则是，停止时间越晚的任务启动越早，按照DESC来进行排序
 */
final class ResumeAllCmd<T extends AbsTaskWrapper> extends AbsNormalCmd<T> {

  ResumeAllCmd(T entity, int taskType) {
    super(entity, taskType);
  }

  @Override public void executeCmd() {
    if (!NetUtils.isConnected(AriaConfig.getInstance().getAPP())) {
      ALog.w(TAG, "恢复任务失败，网络未连接");
      return;
    }
    new Thread(new ResumeThread(isDownloadCmd,
        String.format("state!=%s", IEntity.STATE_COMPLETE))).start();
  }
}
