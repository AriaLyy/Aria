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
import android.os.Parcelable;
import android.text.TextUtils;
import com.arialyy.aria.core.common.AbsNormalEntity;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.annotation.Ignore;
import com.arialyy.aria.orm.annotation.Unique;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by lyy on 2015/12/25.
 * 下载实体
 */
public class DownloadEntity extends AbsNormalEntity implements Parcelable, Cloneable {
  @Unique private String downloadPath; //保存路径

  /**
   * 所属任务组
   */
  private String groupHash;

  /**
   * 从服务器的返回信息中获取的文件md5信息，如果服务器没有返回，则不会设置该信息
   * 如果你已经设置了该任务的MD5信息，Aria也不会从服务器返回的信息中获取该信息
   */
  private String md5Code;

  /**
   * 从服务器的返回信息中获取的文件描述信息
   */
  private String disposition;

  /**
   * 从disposition获取到的文件名，如果可以获取到，则会赋值到这个字段
   */
  private String serverFileName;

  @Ignore
  private M3U8Entity m3U8Entity;

  /**
   * 获取m3u8数据信息
   *
   * @return 如果m3u8信息为空，则返回null
   */
  public M3U8Entity getM3U8Entity() {
    if (TextUtils.isEmpty(downloadPath)) {
      ALog.e("DownloadEntity", "文件保存路径为空，获取m3u8实体之前需要设置文件保存路径");
      return null;
    }
    if (m3U8Entity == null) {
      m3U8Entity = DbEntity.findFirst(M3U8Entity.class, "filePath=?", downloadPath);
    }
    return m3U8Entity;
  }

  /**
   * 设置进来的地址，如果需要获取真实的下载地址，请使用{@link #getRealUrl()}
   */
  @Override public String getKey() {
    return getUrl();
  }

  public String getRealUrl(){
    return isRedirect() ? getRedirectUrl() : getUrl();
  }

  @Override public int getTaskType() {
    int type;
    if (getUrl() == null) {
      type = ITaskWrapper.ERROR;
    } else if (getUrl().startsWith("http")) {
      M3U8Entity temp = getM3U8Entity();
      if (temp == null) {
        type = ITaskWrapper.D_HTTP;
      } else {
        type = temp.isLive() ? ITaskWrapper.M3U8_LIVE : ITaskWrapper.M3U8_VOD;
      }
    } else if (getUrl().startsWith("ftp")) {
      type = ITaskWrapper.D_FTP;
    } else if (getUrl().startsWith("sftp")) {
      type = ITaskWrapper.D_SFTP;
    } else {
      type = ITaskWrapper.ERROR;
    }
    return type;
  }

  public DownloadEntity() {
  }

  public String getMd5Code() {
    return md5Code;
  }

  public void setMd5Code(String md5Code) {
    this.md5Code = md5Code;
  }

  public String getDisposition() {
    return TextUtils.isEmpty(disposition) ? "" : CommonUtil.decryptBASE64(disposition);
  }

  public void setDisposition(String disposition) {
    this.disposition = disposition;
  }

  public String getServerFileName() {
    return serverFileName;
  }

  public void setServerFileName(String serverFileName) {
    this.serverFileName = serverFileName;
  }

  public String getGroupHash() {
    return groupHash;
  }

  public void setGroupHash(String groupHash) {
    this.groupHash = groupHash;
  }

  @Override
  public String getFilePath() {
    return downloadPath;
  }

  public DownloadEntity setFilePath(String filePath) {
    this.downloadPath = filePath;
    return this;
  }

  @Override public DownloadEntity clone() throws CloneNotSupportedException {
    return (DownloadEntity) super.clone();
  }

  @Override public String toString() {
    return "DownloadEntity{"
        + "downloadPath='"
        + downloadPath
        + '\''
        + ", groupHash='"
        + groupHash
        + '\''
        + ", fileName='"
        + getFileName()
        + '\''
        + ", md5Code='"
        + md5Code
        + '\''
        + ", disposition='"
        + disposition
        + '\''
        + ", serverFileName='"
        + serverFileName
        + '\''
        + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(this.downloadPath);
    dest.writeString(this.groupHash);
    dest.writeString(this.md5Code);
    dest.writeString(this.disposition);
    dest.writeString(this.serverFileName);
    dest.writeParcelable(this.m3U8Entity, flags);
  }

  protected DownloadEntity(Parcel in) {
    super(in);
    this.downloadPath = in.readString();
    this.groupHash = in.readString();
    this.md5Code = in.readString();
    this.disposition = in.readString();
    this.serverFileName = in.readString();
    this.m3U8Entity = in.readParcelable(M3U8Entity.class.getClassLoader());
  }

  public static final Creator<DownloadEntity> CREATOR = new Creator<DownloadEntity>() {
    @Override public DownloadEntity createFromParcel(Parcel source) {
      return new DownloadEntity(source);
    }

    @Override public DownloadEntity[] newArray(int size) {
      return new DownloadEntity[size];
    }
  };
}