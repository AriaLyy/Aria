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
package com.arialyy.aria.core.common;

import com.arialyy.annotations.TaskEnum;
import com.arialyy.aria.core.download.DownloadGroupTaskListener;
import com.arialyy.aria.core.download.DownloadTaskListener;
import com.arialyy.aria.core.scheduler.M3U8PeerTaskListener;
import com.arialyy.aria.core.scheduler.SubTaskListener;
import com.arialyy.aria.core.scheduler.TaskInternalListenerInterface;
import com.arialyy.aria.core.upload.UploadTaskListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aria.Lao on 2017/7/10.
 * 代理参数获取
 */
public class ProxyHelper {
  /**
   * 普通下载任务类型
   */
  public static int PROXY_TYPE_DOWNLOAD = 0x01;
  /**
   * 组合下载任务类型
   */
  public static int PROXY_TYPE_DOWNLOAD_GROUP = 0x02;
  /**
   * 普通上传任务类型
   */
  public static int PROXY_TYPE_UPLOAD = 0x03;
  /**
   * m3u8 peer
   */
  public static int PROXY_TYPE_M3U8_PEER = 0x04;
  /**
   * 组合任务子任务类型
   */
  public static int PROXY_TYPE_DOWNLOAD_GROUP_SUB = 0x05;
  public Map<String, Set<Integer>> mProxyCache = new ConcurrentHashMap<>();

  public static volatile ProxyHelper INSTANCE = null;

  private ProxyHelper() {
  }

  public static ProxyHelper getInstance() {
    if (INSTANCE == null) {
      synchronized (ProxyHelper.class) {
        INSTANCE = new ProxyHelper();
      }
    }
    return INSTANCE;
  }

  /**
   * 检查观察者对象的代理文件类型
   *
   * @param clazz 观察者对象
   * @return {@link #PROXY_TYPE_DOWNLOAD}，如果没有实体对象则返回空的list
   */
  public Set<Integer> checkProxyType(Class clazz) {
    Set<Integer> result = mProxyCache.get(clazz.getName());
    if (result != null) {
      return result;
    }
    result = checkProxyTypeByInterface(clazz);
    if (result != null && !result.isEmpty()) {
      mProxyCache.put(clazz.getName(), result);
      return result;
    }
    result = checkProxyTypeByProxyClass(clazz);

    if (!result.isEmpty()) {
      mProxyCache.put(clazz.getName(), result);
    }
    return result;
  }

  private Set<Integer> checkProxyTypeByProxyClass(Class clazz) {
    final String className = clazz.getName();
    Set<Integer> result = new HashSet<>();
    if (checkProxyExist(className, TaskEnum.DOWNLOAD_GROUP.proxySuffix)) {
      result.add(PROXY_TYPE_DOWNLOAD_GROUP);
    }
    if (checkProxyExist(className, TaskEnum.DOWNLOAD.proxySuffix)) {
      result.add(PROXY_TYPE_DOWNLOAD);
    }

    if (checkProxyExist(className, TaskEnum.UPLOAD.proxySuffix)) {
      result.add(PROXY_TYPE_UPLOAD);
    }

    if (checkProxyExist(className, TaskEnum.M3U8_PEER.proxySuffix)) {
      result.add(PROXY_TYPE_M3U8_PEER);
    }

    if (checkProxyExist(className, TaskEnum.DOWNLOAD_GROUP_SUB.proxySuffix)) {
      result.add(PROXY_TYPE_DOWNLOAD_GROUP_SUB);
    }
    return result;
  }

  private Set<Integer> checkProxyTypeByInterface(Class clazz) {
    if (!TaskInternalListenerInterface.class.isAssignableFrom(clazz)) {
      return null;
    }
    Set<Integer> result = new HashSet<>();
    if (DownloadGroupTaskListener.class.isAssignableFrom(clazz)) {
      result.add(PROXY_TYPE_DOWNLOAD_GROUP);
    }
    if (DownloadTaskListener.class.isAssignableFrom(clazz)) {
      result.add(PROXY_TYPE_DOWNLOAD);
    }

    if (UploadTaskListener.class.isAssignableFrom(clazz)) {
      result.add(PROXY_TYPE_UPLOAD);
    }

    if (M3U8PeerTaskListener.class.isAssignableFrom(clazz)) {
      result.add(PROXY_TYPE_M3U8_PEER);
    }

    if (SubTaskListener.class.isAssignableFrom(clazz)) {
      result.add(PROXY_TYPE_DOWNLOAD_GROUP_SUB);
    }
    return result;
  }

  private boolean checkProxyExist(String className, String proxySuffix) {
    String clsName = className.concat(proxySuffix);

    try {
      if (getClass().getClassLoader().loadClass(clsName) != null) {
        return true;
      }
      if (Class.forName(clsName) != null) {
        return true;
      }
    } catch (ClassNotFoundException e) {
      //e.printStackTrace();
    }
    return false;
  }
}
