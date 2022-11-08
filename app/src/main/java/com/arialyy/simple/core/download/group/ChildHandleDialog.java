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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.arialyy.annotations.DownloadGroup;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseDialog;
import com.arialyy.simple.databinding.DialogSubTaskHandlerBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import java.util.List;

/**
 * Created by lyy on 2017/9/5.
 */
@SuppressLint("ValidFragment") public class ChildHandleDialog
    extends BaseDialog<DialogSubTaskHandlerBinding> implements View.OnClickListener {
  TextView mSub;
  TextView mGroup;
  HorizontalProgressBarWithNumber mPb;
  private String mGroupHash;
  private String mChildName;
  private List<String> mUrls;
  private DownloadEntity mChildEntity;

  public ChildHandleDialog(Context context, List<String> urls, DownloadEntity childEntity) {
    super(context);
    setStyle(STYLE_NO_TITLE, R.style.Theme_Light_Dialog);
    mChildEntity = childEntity;
    mGroupHash = "任务组测试";
    mUrls = urls;
    mChildName = childEntity.getFileName();
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(getContext()).register();
    initWidget();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Aria.download(getContext()).unRegister();
  }

  private void initWidget() {
    mSub = findViewById(R.id.sub_task);
    mGroup = findViewById(R.id.task_group);
    mPb = findViewById(R.id.pb);

    findViewById(R.id.stop).setOnClickListener(this);
    findViewById(R.id.start).setOnClickListener(this);
    //findViewById(R.id.cancel).setOnClickListener(this);
    mGroup.setText("任务组：" + mGroupHash);
    mSub.setText("子任务：" + mChildName);
    mPb.setProgress((int) (mChildEntity.getCurrentProgress() * 100 / mChildEntity.getFileSize()));

    Window window = getDialog().getWindow();
    window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
    WindowManager.LayoutParams p = window.getAttributes();
    p.width = ViewGroup.LayoutParams.MATCH_PARENT;
    window.setAttributes(p);
    window.setWindowAnimations(R.style.dialogStyle);
  }

  @DownloadGroup.onSubTaskRunning void onSubTaskRunning(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
    if (!subEntity.getUrl().equals(mChildEntity.getUrl())) return;
    mPb.setProgress(subEntity.getPercent());
  }

  @DownloadGroup.onSubTaskPre void onSubTaskPre(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
    if (!subEntity.getUrl().equals(mChildEntity.getUrl())) return;
    L.d(TAG, subEntity.getPercent() + "");
  }

  @DownloadGroup.onSubTaskStop void onSubTaskStop(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
    if (!subEntity.getUrl().equals(mChildEntity.getUrl())) return;
    mSub.setText("子任务：" + mChildName + "，状态：任务停止");
  }

  @DownloadGroup.onSubTaskStart void onSubTaskStart(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
    if (!subEntity.getUrl().equals(mChildEntity.getUrl())) return;
    mSub.setText("子任务：" + mChildName + "，状态：下载中");
  }

  //@DownloadGroup.onSubTaskCancel void onSubTaskCancel(DownloadGroupTask groupTask,
  //    DownloadEntity subEntity) {
  //  Log.d(TAG, "new Size: " + groupTask.getConvertFileSize());
  //  mSub.setText("子任务：" + mChildName + "，状态：取消下载");
  //}

  @DownloadGroup.onSubTaskComplete void onSubTaskComplete(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
    if (!subEntity.getUrl().equals(mChildEntity.getUrl())) return;
    mSub.setText("子任务：" + mChildName + "，状态：任务完成");
    mPb.setProgress(100);
  }

  @DownloadGroup.onSubTaskFail void onSubTaskFail(DownloadGroupTask groupTask,
      DownloadEntity subEntity) {
    if (!subEntity.getUrl().equals(mChildEntity.getUrl())) return;
    L.d(TAG, subEntity.getPercent() + "");
  }

  @Override protected int setLayoutId() {
    return R.layout.dialog_sub_task_handler;
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.download(getContext())
            .loadGroup(mUrls)
            .getSubTaskManager()
            .startSubTask(mChildEntity.getUrl());
        break;
      case R.id.stop:
        Aria.download(getContext())
            .loadGroup(mUrls)
            .getSubTaskManager()
            .stopSubTask(mChildEntity.getUrl());
        break;
      //case R.id.cancel:
      //  Aria.download(this).load(mUrls).getSubTaskManager().cancelSubTask(mChildEntity.getUrl());
      //  break;
    }
  }

  @Override protected void dataCallback(int result, Object obj) {

  }
}
