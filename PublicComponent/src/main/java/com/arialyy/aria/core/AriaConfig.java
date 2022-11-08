/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import com.arialyy.aria.core.config.AppConfig;
import com.arialyy.aria.core.config.Configuration;
import com.arialyy.aria.core.config.DGroupConfig;
import com.arialyy.aria.core.config.DownloadConfig;
import com.arialyy.aria.core.config.UploadConfig;
import com.arialyy.aria.core.config.XMLReader;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

public class AriaConfig {
  private static final String TAG = "AriaConfig";

  public static final String DOWNLOAD_TEMP_DIR = "/Aria/temp/download/";
  public static final String UPLOAD_TEMP_DIR = "/Aria/temp/upload/";
  public static final String IGNORE_CLASS_KLASS = "shadow$_klass_";
  public static final String IGNORE_CLASS_MONITOR = "shadow$_monitor_";

  private static volatile AriaConfig Instance;
  private static Context APP;
  private DownloadConfig mDConfig;
  private UploadConfig mUConfig;
  private AppConfig mAConfig;
  private DGroupConfig mDGConfig;
  /**
   * 是否已经联网，true 已经联网
   */
  private static boolean isConnectedNet = true;
  private Handler mAriaHandler;

  private AriaConfig(Context context) {
    APP = context.getApplicationContext();
  }

  public static AriaConfig init(Context context) {
    if (Instance == null) {
      synchronized (AriaConfig.class) {
        if (Instance == null) {
          Instance = new AriaConfig(context);
          Instance.initData();
        }
      }
    }
    return Instance;
  }

  public static AriaConfig getInstance() {
    if (Instance == null) {
      ALog.e(TAG, "请使用init()初始化");
    }
    return Instance;
  }

  public Context getAPP() {
    return APP;
  }

  private void initData() {
    initConfig();
    regNetCallBack(APP);
  }

  public DownloadConfig getDConfig() {
    return mDConfig;
  }

  public UploadConfig getUConfig() {
    return mUConfig;
  }

  public AppConfig getAConfig() {
    return mAConfig;
  }

  public DGroupConfig getDGConfig() {
    return mDGConfig;
  }

  public synchronized Handler getAriaHandler() {
    if (mAriaHandler == null) {
      mAriaHandler = new Handler(Looper.getMainLooper());
    }
    return mAriaHandler;
  }

  /**
   * 注册网络监听，只有配置了检查网络{@link AppConfig#isNetCheck()}才会注册事件
   */
  private void regNetCallBack(Context context) {
    isConnectedNet = isNetworkAvailable();
    if (!getAConfig().isNetCheck()) {
      return;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      return;
    }
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) {
      return;
    }

    NetworkRequest.Builder builder = new NetworkRequest.Builder();
    NetworkRequest request = builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {

        @Override public void onLost(Network network) {
          super.onLost(network);
          isConnectedNet = isNetworkAvailable();
          ALog.d(TAG, "onLost, isConnectNet = " + isConnectedNet);
        }

        @Override public void onAvailable(Network network) {
          super.onAvailable(network);
          isConnectedNet = true;
          ALog.d(TAG, "onAvailable, isConnectNet = true");
        }
      });
    }
  }

  public boolean isNetworkAvailable() {
    // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
    ConnectivityManager connectivityManager =
        (ConnectivityManager) getAPP().getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivityManager == null) {

      return false;
    } else {
      // 获取NetworkInfo对象
      NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

      if (networkInfo != null && networkInfo.length > 0) {
        for (NetworkInfo info : networkInfo) {
          // 判断当前网络状态是否为连接状态
          if (info.getState() == NetworkInfo.State.CONNECTED) {
            return true;
          }
        }
      }
    }
    return false;
  }


  public boolean isConnectedNet() {
    return isConnectedNet;
  }

  /**
   * 初始化配置文件
   */
  private void initConfig() {
    mDConfig = Configuration.getInstance().downloadCfg;
    mUConfig = Configuration.getInstance().uploadCfg;
    mAConfig = Configuration.getInstance().appCfg;
    mDGConfig = Configuration.getInstance().dGroupCfg;

    File xmlFile = new File(APP.getFilesDir().getPath() + Configuration.XML_FILE);
    File tempDir = new File(APP.getFilesDir().getPath() + "/temp");
    if (!xmlFile.exists()) {
      loadConfig();
    } else {
      try {
        String md5Code = CommonUtil.getFileMD5(xmlFile);
        File file = new File(APP.getFilesDir().getPath() + "/temp.xml");
        if (file.exists()) {
          file.delete();
        }
        FileUtil.createFileFormInputStream(APP.getAssets().open("aria_config.xml"),
            file.getPath());
        if (!CommonUtil.checkMD5(md5Code, file) || !Configuration.getInstance().configExists()) {
          loadConfig();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (tempDir.exists()) {
      File newDir = new File(APP.getFilesDir().getPath() + AriaConfig.DOWNLOAD_TEMP_DIR);
      newDir.mkdirs();
      tempDir.renameTo(newDir);
    }
  }

  /**
   * 加载配置文件
   */
  private void loadConfig() {
    try {
      XMLReader helper = new XMLReader();
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(APP.getAssets().open("aria_config.xml"), helper);
      FileUtil.createFileFormInputStream(APP.getAssets().open("aria_config.xml"),
          APP.getFilesDir().getPath() + Configuration.XML_FILE);
    } catch (ParserConfigurationException | IOException | SAXException e) {
      ALog.e(TAG, e.toString());
    }
  }
}
