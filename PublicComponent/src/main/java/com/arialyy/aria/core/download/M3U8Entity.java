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
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.annotation.Default;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.DbDataHelper;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * M3U8实体信息
 */
public class M3U8Entity extends DbEntity implements Parcelable {

  /**
   * 文件保存路径
   */
  private String filePath;

  /**
   * 当前peer的位置
   */
  private int peerIndex;

  /**
   * peer总数
   */
  private int peerNum;

  /**
   * 是否是直播，true 直播
   */
  private boolean isLive;

  /**
   * 缓存目录
   */
  private String cacheDir;

  /**
   * 加密key保存地址
   */
  public String keyPath;

  /**
   * 加密key的下载地址
   */
  public String keyUrl;

  /**
   * 加密算法
   */
  public String method;

  /**
   * key的iv值
   */
  public String iv;

  /**
   * key的格式，可能为空
   */
  public String keyFormat;

  /**
   * key的格式版本，默认为1，如果是多个版本，使用"/"分隔，如："1", "1/2", or "1/2/5"
   */
  @Default("1")
  public String keyFormatVersion = "1";

  public String getKeyFormat() {
    return keyFormat;
  }

  public void setKeyFormat(String keyFormat) {
    this.keyFormat = keyFormat;
  }

  public String getKeyFormatVersion() {
    return keyFormatVersion;
  }

  public void setKeyFormatVersion(String keyFormatVersion) {
    this.keyFormatVersion = keyFormatVersion;
  }

  public String getKeyPath() {
    return keyPath;
  }

  public void setKeyPath(String keyPath) {
    this.keyPath = keyPath;
  }

  public String getKeyUrl() {
    return keyUrl;
  }

  public void setKeyUrl(String keyUrl) {
    this.keyUrl = keyUrl;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getIv() {
    return iv;
  }

  public void setIv(String iv) {
    this.iv = iv;
  }

  public boolean isLive() {
    return isLive;
  }

  /**
   * 获取m3u8切片
   * 如果任务未完成，则返回所有已下载完成的切片；
   * 如果任务已完成，如果你设置了合并分块的请求，返回null；如果没有设置该请求，则返回所有已下载完成的切片
   */
  public List<PeerInfo> getCompletedPeer() {
    if (TextUtils.isEmpty(getCacheDir())) {
      ALog.w("M3U8Entity", "任务未下载，获取切片失败");
      return null;
    }
    List<PeerInfo> peers = new ArrayList<>();
    TaskRecord taskRecord = DbDataHelper.getTaskRecord(filePath,
        isLive ? ITaskWrapper.M3U8_LIVE : ITaskWrapper.M3U8_VOD);
    File cacheDir = new File(getCacheDir());
    if ((taskRecord == null
        || taskRecord.threadRecords == null
        || taskRecord.threadRecords.isEmpty())
        && !cacheDir.exists()) {
      return null;
    }

    // 处理任务完成的情况
    if (taskRecord == null
        || taskRecord.threadRecords == null
        || taskRecord.threadRecords.isEmpty()
        && cacheDir.exists()) {
      String[] files = cacheDir.list(new FilenameFilter() {
        @Override public boolean accept(File dir, String name) {
          return name.endsWith(".ts");
        }
      });
      for (String fileName : files) {

        PeerInfo peerInfo =
            new PeerInfo(Integer.parseInt(fileName.substring(0, fileName.lastIndexOf(".ts"))),
                getCacheDir().concat("/").concat(fileName));
        peers.add(peerInfo);
      }

      return peers;
    }

    // 任务未完成的情况
    if (taskRecord.threadRecords != null
        && !taskRecord.threadRecords.isEmpty()
        && cacheDir.exists()) {

      for (ThreadRecord tr : taskRecord.threadRecords) {
        if (!tr.isComplete) {
          continue;
        }
        String peerPath = String.format("%s/%s.ts", cacheDir, tr.threadId);
        if (new File(peerPath).exists()) {
          PeerInfo peerInfo = new PeerInfo(tr.threadId, peerPath);
          peers.add(peerInfo);
        }
      }
      return peers;
    }

    return null;
  }

  public String getCacheDir() {
    return cacheDir;
  }

  public void setCacheDir(String cacheDir) {
    this.cacheDir = cacheDir;
  }

  public void setLive(boolean live) {
    isLive = live;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public int getPeerIndex() {
    return peerIndex;
  }

  public void setPeerIndex(int peerIndex) {
    this.peerIndex = peerIndex;
  }

  public int getPeerNum() {
    return peerNum;
  }

  public void setPeerNum(int peerNum) {
    this.peerNum = peerNum;
  }

  public M3U8Entity() {
  }

  public static class PeerInfo {
    public PeerInfo(int peerId, String peerPath) {
      this.peerId = peerId;
      this.peerPath = peerPath;
    }

    public int peerId;
    public String peerPath;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.filePath);
    dest.writeInt(this.peerIndex);
    dest.writeInt(this.peerNum);
    dest.writeByte(this.isLive ? (byte) 1 : (byte) 0);
    dest.writeString(this.cacheDir);
    dest.writeString(this.keyPath);
    dest.writeString(this.keyUrl);
    dest.writeString(this.method);
    dest.writeString(this.iv);
  }

  protected M3U8Entity(Parcel in) {
    this.filePath = in.readString();
    this.peerIndex = in.readInt();
    this.peerNum = in.readInt();
    this.isLive = in.readByte() != 0;
    this.cacheDir = in.readString();
    this.keyPath = in.readString();
    this.keyUrl = in.readString();
    this.method = in.readString();
    this.iv = in.readString();
  }

  public static final Creator<M3U8Entity> CREATOR = new Creator<M3U8Entity>() {
    @Override public M3U8Entity createFromParcel(Parcel source) {
      return new M3U8Entity(source);
    }

    @Override public M3U8Entity[] newArray(int size) {
      return new M3U8Entity[size];
    }
  };
}
