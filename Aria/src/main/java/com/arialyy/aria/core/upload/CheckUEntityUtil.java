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
package com.arialyy.aria.core.upload;

import android.text.TextUtils;
import com.arialyy.aria.core.inf.ICheckEntityUtil;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import java.io.File;

public class CheckUEntityUtil implements ICheckEntityUtil {
  private final String TAG = "CheckUEntityUtil";
  private UTaskWrapper mWrapper;
  private UploadEntity mEntity;
  private int action;

  public static CheckUEntityUtil newInstance(UTaskWrapper wrapper, int action) {
    return new CheckUEntityUtil(wrapper, action);
  }

  private CheckUEntityUtil(UTaskWrapper wrapper, int action) {
    this.action = action;
    mWrapper = wrapper;
    mEntity = mWrapper.getEntity();
  }

  @Override
  public boolean checkEntity() {
    if (mWrapper.getErrorEvent() != null) {
      ALog.e(TAG, String.format("任务操作失败，%s", mWrapper.getErrorEvent().errorMsg));
      return false;
    }

    boolean b = checkUrl() && checkFilePath();
    if (b) {
      mEntity.save();
    }
    return b;
  }

  private boolean checkFilePath() {
    String filePath = mEntity.getFilePath();
    if (TextUtils.isEmpty(filePath)) {
      ALog.e(TAG, "上传失败，文件路径为null");
      return false;
    } else if (!filePath.startsWith("/")) {
      ALog.e(TAG, "上传失败，文件路径【" + filePath + "】不合法");
      return false;
    }
    // 任务是新任务，并且路径冲突就不会继续执行
    if (mWrapper.isNewTask()
        && !CheckUtil.checkUPathConflicts(mWrapper.isIgnoreFilePathOccupy(), filePath,
        mWrapper.getRequestType())) {
      return false;
    }

    File file = new File(mEntity.getFilePath());
    if (!file.exists()) {
      ALog.e(TAG, "上传失败，文件【" + filePath + "】不存在");
      return false;
    }
    if (file.isDirectory()) {
      ALog.e(TAG, "上传失败，文件【" + filePath + "】不能是文件夹");
      return false;
    }

    return true;
  }

  private boolean checkUrl() {

    final String url = mWrapper.getTempUrl();
    if (TextUtils.isEmpty(url)) {
      ALog.e(TAG, "上传失败，url为null");
      return false;
    } else if (!CheckUtil.checkUrl(url)) {
      ALog.e(TAG, "上传失败，url【" + url + "】错误");
      return false;
    }
    int index = url.indexOf("://");
    if (index == -1) {
      ALog.e(TAG, "上传失败，url【" + url + "】不合法");
      return false;
    }
    mEntity.setUrl(url);
    return true;
  }
}
