package com.arialyy.aria.core.inf;

public interface TaskSchedulerType {
  int TYPE_DEFAULT = 1;
  /**
   * 停止当前任务并且不自动启动下一任务
   */
  int TYPE_STOP_NOT_NEXT = 2;
  /**
   * 停止任务并让当前任务处于等待状态
   */
  int TYPE_STOP_AND_WAIT = 3;

  /**
   * 删除任务并且不通知回调
   */
  int TYPE_CANCEL_AND_NOT_NOTIFY = 4;

  /**
   * 重置状态并启动任务
   */
  int TYPE_START_AND_RESET_STATE = 5;
}