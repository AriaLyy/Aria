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

package com.arialyy.simple.base;

import android.app.Application;
import android.os.StrictMode;
import com.arialyy.aria.core.Aria;
import com.arialyy.frame.core.AbsFrame;
import com.arialyy.simple.BuildConfig;
//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Lyy on 2016/9/27.
 */
public class BaseApplication extends Application {

  private static BaseApplication INSTANCE;

  @Override public void onCreate() {
    super.onCreate();
    INSTANCE = this;
    AbsFrame.init(this);
    Aria.init(this);
    if (BuildConfig.DEBUG) {
      //StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
      //    .detectAll()
      //    .penaltyLog()
      //    .build());
      //StrictMode.setThreadPolicy(
      //    new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
      //if (LeakCanary.isInAnalyzerProcess(this)) {//1
      //  //This process is dedicated to LeakCanary for heap analysis.
      //  //You should not init your app in this process.
      //  return;
      //}
      //LeakCanary.install(this);
    }

    //registerReceiver(new ConnectionChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  }

  public static BaseApplication getApp() {
    return INSTANCE;
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }
}