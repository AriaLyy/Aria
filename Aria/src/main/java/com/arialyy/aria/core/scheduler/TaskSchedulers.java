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
package com.arialyy.aria.core.scheduler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import com.arialyy.annotations.TaskEnum;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.AbsNormalEntity;
import com.arialyy.aria.core.group.GroupSendParams;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.listener.ISchedulers;
import com.arialyy.aria.core.manager.TaskWrapperManager;
import com.arialyy.aria.core.queue.DGroupTaskQueue;
import com.arialyy.aria.core.queue.DTaskQueue;
import com.arialyy.aria.core.queue.ITaskQueue;
import com.arialyy.aria.core.queue.UTaskQueue;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.task.DownloadGroupTask;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.task.UploadTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.NetUtils;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lyy on 2017/6/4. 事件调度器，用于处理任务状态的调度
 */
public class TaskSchedulers<TASK extends ITask> implements ISchedulers {
  private final String TAG = CommonUtil.getClassName(getClass());

  private static volatile TaskSchedulers INSTANCE;
  private static FailureTaskHandler mFailureTaskHandler;

  private Map<String, Map<TaskEnum, Object>> mObservers = new ConcurrentHashMap<>();
  private AriaConfig mAriaConfig;

  private TaskSchedulers() {
    mAriaConfig = AriaConfig.getInstance();
  }

  public static TaskSchedulers getInstance() {
    if (INSTANCE == null) {
      synchronized (TaskSchedulers.class) {
        if (INSTANCE == null) {
          INSTANCE = new TaskSchedulers();
          mFailureTaskHandler = FailureTaskHandler.init(INSTANCE);
        }
      }
    }
    return INSTANCE;
  }

  /**
   * 通过任务类型获取任务队列
   *
   * @param taskType 任务类型
   */
  ITaskQueue getQueue(int taskType) {
    if (taskType == ITask.DOWNLOAD) {
      return DTaskQueue.getInstance();
    }
    if (taskType == ITask.DOWNLOAD_GROUP) {
      return DGroupTaskQueue.getInstance();
    }
    if (taskType == ITask.UPLOAD) {
      return UTaskQueue.getInstance();
    }
    throw new NullPointerException("任务类型错误，type = " + taskType);
  }

  /**
   * 将当前类注册到Aria
   *
   * @param obj 观察者类
   * @param taskEnum 任务类型 {@link TaskEnum}
   */
  public void register(Object obj, TaskEnum taskEnum) {
    String targetName = obj.getClass().getName();
    Map<TaskEnum, Object> listeners = mObservers.get(getKey(obj));

    if (listeners == null) {
      listeners = new ConcurrentHashMap<>();
      mObservers.put(getKey(obj), listeners);
    }

    if (!hasProxyListener(listeners, taskEnum)) {
      if (obj instanceof TaskInternalListenerInterface) {
        listeners.put(taskEnum, obj);
        return;
      }
      String proxyClassName = targetName + taskEnum.proxySuffix;
      ISchedulerListener listener = createListener(proxyClassName);
      if (listener != null) {
        listener.setListener(obj);
        listeners.put(taskEnum, listener);
      } else {
        ALog.e(TAG, "注册错误，没有【" + proxyClassName + "】观察者");
      }
    }
  }

  /**
   * 检查当前类是否已经注册了对应的代理
   *
   * @param taskEnum 代理类类型
   * @return true，已注册代理类，false，没有注册代理类
   */
  private boolean hasProxyListener(Map<TaskEnum, Object> listeners, TaskEnum taskEnum) {
    return !listeners.isEmpty() && listeners.get(taskEnum) != null;
  }

  /**
   * 移除注册
   *
   * @param obj 观察者类
   */
  public void unRegister(Object obj) {
    if (!mObservers.containsKey(getKey(obj))) {
      return;
    }
    for (Iterator<Map.Entry<String, Map<TaskEnum, Object>>> iter =
        mObservers.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<String, Map<TaskEnum, Object>> entry = iter.next();

      if (entry.getKey().equals(getKey(obj))) {
        iter.remove();
      }
    }
  }

  private String getKey(Object obj) {
    return obj.getClass().getName() + obj.hashCode();
  }

  /**
   * 创建代理类
   *
   * @param proxyClassName 代理类类名
   */
  private ISchedulerListener createListener(String proxyClassName) {
    ISchedulerListener listener = null;
    try {
      Class clazz = Class.forName(proxyClassName);
      listener = (ISchedulerListener) clazz.newInstance();
    } catch (ClassNotFoundException e) {
      ALog.e(TAG, e.getMessage());
    } catch (InstantiationException e) {
      ALog.e(TAG, e.getMessage());
    } catch (IllegalAccessException e) {
      ALog.e(TAG, e.getMessage());
    }
    return listener;
  }

