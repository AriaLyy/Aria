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
package com.arialyy.aria.core.manager;

import android.text.TextUtils;
import com.arialyy.aria.core.command.GroupCmdFactory;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.event.EventMsgUtil;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.core.command.CmdHelper;
import java.util.List;

/**
 * Created by lyy on 2017/9/4.
 * 子任务管理器
 */
public class SubTaskManager {
  private final String TAG = getClass().getSimpleName();
  private DGTaskWrapper mEntity;

  public SubTaskManager(DGTaskWrapper entity) {
    mEntity = entity;
  }

  /**
   * 启动任务组中的子任务
   *
   * @param url 子任务下载地址
   */
  public void startSubTask(String url) {
    if (checkUrl(url)) {
      EventMsgUtil.getDefault().post(
          CmdHelper.createGroupCmd(mEntity, GroupCmdFactory.SUB_TASK_START, url));
    }
  }

  /**
   * 停止任务组中的子任务
   *
   * @param url 子任务下载地址
   */
  public void stopSubTask(String url) {
    if (checkUrl(url)) {
      EventMsgUtil.getDefault().post(
          CmdHelper.createGroupCmd(mEntity, GroupCmdFactory.SUB_TASK_STOP, url));
    }
  }

  /**
   * 检查任务地址
   *
   * @param url 子任务地址
   * @return {@code false} 任务地址不合法
   */
  private boolean checkUrl(String url) {
    if (TextUtils.isEmpty(url)) {
      ALog.e(TAG, "子任务地址不能为null");
      return false;
    }
    List<String> urls = mEntity.getEntity().getUrls();
    if (urls == null || urls.isEmpty()) {
      ALog.e(TAG, "任务组任务链接为null");
      return false;
    }
    if (!urls.contains(url)) {
      ALog.e(TAG, "任务组中没有改Url【+ " + url + "】");
      return false;
    }
    return true;
  }
}
