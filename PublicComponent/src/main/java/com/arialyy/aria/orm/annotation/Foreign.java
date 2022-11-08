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
package com.arialyy.aria.orm.annotation;

import com.arialyy.aria.orm.ActionPolicy;
import com.arialyy.aria.orm.DbEntity;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by AriaL on 2017/7/4.
 * 外键约束
 */
@Target(ElementType.FIELD) @Retention(RetentionPolicy.RUNTIME) public @interface Foreign {

  /**
   * 关联的表
   */
  Class<? extends DbEntity> parent();

  /**
   * 父表对应的列名
   */
  String column();

  /**
   * ON UPDATE 约束
   */
  ActionPolicy onUpdate() default ActionPolicy.NO_ACTION;

  /**
   * ON DELETE 约束
   */
  ActionPolicy onDelete() default ActionPolicy.NO_ACTION;
}