  @Override public boolean handleMessage(Message msg) {
    if (msg.what == CHECK_FAIL) {
      handlePreFailTask(msg.arg1);
      return true;
    }

    if (msg.arg1 == IS_SUB_TASK) {
      return handleSubEvent(msg);
    }

    if (msg.arg1 == IS_M3U8_PEER) {
      return handlePeerEvent(msg);
    }

    TASK task = (TASK) msg.obj;
    if (task == null) {
      ALog.e(TAG, "请传入下载任务");
      return true;
    }
    handleNormalEvent(task, msg.what);
    return true;
  }

  /**
   * 处理m3u8切片任务事件
   */
  private boolean handlePeerEvent(Message msg) {
    Bundle data = msg.getData();
    if (mObservers.size() > 0) {
      Set<String> keys = mObservers.keySet();
      for (String key : keys) {
        Map<TaskEnum, Object> listeners = mObservers.get(key);
        if (listeners == null || listeners.isEmpty()) {
          continue;
        }
        M3U8PeerTaskListener listener =
            (M3U8PeerTaskListener) listeners.get(TaskEnum.M3U8_PEER);
        if (listener == null) {
          continue;
        }

        switch (msg.what) {
          case M3U8_PEER_START:
            listener.onPeerStart(data.getString(DATA_M3U8_URL),
                data.getString(DATA_M3U8_PEER_PATH),
                data.getInt(DATA_M3U8_PEER_INDEX));
            break;
          case M3U8_PEER_COMPLETE:
            listener.onPeerComplete(data.getString(DATA_M3U8_URL),
                data.getString(DATA_M3U8_PEER_PATH),
                data.getInt(DATA_M3U8_PEER_INDEX));
            break;
          case M3U8_PEER_FAIL:
            listener.onPeerFail(data.getString(DATA_M3U8_URL),
                data.getString(DATA_M3U8_PEER_PATH),
                data.getInt(DATA_M3U8_PEER_INDEX));
            break;
        }
      }
    }

    boolean canSend = mAriaConfig.getAConfig().isUseBroadcast();
    if (canSend) {
      Intent intent = new Intent(ISchedulers.ARIA_TASK_INFO_ACTION);
      intent.setPackage(mAriaConfig.getAPP().getPackageName());
      intent.putExtras(data);
      mAriaConfig.getAPP().sendBroadcast(intent);
    }

    return true;
  }

  /**
   * 处理任务组子任务事件
   */
  private boolean handleSubEvent(Message msg) {
    GroupSendParams params = (GroupSendParams) msg.obj;
    if (mObservers.size() > 0) {
      Set<String> keys = mObservers.keySet();
      for (String key : keys) {
        Map<TaskEnum, Object> listeners = mObservers.get(key);
        if (listeners == null || listeners.isEmpty()) {
          continue;
        }
        SubTaskListener<TASK, AbsNormalEntity> listener =
            (SubTaskListener<TASK, AbsNormalEntity>) listeners.get(TaskEnum.DOWNLOAD_GROUP_SUB);
        if (listener == null) {
          continue;
        }
        switch (msg.what) {
          case SUB_PRE:
            listener.onSubTaskPre((TASK) params.groupTask, params.entity);
            break;
          case SUB_START:
            listener.onSubTaskStart((TASK) params.groupTask, params.entity);
            break;
          case SUB_STOP:
            listener.onSubTaskStop((TASK) params.groupTask, params.entity);
            break;
          case SUB_FAIL:
            listener.onSubTaskFail((TASK) params.groupTask, params.entity,
                (Exception) (params.groupTask).getExpand(AbsTask.ERROR_INFO_KEY));
            break;
          case SUB_RUNNING:
            listener.onSubTaskRunning((TASK) params.groupTask, params.entity);
            break;
          case SUB_CANCEL:
            listener.onSubTaskCancel((TASK) params.groupTask, params.entity);
            break;
          case SUB_COMPLETE:
            listener.onSubTaskComplete((TASK) params.groupTask, params.entity);
            break;
        }
      }
    }

    boolean canSend = mAriaConfig.getAConfig().isUseBroadcast();
    if (canSend) {
      mAriaConfig.getAPP().sendBroadcast(
          createData(msg.what, ITask.DOWNLOAD_GROUP_SUB, params.entity));
    }

    return true;
  }

