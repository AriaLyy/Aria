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

import android.util.SparseArray;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by laoyuyu on 2018/3/22.
 * Delegate管理器
 */
class DelegateManager {
  private final String TAG = "ModuleFactory";

  private SparseArray<AbsDelegate> mDelegates = new SparseArray<>();
  private static volatile DelegateManager INSTANCE = null;

  private DelegateManager() {

  }

  static DelegateManager getInstance() {
    if (INSTANCE == null) {
      synchronized (DelegateManager.class) {
        INSTANCE = new DelegateManager();
      }
    }
    return INSTANCE;
  }

  /**
   * 获取Module
   */
  <M extends AbsDelegate> M getDelegate(Class<M> clazz) {
    M delegate = (M) mDelegates.get(clazz.hashCode());
    try {
      if (delegate == null) {
        Constructor c = clazz.getDeclaredConstructor();
        c.setAccessible(true);
        delegate = (M) c.newInstance();
        mDelegates.put(clazz.hashCode(), delegate);
        return delegate;
      }
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return delegate;
  }
}
