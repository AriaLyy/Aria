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

import android.os.Parcel;
import android.os.Parcelable;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.annotation.Default;
import com.arialyy.aria.orm.annotation.Ignore;
import java.io.Serializable;

/**
 * Created by AriaL on 2017/6/29.
 */
public abstract class AbsEntity extends DbEntity implements IEntity, Parcelable, Serializable {
  /**
   * 速度
   */
  @Ignore private long speed = 0;
  /**
   * 单位转换后的速度
   */
  @Ignore private String convertSpeed;
  /**
   * 下载失败计数，每次开始都重置为0
   */
  @Ignore private int failNum = 0;

  /**
   * 剩余时间，单位：s
   */
  @Ignore private int timeLeft = Integer.MAX_VALUE;

  /**
   * 扩展字段
   */
  private String str;
  /**
   * 文件大小
   */
  private long fileSize = 0;
  /**
   * 转换后的文件大小
   */
  private String convertFileSize;

  /**
   * 任务状态{@link IEntity}
   */
  @Default("3")
  private int state = STATE_WAIT;
  /**
   * 当前下载进度
   */
  private long currentProgress = 0;
  /**
   * 完成时间
   */
  private long completeTime;

  /**
   * 进度百分比
   */
  private int percent;

  @Default("false")
  private boolean isComplete = false;

  /**
   * 上一次停止时间
   */
  private long stopTime = 0;

  @Ignore
  private int netCode = 200;

  public int getNetCode() {
    return netCode;
  }

  public void setNetCode(int netCode) {
    this.netCode = netCode;
  }

  /**
   * 获取剩余时间，单位：s
   * 如果是m3u8任务，无法获取剩余时间；m2u8任务如果需要获取剩余时间，请设置文件长度{@link #setFileSize(long)}
   */
  public int getTimeLeft() {
    return timeLeft;
  }

  public void setTimeLeft(int timeLeft) {
    this.timeLeft = timeLeft;
  }

  public boolean isComplete() {
    return isComplete;
  }

  public void setComplete(boolean complete) {
    isComplete = complete;
  }

  public String getConvertFileSize() {
    return convertFileSize;
  }

  public void setConvertFileSize(String convertFileSize) {
    this.convertFileSize = convertFileSize;
  }

  public int getFailNum() {
    return failNum;
  }

  public void setFailNum(int failNum) {
    this.failNum = failNum;
  }

  public long getSpeed() {
    return speed;
  }

  public void setSpeed(long speed) {
    this.speed = speed;
  }

  public String getConvertSpeed() {
    return convertSpeed;
  }

  public void setConvertSpeed(String convertSpeed) {
    this.convertSpeed = convertSpeed;
  }

  public String getStr() {
    return str;
  }

  public void setStr(String str) {
    this.str = str;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public long getCurrentProgress() {
    return currentProgress;
  }

  public void setCurrentProgress(long currentProgress) {
    this.currentProgress = currentProgress;
  }

  public long getCompleteTime() {
    return completeTime;
  }

  public void setCompleteTime(long completeTime) {
    this.completeTime = completeTime;
  }

  public int getPercent() {
    return percent;
  }

  public void setPercent(int percent) {
    this.percent = percent;
  }

  public long getStopTime() {
    return stopTime;
  }

  public void setStopTime(long stopTime) {
    this.stopTime = stopTime;
  }

  public long getId() {
    return getRowID();
  }

  /**
   * 实体唯一标识符
   * 下载实体：下载url
   * 上传实体：文件路径
   * 下载任务组：组名
   * ftp文件夹下载：下载url
   */
  public abstract String getKey();

  /**
   * 实体驱动的下载任务类型
   *
   * @return {@link ITaskWrapper#D_FTP}、{@link ITaskWrapper#D_FTP_DIR}、{@link
   * ITaskWrapper#U_HTTP}...
   */
  public abstract int getTaskType();

  public AbsEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(this.rowID);
    dest.writeLong(this.speed);
    dest.writeString(this.convertSpeed);
    dest.writeInt(this.failNum);
    dest.writeString(this.str);
    dest.writeLong(this.fileSize);
    dest.writeString(this.convertFileSize);
    dest.writeInt(this.state);
    dest.writeLong(this.currentProgress);
    dest.writeLong(this.completeTime);
    dest.writeInt(this.percent);
    dest.writeByte(this.isComplete ? (byte) 1 : (byte) 0);
    dest.writeLong(this.stopTime);
  }

  protected AbsEntity(Parcel in) {
    this.rowID = in.readLong();
    this.speed = in.readLong();
    this.convertSpeed = in.readString();
    this.failNum = in.readInt();
    this.str = in.readString();
    this.fileSize = in.readLong();
    this.convertFileSize = in.readString();
    this.state = in.readInt();
    this.currentProgress = in.readLong();
    this.completeTime = in.readLong();
    this.percent = in.readInt();
    this.isComplete = in.readByte() != 0;
    this.stopTime = in.readLong();
  }
}
