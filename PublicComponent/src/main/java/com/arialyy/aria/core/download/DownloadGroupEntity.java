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
package com.arialyy.aria.core.download;

import android.os.Parcel;
import android.text.TextUtils;
import com.arialyy.aria.core.common.AbsGroupEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.annotation.Ignore;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AriaL on 2017/6/29. 下载任务组实体
 */
public class DownloadGroupEntity extends AbsGroupEntity {

  @Ignore private List<DownloadEntity> subEntities;

  /**
   * 子任务实体列表
   */
  public List<DownloadEntity> getSubEntities() {
    if (subEntities == null) {
      subEntities = new ArrayList<>();
    }
    return subEntities;
  }

  public void setSubEntities(List<DownloadEntity> subTasks) {
    this.subEntities = subTasks;
  }

  public void setGroupHash(String key) {
    this.groupHash = key;
  }

  @Override public int getTaskType() {
    if (getSubEntities() == null || getSubEntities().isEmpty() || TextUtils.isEmpty(
        getSubEntities().get(0).getUrl())) {
      return ITaskWrapper.ERROR;
    }
    return (groupHash.startsWith("ftp") || groupHash.startsWith("sftp")) ? ITaskWrapper.D_FTP_DIR
        : ITaskWrapper.DG_HTTP;
  }

  public DownloadGroupEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeTypedList(this.subEntities);
  }

  protected DownloadGroupEntity(Parcel in) {
    super(in);
    this.subEntities = in.createTypedArrayList(DownloadEntity.CREATOR);
  }

  public static final Creator<DownloadGroupEntity> CREATOR = new Creator<DownloadGroupEntity>() {
    @Override public DownloadGroupEntity createFromParcel(Parcel source) {
      return new DownloadGroupEntity(source);
    }

    @Override public DownloadGroupEntity[] newArray(int size) {
      return new DownloadGroupEntity[size];
    }
  };
}
