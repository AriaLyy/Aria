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
package com.arialyy.simple.core.upload;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.arialyy.annotations.Upload;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.FtpOption;
import com.arialyy.aria.core.processor.FtpInterceptHandler;
import com.arialyy.aria.core.processor.IFtpUploadInterceptor;
import com.arialyy.aria.core.task.UploadTask;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySingleBinding;
import com.arialyy.simple.widget.ProgressLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2017/7/28. Ftp 文件上传demo
 */
public class FtpUploadActivity extends BaseActivity<ActivitySingleBinding> {
  private final int OPEN_FILE_MANAGER_CODE = 0xB1;
  private String mFilePath;
  private String mUrl;
  private UploadModule mModule;
  private long mTaskId = -1;
  private String user = "lao", pwd = "123456";

  @Override protected void init(Bundle savedInstanceState) {
    setTile("D_FTP 文件上传");
    super.init(savedInstanceState);
    Aria.upload(this).register();

    setUI();
  }

  private void setUI() {
    mModule = ViewModelProviders.of(this).get(UploadModule.class);
    mModule.getFtpInfo(this).observe(this, new Observer<UploadEntity>() {
      @Override public void onChanged(@Nullable UploadEntity entity) {
        if (entity != null) {
          mFilePath = entity.getFilePath();
          mUrl = entity.getUrl();
          mTaskId = entity.getId();
          getBinding().pl.setInfo(entity);
        }
      }
    });
    getBinding().pl.setBtListener(new ProgressLayout.OnProgressLayoutBtListener() {
      @Override public void create(View v, AbsEntity entity) {
        mTaskId = Aria.upload(this)
            .loadFtp(mFilePath)
            .setUploadUrl(mUrl)
            .option(getOption())
            .ignoreFilePathOccupy()
            .create();
      }

      @Override public void stop(View v, AbsEntity entity) {
        Aria.upload(this).loadFtp(mTaskId).stop();
      }

      @Override public void resume(View v, AbsEntity entity) {
        Aria.upload(this)
            .loadFtp(mTaskId)
            .option(getOption())
            .resume();
      }

      @Override public void cancel(View v, AbsEntity entity) {
        Aria.upload(this).loadFtp(mTaskId).cancel(true);
        mTaskId = -1;
      }
    });
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_single;
  }

  private void upload() {
    List<String> paths = new ArrayList<>();
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/1.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/2.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/3.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/4.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/5.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/6.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/7.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/8.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/9.jpg");
    paths.add(Environment.getExternalStorageDirectory().getPath() + "/Download/img/img/10.jpg");
    for (String path : paths) {
      Aria.upload(this)
          .loadFtp(path)
          .setUploadUrl(mUrl)
          .option(getOption())
          .forceUpload()
          .create();
    }
  }

  private FtpOption getOption() {
    FtpOption option = new FtpOption();
    option.login(user, pwd);
    option.setUploadInterceptor(new FtpUploadInterceptor());
    return option;
  }

  @Upload.onWait
  void onWait(UploadTask task) {
    if (task.getEntity().getUrl().equals(mUrl)) {
      Log.d(TAG, "wait ==> " + task.getEntity().getFileName());
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Upload.onPre
  protected void onPre(UploadTask task) {
    if (task.getEntity().getUrl().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Upload.onTaskStart
  void taskStart(UploadTask task) {
    if (task.getEntity().getUrl().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
      ALog.d(TAG, "isComplete = " + task.isComplete() + ", state = " + task.getState());
    }
  }

  @Upload.onTaskRunning
  protected void running(UploadTask task) {
    if (task.getEntity().getUrl().equals(mUrl)) {
      ALog.d(TAG, "isRunning" + "; state = " + task.getEntity().getState());
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Upload.onTaskResume
  void taskResume(UploadTask task) {
    if (task.getEntity().getUrl().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
      ALog.d(TAG, "resume");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Upload.onTaskStop
  void taskStop(UploadTask task) {
    if (task.getEntity().getUrl().equals(mUrl)) {
      ALog.d(TAG, "stop");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Upload.onTaskCancel
  void taskCancel(UploadTask task) {
    if (task.getEntity().getUrl().equals(mUrl)) {
      mTaskId = -1;
      Log.d(TAG, "cancel");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Upload.onTaskFail
  void taskFail(UploadTask task, Exception e) {
    ALog.d(TAG, "fail");
    Toast.makeText(this, getString(R.string.upload_fail), Toast.LENGTH_SHORT)
        .show();
    if (task != null && task.getEntity().getUrl().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Upload.onTaskComplete
  void taskComplete(UploadTask task) {
    if (task.getEntity().getUrl().equals(mUrl)) {
      Toast.makeText(this, getString(R.string.upload_success),
          Toast.LENGTH_SHORT).show();
      ALog.d(TAG, "md5: " + CommonUtil.getFileMD5(new File(task.getEntity().getFilePath())));
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  private static class FtpUploadInterceptor implements IFtpUploadInterceptor {
    @Override public FtpInterceptHandler onIntercept(UploadEntity entity, List<String> fileList) {
      FtpInterceptHandler.Builder builder = new FtpInterceptHandler.Builder();
      //builder.coverServerFile(); // 覆盖远端同名文件
      builder.resetFileName("test12.zip"); //修改上传到远端服务器的文件名
      return builder.build();
    }
  }
}
