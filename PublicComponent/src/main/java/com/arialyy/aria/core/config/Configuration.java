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
package com.arialyy.aria.core.config;

import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.util.FileUtil;
import java.io.File;

/**
 * Created by lyy on 2016/12/8. 信息配置 kotlin 方式有bug，不能将public去掉
 */
public final class Configuration {
  private static final String TAG = "Configuration";
  private static volatile Configuration INSTANCE = null;
  public static final String XML_FILE = "/Aria/aria_config.xml";
  static final String DOWNLOAD_CONFIG_FILE = "/Aria/AriaDownload.cfg";
  static final String UPLOAD_CONFIG_FILE = "/Aria/AriaUpload.cfg";
  static final String APP_CONFIG_FILE = "/Aria/AriaApp.cfg";
  static final String DGROUP_CONFIG_FILE = "/Aria/AriaDGroup.cfg";
  public DownloadConfig downloadCfg;
  public UploadConfig uploadCfg;
  public AppConfig appCfg;
  public DGroupConfig dGroupCfg;

  private Configuration() {
    //删除老版本的配置文件
    String basePath = AriaConfig.getInstance().getAPP().getFilesDir().getPath();
    del351Config(basePath);
    File newDCfg = new File(String.format("%s%s", basePath, DOWNLOAD_CONFIG_FILE));
    File newUCfg = new File(String.format("%s%s", basePath, UPLOAD_CONFIG_FILE));
    File newACfg = new File(String.format("%s%s", basePath, APP_CONFIG_FILE));
    File dgCfg = new File(String.format("%s%s", basePath, DGROUP_CONFIG_FILE));
    // 加载下载配置
    if (newDCfg.exists()) {
      downloadCfg = (DownloadConfig) FileUtil.readObjFromFile(newDCfg.getPath());
    }
    if (downloadCfg == null) {
      downloadCfg = new DownloadConfig();
    }
    // 加载上传配置
    if (newUCfg.exists()) {
      uploadCfg = (UploadConfig) FileUtil.readObjFromFile(newUCfg.getPath());
    }
    if (uploadCfg == null) {
      uploadCfg = new UploadConfig();
    }
    // 加载app配置
    if (newACfg.exists()) {
      appCfg = (AppConfig) FileUtil.readObjFromFile(newACfg.getPath());
    }
    if (appCfg == null) {
      appCfg = new AppConfig();
    }
    // 加载下载类型组合任务的配置
    if (dgCfg.exists()) {
      dGroupCfg = (DGroupConfig) FileUtil.readObjFromFile(dgCfg.getPath());
    }
    if (dGroupCfg == null) {
      dGroupCfg = new DGroupConfig();
    }
  }

  public static Configuration getInstance() {
    if (INSTANCE == null) {
      synchronized (AppConfig.class) {
        INSTANCE = new Configuration();
      }
    }
    return INSTANCE;
  }

  /**
   * 检查配置文件是否存在，只要{@link DownloadConfig}、{@link UploadConfig}、{@link AppConfig}、{@link
   * DGroupConfig}其中一个不存在 则任务配置文件不存在
   *
   * @return {@code true}配置存在，{@code false}配置不存在
   */
  public boolean configExists() {
    String basePath = AriaConfig.getInstance().getAPP().getFilesDir().getPath();
    return (new File(String.format("%s%s", basePath, DOWNLOAD_CONFIG_FILE))).exists()
        && (new File(String.format("%s%s", basePath, UPLOAD_CONFIG_FILE))).exists()
        && (new File(String.format("%s%s", basePath, APP_CONFIG_FILE))).exists()
        && (new File(String.format("%s%s", basePath, DGROUP_CONFIG_FILE))).exists();
  }

  /**
   * 删除3.5.2之前版本的配置文件，从3.5.2开始，配置文件的保存不再使用properties文件
   */
  private void del351Config(String basePath) {
    File oldDCfg = new File(String.format("%s/Aria/DownloadConfig.properties", basePath));
    if (oldDCfg.exists()) { // 只需要判断一个
      File oldUCfg = new File(String.format("%s/Aria/UploadConfig.properties", basePath));
      File oldACfg = new File(String.format("%s/Aria/AppConfig.properties", basePath));
      oldDCfg.delete();
      oldUCfg.delete();
      oldACfg.delete();
      // 删除配置触发更新
      File temp = new File(String.format("%s%s", basePath, XML_FILE));
      if (temp.exists()) {
        temp.delete();
      }
    }
  }
}
