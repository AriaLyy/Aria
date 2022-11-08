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

package com.arialyy.simple.core.download;

import android.content.Context;
import android.net.Uri;
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

public class FtpDownloadModule extends BaseViewModule {
  private final String FTP_URL_KEY = "FTP_URL_KEY";
  private final String FTP_PATH_KEY = "FTP_PATH_KEY";

  private final String ftpDefUrl = "ftp://192.168.0.104:2121/qqqq.exe";
  private final String ftpDefPath =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

  private MutableLiveData<DownloadEntity> liveData = new MutableLiveData<>();
  private DownloadEntity singDownloadInfo;

  /**
   * 单任务下载的信息
   */
  LiveData<DownloadEntity> getFtpDownloadInfo(Context context) {
    //String url = AppUtil.getConfigValue(context, FTP_URL_KEY, ftpDefUrl);
    //String filePath = AppUtil.getConfigValue(context, FTP_PATH_KEY, ftpDefPath);
    //String url = "ftp://9.9.9.72:2121/Cyberduck-6.9.4.30164.zip";

    singDownloadInfo = Aria.download(context).getFirstDownloadEntity(ftpDefUrl);
    if (singDownloadInfo == null) {
      singDownloadInfo = new DownloadEntity();
      singDownloadInfo.setUrl(ftpDefUrl);
      String name = getFileName(ftpDefUrl);
      singDownloadInfo.setFileName(name);
      singDownloadInfo.setFilePath(ftpDefPath + name);
    } else {
      AppUtil.setConfigValue(context, FTP_PATH_KEY, singDownloadInfo.getFilePath());
      AppUtil.setConfigValue(context, FTP_URL_KEY, singDownloadInfo.getUrl());
    }
    liveData.postValue(singDownloadInfo);

    return liveData;
  }

  /**
   * 单任务下载的信息
   */
  LiveData<DownloadEntity> getSftpDownloadInfo(Context context) {
    //String url = AppUtil.getConfigValue(context, FTP_URL_KEY, ftpDefUrl);
    //String filePath = AppUtil.getConfigValue(context, FTP_PATH_KEY, ftpDefPath);
    String url = "ftp://9.9.9.72:22/Cyberduck-6.9.4.30164.zip";

    singDownloadInfo = Aria.download(context).getFirstDownloadEntity(url);
    if (singDownloadInfo == null) {
      singDownloadInfo = new DownloadEntity();
      singDownloadInfo.setUrl(url);
      String name = getFileName(ftpDefUrl);
      singDownloadInfo.setFileName(name);
      singDownloadInfo.setFilePath(ftpDefPath + name);
    } else {
      AppUtil.setConfigValue(context, FTP_PATH_KEY, singDownloadInfo.getFilePath());
      AppUtil.setConfigValue(context, FTP_URL_KEY, singDownloadInfo.getUrl());
    }
    liveData.postValue(singDownloadInfo);

    return liveData;
  }

  /**
   * 更新文件保存路径
   *
   * @param dirPath 文件保存文件夹
   */
  void updateFilePath(Context context, String dirPath) {
    if (TextUtils.isEmpty(dirPath)) {
      ALog.e(TAG, "文件保存路径为空");
      return;
    }
    AppUtil.setConfigValue(context, FTP_PATH_KEY, dirPath);
    singDownloadInfo.setFilePath(dirPath + singDownloadInfo.getFileName());
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
    AppUtil.setConfigValue(context, FTP_URL_KEY, url);
    File file = new File(singDownloadInfo.getFilePath());
    String name = getFileName(url);
    singDownloadInfo.setFilePath(file.getParent() + name);
    singDownloadInfo.setFileName(name);
    singDownloadInfo.setUrl(url);
    liveData.postValue(singDownloadInfo);
  }

  /**
   * 通过url获取文件名
   *
   * @param url ftp地址
   * @return "/aria.text"
   */
  private String getFileName(String url) {
    Uri uri = Uri.parse(url);
    String path = uri.getPath();
    int index = path.lastIndexOf("/");
    return path.substring(index);
  }
}
