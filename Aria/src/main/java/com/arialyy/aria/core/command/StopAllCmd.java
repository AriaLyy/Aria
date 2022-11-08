package com.arialyy.aria.core.command;

import com.arialyy.aria.core.wrapper.AbsTaskWrapper;

/**
 * Created by AriaL on 2017/6/13.
 * 停止所有任务的命令，并清空所有等待队列
 */
final class StopAllCmd<T extends AbsTaskWrapper> extends AbsNormalCmd<T> {
  StopAllCmd(T entity, int taskType) {
    super(entity, taskType);
  }

  @Override public void executeCmd() {
    stopAll();
  }
}
