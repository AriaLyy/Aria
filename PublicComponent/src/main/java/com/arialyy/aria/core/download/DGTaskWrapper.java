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

import com.arialyy.aria.core.config.Configuration;
import com.arialyy.aria.core.config.DGroupConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AriaL on 2017/7/1. 任务组的任务实体修饰器
 */
public class DGTaskWrapper extends AbsGroupTaskWrapper<DownloadGroupEntity, DTaskWrapper> {

  private List<DTaskWrapper> subWrappers;

  private boolean unknownSize = false;

  /**
   * 保存临时设置的文件夹路径
   */
  private String dirPathTemp;

  /**
   * 子任务文件名
   */
  private List<String> subNameTemp = new ArrayList<>();

  public DGTaskWrapper(DownloadGroupEntity entity) {
    super(entity);
  }

  public List<String> getSubNameTemp() {
    return subNameTemp;
  }

  public void setSubNameTemp(List<String> subNameTemp) {
    this.subNameTemp = subNameTemp;
  }

  public String getDirPathTemp() {
    return dirPathTemp;
  }

  public void setDirPathTemp(String mDirPathTemp) {
    this.dirPathTemp = mDirPathTemp;
  }

  @Override
  public void setSubTaskWrapper(List<DTaskWrapper> subTaskEntities) {
    this.subWrappers = subTaskEntities;
  }

  public boolean isUnknownSize() {
    return unknownSize;
  }

  public void setUnknownSize(boolean unknownSize) {
    this.unknownSize = unknownSize;
  }

  @Override public String getKey() {
    return getEntity().getKey();
  }

  @Override public DGroupConfig getConfig() {
    return Configuration.getInstance().dGroupCfg;
  }

  @Override public List<DTaskWrapper> getSubTaskWrapper() {
    if (subWrappers == null) {
      subWrappers = new ArrayList<>();
    }
    return subWrappers;
  }
}
