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
package com.arialyy.simple.core.download.group;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import com.arialyy.annotations.DownloadGroup;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.FtpOption;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityDownloadGroupBinding;
import com.arialyy.simple.widget.ProgressLayout;
import com.arialyy.simple.widget.SubStateLinearLayout;

/**
 * Created by lyy on 2017/7/6.
 */
public class FTPDirDownloadActivity extends BaseActivity<ActivityDownloadGroupBinding> {
  private static final String dir = "ftp://192.168.0.104:2121/aab/你好";

  private SubStateLinearLayout mChildList;
  private long mTaskId = -1;
  private String user = "lao", pwd = "123456";

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
    setTitle("FTP文件夹下载");
    mChildList = findViewById(R.id.child_list);
    DownloadGroupEntity entity = Aria.download(this).getFtpDirEntity(dir);
    if (entity != null) {
      mTaskId = entity.getId();
      mChildList.addData(entity.getSubEntities());
    }else {
      entity = new DownloadGroupEntity();
    }
    getBinding().pl.setInfo(entity);
    getBinding().pl.setBtListener(new ProgressLayout.OnProgressLayoutBtListener() {
      @Override public void create(View v, AbsEntity entity) {
        mTaskId = Aria.download(this)
            .loadFtpDir(dir)
            .setDirPath(
                Environment.getExternalStorageDirectory().getPath() + "/Download/ftp_dir")
            .setGroupAlias("ftp文件夹下载")
            .option(getFtpOption())
            .ignoreFilePathOccupy()
            .create();
      }

      @Override public void stop(View v, AbsEntity entity) {
        Aria.download(this).loadFtpDir(mTaskId).stop();
      }

      @Override public void resume(View v, AbsEntity entity) {
        Aria.download(this)
            .loadFtpDir(mTaskId)
            .option(getFtpOption())
            .resume();
      }

      @Override public void cancel(View v, AbsEntity entity) {
        Aria.download(this).loadFtpDir(mTaskId).cancel();
        mTaskId = -1;
      }
    });
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_download_group;
  }

  private FtpOption getFtpOption() {
    FtpOption option = new FtpOption();
    option.login(user, pwd);
    return option;
  }

  @DownloadGroup.onPre protected void onPre(DownloadGroupTask task) {
    ALog.d(TAG, "ftp dir pre");
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskPre protected void onTaskPre(DownloadGroupTask task) {
    ALog.d(TAG, "ftp dir task pre");
    if (mChildList.getSubData().size() <= 0) {
      mChildList.addData(task.getEntity().getSubEntities());
    }
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskStart void taskStart(DownloadGroupTask task) {
    ALog.d(TAG, "ftp dir start");
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskRunning protected void running(DownloadGroupTask task) {
    ALog.d(TAG, "ftp dir running, p = " + task.getPercent());
    getBinding().pl.setInfo(task.getEntity());
    mChildList.updateChildProgress(task.getEntity().getSubEntities());
  }

  @DownloadGroup.onTaskResume void taskResume(DownloadGroupTask task) {
    ALog.d(TAG, "ftp dir resume");
  }

  @DownloadGroup.onTaskStop void taskStop(DownloadGroupTask task) {
    ALog.d(TAG, "ftp dir stop");
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskCancel void taskCancel(DownloadGroupTask task) {
    ALog.d(TAG, "ftp dir cancel");
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskFail void taskFail(DownloadGroupTask task) {
    ALog.d(TAG, "group task fail");
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskComplete void taskComplete(DownloadGroupTask task) {
    mChildList.updateChildProgress(task.getEntity().getSubEntities());
    getBinding().pl.setInfo(task.getEntity());
    T.showShort(this, "任务组下载完成");
    ALog.d(TAG, "任务组下载完成");
  }
}
