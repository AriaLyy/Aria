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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.FtpOption;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySingleBinding;
import com.arialyy.simple.widget.ProgressLayout;
import java.io.File;

/**
 * Created by lyy on 2017/7/25.
 * Ftp下载
 */
public class FtpDownloadActivity extends BaseActivity<ActivitySingleBinding> {
  private String mUrl, mFilePath;
  private FtpDownloadModule mModule;
  private long mTaskId;
  private String user = "lao", passw = "123456";

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle("FTP文件下载");
    Aria.download(this).register();
    mModule = ViewModelProviders.of(this).get(FtpDownloadModule.class);

    mModule.getFtpDownloadInfo(this).observe(this, new Observer<DownloadEntity>() {

      @Override public void onChanged(@Nullable DownloadEntity entity) {
        if (entity == null) {
          return;
        }
        mTaskId = entity.getId();
        mUrl = entity.getUrl();
        mFilePath = entity.getFilePath();
        getBinding().pl.setInfo(entity);
      }
    });
    getBinding().pl.setBtListener(new ProgressLayout.OnProgressLayoutBtListener() {
      @Override public void create(View v, AbsEntity entity) {
        mTaskId = Aria.download(this).loadFtp(mUrl)
            .setFilePath(mFilePath)
            .ignoreFilePathOccupy()
            .option(getFtpOption())
            .create();
      }

      @Override public void stop(View v, AbsEntity entity) {
        Aria.download(this).loadFtp(mTaskId).stop();
      }

      @Override public void resume(View v, AbsEntity entity) {
        Aria.download(this)
            .loadFtp(mTaskId)
            .option(getFtpOption())
            .resume();
      }

      @Override public void cancel(View v, AbsEntity entity) {
        Aria.download(this).loadFtp(mTaskId).cancel();
        mTaskId = -1;
      }
    });
  }

  private FtpOption getFtpOption() {
    FtpOption option = new FtpOption();
    option.login(user, passw);
    //option.setServerIdentifier(FtpOption.FTPServerIdentifier.SYST_NT);
    //option.setConnectionMode(FtpConnectionMode.DATA_CONNECTION_MODE_ACTIVITY);
    return option;
  }

  @Download.onWait
  void onWait(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      Log.d(TAG, "wait ==> " + task.getDownloadEntity().getFileName());
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onPre
  protected void onPre(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskStart
  void taskStart(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
      ALog.d(TAG, "isComplete = " + task.isComplete() + ", state = " + task.getState());
    }
  }

  @Download.onTaskRunning
  protected void running(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG, "isRunning" + "; state = " + task.getEntity().getState());
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskResume
  void taskResume(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG, "resume");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskStop
  void taskStop(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG, "stop");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskCancel
  void taskCancel(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      mTaskId = -1;
      Log.d(TAG, "cancel");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskFail
  void taskFail(DownloadTask task, Exception e) {
    ALog.d(TAG, "下载失败");
    Toast.makeText(this, getString(R.string.download_fail), Toast.LENGTH_SHORT)
        .show();
    if (task != null && task.getKey().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskComplete
  void taskComplete(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      Toast.makeText(this, getString(R.string.download_success),
          Toast.LENGTH_SHORT).show();
      ALog.d(TAG, "md5: " + CommonUtil.getFileMD5(new File(task.getFilePath())));
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_single;
  }
}
