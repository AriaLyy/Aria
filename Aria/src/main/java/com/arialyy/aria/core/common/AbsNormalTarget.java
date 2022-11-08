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

import com.arialyy.aria.core.common.controller.INormalFeature;
import com.arialyy.aria.core.common.controller.NormalController;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.manager.TaskWrapperManager;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.DeleteDGRecord;
import com.arialyy.aria.util.RecordUtil;

/**
 * 处理任务停止、恢复、删除等功能
 */
public abstract class AbsNormalTarget<TARGET extends AbsNormalTarget> extends AbsTarget<TARGET>
    implements INormalFeature {

  /**
   * 任务操作前调用
   */
  protected void onPre() {

  }

  /**
   * 是否忽略权限检查
   */
  public TARGET ignoreCheckPermissions() {
    getController().ignoreCheckPermissions();
    return (TARGET) this;
  }

  /**
   * 任务是否在执行
   *
   * @return {@code true} 任务正在执行
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * 任务是否存在
   *
   * @return {@code true} 任务存在
   */
  public boolean taskExists() {
    return false;
  }

  private NormalController mNormalController;

  private synchronized NormalController getController() {
    if (mNormalController == null) {
      mNormalController = new NormalController(getTaskWrapper());
    }
    return mNormalController;
  }

  /**
   * 删除记录，如果任务正在执行，则会删除正在下载的任务
   */
  public void removeRecord() {
    if (isRunning()) {
      ALog.d("AbsTarget", "任务正在下载，即将删除任务");
      cancel();
    } else {
      if (getEntity() instanceof AbsNormalEntity) {
        RecordUtil.delNormalTaskRecord((AbsNormalEntity) getEntity(), getTaskWrapper().isRemoveFile());
      } else if (getEntity() instanceof DownloadGroupEntity) {
        DeleteDGRecord.getInstance()
            .deleteRecord(getEntity(), getTaskWrapper().isRemoveFile(), true);
      }
      TaskWrapperManager.getInstance().removeTaskWrapper(getTaskWrapper());
    }
  }

  /**
   * 获取任务进度，如果任务存在，则返回当前进度
   *
   * @return 该任务进度
   */
  public long getCurrentProgress() {
    return getEntity() == null ? -1 : getEntity().getCurrentProgress();
  }

  /**
   * 获取任务文件大小
   *
   * @return 文件大小
   */
  public long getFileSize() {
    return getEntity() == null ? 0 : getEntity().getFileSize();
  }

  /**
   * 获取单位转换后的文件大小
   *
   * @return 文件大小{@code xxx mb}
   */
  public String getConvertFileSize() {
    return getEntity() == null ? "0b" : CommonUtil.formatFileSize(getEntity().getFileSize());
  }

  /**
   * 获取存放的扩展字段
   * 设置扩展字段{@link #setExtendField(String)}
   */
  public String getExtendField() {
    return getEntity().getStr();
  }

  /**
   * 获取任务状态
   *
   * @return {@link IEntity}
   */
  public int getTaskState() {
    return getEntity().getState();
  }

  /**
   * 获取任务进度百分比
   *
   * @return 返回任务进度
   */
  public int getPercent() {
    if (getEntity() == null) {
      ALog.e("AbsTarget", "下载管理器中没有该任务");
      return 0;
    }
    if (getEntity().getFileSize() != 0) {
      return (int) (getEntity().getCurrentProgress() * 100 / getEntity().getFileSize());
    }
    return 0;
  }

  /**
   * 停止任务
   */
  @Override
  public void stop() {
    onPre();
    getController().stop();
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
    onPre();
    getController().resume(newStart);
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
    onPre();
    getController().reTry();
  }

  /**
   * 删除任务
   *
   * @param removeFile {@code true} 不仅删除任务数据库记录，还会删除已经完成的文件
   * {@code false}如果任务已经完成，只删除任务数据库记录，
   */
  @Override
  public void cancel(boolean removeFile) {
    onPre();
    getController().cancel(removeFile);
  }

  /**
   * 重新下载
   */
  @Override
  public long reStart() {
    onPre();
    return getController().reStart();
  }

  @Override
  public void save() {
    onPre();
    getController().save();
  }
}
