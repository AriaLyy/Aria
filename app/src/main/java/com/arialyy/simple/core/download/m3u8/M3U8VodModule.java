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

public class M3U8VodModule extends BaseViewModule {
  private final String M3U8_URL_KEY = "M3U8_URL_KEY";
  private final String M3U8_PATH_KEY = "M3U8_PATH_KEY";
  // m3u8测试集合：http://www.voidcn.com/article/p-snaliarm-ct.html
  //private final String defUrl = "https://www.gaoya123.cn/2019/1557993797897.m3u8";
  // 多码率地址：
  //private final String defUrl = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
  private final String defUrl = "https://5.voooe.cn/cache/youku/4e00b49a6e4f11155967c2cb3385a2ab.m3u8";
  //private final String defUrl = "http://pp3zvsk2n.bkt.clouddn.com/20200806/sd/15967206011811803/38475fadd55e4ecea3.m3u8";
  //private final String defUrl = "http://youku.cdn7-okzy.com/20200123/16815_fbe419ed/index.m3u8";

  //private final String defUrl = "https://cn7.kankia.com/hls/20200108/e1eaec074274c64fe46a3bdb5d2ba487/1578488360/index.m3u8";
  //private final String defUrl = "https://youku.cdn7-okzy.com/20191213/16167_c3592a02/index.m3u8";
  //private final String defUrl = "http://qn.shytong.cn/b83137769ff6b555/11b0c9970f9a3fa0.mp4.m3u8";
  //private final String defUrl = "https://135zyv5.xw0371.com/2018/10/29/X05c7CG3VB91gi1M/playlist.m3u8";
  //private final String defUrl = "https://fangao.qfxmj.com/video/20191111/dbf7e2aa0c5f42a8b040442b54c13e3a/cloudv-transfer/555555556nr593o75556r165q86n82n0_eb3da35b8f4442808756a6ddc8ec1372_0_3.m3u8?wsSecret=d5fb403512d4cd427f18858086b35ce4&wsTime=1582197537";
  //private final String defUrl = "https://v1.szjal.cn/20190819/Ql6UD1od/index.m3u8";
  private final String filePath =
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
          //+ "/道士下山.ts";
          + "/bb1.ts";

  private MutableLiveData<DownloadEntity> liveData = new MutableLiveData<>();
  private DownloadEntity singDownloadInfo;

  /**
   * 单任务下载的信息
   */
  LiveData<DownloadEntity> getHttpDownloadInfo(Context context) {
    String url = AppUtil.getConfigValue(context, M3U8_URL_KEY, defUrl);
    String filePath = AppUtil.getConfigValue(context, M3U8_PATH_KEY, this.filePath);

    singDownloadInfo = Aria.download(context).getFirstDownloadEntity(url);
    if (singDownloadInfo == null) {
      singDownloadInfo = new DownloadEntity();
      singDownloadInfo.setUrl(url);
      File temp = new File(this.filePath);
      singDownloadInfo.setFilePath(filePath);
      singDownloadInfo.setFileName(temp.getName());
    } else {
      AppUtil.setConfigValue(context, M3U8_PATH_KEY, singDownloadInfo.getFilePath());
      AppUtil.setConfigValue(context, M3U8_URL_KEY, singDownloadInfo.getUrl());
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
    AppUtil.setConfigValue(context, M3U8_PATH_KEY, filePath);
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
    AppUtil.setConfigValue(context, M3U8_URL_KEY, url);
    singDownloadInfo.setUrl(url);
    liveData.postValue(singDownloadInfo);
  }
}
