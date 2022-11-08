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

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.core.AbsDialog;
import com.arialyy.simple.R;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

/**
 * Created by AriaL on 2017/1/2.
 */
public class DownloadDialog extends AbsDialog implements View.OnClickListener {
  private HorizontalProgressBarWithNumber mPb;
  private Button mStart;
  private Button mCancel;
  private TextView mSize;
  private TextView mSpeed;
  private long mTaskId = -1;

  private static final String DOWNLOAD_URL =
      "http://sdkdown.muzhiwan.com/openfile/2019/07/12/com.nswj.acing.mzw_5d283abee9a3c.apk";

  public DownloadDialog(Context context) {
    super(context);
    init();
  }

  @Override protected int setLayoutId() {
    return R.layout.dialog_download;
  }

  private void init() {
    Aria.download(this).register();
    mPb = findViewById(R.id.progressBar);
    mStart = findViewById(R.id.start);
    mCancel = findViewById(R.id.cancel);
    mSize = findViewById(R.id.size);
    mSpeed = findViewById(R.id.speed);
    DownloadEntity entity = Aria.download(this).getFirstDownloadEntity(DOWNLOAD_URL);
    if (entity != null) {
      mSize.setText(CommonUtil.formatFileSize(entity.getFileSize()));
      int p = (int) (entity.getCurrentProgress() * 100 / entity.getFileSize());
      mPb.setProgress(p);
      int state = entity.getState();
      mTaskId = entity.getId();
    }
    mStart.setOnClickListener(this);
    mCancel.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        if (mTaskId == -1) {
          mTaskId = Aria.download(this)
              .load(DOWNLOAD_URL)
              .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/女神危机.apk")
              .create();
          mStart.setText(getContext().getString(R.string.stop));
          break;
        }
        if (Aria.download(this).load(mTaskId).isRunning()) {
          Aria.download(this).load(mTaskId).stop();
          mStart.setText(getContext().getString(R.string.resume));
        } else {
          Aria.download(this).load(mTaskId).resume();
          mStart.setText(getContext().getString(R.string.stop));
        }
        break;

      case R.id.cancel:
        Aria.download(this).load(mTaskId).cancel();
        mTaskId = -1;
        mStart.setText(getContext().getString(R.string.start));
        break;
    }
  }

  @Download.onTaskPre public void onTaskPre(DownloadTask task) {
    mSize.setText(CommonUtil.formatFileSize(task.getFileSize()));
  }

  @Download.onTaskStop public void onTaskStop(DownloadTask task) {
    mSpeed.setText(task.getConvertSpeed());
  }

  @Download.onTaskCancel public void onTaskCancel(DownloadTask task) {
    mPb.setProgress(0);
    mSpeed.setText(task.getConvertSpeed());
  }

  @Download.onTaskRunning public void onTaskRunning(DownloadTask task) {
    if (task.getKey().equals(DOWNLOAD_URL)) {
      mPb.setProgress(task.getPercent());
      mSpeed.setText(task.getConvertSpeed());
    }
  }

  @Override protected void dataCallback(int result, Object obj) {

  }

}
