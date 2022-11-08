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
package com.arialyy.aria.core.download;

import android.text.TextUtils;
import com.arialyy.aria.core.common.controller.FeatureController;
import com.arialyy.aria.core.inf.ICheckEntityUtil;
import com.arialyy.aria.core.inf.IOptionConstant;
import com.arialyy.aria.core.inf.ITargetHandler;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.FileUtil;
import com.arialyy.aria.util.RecordUtil;
import java.io.File;

/**
 * 检查下载任务实体
 */
public class CheckDEntityUtil implements ICheckEntityUtil {
  private final String TAG = CommonUtil.getClassName(getClass());
  private DTaskWrapper mWrapper;
  private DownloadEntity mEntity;
  private int action;

  public static CheckDEntityUtil newInstance(DTaskWrapper wrapper, int action) {
    return new CheckDEntityUtil(wrapper, action);
  }

  private CheckDEntityUtil(DTaskWrapper wrapper, int action) {
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
    if (mWrapper.getRequestType() == ITaskWrapper.M3U8_VOD
        || mWrapper.getRequestType() == ITaskWrapper.M3U8_LIVE) {
      handleM3U8();
    }
    return b;
  }

  private void handleM3U8() {
    File file = new File(mWrapper.getTempFilePath());
    Object bw = mWrapper.getM3U8Params().getParam(IOptionConstant.bandWidth);
    int bandWidth = bw == null ? 0 : (int) bw;
    String cacheDir = FileUtil.getTsCacheDir(file.getPath(), bandWidth);

    mWrapper.getM3U8Params().setParams(IOptionConstant.cacheDir, cacheDir);
    M3U8Entity m3U8Entity = mEntity.getM3U8Entity();

    if (m3U8Entity == null) {
      m3U8Entity = new M3U8Entity();
      m3U8Entity.setFilePath(mEntity.getFilePath());
      m3U8Entity.setPeerIndex(0);
      m3U8Entity.setCacheDir(cacheDir);
      m3U8Entity.insert();
    } else {
      m3U8Entity.update();
    }
    if (mWrapper.getRequestType() == ITaskWrapper.M3U8_VOD
        && action == FeatureController.ACTION_CREATE) {
      if (mEntity.getFileSize() == 0) {
        ALog.w(TAG,
            "由于m3u8协议的特殊性质，无法有效快速获取到正确到文件长度，如果你需要显示文件中长度，你需要自行设置文件长度：.asM3U8().asVod().setFileSize(xxx)");
      }
    } else if (mWrapper.getRequestType() == ITaskWrapper.M3U8_LIVE
        && action != FeatureController.ACTION_CANCEL) {
      if (file.exists()) {
        ALog.w(TAG, "对于直播来说，每次下载都是一个新文件，所以你需要设置新都文件路径，否则Aria框架将会覆盖已下载的文件");
        file.delete();
      }
    }

    if (action != FeatureController.ACTION_CANCEL
        && mWrapper.getM3U8Params().getHandler(IOptionConstant.bandWidthUrlConverter) != null
        && bandWidth == 0) {
      ALog.w(TAG, "你已经设置了码率url转换器，但是没有设置码率，Aria框架将采用第一个获取到的码率");
    }
  }

  private boolean checkFilePath() {
    String filePath = mWrapper.getTempFilePath();
    if (TextUtils.isEmpty(filePath)) {
      ALog.e(TAG, "下载失败，文件保存路径为null");
      return false;
    }
    if (!FileUtil.canWrite(new File(filePath).getParent())){
      ALog.e(TAG, String.format("路径【%s】不可写", filePath));
      return false;
    }
    if (!filePath.startsWith("/")) {
      ALog.e(TAG, String.format("下载失败，文件保存路径【%s】错误", filePath));
      return false;
    }
    File file = new File(filePath);
    if (file.isDirectory()) {
      if (mWrapper.getRequestType() == ITargetHandler.D_HTTP
          || mWrapper.getRequestType() == ITaskWrapper.M3U8_VOD) {
        ALog.e(TAG,
            String.format("下载失败，保存路径【%s】不能为文件夹，路径需要是完整的文件路径，如：/mnt/sdcard/game.zip", filePath));
        return false;
      } else if (mWrapper.getRequestType() == ITargetHandler.D_FTP) {
        filePath += mEntity.getFileName();
      }
    } else {
      // http文件名设置
      if (TextUtils.isEmpty(mEntity.getFileName())) {
        mEntity.setFileName(file.getName());
      }
    }

    return checkPathConflicts(filePath);
  }

  /**
   * 检查路径冲突
   * @return true 路径没有冲突
   */
  private boolean checkPathConflicts(String filePath) {
    DownloadEntity de = DbEntity.findFirst(DownloadEntity.class, "downloadPath=?", filePath);
    if (de != null && de.getUrl().equals(mEntity.getUrl())){
      mEntity.rowID = de.rowID;
      mEntity.setFilePath(filePath);
      mEntity.setFileName(new File(filePath).getName());
      return true;
    }
    //设置文件保存路径，如果新文件路径和旧文件路径不同，则修改路径
    if (!filePath.equals(mEntity.getFilePath())) {
      // 检查路径冲突
      if (!CheckUtil.checkDPathConflicts(mWrapper.isIgnoreFilePathOccupy(), filePath,
          mWrapper.getRequestType())) {
        return false;
      }

      File newFile = new File(filePath);
      mEntity.setFilePath(filePath);
      mEntity.setFileName(newFile.getName());

      // 如过使用Content-Disposition中的文件名，将不会执行重命名工作
      Object usf = mWrapper.getOptionParams().getParam(IOptionConstant.useServerFileName);
      if ((usf != null && (boolean) usf) || mWrapper.getRequestType() == ITaskWrapper.M3U8_LIVE) {
        return true;
      }
      if (!TextUtils.isEmpty(mEntity.getFilePath())) {
        File oldFile = new File(mEntity.getFilePath());
        if (oldFile.exists()) {
          // 处理普通任务的重命名
          RecordUtil.modifyTaskRecord(oldFile.getPath(), newFile.getPath(), mEntity.getTaskType());
          ALog.i(TAG, String.format("将任务重命名为：%s", newFile.getName()));
        } else if (RecordUtil.blockTaskExists(oldFile.getPath())) {
          // 处理分块任务的重命名
          RecordUtil.modifyTaskRecord(oldFile.getPath(), newFile.getPath(), mEntity.getTaskType());
          ALog.i(TAG, String.format("将分块任务重命名为：%s", newFile.getName()));
        }
      }
    }
    return true;
  }

  private boolean checkUrl() {
    final String url = mEntity.getUrl();
    if (TextUtils.isEmpty(url)) {
      ALog.e(TAG, "下载失败，url为null");
      return false;
    } else if (!CheckUtil.checkUrl(url)) {
      ALog.e(TAG, "下载失败，url【" + url + "】错误");
      return false;
    }
    int index = url.indexOf("://");
    if (index == -1) {
      ALog.e(TAG, "下载失败，url【" + url + "】不合法");
      return false;
    }
    if (!TextUtils.isEmpty(mWrapper.getTempUrl())) {
      mEntity.setUrl(mWrapper.getTempUrl());
    }
    return true;
  }
}
