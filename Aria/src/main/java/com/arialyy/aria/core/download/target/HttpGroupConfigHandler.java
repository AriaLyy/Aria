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
package com.arialyy.aria.core.download.target;

import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.DbDataHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2019/4/9.
 *
 * http组合任务功能代理
 */
class HttpGroupConfigHandler<TARGET extends AbsTarget> extends AbsGroupConfigHandler<TARGET> {

  /**
   * 子任务下载地址，
   */
  private List<String> mUrls = new ArrayList<>();

  /**
   * 子任务文件名
   */
  private List<String> mSubNameTemp = new ArrayList<>();

  HttpGroupConfigHandler(TARGET target, long taskId) {
    super(target, taskId);
    mUrls.addAll(getEntity().getUrls());
  }

  void setGroupUrl(List<String> urls) {
    if (urls == null || urls.isEmpty()) {
      throw new NullPointerException("组合任务子任务下载地址列表为空");
    }
    mUrls.clear();
    mUrls.addAll(urls);
    String groupHash = CommonUtil.getMd5Code(urls);
    getEntity().setGroupHash(groupHash);
    List<DownloadEntity> subEntities = DbDataHelper.createHttpSubTask(groupHash, mUrls);
    List<DTaskWrapper> wrappers = new ArrayList<>();
    for (DownloadEntity subEntity : subEntities) {
      subEntity.setTaskType(ITaskWrapper.D_HTTP);
      wrappers.add(new DTaskWrapper(subEntity));
    }
    getEntity().setUrls(urls);
    getEntity().setSubEntities(subEntities);
    getTaskWrapper().setSubTaskWrapper(wrappers);
  }

  /**
   * 设置子任务文件名，该方法必须在{@link #setDirPath(String)}之后调用，否则不生效
   */
  TARGET setSubFileName(List<String> subTaskFileName) {
    if (subTaskFileName == null || subTaskFileName.isEmpty()) {
      ALog.w(TAG, "修改子任务的文件名失败：列表为null");
      return getTarget();
    }
    if (subTaskFileName.size() != getTaskWrapper().getSubTaskWrapper().size()) {
      ALog.w(TAG, "修改子任务的文件名失败：子任务文件名列表数量和子任务的数量不匹配");
      return getTarget();
    }
    mSubNameTemp.clear();
    mSubNameTemp.addAll(subTaskFileName);
    getTaskWrapper().setSubNameTemp(mSubNameTemp);
    return getTarget();
  }

  /**
   * 更新组合任务下载地址
   *
   * @param urls 新的组合任务下载地址列表
   */
  TARGET updateUrls(List<String> urls) {
    if (urls == null || urls.isEmpty()) {
      throw new NullPointerException("下载地址列表为空");
    }
    if (urls.size() != mUrls.size()) {
      throw new IllegalArgumentException("新下载地址数量和旧下载地址数量不一致");
    }
    mUrls.clear();
    mUrls.addAll(urls);
    String newHash = CommonUtil.getMd5Code(urls);
    getEntity().setUrls(mUrls);
    getEntity().setGroupHash(newHash);
    getEntity().update();
    if (getEntity().getSubEntities() != null && !getEntity().getSubEntities().isEmpty()) {
      int i = 0;
      for (DownloadEntity de : getEntity().getSubEntities()) {
        de.setUrl(mUrls.get(i));
        de.setGroupHash(newHash);
        i++;
      }
      DbEntity.updateManyData(getEntity().getSubEntities());
    }
    return getTarget();
  }
}
