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
package com.arialyy.aria.core.common;

import com.arialyy.aria.core.common.controller.BuilderController;
import com.arialyy.aria.core.common.controller.IStartFeature;
import com.arialyy.aria.core.inf.AbsTarget;

/**
 * 处理第一次创建的任务
 */
public abstract class AbsBuilderTarget<TARGET extends AbsBuilderTarget> extends AbsTarget<TARGET>
    implements IStartFeature {

  private BuilderController mStartController;

  /**
   * 任务操作前调用
   */
  protected void onPre() {

  }

  private synchronized BuilderController getController() {
    if (mStartController == null) {
      mStartController = new BuilderController(getTaskWrapper());
    }
    return mStartController;
  }

  /**
   * 是否忽略权限检查
   */
  public TARGET ignoreCheckPermissions() {
    getController().ignoreCheckPermissions();
    return (TARGET) this;
  }

  /**
   * 忽略文件占用，不管文件路径是否被其它任务占用，都执行上传\下载任务
   * 需要注意的是：如果文件被其它任务占用，并且还调用了该方法，将自动删除占用了该文件路径的任务
   */
  public TARGET ignoreFilePathOccupy() {
    getController().ignoreFilePathOccupy();
    return (TARGET) this;
  }

  /**
   * 添加任务
   *
   * @return 添加成功，返回任务id，创建失败，返回-1
   */
  @Override
  public long add() {
    onPre();
    return getController().add();
  }

  /**
   * 开始任务
   *
   * @return 创建成功，返回任务id，创建失败，返回-1
   */
  @Override
  public long create() {
    onPre();
    return getController().create();
  }

  /**
   * 将任务设置为最高优先级任务，最高优先级任务有以下特点：
   * 1、在下载队列中，有且只有一个最高优先级任务
   * 2、最高优先级任务会一直存在，直到用户手动暂停或任务完成
   * 3、任务调度器不会暂停最高优先级任务
   * 4、用户手动暂停或任务完成后，第二次重新执行该任务，该命令将失效
   * 5、如果下载队列中已经满了，则会停止队尾的任务，当高优先级任务完成后，该队尾任务将自动执行
   * 6、把任务设置为最高优先级任务后，将自动执行任务，不需要重新调用start()启动任务
   */
  @Override
  public long setHighestPriority() {
    onPre();
    return getController().setHighestPriority();
  }
}
