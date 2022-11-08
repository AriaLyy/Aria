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
package com.arialyy.aria.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.arialyy.aria.core.AriaConfig;

/**
 * 跟网络相关的工具类
 */
public class NetUtils {

  /**
   * 没有网络
   */
  public static final int NETWORK_TYPE_INVALID = 0;
  /**
   * wap网络
   */
  public static final int NETWORK_TYPE_WAP = 1;
  /**
   * 2G网络
   */
  public static final int NETWORK_TYPE_2G = 2;
  /**
   * 3G和3G以上网络，或统称为快速网络
   */
  public static final int NETWORK_TYPE_3G = 3;
  /**
   * wifi网络
   */
  public static final int NETWORK_TYPE_WIFI = 4;

  /**
   * 判断网络是否连接
   *
   * @return {@code true} 网络已连接、{@code false}网络未连接
   */
  public static boolean isConnected(Context context) {
    if (!AriaConfig.getInstance().getAConfig().isNetCheck()) {
      return true;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return AriaConfig.getInstance().isNetworkAvailable();
    }
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo ni = cm.getActiveNetworkInfo();
    return ni != null && ni.isConnectedOrConnecting();
  }

  /**
   * 判断是否是wifi连接
   */
  public static boolean isWifi(Context context) {
    return getNetWorkType(context) == NETWORK_TYPE_WIFI;
  }

  /**
   * 获取网络状态，wifi,wap,2g,3g.
   *
   * @param context 上下文
   * @return int 网络状态 {@link #NETWORK_TYPE_2G},{@link #NETWORK_TYPE_3G},
   * {@link #NETWORK_TYPE_INVALID},{@link #NETWORK_TYPE_WAP},{@link #NETWORK_TYPE_WIFI}
   */
  public static int getNetWorkType(Context context) {
    int netWorkType = -1;
    ConnectivityManager manager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      String type = networkInfo.getTypeName();
      if (type.equalsIgnoreCase("WIFI")) {
        netWorkType = NETWORK_TYPE_WIFI;
      } else if (type.equalsIgnoreCase("MOBILE")) {
        String proxyHost = android.net.Proxy.getDefaultHost();
        netWorkType = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? NETWORK_TYPE_3G
            : NETWORK_TYPE_2G) : NETWORK_TYPE_WAP;
      }
    } else {
      netWorkType = NETWORK_TYPE_INVALID;
    }
    return netWorkType;
  }

  private static boolean isFastMobileNetwork(Context context) {
    TelephonyManager telephonyManager =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    switch (telephonyManager.getNetworkType()) {
      case TelephonyManager.NETWORK_TYPE_1xRTT:
        return false; // ~ 50-100 kbps
      case TelephonyManager.NETWORK_TYPE_CDMA:
        return false; // ~ 14-64 kbps
      case TelephonyManager.NETWORK_TYPE_EDGE:
        return false; // ~ 50-100 kbps
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
        return true; // ~ 400-1000 kbps
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
        return true; // ~ 600-1400 kbps
      case TelephonyManager.NETWORK_TYPE_GPRS:
        return false; // ~ 100 kbps
      case TelephonyManager.NETWORK_TYPE_HSDPA:
        return true; // ~ 2-14 Mbps
      case TelephonyManager.NETWORK_TYPE_HSPA:
        return true; // ~ 700-1700 kbps
      case TelephonyManager.NETWORK_TYPE_HSUPA:
        return true; // ~ 1-23 Mbps
      case TelephonyManager.NETWORK_TYPE_UMTS:
        return true; // ~ 400-7000 kbps
      case TelephonyManager.NETWORK_TYPE_EHRPD:
        return true; // ~ 1-2 Mbps
      case TelephonyManager.NETWORK_TYPE_EVDO_B:
        return true; // ~ 5 Mbps
      case TelephonyManager.NETWORK_TYPE_HSPAP:
        return true; // ~ 10-20 Mbps
      case TelephonyManager.NETWORK_TYPE_IDEN:
        return false; // ~25 kbps
      case TelephonyManager.NETWORK_TYPE_LTE:
        return true; // ~ 10+ Mbps
      case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        return false;
      default:
        return false;
    }
  }
}