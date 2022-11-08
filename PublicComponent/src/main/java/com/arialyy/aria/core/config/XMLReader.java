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

import android.text.TextUtils;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by lyy on 2017/5/22. 读取配置文件
 */
public class XMLReader extends DefaultHandler {
  private final String TAG = CommonUtil.getClassName(this);

  private DownloadConfig mDownloadConfig = Configuration.getInstance().downloadCfg;
  private UploadConfig mUploadConfig = Configuration.getInstance().uploadCfg;
  private AppConfig mAppConfig = Configuration.getInstance().appCfg;
  private DGroupConfig mDGroupConfig = Configuration.getInstance().dGroupCfg;
  private int mType;

  @Override public void startDocument() throws SAXException {
    super.startDocument();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    super.startElement(uri, localName, qName, attributes);
    switch (qName) {
      case "download":
        mType = ConfigType.DOWNLOAD;
        break;
      case "upload":
        mType = ConfigType.UPLOAD;
        break;
      case "app":
        mType = ConfigType.APP;
        break;
      case "dGroup":
        mType = ConfigType.D_GROUP;
        break;
    }

    if (mType == ConfigType.DOWNLOAD || mType == ConfigType.UPLOAD || mType == ConfigType.D_GROUP) {

      String value = attributes.getValue("value");
      switch (qName) {
        case "threadNum": // 线程数
          int threadNum = checkInt(value) ? Integer.parseInt(value) : 3;
          if (threadNum < 1) {
            ALog.w(TAG, "下载线程数不能小于 1");
            threadNum = 1;
          }
          setField("threadNum", threadNum, ConfigType.DOWNLOAD);
          break;
        case "maxTaskNum":  //最大任务书
          int maxTaskNum = checkInt(value) ? Integer.parseInt(value) : 2;
          if (maxTaskNum < 1) {
            ALog.w(TAG, "任务队列数不能小于 1");
            maxTaskNum = 2;
          }
          setField("maxTaskNum", maxTaskNum, mType);
          break;
        case "reTryNum":  //任务重试次数
          setField("reTryNum", checkInt(value) ? Integer.parseInt(value) : 0, mType);
          break;
        case "connectTimeOut": // 连接超时时间
          setField("connectTimeOut", checkInt(value) ? Integer.parseInt(value) : 5 * 1000,
              mType);
          break;
        case "iOTimeOut":   //io流超时时间
          int iOTimeOut = checkInt(value) ? Integer.parseInt(value) : 10 * 1000;
          if (iOTimeOut < 10 * 1000) {
            iOTimeOut = 10 * 1000;
          }
          setField("iOTimeOut", iOTimeOut, mType);
          break;
        case "reTryInterval":   //失败重试间隔
          int reTryInterval = checkInt(value) ? Integer.parseInt(value) : 2 * 1000;

          if (reTryInterval < 2 * 1000) {
            reTryInterval = 2 * 1000;
          }
          setField("reTryInterval", reTryInterval, mType);
          break;
        case "buffSize":    //缓冲大小
          int buffSize = checkInt(value) ? Integer.parseInt(value) : 8192;

          if (buffSize < 2048) {
            buffSize = 2048;
          }

          setField("buffSize", buffSize, mType);
          break;
        case "ca":    // ca证书
          String caName = attributes.getValue("name");
          String caPath = attributes.getValue("path");
          setField("caName", caName, mType);
          setField("caPath", caPath, mType);
          break;
        case "convertSpeed": // 是否转换速度
          setField("isConvertSpeed", !checkBoolean(value) || Boolean.parseBoolean(value),
              mType);
          break;
        case "maxSpeed":  // 最大速度
          int maxSpeed = checkInt(value) ? Integer.parseInt(value) : 0;
          setField("maxSpeed", maxSpeed, mType);
          break;
        case "queueMod":  // 队列类型
          String mod = "now";
          if (!TextUtils.isEmpty(value) && (value.equalsIgnoreCase("now") || value.equalsIgnoreCase(
              "wait"))) {
            mod = value;
          }
          setField("queueMod", mod, mType);
          break;
        case "updateInterval":  // 进度更新时间
          setField("updateInterval", checkLong(value) ? Long.parseLong(value) : 1000,
              mType);
          break;

        case "useBlock":    // 是否使用分块任务
          setField("useBlock", checkBoolean(value) ? Boolean.valueOf(value) : false,
              ConfigType.DOWNLOAD);
          break;
        case "subMaxTaskNum": // 子任务最大任务数
          int subMaxTaskNum = checkInt(value) ? Integer.parseInt(value) : 3;
          setField("subMaxTaskNum", subMaxTaskNum, ConfigType.D_GROUP);
          break;
        case "subFailAsStop": // 子任务失败时回调stop
          setField("subFailAsStop", checkBoolean(value) ? Boolean.valueOf(value) : false,
              ConfigType.D_GROUP);
          break;
        case "subReTryNum": // 子任务重试次数
          int subReTryNum = checkInt(value) ? Integer.parseInt(value) : 5;
          setField("subReTryNum", subReTryNum, ConfigType.D_GROUP);
          break;
        case "subReTryInterval":  // 子任务重试间隔
          int subReTryInterval = checkInt(value) ? Integer.parseInt(value) : 2000;
          setField("subReTryInterval", subReTryInterval, ConfigType.D_GROUP);
          break;
        case "useHeadRequest": // 是否使用head请求
          boolean useHeadRequest = checkBoolean(value) ? Boolean.valueOf(value) : false;
          setField("useHeadRequest", useHeadRequest, ConfigType.DOWNLOAD);
          break;
      }
    } else if (mType == ConfigType.APP) {
      String value = attributes.getValue("value");
      switch (qName) {
        case "useAriaCrashHandler": // 是否捕捉崩溃日志
          setField("useAriaCrashHandler", checkBoolean(value) ? Boolean.valueOf(value) : true,
              ConfigType.APP);
          break;
        case "logLevel":    // 日记等级
          int level = checkInt(value) ? Integer.parseInt(value) : ALog.LOG_LEVEL_VERBOSE;
          if (level < ALog.LOG_LEVEL_VERBOSE || level > ALog.LOG_CLOSE) {
            ALog.w(TAG, "level【" + level + "】错误");
            level = ALog.LOG_LEVEL_VERBOSE;
          }
          setField("logLevel", level, ConfigType.APP);
          break;
        case "netCheck":    // 是否检查网络
          setField("netCheck", checkBoolean(value) ? Boolean.valueOf(value) : false,
              ConfigType.APP);
          break;
        case "useBroadcast":  // 是否使用广播
          setField("useBroadcast", checkBoolean(value) ? Boolean.valueOf(value) : false,
              ConfigType.APP);
          break;
        case "notNetRetry":   // 没有网络也重试
          setField("notNetRetry", checkBoolean(value) ? Boolean.valueOf(value) : false,
              ConfigType.APP);
          break;
      }
    }
  }

