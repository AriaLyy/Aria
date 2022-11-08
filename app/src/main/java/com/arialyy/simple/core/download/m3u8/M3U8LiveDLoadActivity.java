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

package com.arialyy.simple.core.download.m3u8;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.arialyy.annotations.Download;
import com.arialyy.annotations.M3U8;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.m3u8.M3U8LiveOption;
import com.arialyy.aria.core.processor.IBandWidthUrlConverter;
import com.arialyy.aria.core.processor.ILiveTsUrlConverter;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.common.ModifyPathDialog;
import com.arialyy.simple.common.ModifyUrlDialog;
import com.arialyy.simple.databinding.ActivityM3u8LiveBinding;
import com.arialyy.simple.widget.ProgressLayout;
import java.io.File;

public class M3U8LiveDLoadActivity extends BaseActivity<ActivityM3u8LiveBinding> {

  private String mUrl;
  private String mFilePath;
  private M3U8LiveModule mModule;
  private long mTaskId;

  @Override
  protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle(getString(R.string.m3u8_live));
    Aria.download(this).register();
    mModule = ViewModelProviders.of(this).get(M3U8LiveModule.class);
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
        Aria.download(this).load(mTaskId).stop();
      }

      @Override public void resume(View v, AbsEntity entity) {
        ALog.d(TAG, "m3u8 live 不支持恢复");
      }

      @Override public void cancel(View v, AbsEntity entity) {
        Aria.download(this).load(mTaskId).cancel(true);
        mTaskId = -1;
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
      Aria.download(this).setMaxSpeed(speed);
      T.showShort(this, msg);
    }
    return true;
  }

  @M3U8.onPeerStart
  void onPeerStart(String m3u8Url, String peerPath, int peerIndex) {
    //ALog.d(TAG, "peer create, path: " + peerPath + ", index: " + peerIndex);
  }

  @M3U8.onPeerComplete
  void onPeerComplete(String m3u8Url, String peerPath, int peerIndex) {
    //ALog.d(TAG, "peer complete, path: " + peerPath + ", index: " + peerIndex);
    //mVideoFragment.addPlayer(peerIndex, peerPath);
  }

  @M3U8.onPeerFail
  void onPeerFail(String m3u8Url, String peerPath, int peerIndex) {
    //ALog.d(TAG, "peer fail, path: " + peerPath + ", index: " + peerIndex);
  }

  @Download.onWait
  void onWait(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      Log.d(TAG, "wait ==> " + task.getDownloadEntity().getFileName());
    }
  }

  @Download.onPre
  protected void onPre(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG, "pre");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskStart
  void taskStart(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG, "isComplete = " + task.isComplete() + ", state = " + task.getState());
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskRunning
  protected void running(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG,
          "m3u8 void running, p = " + task.getPercent() + ", speed  = " + task.getConvertSpeed());
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskResume
  void taskResume(DownloadTask task) {
    if (task.getKey().equals(mUrl)) {
      ALog.d(TAG, "m3u8 vod resume");
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
      Log.d(TAG, "cancel");
      getBinding().pl.setInfo(task.getEntity());
    }
  }

  @Download.onTaskFail
  void taskFail(DownloadTask task, Exception e) {
    if (task.getKey().equals(mUrl)) {
      Toast.makeText(this, getString(R.string.download_fail),
          Toast.LENGTH_SHORT)
          .show();
      Log.d(TAG, "fail");
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

  @Override
  protected int setLayoutId() {
    return R.layout.activity_m3u8_live;
  }

  private void startD() {
    mTaskId = Aria.download(M3U8LiveDLoadActivity.this)
        .load(mUrl)
        .setFilePath(mFilePath)
        .ignoreFilePathOccupy()
        .m3u8LiveOption(getLiveoption())
        .create();
  }

  private M3U8LiveOption getLiveoption() {
    M3U8LiveOption option = new M3U8LiveOption();
    option
        //.generateIndexFile()
        .setLiveTsUrlConvert(new LiveTsUrlConverter());
    //.setBandWidthUrlConverter(new BandWidthUrlConverter(mUrl));
    return option;
  }

  @Override protected void dataCallback(int result, Object data) {
    super.dataCallback(result, data);
    if (result == ModifyUrlDialog.MODIFY_URL_DIALOG_RESULT) {
      mModule.uploadUrl(this, String.valueOf(data));
    } else if (result == ModifyPathDialog.MODIFY_PATH_RESULT) {
      mModule.updateFilePath(this, String.valueOf(data));
    }
  }

  static class LiveTsUrlConverter implements ILiveTsUrlConverter {
    @Override public String convert(String m3u8Url, String tsUrl) {
      int index = m3u8Url.lastIndexOf("/");
      String parentUrl = m3u8Url.substring(0, index + 1);
      return parentUrl + tsUrl;
    }
  }

  static class BandWidthUrlConverter implements IBandWidthUrlConverter {

    @Override public String convert(String m3u8Url, String bandWidthUrl) {
      int peerIndex = m3u8Url.lastIndexOf("/");
      return m3u8Url.substring(0, peerIndex + 1) + bandWidthUrl;
    }
  }
}