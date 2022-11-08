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
package com.arialyy.aria.m3u8.live;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.m3u8.M3U8Listener;
import com.arialyy.aria.m3u8.M3U8TaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.arialyy.aria.m3u8.M3U8InfoTask.M3U8_INDEX_FORMAT;

final class LiveStateManager implements IThreadStateManager {
  private final String TAG = CommonUtil.getClassName(getClass());

  private M3U8Listener mListener;
  private long mProgress; //当前总进度
  private Looper mLooper;
  private DTaskWrapper mTaskWrapper;
  private M3U8TaskOption mM3U8Option;
  private FileOutputStream mIndexFos;
  private M3U8LiveLoader mLoader;

  /**
   * @param listener 任务事件
   */
  LiveStateManager(DTaskWrapper wrapper, IEventListener listener) {
    mTaskWrapper = wrapper;
    mListener = (M3U8Listener) listener;
    mM3U8Option = (M3U8TaskOption) mTaskWrapper.getM3u8Option();
  }

  private Handler.Callback mCallback = new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      int peerIndex = msg.getData().getInt(ISchedulers.DATA_M3U8_PEER_INDEX);
      switch (msg.what) {
        case STATE_STOP:
          if (mLoader.isBreak()) {
            ALog.d(TAG, "任务停止");
            quitLooper();
          }
          break;
        case STATE_CANCEL:
          if (mLoader.isBreak()) {
            ALog.d(TAG, "任务取消");
            quitLooper();
          }
          break;
        case STATE_COMPLETE:
          mLoader.notifyLock(true, peerIndex);
          if (mM3U8Option.isGenerateIndexFile() && !mLoader.isBreak()) {
            addExtInf(mLoader.getCurExtInfo().url, mLoader.getCurExtInfo().extInf);
          }
          mListener.onPeerComplete(mTaskWrapper.getKey(),
              msg.getData().getString(ISchedulers.DATA_M3U8_PEER_PATH), peerIndex);
          break;
        case STATE_RUNNING:
          Bundle b = msg.getData();
          if (b != null) {
            long len = b.getLong(IThreadStateManager.DATA_ADD_LEN, 0);
            mProgress += len;
          }
          break;
        case STATE_FAIL:
          mLoader.notifyLock(false, peerIndex);
          mListener.onPeerFail(mTaskWrapper.getKey(),
              msg.getData().getString(ISchedulers.DATA_M3U8_PEER_PATH), peerIndex);
          break;
      }
      return true;
    }
  };

  void setLoader(M3U8LiveLoader loader) {
    mLoader = loader;
  }

  /**
   * 退出looper循环
   */
  private void quitLooper() {
    ALog.d(TAG, "quitLooper");
    mLooper.quit();
    if (mIndexFos != null) {
      try {
        mIndexFos.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 给索引文件添加extInfo信息
   */
  private void addExtInf(String url, String extInf) {
    File indexFile =
        new File(String.format(M3U8_INDEX_FORMAT, mTaskWrapper.getEntity().getFilePath()));
    if (!indexFile.exists()) {
      ALog.e(TAG, String.format("索引文件【%s】不存在，添加peer的extInf失败", indexFile.getPath()));
      return;
    }
    try {
      if (mIndexFos == null) {
        mIndexFos = new FileOutputStream(indexFile, true);
      }
      mIndexFos.write(extInf.concat("\r\n").getBytes(Charset.forName("UTF-8")));
      mIndexFos.write(url.concat("\r\n").getBytes(Charset.forName("UTF-8")));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override public boolean isFail() {
    return false;
  }

  @Override public boolean isComplete() {
    return false;
  }

  @Override public long getCurrentProgress() {
    return mProgress;
  }

  @Override public void updateCurrentProgress(long currentProgress) {
    mProgress = currentProgress;
  }

  @Override public void setLooper(TaskRecord taskRecord, Looper looper) {
    mLooper = looper;
  }

  @Override public Handler.Callback getHandlerCallback() {
    return mCallback;
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }
}
