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
import android.content.res.Resources;
import android.os.Environment;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseModule;
import com.arialyy.simple.core.download.mutil.FileListEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lyy on 2016/9/27.
 */
public class DownloadModule extends BaseModule {

  public DownloadModule(Context context) {
    super(context);
  }

  /**
   * 最高优先级任务测试列表
   */
  public List<DownloadEntity> getHighestTestList() {
    List<DownloadEntity> list = new LinkedList<>();
    Resources res = getContext().getResources();
    String[] urls = res.getStringArray(R.array.highest_urls);
    String[] names = res.getStringArray(R.array.highest_names);
    for (int i = 0, len = urls.length; i < len; i++) {
      list.add(createDownloadEntity(urls[i], names[i]));
    }
    return list;
  }

  /**
   * 创建下载地址
   */
  public List<FileListEntity> createMultiTestList() {
    String[] names = getStringArray(R.array.file_nams);
    String[] downloadUrl = getStringArray(R.array.download_url);
    List<FileListEntity> list = new ArrayList<>();
    int i = 0;
    for (String name : names) {
      FileListEntity entity = new FileListEntity();
      entity.name = name;
      entity.key = downloadUrl[i];
      entity.downloadPath = Environment.getExternalStorageDirectory() + "/Download/" + name;
      list.add(entity);
      i++;
    }
    return list;
  }

  /**
   * 创建m3u8下载地址
   */
  public List<FileListEntity> createM3u8TestList(){
    String[] names = new String[]{"m3u8test1.ts", "m3u8test2.ts"};
    String[] urls = new String[]{
        "http://qn.shytong.cn/b83137769ff6b555/11b0c9970f9a3fa0.mp4.m3u8",
        "http://qn.shytong.cn/8f4011f2a31bd347da42b54fe37a7ba8-transcode.m3u8"
    };
    List<FileListEntity> list = new ArrayList<>();
    int i = 0;
    for (String name : names) {
      FileListEntity entity = new FileListEntity();
      entity.name = name;
      entity.key = urls[i];
      entity.type = 2;
      entity.downloadPath = Environment.getExternalStorageDirectory() + "/Download/" + name;
      list.add(entity);
      i++;
    }
    return list;
  }

  private String[] getStringArray(int array) {
    return getContext().getResources().getStringArray(array);
  }

  /**
   * 创建任务组
   */
  public List<FileListEntity> createGroupTestList() {
    List<FileListEntity> list = new ArrayList<>();
    list.add(createGroupEntity(R.array.group_urls, R.array.group_names, "任务组_0"));
    list.add(createGroupEntity(R.array.group_urls_1, R.array.group_names_1, "任务组_1"));
    list.add(createGroupEntity(R.array.group_urls_2, R.array.group_names_2, "任务组_2"));
    list.add(createGroupEntity(R.array.group_urls_3, R.array.group_names_3, "任务组_3"));
    return list;
  }

  private FileListEntity createGroupEntity(int urls, int names, String alias) {
    FileListEntity entity = new FileListEntity();
    entity.urls = getStringArray(urls);
    entity.names = getStringArray(names);
    entity.type = 1;
    entity.name = alias;
    entity.key = CommonUtil.getMd5Code(Arrays.asList(entity.urls));
    entity.downloadPath = Environment.getExternalStorageDirectory() + "/Download/" + alias;
    return entity;
  }

  /**
   * 创建下载实体，Aria也可以通过下载实体启动下载
   */
  private DownloadEntity createDownloadEntity(String downloadUrl, String name) {
    String path = Environment.getExternalStorageDirectory() + "/download/" + name + ".apk";
    DownloadEntity entity = new DownloadEntity();
    entity.setFileName(name);
    entity.setUrl(downloadUrl);
    entity.setFilePath(path);
    return entity;
  }
}