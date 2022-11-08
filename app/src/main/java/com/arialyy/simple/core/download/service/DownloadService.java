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
package com.arialyy.simple.core.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.frame.util.show.T;

/**
 * Created by lyy on 2017/4/5.
 * 在服务中使用 Aria进行下载
 */
public class DownloadService extends Service {
  private static final String DOWNLOAD_URL =
      "http://rs.0.gaoshouyou.com/d/df/db/03df9eab61dbc48a5939f671f05f1cdf.apk";
  private DownloadNotification mNotify;

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onCreate() {
    super.onCreate();
    mNotify = new DownloadNotification(getApplicationContext());
    Aria.download(this).register();
    Aria.download(this)
        .load(DOWNLOAD_URL)
        .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/service_task.apk")
        .create();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Aria.download(this).unRegister();
  }

  @Download.onNoSupportBreakPoint public void onNoSupportBreakPoint(DownloadTask task) {
    T.showShort(getApplicationContext(), "该下载链接不支持断点");
  }

  @Download.onTaskStart public void onTaskStart(DownloadTask task) {
    T.showShort(getApplicationContext(), task.getDownloadEntity().getFileName() + "，开始下载");
  }

  @Download.onTaskStop public void onTaskStop(DownloadTask task) {
    T.showShort(getApplicationContext(), task.getDownloadEntity().getFileName() + "，停止下载");
  }

  @Download.onTaskCancel public void onTaskCancel(DownloadTask task) {
    T.showShort(getApplicationContext(), task.getDownloadEntity().getFileName() + "，取消下载");
  }

  @Download.onTaskFail public void onTaskFail(DownloadTask task) {
    T.showShort(getApplicationContext(), task.getDownloadEntity().getFileName() + "，下载失败");
  }

  @Download.onTaskComplete public void onTaskComplete(DownloadTask task) {
    T.showShort(getApplicationContext(), task.getDownloadEntity().getFileName() + "，下载完成");
    mNotify.upload(100);
  }

  @Download.onTaskRunning public void onTaskRunning(DownloadTask task) {
    long len = task.getFileSize();
    int p = (int) (task.getCurrentProgress() * 100 / len);
    mNotify.upload(p);
  }
}
