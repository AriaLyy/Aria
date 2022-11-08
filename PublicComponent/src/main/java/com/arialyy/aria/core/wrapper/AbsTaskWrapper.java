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
package com.arialyy.aria.core.wrapper;

import com.arialyy.aria.core.TaskOptionParams;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.config.BaseTaskConfig;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.event.ErrorEvent;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITaskOption;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.util.ComponentUtil;

/**
 * Created by lyy on 2017/2/23. 所有任务实体的父类
 */
public abstract class AbsTaskWrapper<ENTITY extends AbsEntity> implements ITaskWrapper {

  /**
   * 刷新信息 {@code true} 重新刷新下载信息
   */
  private boolean refreshInfo = false;

  /**
   * 是否是新任务，{@code true} 新任务
   */
  private boolean isNewTask = false;

  /**
   * 请求类型 {@link ITaskWrapper#D_HTTP}、{@link ITaskWrapper#D_FTP}、{@link
   * ITaskWrapper#D_FTP_DIR}..
   */
  private int requestType = D_HTTP;

  /**
   * 删除任务时，是否删除已下载完成的文件 未完成的任务，不管true还是false，都会删除文件 {@code true}  删除任务数据库记录，并且删除已经下载完成的文件 {@code
   * false} 如果任务已经完成，只删除任务数据库记录
   */
  private boolean removeFile = false;

  /**
   * 是否支持断点, {@code true} 为支持断点
   */
  private boolean isSupportBP = true;

  /**
   * 状态码
   */
  private int code;

  /**
   * {@link DownloadEntity} or {@link UploadEntity} or {@link DownloadGroupEntity}
   */
  private ENTITY entity;

  /**
   * 错误信息
   */
  private ErrorEvent errorEvent;

  /**
   * 任务配置
   */
  private ITaskOption taskOption;

  /**
   * 任务配置信息
   */
  private TaskOptionParams optionParams = new TaskOptionParams();

  /**
   * {@code true}强制下载\上传，不考虑文件路径是否被占用
   */
  private boolean ignoreFilePathOccupy = false;

  public boolean isIgnoreFilePathOccupy() {
    return ignoreFilePathOccupy;
  }

  public void setIgnoreFilePathOccupy(boolean ignoreFilePathOccupy) {
    this.ignoreFilePathOccupy = ignoreFilePathOccupy;
  }

  public void setTaskOption(ITaskOption option) {
    this.taskOption = option;
  }

  public ITaskOption getTaskOption() {
    return taskOption;
  }

  public void generateTaskOption(Class<? extends ITaskOption> clazz) {
    taskOption = ComponentUtil.getInstance().buildTaskOption(clazz, optionParams);
  }

  public TaskOptionParams getOptionParams() {
    if (optionParams == null) {
      optionParams = new TaskOptionParams();
    }
    return optionParams;
  }

  public AbsTaskWrapper(ENTITY entity) {
    this.entity = entity;
  }

  public ErrorEvent getErrorEvent() {
    return errorEvent;
  }

  public void setErrorEvent(ErrorEvent errorEvent) {
    this.errorEvent = errorEvent;
  }

  @Override public ENTITY getEntity() {
    return entity;
  }

  /**
   * 获取任务下载状态
   *
   * @return {@link IEntity}
   */
  public int getState() {
    return getEntity().getState();
  }

  public abstract BaseTaskConfig getConfig();

  public boolean isRefreshInfo() {
    return refreshInfo;
  }

  public void setRefreshInfo(boolean refreshInfo) {
    this.refreshInfo = refreshInfo;
  }

  public boolean isNewTask() {
    return isNewTask;
  }

  public void setNewTask(boolean newTask) {
    isNewTask = newTask;
  }

  public void setState(int state) {
    entity.setState(state);
  }

  @Override
  public int getRequestType() {
    return requestType;
  }

  public void setRequestType(int requestType) {
    this.requestType = requestType;
  }

  public boolean isRemoveFile() {
    return removeFile;
  }

  public void setRemoveFile(boolean removeFile) {
    this.removeFile = removeFile;
  }

  public boolean isSupportBP() {
    return isSupportBP;
  }

  public void setSupportBP(boolean supportBP) {
    isSupportBP = supportBP;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }
}
