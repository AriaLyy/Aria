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

import android.app.Dialog;
import android.util.Log;
import android.widget.PopupWindow;
import com.arialyy.aria.core.WidgetLiftManager;
import com.arialyy.aria.core.queue.DGroupTaskQueue;
import com.arialyy.aria.core.queue.DTaskQueue;
import com.arialyy.aria.core.queue.UTaskQueue;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by AriaL on 2017/6/27.
 * 接收器
 */
public abstract class AbsReceiver implements IReceiver {
  protected String TAG = getClass().getSimpleName();

  /**
   * 观察者对象
   */
  public Object obj;

  /**
   * 观察者对象类的完整名称
   */
  private String targetName;

  /**
   * 当dialog、dialogFragment、popupwindow已经被用户使用了Dismiss事件或Cancel事件，需要手动移除receiver
   */
  private boolean needRmReceiver = false;

  private boolean isFragment = false;

  public boolean isLocalOrAnonymousClass = false;

  public AbsReceiver(Object obj) {
    this.obj = obj;
    initParams();
  }

  private void initParams() {
    try {
      targetName = CommonUtil.getTargetName(obj);
      Class clazz = obj.getClass();
      if (CommonUtil.isLocalOrAnonymousClass(clazz)) {
        isLocalOrAnonymousClass = true;
        String parentName = CommonUtil.getTargetName(obj);
        Class parentClazz = Class.forName(parentName);
        handleFragmentOrDialogParam(parentClazz, true);
        return;
      }
      handleFragmentOrDialogParam(clazz, false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleFragmentOrDialogParam(Class clazz, boolean isLocalOrAnonymousClass) {
    final WidgetLiftManager widgetLiftManager = new WidgetLiftManager();
    if (obj instanceof Dialog) {
      needRmReceiver = widgetLiftManager.handleDialogLift((Dialog) obj);
      return;
    }
    if (obj instanceof PopupWindow) {
      needRmReceiver = widgetLiftManager.handlePopupWindowLift((PopupWindow) obj);
      return;
    }

    if (CommonUtil.isFragment(clazz)){
      isFragment = true;
    }

    if (CommonUtil.isDialogFragment(clazz)) {
      isFragment = true;
      if (isLocalOrAnonymousClass) {
        Log.e(TAG, String.format(
            "%s 是匿名内部类，无法获取到dialog对象，为了防止内存泄漏，请在dismiss方法中调用Aria.download(this).unRegister();来注销事件",
            obj.getClass().getName()
        ));
        return;
      }
      needRmReceiver = widgetLiftManager.handleDialogFragmentLift(getDialog(obj));
    }
  }

  /**
   * 获取DialogFragment的dialog
   *
   * @return 获取失败，返回null
   */
  private Dialog getDialog(Object obj) {
    try {
      Method method = obj.getClass().getMethod("getDialog");
      return (Dialog) method.invoke(obj);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  protected boolean isNeedRmListener() {
    return needRmReceiver;
  }

  @Override public boolean isFragment() {
    return isFragment;
  }

  /**
   * 创建观察者对象map的key，生成规则：
   * {@link #targetName}_{@code download}{@code upload}_{@link #hashCode()}
   *
   * @param receiver 当前接收器
   * @return 返回key
   */
  public static String getKey(IReceiver receiver) {
    return String.format("%s_%s_%s", receiver.getTargetName(), receiver.getType(),
        receiver.hashCode());
  }

  @Override public String getTargetName() {
    return targetName;
  }

  /**
   * 获取当前Receiver的key
   */
  @Override public String getKey() {
    return getKey(this);
  }

  /**
   * 移除观察者对象
   */
  private void removeObj() {
    obj = null;
  }

  @Override public void destroy() {
    unRegisterListener();
    removeObj();
  }

  /**
   * 移除{@link DTaskQueue}、{@link DGroupTaskQueue}、{@link UTaskQueue}中注册的观察者
   */
  protected abstract void unRegisterListener();
}
