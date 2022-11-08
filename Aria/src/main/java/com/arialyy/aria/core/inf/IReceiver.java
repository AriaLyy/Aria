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

/**
 * Created by lyy on 2017/2/6.
 */
public interface IReceiver {
  /**
   * Receiver 销毁
   */
  void destroy();

  /**
   * 注册
   */
  void register();

  /**
   * 移除观察者
   */
  void unRegister();

  /**
   * 观察者对象的类完整名称
   */
  String getTargetName();

  /**
   * 获取当前Receiver的key
   */
  String getKey();

  /**
   * 设置类型
   *
   * @return {@link ReceiverType}
   */
  ReceiverType getType();

  /**
   * 判断是否是fragment，如果是fragment，在activity销毁时，需要将其从receiver中移除
   */
  boolean isFragment();

}
