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

package com.arialyy.simple.core.download.mutil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.arialyy.annotations.Download;
import com.arialyy.annotations.DownloadGroup;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.frame.util.FileUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.core.download.DownloadModule;
import com.arialyy.simple.databinding.ActivityMultiBinding;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyy on 2016/9/27.
 */
public class MultiTaskListActivity extends BaseActivity<ActivityMultiBinding> {
  RecyclerView mList;
  Toolbar mBar;
  private FileListAdapter mAdapter;
  List<FileListEntity> mData = new ArrayList<>();

  @Override protected int setLayoutId() {
    return R.layout.activity_multi;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
    setTitle("多任务下载");
    mData.addAll(getModule(DownloadModule.class).createGroupTestList());
    mData.addAll(getModule(DownloadModule.class).createMultiTestList());
    mData.addAll(getModule(DownloadModule.class).createM3u8TestList());
    mAdapter = new FileListAdapter(this, mData);
    mList = getBinding().list;
    mBar = findViewById(R.id.toolbar);
    mList.setLayoutManager(new LinearLayoutManager(this));
    mList.setAdapter(mAdapter);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.num:
        DownloadNumDialog dialog = new DownloadNumDialog(this);
        dialog.show(getSupportFragmentManager(), "download_num");
        break;
      case R.id.stop_all:
        //Aria.download(this).stopAllTask();
        List<AbsEntity> list = Aria.download(this).getTotalTaskList();
        for (AbsEntity entity : list){
          Aria.download(this).load(entity.getId()).cancel(true);
        }
        //Aria.download(this).removeAllTask(false);
        break;
      case R.id.turn:
        startActivity(new Intent(this, MultiDownloadActivity.class));
        break;
    }
  }

  @Download.onWait void taskWait(DownloadTask task) {
    Log.d(TAG, "wait ==> " + task.getDownloadEntity().getFileName());
  }

  @Download.onTaskStart void taskStart(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), false);
  }

  @Download.onTaskResume void taskResume(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), false);
  }

  @Download.onTaskStop void taskStop(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @Download.onTaskCancel void taskCancel(DownloadTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @Download.onTaskFail void taskFail(DownloadTask task) {
    if (task == null || task.getEntity() == null){
      return;
    }
    mAdapter.updateBtState(task.getKey(), true);
  }

  @Download.onTaskComplete void taskComplete(DownloadTask task) {
    Log.d(TAG, FileUtil.getFileMD5(new File(task.getFilePath())));
  }

  //############################### 任务组 ##############################
  @DownloadGroup.onTaskComplete void groupTaskComplete(DownloadGroupTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @DownloadGroup.onTaskStart void groupTaskStart(DownloadGroupTask task) {
    mAdapter.updateBtState(task.getKey(), false);
  }

  @DownloadGroup.onTaskResume void groupTaskResume(DownloadGroupTask task) {
    mAdapter.updateBtState(task.getKey(), false);
  }

  @DownloadGroup.onWait void groupTaskWait(DownloadGroupTask task) {
    ALog.d(TAG, String.format("group【%s】wait---", task.getTaskName()));
    mAdapter.updateBtState(task.getKey(), true);
  }

  @DownloadGroup.onTaskStop void groupTaskStop(DownloadGroupTask task) {
    mAdapter.updateBtState(task.getKey(), true);
    Log.d(TAG, String.format("group【%s】stop", task.getTaskName()));
  }

  @DownloadGroup.onTaskCancel void groupTaskCancel(DownloadGroupTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @DownloadGroup.onTaskFail void groupTaskFail(DownloadGroupTask task) {
    if (task == null) {
      return;
    }
    mAdapter.updateBtState(task.getKey(), true);
    Log.d(TAG, String.format("group【%s】fail", task.getTaskName()));
  }

  @DownloadGroup.onTaskComplete void taskComplete(DownloadGroupTask task) {
    mAdapter.updateBtState(task.getKey(), true);
  }

  @Override protected void dataCallback(int result, Object data) {
    super.dataCallback(result, data);
    if (result == DownloadNumDialog.RESULT_CODE) {
      Aria.get(this).getDownloadConfig().setMaxTaskNum(Integer.parseInt(data + ""));
    }
  }
}