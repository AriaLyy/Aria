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
package com.arialyy.aria.m3u8;

import android.os.Bundle;
import android.os.Message;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.listener.BaseListener;
import com.arialyy.aria.core.listener.IDLoadListener;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.DeleteM3u8Record;

/**
 * 下载监听类
 */
public final class M3U8Listener extends BaseListener implements IDLoadListener {

  @Override
  public void onPostPre(long fileSize) {
    mEntity.setFileSize(fileSize);
    mEntity.setConvertFileSize(CommonUtil.formatFileSize(fileSize));
    saveData(IEntity.STATE_POST_PRE, -1);
    sendInState2Target(ISchedulers.POST_PRE);
  }

  @Override public void supportBreakpoint(boolean support) {
    if (!support) {
      sendInState2Target(ISchedulers.NO_SUPPORT_BREAK_POINT);
    }
  }

  /**
   * 切片开始下载
   */
  public void onPeerStart(String m3u8Url, String peerPath, int peerIndex) {
    sendPeerStateToTarget(ISchedulers.M3U8_PEER_START, m3u8Url, peerPath, peerIndex);
  }

  /**
   * 切片下载完成
   */
  public void onPeerComplete(String m3u8Url, String peerPath, int peerIndex) {
    sendPeerStateToTarget(ISchedulers.M3U8_PEER_COMPLETE, m3u8Url, peerPath, peerIndex);
  }

  /**
   * 切片下载失败
   */
  public void onPeerFail(String m3u8Url, String peerPath, int peerIndex) {
    sendPeerStateToTarget(ISchedulers.M3U8_PEER_FAIL, m3u8Url, peerPath, peerIndex);
  }

  private void sendPeerStateToTarget(int state, String m3u8Url, String peerPath, int peerIndex) {
    Bundle bundle = new Bundle();
    bundle.putString(ISchedulers.DATA_M3U8_URL, m3u8Url);
    bundle.putString(ISchedulers.DATA_M3U8_PEER_PATH, peerPath);
    bundle.putInt(ISchedulers.DATA_M3U8_PEER_INDEX, peerIndex);
    Message msg = outHandler.get().obtainMessage();
    msg.setData(bundle);
    msg.what = state;
    msg.arg1 = ISchedulers.IS_M3U8_PEER;
    msg.sendToTarget();
  }

  @Override protected void handleCancel() {
    int sType = getTask(DownloadTask.class).getSchedulerType();
    if (sType == TaskSchedulerType.TYPE_CANCEL_AND_NOT_NOTIFY) {
      mEntity.setComplete(false);
      mEntity.setState(IEntity.STATE_WAIT);
      DeleteM3u8Record.getInstance().deleteRecord(mEntity, mTaskWrapper.isRemoveFile(), false);
    } else {
      DeleteM3u8Record.getInstance().deleteRecord(mEntity, mTaskWrapper.isRemoveFile(), true);
    }
  }
}