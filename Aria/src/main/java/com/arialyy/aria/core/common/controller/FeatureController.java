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
package com.arialyy.aria.core.common.controller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.download.CheckDEntityUtil;
import com.arialyy.aria.core.download.CheckDGEntityUtil;
import com.arialyy.aria.core.download.CheckFtpDirEntityUtil;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.inf.ICheckEntityUtil;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.scheduler.TaskSchedulers;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.upload.CheckUEntityUtil;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 功能控制器
 */
public abstract class FeatureController {
  private static final int ACTION_DEF = 0;
  public static final int ACTION_CREATE = 1;
  public static final int ACTION_RESUME = 2;
  public static final int ACTION_STOP = 3;
  public static final int ACTION_CANCEL = 4;
  public static final int ACTION_ADD = 5;
  public static final int ACTION_PRIORITY = 6;
  public static final int ACTION_RETRY = 7;
  public static final int ACTION_RESTART = 8;
  public static final int ACTION_SAVE = 9;

  private final String TAG;

  private AbsTaskWrapper mTaskWrapper;
  /**
   * 是否忽略权限检查 true 忽略权限检查
   */
  private boolean ignoreCheckPermissions = false;
  private int action = ACTION_DEF;

  FeatureController(AbsTaskWrapper wrapper) {
    mTaskWrapper = wrapper;
    TAG = CommonUtil.getClassName(getClass());
  }

  /**
   * 使用对应等控制器，注意：
   * 1、对于不存在的任务（第一次下载），只能使用{@link ControllerType#CREATE_CONTROLLER}
   * 2、对于已存在的任务，只能使用{@link ControllerType#TASK_CONTROLLER}
   *
   * @param clazz {@link ControllerType#CREATE_CONTROLLER}、{@link ControllerType#TASK_CONTROLLER}
   */
  public static <T extends FeatureController> T newInstance(@ControllerType Class<T> clazz,
      AbsTaskWrapper wrapper) {
    if (wrapper.getEntity().getId() == -1 && clazz != ControllerType.CREATE_CONTROLLER) {
      throw new IllegalArgumentException(
          "对于不存在的任务（第一次下载），只能使用\"ControllerType.CREATE_CONTROLLER\"");
    }
    if (wrapper.getEntity().getId() != -1 && clazz != ControllerType.TASK_CONTROLLER) {
      throw new IllegalArgumentException(
          "对于已存在的任务，只能使用\" ControllerType.TASK_CONTROLLER\"，请检查是否重复调用#create()方法");
    }

    Class[] paramTypes = { AbsTaskWrapper.class };
    Object[] params = { wrapper };
    try {
      Constructor<T> con = clazz.getConstructor(paramTypes);
      return con.newInstance(params);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  void setAction(int action) {
    this.action = action;
  }

  /**
   * 是否忽略权限检查
   */
  public void ignoreCheckPermissions() {
    this.ignoreCheckPermissions = true;
  }

  /**
   * 强制执行任务，不管文件路径是否被占用
   */
  public void ignoreFilePathOccupy() {
    mTaskWrapper.setIgnoreFilePathOccupy(true);
  }

  protected AbsTaskWrapper getTaskWrapper() {
    return mTaskWrapper;
  }

  protected AbsEntity getEntity() {
    return mTaskWrapper.getEntity();
  }

  int checkTaskType() {
    int taskType = 0;
    if (mTaskWrapper instanceof DTaskWrapper) {
      taskType = ITask.DOWNLOAD;
    } else if (mTaskWrapper instanceof DGTaskWrapper) {
      taskType = ITask.DOWNLOAD_GROUP;
    } else if (mTaskWrapper instanceof UTaskWrapper) {
      taskType = ITask.UPLOAD;
    }
    return taskType;
  }

  /**
   * 如果检查实体失败，将错误回调
   */
  boolean checkConfig() {
    if (!ignoreCheckPermissions && !checkPermission()) {
      return false;
    }
    boolean b = checkEntity();
    TaskSchedulers schedulers = TaskSchedulers.getInstance();
    if (!b && schedulers != null) {
      new Handler(Looper.getMainLooper(), schedulers).obtainMessage(ISchedulers.CHECK_FAIL,
          checkTaskType(), -1, null).sendToTarget();
    }

    return b;
  }

  /**
   * 检查权限，需要的权限有
   * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE}
   * {@link Manifest.permission#READ_EXTERNAL_STORAGE}
   * {@link Manifest.permission#INTERNET}
   *
   * @return {@code false} 缺少权限
   */
  private boolean checkPermission() {

    if (AriaConfig.getInstance()
        .getAPP()
        .checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ALog.e(TAG, "启动失败，缺少权限：Manifest.permission.WRITE_EXTERNAL_STORAGE");
      return false;
    }
    if (AriaConfig.getInstance()
        .getAPP()
        .checkCallingOrSelfPermission(Manifest.permission.INTERNET)
        != PackageManager.PERMISSION_GRANTED) {
      ALog.e(TAG, "启动失败，缺少权限：Manifest.permission.INTERNET");
      return false;
    }
    if (AriaConfig.getInstance()
        .getAPP()
        .checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      ALog.e(TAG, "启动失败，缺少权限：Manifest.permission.READ_EXTERNAL_STORAGE");
      return false;
    }

    return true;
  }

  private boolean checkEntity() {
    ICheckEntityUtil checkUtil = null;
    if (mTaskWrapper instanceof DTaskWrapper) {
      checkUtil = CheckDEntityUtil.newInstance((DTaskWrapper) mTaskWrapper, action);
    } else if (mTaskWrapper instanceof DGTaskWrapper) {
      if (mTaskWrapper.getRequestType() == ITaskWrapper.D_FTP_DIR) {
        checkUtil = CheckFtpDirEntityUtil.newInstance((DGTaskWrapper) mTaskWrapper, action);
      } else if (mTaskWrapper.getRequestType() == ITaskWrapper.DG_HTTP) {
        checkUtil = CheckDGEntityUtil.newInstance((DGTaskWrapper) mTaskWrapper, action);
      }
    } else if (mTaskWrapper instanceof UTaskWrapper) {
      checkUtil = CheckUEntityUtil.newInstance((UTaskWrapper) mTaskWrapper, action);
    }
    return checkUtil != null && checkUtil.checkEntity();
  }
}
