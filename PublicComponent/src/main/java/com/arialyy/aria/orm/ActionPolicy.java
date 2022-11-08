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
package com.arialyy.aria.orm;

import com.arialyy.aria.orm.annotation.Foreign;

/**
 * Created by laoyuyu on 2018/3/22.
 * on update 或 on delete 都可跟不同action功能
 *
 * @see <a href="https://sqlite.org/foreignkeys.html"></a>
 * {@link Foreign#onDelete()}、{@link Foreign#onUpdate()}
 */
public enum ActionPolicy {

  /**
   * 如果子表中有匹配的记录,则不允许对父表对应候选键进行update/delete操作
   */
  NO_ACTION("NO ACTION"),

  /**
   * 和NO ACTION 作用一致，和NO ACTION的区别是：
   * 主表update/delete执行时，马上就触发约束；
   * 而NO ACTION 是执行完成语句后才触发约束，
   */
  RESTRICT("RESTRICT"),

  /**
   * 在父表上update/delete记录时，将子表上匹配记录的列设为null (要注意子表的外键列不能为not null)
   */
  SET_NULL("SET NULL"),

  /**
   * 父表有变更时,子表将外键列设置成一个默认的值，default配置的值
   */
  SET_DEFAULT("SET ERROR"),

  /**
   * 在父表上update/delete记录时，同步update/delete掉子表的匹配记录
   */
  CASCADE("CASCADE");

  String function;

  ActionPolicy(String function) {
    this.function = function;
  }

}
