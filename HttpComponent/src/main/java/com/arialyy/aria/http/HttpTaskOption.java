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

package com.arialyy.aria.http;

import android.text.TextUtils;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.inf.ITaskOption;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;
import com.arialyy.aria.core.processor.IHttpFileNameAdapter;

import java.lang.ref.SoftReference;
import java.net.CookieManager;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Http任务设置的信息，如：cookie、请求参数
 */
public final class HttpTaskOption implements ITaskOption {

  private CookieManager cookieManager;

  /**
   * 请求参数
   */
  private Map<String, String> params;

  /**
   * http 请求头
   */
  private Map<String, String> headers = new HashMap<>();

  /**
   * 字符编码，默认为"utf-8"
   */
  private String charSet = "utf-8";

  /**
   * 网络请求类型
   */
  private RequestEnum requestEnum = RequestEnum.GET;

  /**
   * 是否使用服务器通过content-disposition传递的文件名，内容格式{@code attachment; filename="filename.jpg"} {@code true}
   * 使用
   */
  private boolean useServerFileName = false;

  /**
   * 重定向链接
   */
  private String redirectUrl = "";

  /**
   * 是否是chunk模式
   */
  private boolean isChunked = false;
  /**
   * 文件上传需要的key
   */
  private String attachment;

  private Proxy proxy;
  /**
   * 文件上传表单
   */
  private Map<String, String> formFields = new HashMap<>();

  private SoftReference<IHttpFileLenAdapter> fileLenAdapter;

  private SoftReference<IHttpFileNameAdapter> fileNameAdapter;

  public IHttpFileLenAdapter getFileLenAdapter() {
    return fileLenAdapter == null ? null : fileLenAdapter.get();
  }
  public IHttpFileNameAdapter getFileNameAdapter() {
    return fileNameAdapter == null ? null : fileNameAdapter.get();
  }
  /**
   * 如果是匿名内部类，完成后需要将adapter设置为空，否则会出现内存泄漏
   */
  public void setFileLenAdapter(IHttpFileLenAdapter fileLenAdapter) {
    this.fileLenAdapter = new SoftReference<>(fileLenAdapter);
  }
  public void setFileNameAdapter(IHttpFileNameAdapter fileNameAdapter) {
    this.fileNameAdapter = new SoftReference<>(fileNameAdapter);
  }
  public Map<String, String> getFormFields() {
    return formFields;
  }

  public void setFormFields(Map<String, String> formFields) {
    this.formFields = formFields;
  }

  public String getAttachment() {
    return TextUtils.isEmpty(attachment) ? "file" : attachment;
  }

  public void setAttachment(String attachment) {
    this.attachment = attachment;
  }

  public boolean isChunked() {
    return isChunked;
  }

  public void setChunked(boolean chunked) {
    isChunked = chunked;
  }

  public CookieManager getCookieManager() {
    return cookieManager;
  }

  public void setCookieManager(CookieManager cookieManager) {
    this.cookieManager = cookieManager;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getCharSet() {
    return TextUtils.isEmpty(charSet) ? "utf-8" : charSet;
  }

  public void setCharSet(String charSet) {
    this.charSet = charSet;
  }

  public RequestEnum getRequestEnum() {
    return requestEnum;
  }

  public void setRequestEnum(RequestEnum requestEnum) {
    this.requestEnum = requestEnum;
  }

  public boolean isUseServerFileName() {
    return useServerFileName;
  }

  public void setUseServerFileName(boolean useServerFileName) {
    this.useServerFileName = useServerFileName;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }

}
