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
package com.arialyy.aria.core.common;

import android.text.TextUtils;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;
import com.arialyy.aria.core.processor.IHttpFileNameAdapter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP任务设置
 */
public class HttpOption extends BaseOption {

  private Map<String, String> params;
  private Map<String, String> headers;
  private RequestEnum requestEnum = RequestEnum.GET;
  private Map<String, String> formFields;
  private Proxy proxy;
  private boolean useServerFileName = false;
  private IHttpFileLenAdapter fileLenAdapter;
  private IHttpFileNameAdapter fileNameAdapter;
  private String attachment;

  public HttpOption() {
    super();
  }

  /**
   * 设置请求类型
   *
   * @param requestEnum {@link RequestEnum}
   */
  public HttpOption setRequestType(RequestEnum requestEnum) {
    this.requestEnum = requestEnum;
    return this;
  }

  /**
   * 设置http请求参数
   */
  public HttpOption setParams(Map<String, String> params) {
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    this.params.putAll(params);
    return this;
  }

  /**
   * 设置http请求参数
   */
  public HttpOption setParam(String key, String value) {
    if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
      ALog.d(TAG, "key 或value 为空");
      return this;
    }
    if (params == null) {
      params = new HashMap<>();
    }
    params.put(key, value);
    return this;
  }

  /**
   * 设置http表单字段
   */
  public HttpOption setFormFields(Map<String, String> formFields) {
    this.formFields = formFields;
    return this;
  }

  /**
   * 设置文件上传需要的key
   *
   * @param attachment 如果为空，默认为"file"
   */
  public HttpOption setAttachment(String attachment) {
    if (TextUtils.isEmpty(attachment)) {
      attachment = "file";
    }
    this.attachment = attachment;
    return this;
  }

  /**
   * 给url请求添加Header数据
   * 如果新的header数据和数据保存的不一致，则更新数据库中对应的header数据
   *
   * @param key header对应的key
   * @param value header对应的value
   */
  public HttpOption addHeader(String key, String value) {
    if (TextUtils.isEmpty(key)) {
      ALog.w(TAG, "设置header失败，header对应的key不能为null");
      return this;
    } else if (TextUtils.isEmpty(value)) {
      ALog.w(TAG, "设置header失败，header对应的value不能为null");
      return this;
    }
    if (this.headers == null) {
      this.headers = new HashMap<>();
    }
    this.headers.put(key, value);
    return this;
  }

  /**
   * 给url请求添加一组header数据
   * 如果新的header数据和数据保存的不一致，则更新数据库中对应的header数据
   *
   * @param headers 一组http header数据
   */
  public HttpOption addHeaders(Map<String, String> headers) {
    if (headers.size() == 0) {
      ALog.w(TAG, "设置header失败，map没有header数据");
      return this;
    }
    if (this.headers == null) {
      this.headers = new HashMap<>();
    }
    this.headers.putAll(headers);
    return this;
  }

  /**
   * 设置代理
   */
  public HttpOption setUrlProxy(Proxy proxy) {
    this.proxy = proxy;
    return this;
  }

  /**
   * 是否使用服务器通过content-disposition传递的文件名，内容格式{@code attachment;filename=***}
   * 如果获取不到服务器文件名，则使用用户设置的文件名
   *
   * @param use {@code true} 使用
   */
  public HttpOption useServerFileName(boolean use) {
    this.useServerFileName = use;
    return this;
  }

  /**
   * 如果你需要使用header中特定的key来设置文件长度，或有定制文件长度的需要，那么你可以通过该方法自行处理文件长度
   */
  public HttpOption setFileLenAdapter(IHttpFileLenAdapter fileLenAdapter) {
    if (fileLenAdapter == null) {
      throw new IllegalArgumentException("adapter为空");
    }
    CheckUtil.checkMemberClass(fileLenAdapter.getClass());
    this.fileLenAdapter = fileLenAdapter;
    return this;
  }
  public HttpOption setFilNameAdapter(IHttpFileNameAdapter fileNameAdapter) {
    if (fileNameAdapter == null) {
      throw new IllegalArgumentException("adapter为空");
    }
    CheckUtil.checkMemberClass(fileNameAdapter.getClass());
    this.fileNameAdapter = fileNameAdapter;
    return this;
  }
}
