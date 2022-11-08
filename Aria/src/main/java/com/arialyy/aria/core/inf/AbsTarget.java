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
package com.arialyy.aria.core.inf;

import android.text.TextUtils;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.controller.BuilderController;
import com.arialyy.aria.core.common.controller.NormalController;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;

/**
 * Created by AriaL on 2017/7/3.
 */
public abstract class AbsTarget<TARGET extends AbsTarget> {

  protected String TAG;
  private AbsEntity mEntity;
  private AbsTaskWrapper mTaskWrapper;

  protected AbsTarget() {
    TAG = CommonUtil.getClassName(this);
  }

  public void setTaskWrapper(AbsTaskWrapper wrapper) {
    mTaskWrapper = wrapper;
    mEntity = wrapper.getEntity();
  }

  public AbsEntity getEntity() {
    return mEntity;
  }

  /**
   * 获取任务实体
   */
  protected AbsTaskWrapper getTaskWrapper() {
    return mTaskWrapper;
  }

  /**
   * 设置扩展字段，用来保存你的其它数据，如果你的数据比较多，你可以把你的数据转换为JSON字符串，然后再存到数据库中
   * 注意：如果在后续方法调用链中没有调用 {@link ITargetHandler#start()}、{@link ITargetHandler#stop()}、{@link
   * ITargetHandler#cancel()}、{@link ITargetHandler#resume()}
   * 等操作任务的方法，那么你需要调用{@link NormalController#save()}才能将修改保存到数据库
   *
   * @param str 扩展数据
   */
  public TARGET setExtendField(String str) {
    if (TextUtils.isEmpty(str)) return (TARGET) this;
    if (TextUtils.isEmpty(mEntity.getStr()) || !mEntity.getStr().equals(str)) {
      mEntity.setStr(str);
    } else {
      ALog.e(TAG, "设置扩展字段失败，扩展字段为一致");
    }

    return (TARGET) this;
  }

  /**
   * 重置状态，将任务状态设置为未开始状态
   * 注意：如果在后续方法调用链中没有调用 {@link NormalController#stop()}、{@link NormalController#cancel()}、
   * {@link NormalController#resume()}、{@link BuilderController#create()}、{@link
   * BuilderController#add()}
   * 等操作任务的方法，那么你需要调用{@link NormalController#save()}才能将修改保存到数据库
   */
  public TARGET resetState() {
    getTaskWrapper().getEntity().setState(IEntity.STATE_WAIT);
    getTaskWrapper().setRefreshInfo(true);
    return (TARGET) this;
  }
}
