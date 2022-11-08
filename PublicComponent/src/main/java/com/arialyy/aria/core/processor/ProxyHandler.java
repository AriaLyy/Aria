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
package com.arialyy.aria.core.processor;

import com.arialyy.aria.core.inf.IEventHandler;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author lyy
 * Date: 2019-10-7
 * 处理器的动态代理
 */
public class ProxyHandler implements InvocationHandler {

  private Object mTarget;

  /**
   * 绑定代理对象并返回代理类
   */
  public Object bind(Class<? extends IEventHandler> clazz) {
    //绑定该类实现的所有接口，取得代理类
    try {
      mTarget = clazz.newInstance();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
    return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return method.invoke(mTarget, args);
  }
}
