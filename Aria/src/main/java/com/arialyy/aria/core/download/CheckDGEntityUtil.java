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
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.common.controller.FeatureController;
import com.arialyy.aria.core.inf.ICheckEntityUtil;
import com.arialyy.aria.core.inf.IOptionConstant;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.FileUtil;
import com.arialyy.aria.util.RecordUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckDGEntityUtil implements ICheckEntityUtil {

  private final String TAG = "CheckDGEntityUtil";
  private DGTaskWrapper mWrapper;
  private DownloadGroupEntity mEntity;

  /**
   * 是否需要修改路径
   */
  private boolean needModifyPath = false;
  private int action;

  /**
   * @param action {@link FeatureController#ACTION_CREATE}
   */
  public static CheckDGEntityUtil newInstance(DGTaskWrapper wrapper, int action) {
    return new CheckDGEntityUtil(wrapper, action);
  }

  private CheckDGEntityUtil(DGTaskWrapper wrapper, int action) {
    this.action = action;
    mWrapper = wrapper;
    mEntity = mWrapper.getEntity();
  }

  /**
   * 检查并设置文件夹路径
   *
   * @return {@code true} 合法
   */
  private boolean checkDirPath() {
    String dirPath = mWrapper.getDirPathTemp();

    if (TextUtils.isEmpty(dirPath)) {
      ALog.e(TAG, "文件夹路径不能为null");
      return false;
    }
    File file = new File(dirPath);
    if (!FileUtil.canWrite(file.getParent()) && !FileUtil.canWrite(dirPath)) {
      ALog.e(TAG, String.format("路径【%s】不可写", dirPath));
      return false;
    }
    if (!dirPath.startsWith("/")) {
      ALog.e(TAG, String.format("文件夹路径【%s】错误", dirPath));
      return false;
    }

    if (file.isFile()) {
      ALog.e(TAG, String.format("路径【%s】是文件，请设置文件夹路径", dirPath));
      return false;
    }

    // 检查路径冲突
    if (mWrapper.isNewTask() && !CheckUtil.checkDGPathConflicts(mWrapper.isIgnoreFilePathOccupy(),
        dirPath)) {
      return false;
    }

    if (TextUtils.isEmpty(mEntity.getDirPath()) || !mEntity.getDirPath()
        .equals(dirPath)) {
      if (!file.exists()) {
        file.mkdirs();
      }
      needModifyPath = true;
      mEntity.setDirPath(dirPath);
      ALog.i(TAG, String.format("文件夹路径改变，将更新文件夹路径为：%s", dirPath));
    }
    return true;
  }

  /**
   * 改变任务组文件夹路径，修改文件夹路径会将子任务所有路径更换
   *
   * @param newDirPath 新的文件夹路径
   */
  private void reChangeDirPath(String newDirPath) {
    ALog.d(TAG, String.format("修改新路径为：%s", newDirPath));
    List<DTaskWrapper> subTasks = mWrapper.getSubTaskWrapper();
    if (subTasks != null && !subTasks.isEmpty()) {
      for (DTaskWrapper dte : subTasks) {
        DownloadEntity de = dte.getEntity();
        String oldPath = de.getFilePath();
        String newPath = newDirPath + "/" + de.getFileName();
        File file = new File(oldPath);
        if (file.exists()) {
          file.renameTo(new File(newPath));
        }
        de.setFilePath(newPath);
      }
    }
  }

  /**
   * 检查和处理组合任务的路径冲突
   *
   * @param isIgnoreTaskOccupy true，如果hash冲突，将删除其它任务的记录的
   * @param groupHash 组任务hash
   * @return false 任务不再执行，true 任务继续执行
   */
  private boolean checkGroupHash(boolean isIgnoreTaskOccupy, String groupHash) {
    DownloadGroupEntity dge =
        DbEntity.findFirst(DownloadGroupEntity.class, "groupHash=?", groupHash);
    if (dge != null && dge.getGroupHash().equals(mEntity.getGroupHash())) {
      mEntity.rowID = dge.rowID;
      return true;
    }

    if (DbEntity.checkDataExist(DownloadGroupEntity.class, "groupHash=?", groupHash)) {
      if (!isIgnoreTaskOccupy) {
        ALog.e(TAG, String.format("下载失败，数据库中已存在相同的url的组任务，groupHash = %s", groupHash));
        return false;
      } else {
        ALog.w(TAG, String.format("数据库中已存在相同的url的组任务，将删除groupHash = %s 的旧任务", groupHash));
        RecordUtil.delGroupTaskRecordByHash(groupHash, true);
        return true;
      }
    }
    return true;
  }

  @Override
  public boolean checkEntity() {
    if (mWrapper.getErrorEvent() != null) {
      ALog.e(TAG, String.format("任务操作失败，%s", mWrapper.getErrorEvent().errorMsg));
      return false;
    }

    if ((action == FeatureController.ACTION_CREATE || action == FeatureController.ACTION_ADD)
        && !checkGroupHash(mWrapper.isIgnoreTaskOccupy(), mEntity.getGroupHash())) {
      return false;
    }

    if (!checkDirPath()) {
      return false;
    }

    if (!checkSubName()) {
      return false;
    }

    if (!checkUrls()) {
      return false;
    }

    if (action != FeatureController.ACTION_CANCEL
        && !mWrapper.isUnknownSize()
        && mEntity.getFileSize() == 0) {
      ALog.e(TAG, "组合任务必须设置文件文件大小，默认需要强制设置文件大小。如果无法获取到总长度，请调用#unknownSize()来标志该组合任务");
      return false;
    }

    if (mWrapper.getOptionParams().getParam(IOptionConstant.requestEnum) == RequestEnum.POST) {
      for (DTaskWrapper subWrapper : mWrapper.getSubTaskWrapper()) {
        subWrapper.getOptionParams().setParams(IOptionConstant.requestEnum, RequestEnum.POST);
      }
    }

    if (needModifyPath) {
      reChangeDirPath(mWrapper.getDirPathTemp());
    }

    if (!mWrapper.getSubNameTemp().isEmpty()) {
      updateSingleSubFileName();
    }
    saveEntity();
    return true;
  }

  private void saveEntity() {
    mEntity.save();
    DbEntity.saveAll(mEntity.getSubEntities());
  }

  /**
   * 更新所有改动的子任务文件名
   */
  private void updateSingleSubFileName() {
    List<DTaskWrapper> entities = mWrapper.getSubTaskWrapper();
    int i = 0;
    for (DTaskWrapper taskWrapper : entities) {
      if (i < mWrapper.getSubNameTemp().size()) {
        String newName = mWrapper.getSubNameTemp().get(i);
        DownloadEntity entity = taskWrapper.getEntity();
        if (!newName.equals(entity.getFileName())) {
          String oldPath = mEntity.getDirPath() + "/" + entity.getFileName();
          String newPath = mEntity.getDirPath() + "/" + newName;
          if (DbEntity.checkDataExist(DownloadEntity.class, "downloadPath=?", newPath)) {
            ALog.w(TAG, String.format("更新文件名失败，路径【%s】被其它任务占用", newPath));
            return;
          }

          RecordUtil.modifyTaskRecord(oldPath, newPath, mEntity.getTaskType());
          entity.setFilePath(newPath);
          entity.setFileName(newName);
        }
      }
      i++;
    }
  }

  /**
   * 检查urls是否合法，并删除不合法的子任务
   *
   * @return {@code true} 合法
   */
  private boolean checkUrls() {
    if (mEntity.getUrls().isEmpty()) {
      ALog.e(TAG, "操作失败，子任务下载列表为null");
      return false;
    }

    Set<String> repeated = new HashSet<>();
    List<String> results = new ArrayList<>();
    for (String url : mEntity.getUrls()) {
      if (!repeated.add(url)) {
        results.add(url);
      }
    }
    if (!results.isEmpty()) {
      ALog.e(TAG, String.format("组合任务中有url重复，重复的url：%s", Arrays.toString(results.toArray())));
      return false;
    }

    Set<Integer> delItem = new HashSet<>();

    int i = 0;
    for (String url : mEntity.getUrls()) {
      if (TextUtils.isEmpty(url)) {
        ALog.e(TAG, "子任务url为null，即将删除该子任务。");
        delItem.add(i);
        continue;
      } else if (!url.startsWith("http")) {
        ALog.e(TAG, "子任务url【" + url + "】错误，即将删除该子任务。");
        delItem.add(i);
        continue;
      }
      int index = url.indexOf("://");
      if (index == -1) {
        ALog.e(TAG, "子任务url【" + url + "】不合法，即将删除该子任务。");
        delItem.add(i);
        continue;
      }

      i++;
    }

    for (int index : delItem) {
      mEntity.getUrls().remove(index);
      if (mWrapper.getSubNameTemp() != null && !mWrapper.getSubNameTemp().isEmpty()) {
        mWrapper.getSubNameTemp().remove(index);
      }
    }
    return true;
  }

  /**
   * 如果用户设置了子任务文件名，检查子任务文件名
   *
   * @return {@code true} 合法
   */
  private boolean checkSubName() {
    if (mWrapper.getSubNameTemp() == null || mWrapper.getSubNameTemp().isEmpty()) {
      return true;
    }
    if (mEntity.getUrls().size() != mWrapper.getSubNameTemp().size()) {
      ALog.e(TAG, "子任务文件名必须和子任务数量一致");
      return false;
    }

    return true;
  }
}
