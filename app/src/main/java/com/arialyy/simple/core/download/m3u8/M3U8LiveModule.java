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

package com.arialyy.simple.core.download.m3u8;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.frame.base.BaseViewModule;
import com.arialyy.simple.util.AppUtil;
import java.io.File;

public class M3U8LiveModule extends BaseViewModule {
  private final String M3U8_LIVE_URL_KEY = "M3U8_LIVE_URL_KEY";
  private final String M3U8_LIVE_PATH_KEY = "M3U8_LIVE_PATH_KEY";
  // 多码率地址：
  private final String defUrl = "http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8";
  private final String filePath =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
          + "/live.ts";

  private MutableLiveData<DownloadEntity> liveData = new MutableLiveData<>();
  private DownloadEntity singDownloadInfo;

  /**
   * 单任务下载的信息
   */
  LiveData<DownloadEntity> getHttpDownloadInfo(Context context) {
    String url = AppUtil.getConfigValue(context, M3U8_LIVE_URL_KEY, defUrl);
    String filePath = AppUtil.getConfigValue(context, M3U8_LIVE_PATH_KEY, this.filePath);

    singDownloadInfo = Aria.download(context).getFirstDownloadEntity(url);
    if (singDownloadInfo == null) {
      singDownloadInfo = new DownloadEntity();
      singDownloadInfo.setUrl(url);
      File temp = new File(this.filePath);
      singDownloadInfo.setFilePath(filePath);
      singDownloadInfo.setFileName(temp.getName());
    } else {
      AppUtil.setConfigValue(context, M3U8_LIVE_PATH_KEY, singDownloadInfo.getFilePath());
      AppUtil.setConfigValue(context, M3U8_LIVE_URL_KEY, singDownloadInfo.getUrl());
    }
    liveData.postValue(singDownloadInfo);

    return liveData;
  }

  /**
   * 更新文件保存路径
   *
   * @param filePath 文件保存路径
   */
  void updateFilePath(Context context, String filePath) {
    if (TextUtils.isEmpty(filePath)) {
      ALog.e(TAG, "文件保存路径为空");
      return;
    }
    File temp = new File(filePath);
    AppUtil.setConfigValue(context, M3U8_LIVE_PATH_KEY, filePath);
    singDownloadInfo.setFileName(temp.getName());
    singDownloadInfo.setFilePath(filePath);
    liveData.postValue(singDownloadInfo);
  }

  /**
   * 更新url
   */
  void uploadUrl(Context context, String url) {
    if (TextUtils.isEmpty(url)) {
      ALog.e(TAG, "下载地址为空");
      return;
    }
    AppUtil.setConfigValue(context, M3U8_LIVE_URL_KEY, url);
    singDownloadInfo.setUrl(url);
    liveData.postValue(singDownloadInfo);
  }
}