  /**
   * 处理普通任务和任务组的事件
   */
  private void handleNormalEvent(TASK task, int what) {
    ITaskQueue queue = getQueue(task.getTaskType());
    switch (what) {
      case STOP:
        if (task.getState() == IEntity.STATE_WAIT) {
          break;
        }
        queue.removeTaskFormQueue(task.getKey());
        if (queue.getCurrentExePoolNum() < queue.getMaxTaskNum()) {
          ALog.d(TAG, String.format("停止任务【%s】成功，尝试开始下一任务", task.getTaskName()));
          startNextTask(queue, task.getSchedulerType());
        } else {
          ALog.d(TAG, String.format("停止任务【%s】成功", task.getTaskName()));
        }
        break;
      case CANCEL:
        queue.removeTaskFormQueue(task.getKey());
        if (queue.getCurrentExePoolNum() < queue.getMaxTaskNum()) {
          ALog.d(TAG, String.format("删除任务【%s】成功，尝试开始下一任务", task.getTaskName()));
          startNextTask(queue, task.getSchedulerType());
        } else {
          ALog.d(TAG, String.format("删除任务【%s】成功", task.getTaskName()));
        }
        break;
      case COMPLETE:
        queue.removeTaskFormQueue(task.getKey());
        ALog.d(TAG, String.format("任务【%s】处理完成", task.getTaskName()));
        startNextTask(queue, task.getSchedulerType());
        break;
      case FAIL:
        handleFailTask(queue, task);
        break;
    }

    if (what == FAIL || what == CHECK_FAIL) {
      return;
    }

    if (what == CANCEL || what == COMPLETE) {
      TaskWrapperManager.getInstance().removeTaskWrapper(task.getTaskWrapper());
    } else {
      if (what != RUNNING) {
        TaskWrapperManager.getInstance().putTaskWrapper(task.getTaskWrapper());
      }
    }
    normalTaskCallback(what, task);
  }

  /**
   * 处理what为{@link #CHECK_FAIL}信息错误的任务
   *
   * @param taskType 任务类型
   */
  private void handlePreFailTask(int taskType) {
    startNextTask(getQueue(taskType), TaskSchedulerType.TYPE_DEFAULT);

    // 发送广播
    boolean canSend = mAriaConfig.getAConfig().isUseBroadcast();
    if (canSend) {
      Intent intent = new Intent(ISchedulers.ARIA_TASK_INFO_ACTION);
      intent.setPackage(mAriaConfig.getAPP().getPackageName());
      Bundle b = new Bundle();
      b.putInt(ISchedulers.TASK_TYPE, taskType);
      b.putInt(ISchedulers.TASK_STATE, ISchedulers.FAIL);
      mAriaConfig.getAPP().sendBroadcast(intent);
    }

    // 处理回调
    if (mObservers.size() > 0) {
      Set<String> keys = mObservers.keySet();
      for (String key : keys) {
        Map<TaskEnum, Object> listeners = mObservers.get(key);
        if (listeners == null || listeners.isEmpty()) {
          continue;
        }
        NormalTaskListenerInterface<TASK> listener = null;
        if (mObservers.get(key) != null) {
          if (taskType == ITask.DOWNLOAD) {
            listener = (NormalTaskListenerInterface<TASK>) listeners.get(TaskEnum.DOWNLOAD);
          } else if (taskType == ITask.DOWNLOAD_GROUP) {
            listener = (NormalTaskListenerInterface<TASK>) listeners.get(TaskEnum.DOWNLOAD_GROUP);
          } else if (taskType == ITask.DOWNLOAD_GROUP) {
            listener = (NormalTaskListenerInterface<TASK>) listeners.get(TaskEnum.UPLOAD);
          }
        }
        if (listener != null) {
          normalTaskCallback(ISchedulers.CHECK_FAIL, null, listener);
        }
      }
    }
  }

  /**
   * 回调
   *
   * @param state 状态
   */
  private void normalTaskCallback(int state, TASK task) {
    sendNormalBroadcast(state, task);
    if (mObservers.size() > 0) {
      Set<String> keys = mObservers.keySet();
      for (String key : keys) {
        Map<TaskEnum, Object> listeners = mObservers.get(key);
        if (listeners == null || listeners.isEmpty()) {
          continue;
        }
        NormalTaskListenerInterface<TASK> listener = null;
        if (mObservers.get(key) != null) {
          if (task instanceof DownloadTask) {
            listener = (NormalTaskListenerInterface<TASK>) listeners.get(TaskEnum.DOWNLOAD);
          } else if (task instanceof DownloadGroupTask) {
            listener = (NormalTaskListenerInterface<TASK>) listeners.get(TaskEnum.DOWNLOAD_GROUP);
          } else if (task instanceof UploadTask) {
            listener = (NormalTaskListenerInterface<TASK>) listeners.get(TaskEnum.UPLOAD);
          }
        }
        if (listener != null) {
          normalTaskCallback(state, task, listener);
        }
      }
    }
  }