  private void setField(String key, Object value, int type) {
    if (type == ConfigType.DOWNLOAD) {
      setField(DownloadConfig.class, mDownloadConfig, key, value);
    } else if (type == ConfigType.UPLOAD) {
      setField(UploadConfig.class, mUploadConfig, key, value);
    } else if (type == ConfigType.APP) {
      setField(AppConfig.class, mAppConfig, key, value);
    } else if (type == ConfigType.D_GROUP) {
      setField(DGroupConfig.class, mDGroupConfig, key, value);
    }
  }

  private void setField(Class clazz, Object target, String key, Object value) {
    Field field = CommonUtil.getField(clazz, key);
    try {
      field.set(target, value);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * 检查是否int值是否合法
   *
   * @return {@code true} 合法
   */
  private boolean checkInt(String value) {
    if (TextUtils.isEmpty(value)) {
      return false;
    }
    try {
      int l = Integer.parseInt(value);
      return l >= 0;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 检查是否long值是否合法
   *
   * @return {@code true} 合法
   */
  private boolean checkLong(String value) {
    if (TextUtils.isEmpty(value)) {
      return false;
    }
    try {
      Long l = Long.parseLong(value);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 检查boolean值是否合法
   *
   * @return {@code true} 合法
   */
  private boolean checkBoolean(String value) {
    return !TextUtils.isEmpty(value) && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase(
        "false"));
  }

  @Override public void characters(char[] ch, int start, int length) throws SAXException {
    super.characters(ch, start, length);
  }

  @Override public void endElement(String uri, String localName, String qName) throws SAXException {
    super.endElement(uri, localName, qName);
  }

  @Override public void endDocument() throws SAXException {
    super.endDocument();
    mDownloadConfig.save();
    mUploadConfig.save();
    mAppConfig.save();
    mDGroupConfig.save();
  }
}
