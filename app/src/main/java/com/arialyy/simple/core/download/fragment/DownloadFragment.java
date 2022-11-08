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

package com.arialyy.simple.core.download.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.frame.core.AbsFragment;
import com.arialyy.simple.R;
import com.arialyy.simple.databinding.FragmentDownloadBinding;
import com.arialyy.simple.widget.ProgressLayout;

/**
 * Created by lyy on 2017/1/4.
 */
public class DownloadFragment extends AbsFragment<FragmentDownloadBinding> {
  private long mTaskId = -1;

  private static final String DOWNLOAD_URL =
      "https://res5.d.cn/2137e42d610b3488d9420c6421529386eee5bdbfd9be1fafe0a05d6dabaec8c156ddbd00581055bbaeac03904fb63310e80010680235d16bd4c040b50096a0c20dd1c4b0854529a1.apk";
  private static final String FILE_NAME = "王者军团";

  @Override protected void init(Bundle savedInstanceState) {
    Aria.download(this).register();
    DownloadEntity entity = Aria.download(this).getFirstDownloadEntity(DOWNLOAD_URL);
    if (entity == null){
      entity = new DownloadEntity();
      entity.setUrl(DOWNLOAD_URL);
    }
    getBinding().pl.setInfo(entity);
    getBinding().pl.setBtListener(new ProgressLayout.OnProgressLayoutBtListener() {
      @Override public void create(View v, AbsEntity entity) {
        mTaskId = Aria.download(this)
            .load(DOWNLOAD_URL)
            .setFilePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .getPath() + String.format("/%s.apk",
                    FILE_NAME))
            .create();
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
        mTaskId = -1;
      }
    });
  }

  @Download.onTaskPre public void onTaskPre(DownloadTask task) {
    getBinding().pl.setInfo(task.getEntity());
  }

  @Download.onTaskStop public void onTaskStop(DownloadTask task) {
    getBinding().pl.setInfo(task.getEntity());
  }

  @Download.onTaskCancel public void onTaskCancel(DownloadTask task) {
    getBinding().pl.setInfo(task.getEntity());
  }

  @Download.onTaskRunning public void onTaskRunning(DownloadTask task) {
    long len = task.getFileSize();
    getBinding().pl.setInfo(task.getEntity());
  }

  @Override protected void onDelayLoad() {

  }

  @Override protected int setLayoutId() {
    return R.layout.fragment_download;
  }

  @Override protected void dataCallback(int result, Object obj) {

  }
}
