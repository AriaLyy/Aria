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
import android.util.Log;
import android.view.View;
import com.arialyy.annotations.DownloadGroup;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityDownloadGroupBinding;
import com.arialyy.simple.widget.ProgressLayout;
import com.arialyy.simple.widget.SubStateLinearLayout;
import java.util.List;
import java.util.Map;

/**
 * Created by lyy on 2017/7/6.
 */
public class DownloadGroupActivity extends BaseActivity<ActivityDownloadGroupBinding> {

  private SubStateLinearLayout mChildList;
  private List<String> mUrls;
  private long mTaskId = -1;

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
    setTitle("任务组");

    mChildList = getBinding().childList;
    mUrls = getModule(GroupModule.class).getUrls();
    DownloadGroupEntity entity = Aria.download(this).getGroupEntity(mUrls);
    if (entity != null) {
      mTaskId = entity.getId();
      mChildList.addData(entity.getSubEntities());
      ALog.d(TAG,
          "size = " + entity.getSubEntities().size() + "; len = " + entity.getConvertFileSize());
    }else {
      entity = new DownloadGroupEntity();
    }
    getBinding().pl.setInfo(entity);

    mChildList.setOnItemClickListener(new SubStateLinearLayout.OnItemClickListener() {
      @Override public void onItemClick(int position, View view) {
        showPopupWindow(position);
      }
    });
    getBinding().pl.setBtListener(new ProgressLayout.OnProgressLayoutBtListener() {
      @Override public void create(View v, AbsEntity entity) {
        mTaskId = Aria.download(this)
            .loadGroup(mUrls)
            .setDirPath(
                Environment.getExternalStorageDirectory().getPath() + "/Download/group_imgs1")
            //.setSubFileName(getModule(GroupModule.class).getSubName2())
            .setSubFileName(getModule(GroupModule.class).getSubName())
            .unknownSize()
            .option(getHttpOption())
            .ignoreFilePathOccupy()
            //.setFileSize(114981416)
            //.updateUrls(temp)
            .create();
        Log.d(TAG, "taskId = " + mTaskId);
      }

      @Override public void stop(View v, AbsEntity entity) {
        Aria.download(this).loadGroup(mTaskId).stop();
      }

      @Override public void resume(View v, AbsEntity entity) {
        Aria.download(this).loadGroup(mTaskId)
            .unknownSize()
            .resume(true);
      }

      @Override public void cancel(View v, AbsEntity entity) {
        Aria.download(this).loadGroup(mTaskId).cancel(true);
        mTaskId = -1;
      }
    });
  }

  private void showPopupWindow(int position) {
    ChildHandleDialog dialog =
        new ChildHandleDialog(this, mUrls, mChildList.getSubData().get(position));
    dialog.show(getSupportFragmentManager(), "sub_dialog");
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_download_group;
  }

  private HttpOption getHttpOption() {
    HttpOption option = new HttpOption();
    option.setFileLenAdapter(new HttpFileLenAdapter());
    return option;
  }

  @DownloadGroup.onWait void taskWait(DownloadGroupTask task) {
    ALog.d(TAG, task.getTaskName() + "wait");
  }

  @DownloadGroup.onPre() protected void onPre(DownloadGroupTask task) {
    ALog.d(TAG, "group pre, p = " + task.getPercent());
  }

  @DownloadGroup.onTaskPre() protected void onTaskPre(DownloadGroupTask task) {
    ALog.d(TAG, "group task pre, p = " + task.getPercent());
    if (mChildList.getSubData().size() <= 0) {
      mChildList.addData(task.getEntity().getSubEntities());
    }
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskStart() void taskStart(DownloadGroupTask task) {
    ALog.d(TAG, "group task create, p = " + task.getPercent());
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskRunning() protected void running(DownloadGroupTask task) {
    ALog.d(TAG, "group running, p = "
        + task.getPercent()
        + ", speed = "
        + task.getConvertSpeed()
        + ", current_p = "
        + task.getCurrentProgress());
    getBinding().pl.setInfo(task.getEntity());
    //Log.d(TAG, "sub_len = " + task.getEntity().getSubEntities().size());
    mChildList.updateChildProgress(task.getEntity().getSubEntities());
  }

  @DownloadGroup.onTaskResume() void taskResume(DownloadGroupTask task) {
    ALog.d(TAG, "group task resume, p = " + task.getPercent());
  }

  @DownloadGroup.onTaskStop() void taskStop(DownloadGroupTask task) {
    ALog.d(TAG, "group task stop");
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskCancel() void taskCancel(DownloadGroupTask task) {
    ALog.d(TAG, "group task cancel");
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onTaskFail() void taskFail(DownloadGroupTask task) {
    if (task != null){

      ALog.d(TAG, "group task fail");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @DownloadGroup.onTaskComplete() void taskComplete(DownloadGroupTask task) {
    mChildList.updateChildProgress(task.getEntity().getSubEntities());
    T.showShort(this, "任务组下载完成");
    ALog.d(TAG, "任务组下载完成");
    getBinding().pl.setInfo(task.getEntity());
  }

  @DownloadGroup.onSubTaskRunning void onSubTaskRunning(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
    //Log.e(TAG, "gHash = "
    //    + groupTask.getEntity().getSubEntities().get(0).hashCode()
    //    + "; subHash = "
    //    + groupTask.getNormalTaskWrapper().getSubTaskEntities().get(0).getEntity().hashCode() +
    //    "; subHash = " + subEntity.hashCode());
    //int percent = subEntity.getPercent();
    ////如果你打开了速度单位转换配置，将可以通过以下方法获取带单位的下载速度，如：1 mb/s
    //String convertSpeed = subEntity.getConvertSpeed();
    ////当前下载完成的进度，长度bytes
    //long completedSize = subEntity.getCurrentProgress();
    //Log.d(TAG, "subTask名字："
    //    + subEntity.getFileName()
    //    + ", "
    //    + " speed:"
    //    + convertSpeed
    //    + ",percent: "
    //    + percent
    //    + "%,  completedSize:"
    //    + completedSize);
  }

  @DownloadGroup.onSubTaskPre void onSubTaskPre(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
  }

  @DownloadGroup.onSubTaskStop void onSubTaskStop(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
  }

  @DownloadGroup.onSubTaskStart void onSubTaskStart(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
  }

  //@DownloadGroup.onSubTaskCancel void onSubTaskCancel(DownloadGroupTask groupTask,
  //    DownloadEntity subEntity) {
  //  Log.d(TAG, "new Size: " + groupTask.getConvertFileSize());
  //  mSub.setText("子任务：" + mChildName + "，状态：取消下载");
  //}

  @DownloadGroup.onSubTaskComplete void onSubTaskComplete(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
    mChildList.updateChildState(subEntity);
  }

  @DownloadGroup.onSubTaskFail void onSubTaskFail(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
  }

  static class HttpFileLenAdapter implements IHttpFileLenAdapter {
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
