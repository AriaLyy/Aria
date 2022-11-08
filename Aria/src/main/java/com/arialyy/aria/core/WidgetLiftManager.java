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
package com.arialyy.aria.core;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Message;
import android.widget.PopupWindow;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;

/**
 * Created by lyy on 2017/2/7.
 * 为组件添加生命周期
 */
public final class WidgetLiftManager {
  private final String TAG = "WidgetLiftManager";

  /**
   * 处理DialogFragment事件
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB) public boolean handleDialogFragmentLift(Dialog dialog) {
    return handleDialogLift(dialog);
  }

  /**
   * 处理悬浮框取消或dismiss事件
   */
  public boolean handlePopupWindowLift(PopupWindow popupWindow) {
    try {
      Field dismissField = CommonUtil.getField(popupWindow.getClass(), "mOnDismissListener");
      PopupWindow.OnDismissListener listener =
          (PopupWindow.OnDismissListener) dismissField.get(popupWindow);
      if (listener != null) {
        ALog.e(TAG, "你已经对PopupWindow设置了Dismiss事件。为了防止内存泄露，"
            + "请在dismiss方法中调用Aria.download(this).unRegister();来注销事件");
        return true;
      } else {
        popupWindow.setOnDismissListener(createPopupWindowListener(popupWindow));
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 创建popupWindow dismiss事件
   */
  private PopupWindow.OnDismissListener createPopupWindowListener(final PopupWindow popupWindow) {
    return new PopupWindow.OnDismissListener() {
      @Override public void onDismiss() {
        AriaManager.getInstance().removeReceiver(popupWindow);
      }
    };
  }

  /**
   * 处理对话框取消或dismiss
   *
   * @return true 设置了dialog的销毁事件。false 没有设置dialog的销毁事件
   */
  public boolean handleDialogLift(Dialog dialog) {
    if (dialog == null) {
      ALog.w(TAG,
          "dialog 为空，没有设置自动销毁事件，为了防止内存泄露，请在dismiss方法中调用Aria.download(this).unRegister();来注销事件\n"
              + "如果你使用的是DialogFragment，那么你需要在onDestroy()中进行销毁Aria事件操作");
      return false;
    }
    try {
      Field dismissField = CommonUtil.getField(dialog.getClass(), "mDismissMessage");
      Message dismissMsg = (Message) dismissField.get(dialog);
      //如果Dialog已经设置Dismiss事件，则查找cancel事件
      if (dismissMsg != null) {
        Field cancelField = CommonUtil.getField(dialog.getClass(), "mCancelMessage");
        Message cancelMsg = (Message) cancelField.get(dialog);
        if (cancelMsg != null) {
          ALog.e(TAG, "你已经对Dialog设置了Dismiss和cancel事件。"
              + "为了防止内存泄露，请在dismiss方法中调用Aria.download(this).unRegister();来注销事件\n"
              + "如果你使用的是DialogFragment，那么你需要在onDestroy()中进行销毁Aria事件操作");
          return true;
        } else {
          dialog.setOnCancelListener(createCancelListener());
        }
      } else {
        dialog.setOnDismissListener(createDismissListener());
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 创建Dialog取消事件
   */
  private Dialog.OnCancelListener createCancelListener() {
    return new Dialog.OnCancelListener() {

      @Override public void onCancel(DialogInterface dialog) {
        AriaManager.getInstance().removeReceiver(dialog);
      }
    };
  }

  /**
   * 创建Dialog dismiss取消事件
   */
  private Dialog.OnDismissListener createDismissListener() {
    return new Dialog.OnDismissListener() {

      @Override public void onDismiss(DialogInterface dialog) {
        AriaManager.getInstance().removeReceiver(dialog);
      }
    };
  }
}
