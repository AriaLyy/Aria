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
package com.arialyy.aria.core;

import com.arialyy.aria.core.common.BaseOption;
import com.arialyy.aria.core.inf.IEventHandler;
import com.arialyy.aria.core.inf.IOptionConstant;
import com.arialyy.aria.core.processor.FtpInterceptHandler;
import com.arialyy.aria.core.processor.IBandWidthUrlConverter;
import com.arialyy.aria.core.processor.IFtpUploadInterceptor;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;
import com.arialyy.aria.core.processor.IHttpFileNameAdapter;
import com.arialyy.aria.core.processor.IKeyUrlConverter;
import com.arialyy.aria.core.processor.ILiveTsUrlConverter;
import com.arialyy.aria.core.processor.ITsMergeHandler;
import com.arialyy.aria.core.processor.IVodTsUrlConverter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务配置参数
 *
 * @author lyy
 * Date: 2019-09-10
 */
public class TaskOptionParams {

  private static List<Class> PROCESSORES = new ArrayList<>();

  /**
   * 普通参数
   */
  private Map<String, Object> params = new HashMap<>();

  /**
   * 事件处理对象
   */
  private Map<String, IEventHandler> handler = new HashMap<>();

  static {
    PROCESSORES.add(FtpInterceptHandler.class);
    PROCESSORES.add(IBandWidthUrlConverter.class);
    PROCESSORES.add(IFtpUploadInterceptor.class);
    PROCESSORES.add(IHttpFileLenAdapter.class);
    PROCESSORES.add(IHttpFileNameAdapter.class);
    PROCESSORES.add(ILiveTsUrlConverter.class);
    PROCESSORES.add(ITsMergeHandler.class);
    PROCESSORES.add(IVodTsUrlConverter.class);
    PROCESSORES.add(IKeyUrlConverter.class);
  }

  /**
   * 设置任务参数
   *
   * @param option 任务配置
   */
  public void setParams(BaseOption option) {
    List<Field> fields = CommonUtil.getAllFields(option.getClass());

    for (Field field : fields) {
      field.setAccessible(true);
      try {

        if (PROCESSORES.contains(field.getType())) {
          Object eventHandler = field.get(option);
          if (eventHandler != null) {
            setObjs(field.getName(), (IEventHandler) eventHandler);
          }
        } else {
          Object params = field.get(option);
          if (params != null) {
            setParams(field.getName(), params);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 设置普通参数
   *
   * @param key {@link IOptionConstant}
   */
  public TaskOptionParams setParams(String key, Object value) {
    params.put(key, value);
    return this;
  }

  /**
   * 设置对象参数
   */
  public TaskOptionParams setObjs(String key, IEventHandler handler) {
    this.handler.put(key, handler);
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public Object getParam(String key) {
    return params.get(key);
  }

  public IEventHandler getHandler(String key) {
    return handler.get(key);
  }

  public Map<String, IEventHandler> getHandler() {
    return handler;
  }
}
