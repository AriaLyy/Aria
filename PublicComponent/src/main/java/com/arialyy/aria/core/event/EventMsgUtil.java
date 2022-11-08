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
package com.arialyy.aria.core.event;

import com.arialyy.aria.util.ALog;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 消息发送工具
 */
public class EventMsgUtil {
  private static final String TAG = "EventUtil";
  private static EventMsgUtil defaultInstance;
  private Map<Object, List<EventMethodInfo>> mEventMethods =
      new ConcurrentHashMap<>();
  private ArrayBlockingQueue<Object> mEventQueue = new ArrayBlockingQueue<>(10);
  private ExecutorService mPool = Executors.newFixedThreadPool(5);

  private EventMsgUtil() {
    ExecutorService pool = Executors.newSingleThreadExecutor();
    pool.execute(new Runnable() {
      @Override public void run() {
        while (true) {
          try {
            Object info = mEventQueue.take();
            sendEvent(info);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    });
  }

  private void sendEvent(final Object param) {
    mPool.submit(new Runnable() {
      @Override public void run() {
        Set<Object> keys = mEventMethods.keySet();
        for (Object key : keys) {
          List<EventMethodInfo> list = mEventMethods.get(key);
          if (list != null && !list.isEmpty()) {
            for (EventMethodInfo info : list) {
              try {
                if (info.param == param.getClass()) {
                  Method method = key.getClass().getDeclaredMethod(info.methodName, info.param);
                  method.setAccessible(true);
                  method.invoke(key, param);
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
    });
  }

  public static EventMsgUtil getDefault() {
    if (defaultInstance == null) {
      synchronized (EventMsgUtil.class) {
        if (defaultInstance == null) {
          defaultInstance = new EventMsgUtil();
        }
      }
    }
    return defaultInstance;
  }

  /**
   * 注册事件
   */
  public void register(Object obj) {
    Method[] methods = obj.getClass().getDeclaredMethods();
    for (Method method : methods) {
      method.setAccessible(true);
      if (method.getAnnotation(Event.class) == null) {
        continue;
      }
      Class<?>[] clazz = method.getParameterTypes();
      if (clazz.length == 0 || clazz.length > 1) {
        ALog.e(TAG,
            String.format("%s.%s参数数量为0或参数数量大于1", obj.getClass().getName(), method.getName()));
        continue;
      }
      int modifier = method.getModifiers();
      if (Modifier.isStatic(modifier) || Modifier.isAbstract(modifier) || Modifier.isFinal(
          modifier)) {
        ALog.e(TAG, "注册的方法不能使用final、static、abstract修饰");
        continue;
      }

      EventMethodInfo methodInfo = new EventMethodInfo();
      methodInfo.methodName = method.getName();
      methodInfo.param = clazz[0];
      List<EventMethodInfo> list = mEventMethods.get(obj);
      if (list == null) {
        list = new ArrayList<>();
        mEventMethods.put(obj, list);
      }
      list.add(methodInfo);
    }
  }

  public void unRegister(Object obj) {
    for (Iterator<Map.Entry<Object, List<EventMethodInfo>>> iter =
        mEventMethods.entrySet().iterator(); iter.hasNext(); ) {
      Map.Entry<Object, List<EventMethodInfo>> entry = iter.next();
      if (entry.getKey().equals(obj)) {
        entry.getValue().clear();
        iter.remove();
      }
    }
  }

  /**
   * 发送事件，接收消息的方法需要使用{@link Event}注解
   */
  public void post(Object param) {

    synchronized (EventMsgUtil.class) {
      try {
        mEventQueue.offer(param, 2, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
