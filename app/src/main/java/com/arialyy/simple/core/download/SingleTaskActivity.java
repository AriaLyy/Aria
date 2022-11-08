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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskListener;
import com.arialyy.aria.core.download.target.HttpNormalTarget;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;
import com.arialyy.aria.core.scheduler.NormalTaskListenerInterface;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.FileUtil;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySingleBinding;
import com.arialyy.simple.widget.ProgressLayout;
import java.io.File;
import java.util.List;
import java.util.Map;

public class SingleTaskActivity extends BaseActivity<ActivitySingleBinding> {

  private String mUrl;
  private String mFilePath;
  private HttpDownloadModule mModule;
  private long mTaskId = -1;

  BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(ISchedulers.ARIA_TASK_INFO_ACTION)) {
        ALog.d(TAG, "state = " + intent.getIntExtra(ISchedulers.TASK_STATE, -1));
        ALog.d(TAG, "type = " + intent.getIntExtra(ISchedulers.TASK_TYPE, -1));
        ALog.d(TAG, "speed = " + intent.getLongExtra(ISchedulers.TASK_SPEED, -1));
        ALog.d(TAG, "percent = " + intent.getIntExtra(ISchedulers.TASK_PERCENT, -1));
        ALog.d(TAG, "entity = " + intent.getParcelableExtra(ISchedulers.TASK_ENTITY).toString());
      }
    }
  };

  @Override
  protected void onResume() {
    super.onResume();
    //registerReceiver(receiver, new IntentFilter(ISchedulers.ARIA_TASK_INFO_ACTION));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //unregisterReceiver(receiver);
    Aria.download(this).unRegister();
  }

  @Override
  protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle("单任务下载");
    Aria.download(this).register();
    mModule = ViewModelProviders.of(this).get(HttpDownloadModule.class);
    mModule.getHttpDownloadInfo(this).observe(this, new Observer<DownloadEntity>() {

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
        startD();
      }

      @Override public void stop(View v, AbsEntity entity) {
        Aria.download(this)
            .load(mTaskId)
            .stop();
      }

      @Override public void resume(View v, AbsEntity entity) {
        Aria.download(this).load(mTaskId)
            //.updateUrl(mUrl)
            .resume();
      }

      @Override public void cancel(View v, AbsEntity entity) {
        Aria.download(this).load(mTaskId).cancel(false);
      //  //1. 删除本地文件目录
      //  File localDirFile = new File(mFilePath);
      //  if (localDirFile.exists()) {
      //    FileUtil.deleteDir(localDirFile.getParentFile());
      //  }
      //
      //  //2. 删除记录
      //  HttpNormalTarget target = Aria.download(SingleTaskActivity.this).load(mTaskId);
      //  if (target != null) {
      //    //            target.cancel();
      //    target.removeRecord();
      //  }
      //
      //  List<DownloadEntity> notCompleteTask = Aria.download(SingleTaskActivity.this).getAllNotCompleteTask();
      //  if (notCompleteTask == null){
      //    Log.d(TAG, "未完成的任务数：0");
      //    return;
      //  }
      //  Log.d(TAG, "未完成的任务数：" + notCompleteTask.size());
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_single_task_activity, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    int speed = -1;
    String msg = "";
    switch (item.getItemId()) {
      case R.id.help:
        msg = "一些小知识点：\n"
            + "1、你可以在注解中增加链接，用于指定被注解的方法只能被特定的下载任务回调，以防止progress乱跳\n"
            + "2、当遇到网络慢的情况时，你可以先使用onPre()更新UI界面，待连接成功时，再在onTaskPre()获取完整的task数据，然后给UI界面设置正确的数据\n"
            + "3、你可以在界面初始化时通过Aria.download(this).load(URL).getPercent()等方法快速获取相关任务的一些数据";
        showMsgDialog("tip", msg);
        break;
      case R.id.speed_0:
        speed = 0;
        break;
      case R.id.speed_128:
        speed = 128;
        break;
      case R.id.speed_256:
        speed = 256;
        break;
      case R.id.speed_512:
        speed = 512;
        break;
      case R.id.speed_1m:
        speed = 1024;
        break;
    }
    if (speed > -1) {
      msg = item.getTitle().toString();
      T.showShort(this, msg);
      Aria.get(this).getDownloadConfig().setMaxSpeed(speed);
    }
    return true;
  }

  @Download.onWait
  public void onWait(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      Log.d(TAG, "wait ==> " + task.getDownloadEntity().getFileName());
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onPre
  public void onPre(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  //@Override public void onTaskPre(DownloadTask task) {
  //
  //}

  @Download.onTaskStart
  public void onTaskStart(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
      ALog.d(TAG, "isComplete = " + task.isComplete() + ", state = " + task.getState());
    }
  }

  @Download.onTaskRunning
  public void onTaskRunning(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      //ALog.d(TAG, "isRunning" + "; state = " + task.getEntity().getState());
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  //@Override public void onNoSupportBreakPoint(DownloadTask task) {
  //
  //}

  @Download.onTaskResume
  public void onTaskResume(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG, "resume");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskStop
  public void onTaskStop(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG, "stop");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskCancel
  public void onTaskCancel(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      mTaskId = -1;
      Log.d(TAG, "cancel");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskFail
  public void onTaskFail(DownloadTask task, Exception e) {
    ALog.d(TAG, "下载失败");
    Toast.makeText(SingleTaskActivity.this, getString(R.string.download_fail), Toast.LENGTH_SHORT)
        .show();
    if (task != null && task.getKey().equals(mUrl)) {
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskComplete
  public void onTaskComplete(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      Toast.makeText(SingleTaskActivity.this, getString(R.string.download_success),
          Toast.LENGTH_SHORT).show();
      ALog.d(TAG, "文件md5: e088677570afe2e9f847cc8159b932dd");
      ALog.d(TAG, "下载完成的文件md5: " + CommonUtil.getFileMD5(new File(task.getFilePath())));
      getBinding().pl.setInfo(task.getEntity());
      getBinding().pl.setProgress(100);
    }
  }

  @Override
  protected int setLayoutId() {
    return R.layout.activity_single;
  }

  private void startD() {
    HttpOption option = new HttpOption();
    option.useServerFileName(true);
    option.addHeader("1", "@")
        .useServerFileName(true)
        .setFileLenAdapter(new FileLenAdapter());
    //option.setRequestType(RequestEnum.POST);
    mTaskId = Aria.download(SingleTaskActivity.this)
        .load(mUrl)
        .setFilePath(mFilePath, true)
        .option(option)
        .ignoreCheckPermissions()
        .create();
  }

  @Override
  protected void onStop() {
    super.onStop();
    //Aria.download(this).unRegister();
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    return super.dispatchTouchEvent(ev);
  }

  static class FileLenAdapter implements IHttpFileLenAdapter {
    @Override public long handleFileLen(Map<String, List<String>> headers) {

      List<String> sLength = headers.get("Content-Length");
      if (sLength == null || sLength.isEmpty()) {
        return -1;
      }
      String temp = sLength.get(0);

      return Long.parseLong(temp);
    }
  }
}