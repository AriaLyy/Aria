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

public class HttpDownloadModule extends BaseViewModule {
  private final String HTTP_URL_KEY = "HTTP_URL_KEY";
  private final String HTTP_PATH_KEY = "HTTP_PATH_KEY";

  private final String defUrl =
      "http://hzdown.muzhiwan.com/2017/05/08/nl.noio.kingdom_59104935e56f0.apk";
      //"https://ss1.baidu.com/-4o3dSag_xI4khGko9WTAnF6hhy/image/h%3D300/sign=a9e671b9a551f3dedcb2bf64a4eff0ec/4610b912c8fcc3cef70d70409845d688d53f20f7.jpg";
  //"http://9.9.9.205:5000/download/Cyberduck-6.9.4.30164.zip";
  //"http://202.98.201.103:7000/vrs/TPK/ZTC440402001Z.tpk";
  private final String defFilePath =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
          + "/update.zip";

  private MutableLiveData<DownloadEntity> liveData = new MutableLiveData<>();
  private DownloadEntity singDownloadInfo;

  /**
   * 单任务下载的信息
   */
  LiveData<DownloadEntity> getHttpDownloadInfo(Context context) {
    //String url = AppUtil.getConfigValue(context, HTTP_URL_KEY, defUrl);
    //String url = "http://fdfs.speedata.cn:9989/group1/M00/00/05/rBGFrl3fdAKAVJwfMtSa9R18wLU139.zip";
    //String url = "http://9.9.9.28:8088/files/update.zip";
    String url = "https://y.qq.com/download/import/QQMusic-import-1.2.1.zip";
    //String url = "https://gitee.com/huang-junhua/iptv/raw/master/guonei.m3u8";
    //String url = "http://v.kjjl100.com/kz/zx/cj/2020cjswxdb/1.mp4";
    //String url = "https://static.runoob.com/images/demo/demo2.jpg";
    String filePath = "/mnt/sdcard/qq.zip";

    singDownloadInfo = Aria.download(context).getFirstDownloadEntity(url);
    if (singDownloadInfo == null) {
      singDownloadInfo = new DownloadEntity();
      singDownloadInfo.setUrl(url);
      File file = new File(filePath);
      singDownloadInfo.setFilePath(filePath);
      singDownloadInfo.setFileName(file.getName());
    } else {
      AppUtil.setConfigValue(context, HTTP_PATH_KEY, singDownloadInfo.getFilePath());
      AppUtil.setConfigValue(context, HTTP_URL_KEY, singDownloadInfo.getUrl());
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
    AppUtil.setConfigValue(context, HTTP_PATH_KEY, filePath);
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
    AppUtil.setConfigValue(context, HTTP_URL_KEY, url);
    singDownloadInfo.setUrl(url);
    liveData.postValue(singDownloadInfo);
  }
}
