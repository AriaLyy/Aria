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
import com.arialyy.aria.orm.annotation.Unique;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AriaL on 2017/6/3.
 */
public abstract class AbsGroupEntity extends AbsEntity implements Parcelable {
  /**
   * 组合任务等hash为： 为子任务地址相加的url的Md5
   * ftpdir为：ftpdir下载地址
   */
  @Unique protected String groupHash;

  /**
   * 任务组别名
   */
  private String alias;

  /**
   * 任务组下载文件的文件夹地址
   */
  @Unique private String dirPath;

  /**
   * 子任务url地址
   */
  private List<String> urls = new ArrayList<>();

  public String getDirPath() {
    return dirPath;
  }

  public void setDirPath(String dirPath) {
    this.dirPath = dirPath;
  }

  public List<String> getUrls() {
    return urls;
  }

  public void setUrls(List<String> urls) {
    this.urls = urls;
  }

  /**
   * 组合任务等hash为： 为子任务地址相加的url的Md5
   * ftpdir为：ftpdir下载地址
   */
  public String getGroupHash() {
    return groupHash;
  }

  public String getAlias() {
    return alias;
  }

  @Override public String getKey() {
    return groupHash;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public AbsGroupEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(this.groupHash);
    dest.writeString(this.alias);
  }

  protected AbsGroupEntity(Parcel in) {
    super(in);
    this.groupHash = in.readString();
    this.alias = in.readString();
  }
}
