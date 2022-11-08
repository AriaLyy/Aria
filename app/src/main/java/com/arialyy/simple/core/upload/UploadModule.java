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

package com.arialyy.simple.core.upload;

import android.content.Context;
import android.os.Environment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.frame.base.BaseViewModule;
import com.arialyy.simple.util.AppUtil;

public class UploadModule extends BaseViewModule {
  private final String FTP_URL_KEY = "FTP_URL_KEY";
  private final String FTP_PATH_KEY = "FTP_PATH_KEY";
  private MutableLiveData<UploadEntity> liveData = new MutableLiveData<>();
  UploadEntity uploadInfo;

  /**
   * 获取Ftp上传信息
   */
  LiveData<UploadEntity> getFtpInfo(Context context) {
    //String url = AppUtil.getConfigValue(context, FTP_URL_KEY, "ftp://9.9.9.72:2121/aab/你好");
    //String filePath = AppUtil.getConfigValue(context, FTP_PATH_KEY,
    //    Environment.getExternalStorageDirectory().getPath() + "/Download/AndroidAria.db");
    String url = "ftp://192.168.0.105:2121/aab/你好";
    String filePath = "/mnt/sdcard/QQMusic-import-1.2.1.zip";

    UploadEntity entity = Aria.upload(context).getFirstUploadEntity(filePath);
    if (entity != null) {
      uploadInfo = entity;
      AppUtil.setConfigValue(context, FTP_URL_KEY, uploadInfo.getUrl());
      AppUtil.setConfigValue(context, FTP_PATH_KEY, uploadInfo.getFilePath());
    } else {
      uploadInfo = new UploadEntity();
      uploadInfo.setUrl(url);
      uploadInfo.setFilePath(filePath);
    }

    liveData.postValue(uploadInfo);
    return liveData;
  }

  /**
   * 获取Ftp上传信息
   */
  LiveData<UploadEntity> getSFtpInfo(Context context) {
    //String url = AppUtil.getConfigValue(context, FTP_URL_KEY, "ftp://9.9.9.72:2121/aab/你好");
    //String filePath = AppUtil.getConfigValue(context, FTP_PATH_KEY,
    //    Environment.getExternalStorageDirectory().getPath() + "/Download/AndroidAria.db");
    String url = "ftp://9.9.9.72:22/aab/你好";
    String filePath = "/mnt/sdcard/QQMusic-import-1.2.1.zip";

    UploadEntity entity = Aria.upload(context).getFirstUploadEntity(filePath);
    if (entity != null) {
      uploadInfo = entity;
      AppUtil.setConfigValue(context, FTP_URL_KEY, uploadInfo.getUrl());
      AppUtil.setConfigValue(context, FTP_PATH_KEY, uploadInfo.getFilePath());
    } else {
      uploadInfo = new UploadEntity();
      uploadInfo.setUrl(url);
      uploadInfo.setFilePath(filePath);
    }

    liveData.postValue(uploadInfo);
    return liveData;
  }

  /**
   * 更新Url
   */
  void updateFtpUrl(Context context, String url) {
    uploadInfo.setUrl(url);
    AppUtil.setConfigValue(context, FTP_URL_KEY, url);
    liveData.postValue(uploadInfo);
  }

  /**
   * 更新文件路径
   */
  void updateFtpFilePath(Context context, String filePath) {
    uploadInfo.setFilePath(filePath);
    AppUtil.setConfigValue(context, FTP_PATH_KEY, filePath);
    liveData.postValue(uploadInfo);
  }
}