  private void normalTaskCallback(int state, TASK task, NormalTaskListenerInterface<TASK> listener) {
    if (listener != null) {
      if (task == null && state != ISchedulers.CHECK_FAIL) {
        ALog.e(TAG, "TASK 为null，回调失败");
        return;
      }
      switch (state) {
        case WAIT:
          listener.onWait(task);
          break;
        case PRE:
          listener.onPre(task);
          break;
        case POST_PRE:
          listener.onTaskPre(task);
          break;
        case RUNNING:
          listener.onTaskRunning(task);
          break;
        case START:
          listener.onTaskStart(task);
          break;
        case STOP:
          listener.onTaskStop(task);
          break;
        case RESUME:
          listener.onTaskResume(task);
          break;
        case CANCEL:
          listener.onTaskCancel(task);
          break;
        case COMPLETE:
          listener.onTaskComplete(task);
          break;
        case FAIL:
          listener.onTaskFail(task.getTaskType() == ITask.TEMP ? null : task,
              (Exception) task.getExpand(AbsTask.ERROR_INFO_KEY));
          break;
        case CHECK_FAIL:
          listener.onTaskFail(null, null);
          break;
        case NO_SUPPORT_BREAK_POINT:
          listener.onNoSupportBreakPoint(task);
          break;
      }
    }
  }

  /**
   * 发送普通任务的广播
   */
  private void sendNormalBroadcast(int state, TASK task) {
    boolean canSend = mAriaConfig.getAConfig().isUseBroadcast();
    if (!canSend) {
      return;
    }
    int type = task.getTaskType();
    if (type == ITask.DOWNLOAD || type == ITask.DOWNLOAD_GROUP) {
      mAriaConfig.getAPP().sendBroadcast(
          createData(state, type, task.getTaskWrapper().getEntity()));
    } else if (type == ITask.UPLOAD) {
      mAriaConfig.getAPP().sendBroadcast(
          createData(state, type, task.getTaskWrapper().getEntity()));
    } else {
      ALog.w(TAG, "发送广播失败，没有对应的任务");
    }
  }

  /**
   * 创建广播发送的数据
   *
   * @param taskState 任务状态 {@link ISchedulers}
   * @param taskType 任务类型 {@link ITask}
   * @param entity 任务实体
   */
  private Intent createData(int taskState, int taskType, AbsEntity entity) {
    Intent intent = new Intent(ISchedulers.ARIA_TASK_INFO_ACTION);
    intent.setPackage(mAriaConfig.getAPP().getPackageName());
    Bundle b = new Bundle();
    b.putInt(ISchedulers.TASK_TYPE, taskType);
    b.putInt(ISchedulers.TASK_STATE, taskState);
    b.putLong(ISchedulers.TASK_SPEED, entity.getSpeed());
    b.putInt(ISchedulers.TASK_PERCENT, entity.getPercent());
    b.putParcelable(ISchedulers.TASK_ENTITY, entity);
    intent.putExtras(b);
    return intent;
  }

  /**
   * 处理下载任务下载失败的情形
   *
   * @param task 下载任务
   */
  private void handleFailTask(final ITaskQueue queue, final TASK task) {
    if (!task.isNeedRetry() || task.isStop() || task.isCancel()) {
      queue.removeTaskFormQueue(task.getKey());
      startNextTask(queue, task.getSchedulerType());
      normalTaskCallback(FAIL, task);
      return;
    }

    int num = task.getTaskWrapper().getConfig().getReTryNum();
    boolean isNotNetRetry = mAriaConfig.getAConfig().isNotNetRetry();

    if ((!NetUtils.isConnected(mAriaConfig.getAPP()) && !isNotNetRetry)
        || task.getTaskWrapper().getEntity().getFailNum() > num) {
      queue.removeTaskFormQueue(task.getKey());
      startNextTask(queue, task.getSchedulerType());
      TaskWrapperManager.getInstance().removeTaskWrapper(task.getTaskWrapper());
      normalTaskCallback(FAIL, task);
      return;
    }

    mFailureTaskHandler.offer(task);
  }

  /**
   * 启动下一个任务，条件：任务停止，取消下载，任务完成
   */
  void startNextTask(final ITaskQueue queue, int schedulerType) {
    if (schedulerType == TaskSchedulerType.TYPE_STOP_NOT_NEXT) {
      return;
    }
    TASK newTask = (TASK) queue.getNextTask();
    if (newTask == null) {
      if (queue.getCurrentExePoolNum() == 0) {
        ALog.i(TAG, "没有等待中的任务");
      }
      return;
    }
    if (newTask.getState() == IEntity.STATE_WAIT) {
      queue.startTask(newTask);
    }
  }
}
