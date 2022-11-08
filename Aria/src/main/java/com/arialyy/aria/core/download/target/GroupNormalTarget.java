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
package com.arialyy.aria.core.download.target;

import com.arialyy.aria.core.common.AbsNormalTarget;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.download.AbsGroupTaskWrapper;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.manager.SubTaskManager;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.ALog;
import java.util.List;

/**
 * Created by AriaL on 2017/6/29.
 * 下载任务组
 */
public class GroupNormalTarget extends AbsNormalTarget<GroupNormalTarget> {
  private HttpGroupConfigHandler<GroupNormalTarget> mConfigHandler;

  GroupNormalTarget(long taskId) {
    mConfigHandler = new HttpGroupConfigHandler<>(this, taskId);
    getTaskWrapper().setRequestType(ITaskWrapper.DG_HTTP);
    getTaskWrapper().setNewTask(false);
  }

  /**
   * 设置http请求参数，header等信息
   */
  public GroupNormalTarget option(HttpOption option) {
    if (option == null) {
      throw new NullPointerException("任务配置为空");
    }
    getTaskWrapper().getOptionParams().setParams(option);
    return this;
  }

  /**
   * 获取子任务管理器
   *
   * @return 子任务管理器
   */
  public SubTaskManager getSubTaskManager() {
    return mConfigHandler.getSubTaskManager();
  }

  /**
   * 设置任务组别名
   */
  public GroupNormalTarget setGroupAlias(String alias) {
    mConfigHandler.setGroupAlias(alias);
    return this;
  }

  /**
   * 更新组合任务下载地址
   *
   * @param urls 新的组合任务下载地址列表
   */
  public GroupNormalTarget updateUrls(List<String> urls) {
    return mConfigHandler.updateUrls(urls);
  }

  /**
   * {@code true} 忽略任务冲突，不考虑组任务hash冲突的情况
   */
  public GroupNormalTarget ignoreTaskOccupy() {
    ((AbsGroupTaskWrapper) getTaskWrapper()).setIgnoreTaskOccupy(true);
    return this;
  }

  /**
   * 更新任务组的文件夹路径，在Aria中，任务组的所有子任务都会下载到以任务组组名的文件夹中。
   * 如：groupDirPath = "/mnt/sdcard/download/group_test"
   * <pre>
   *   {@code
   *      + mnt
   *        + sdcard
   *          + download
   *            + group_test
   *              - task1.apk
   *              - task2.apk
   *              - task3.apk
   *              ....
   *
   *   }
   * </pre>
   *
   * @param dirPath 任务组保存文件夹路径
   */
  public GroupNormalTarget modifyDirPath(String dirPath) {
    return mConfigHandler.setDirPath(dirPath);
  }

  /**
   * 更新子任务文件名，该方法必须在{@link #modifyDirPath(String)}之后调用，否则不生效
   */
  public GroupNormalTarget modifySubFileName(List<String> subTaskFileName) {
    return mConfigHandler.setSubFileName(subTaskFileName);
  }

  /**
   * 如果无法获取到组合任务到总长度，请调用该方法，
   * 请注意：
   * 1、如果组合任务到子任务数过多，请不要使用该标志位，否则Aria将需要消耗大量的时间获取组合任务的总长度，直到获取完成组合任务总长度后才会执行下载。
   * 2、如果你的知道组合任务的总长度，请使用{@link #setFileSize(long)}设置组合任务的长度。
   * 3、由于网络或其它原因的存在，这种方式获取的组合任务大小有可能是不准确的。
   */
  public GroupNormalTarget unknownSize() {
    ((DGTaskWrapper) getTaskWrapper()).setUnknownSize(true);
    return this;
  }

  /**
   * 任务组总任务大小，任务组是一个抽象的概念，没有真实的数据实体，任务组的大小是Aria动态获取子任务大小相加而得到的，
   * 如果你知道当前任务组总大小，你也可以调用该方法给任务组设置大小
   *
   * 为了更好的用户体验，组合任务最好设置文件大小，默认需要强制设置文件大小。如果无法获取到总长度，请调用{@link #unknownSize()}
   *
   * @param fileSize 任务组总大小
   */
  public GroupNormalTarget setFileSize(long fileSize) {
    if (fileSize <= 0) {
      ALog.e(TAG, "文件大小不能小于 0");
      return this;
    }
    if (getEntity().getFileSize() <= 1 || getEntity().getFileSize() != fileSize) {
      getEntity().setFileSize(fileSize);
    }
    return this;
  }

  @Override public boolean isRunning() {
    return mConfigHandler.isRunning();
  }

  @Override public boolean taskExists() {
    return mConfigHandler.taskExists();
  }
}
