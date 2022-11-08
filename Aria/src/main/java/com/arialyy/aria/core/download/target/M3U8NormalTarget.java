
package com.arialyy.aria.core.download.target;
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

import com.arialyy.aria.core.common.AbsNormalTarget;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.core.event.PeerIndexEvent;
import com.arialyy.aria.core.queue.DTaskQueue;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.ALog;

public class M3U8NormalTarget extends AbsNormalTarget<M3U8NormalTarget> {

  M3U8NormalTarget(DTaskWrapper wrapper) {
    setTaskWrapper(wrapper);
    getTaskWrapper().setNewTask(false);
    getTaskWrapper().setRequestType(ITaskWrapper.M3U8_VOD);
  }

  /**
   * 任务执行中，跳转索引位置
   * 优先下载指定索引后的切片
   * 如果指定的切片索引大于切片总数，则此操作无效
   * 如果指定的切片索引小于当前正在下载的切片索引，并且指定索引和当前索引区间内有未下载的切片，则优先下载该区间的切片；否则此操作无效
   * 如果指定索引后的切片已经全部下载完成，但是索引前有未下载的切片，间会自动下载未下载的切片
   *
   * @param index 指定的切片位置
   */
  public void jumPeerIndex(int index) {
    if (index < 1) {
      ALog.e(TAG, "切片索引不能小于1");
      return;
    }

    if (!DTaskQueue.getInstance().taskIsRunning(getTaskWrapper().getKey())) {
      ALog.e(TAG,
          String.format("任务【%s】没有运行，如果你希望在启动任务时初始化索引位置，请调用setPeerIndex(xxx）",
              getTaskWrapper().getKey()));
      return;
    }

    EventMsgUtil.getDefault().post(new PeerIndexEvent(getTaskWrapper().getKey(), index));
  }
}
