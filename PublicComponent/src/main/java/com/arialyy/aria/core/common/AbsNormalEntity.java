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
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.annotation.Default;

/**
 * Created by AriaL on 2017/6/3.
 */
public abstract class AbsNormalEntity extends AbsEntity implements Parcelable {

  /**
   * 服务器地址
   */
  private String url;

  /**
   * 文件名
   */
  private String fileName;

  /**
   * 是否是任务组里面的下载实体
   */
  @Default("false")
  private boolean isGroupChild = false;

  @Default("false")
  private boolean isRedirect = false; //是否重定向
  private String redirectUrl; //重定向链接

  /**
   * 任务类型
   * {@link ITaskWrapper}
   */
  private int taskType;


  public void setTaskType(int taskType) {
    this.taskType = taskType;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isGroupChild() {
    return isGroupChild;
  }

  public void setGroupChild(boolean groupChild) {
    isGroupChild = groupChild;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public boolean isRedirect() {
    return isRedirect;
  }

  public void setRedirect(boolean redirect) {
    isRedirect = redirect;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public abstract String getFilePath();

  public AbsNormalEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(this.url);
    dest.writeString(this.fileName);
    dest.writeByte(this.isGroupChild ? (byte) 1 : (byte) 0);
    dest.writeByte(this.isRedirect ? (byte) 1 : (byte) 0);
    dest.writeString(this.redirectUrl);
  }

  protected AbsNormalEntity(Parcel in) {
    super(in);
    this.url = in.readString();
    this.fileName = in.readString();
    this.isGroupChild = in.readByte() != 0;
    this.isRedirect = in.readByte() != 0;
    this.redirectUrl = in.readString();
  }
}
