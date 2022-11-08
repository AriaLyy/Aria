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

/**
 * 证书信息
 */
public class IdEntity {

  /**
   * 私钥证书路径
   */
  public String prvKey;

  /**
   * 私钥证书密码
   */
  public String prvPass;

  /**
   * 公钥证书路径
   */
  public String pubKey;

  /**
   * knowhost文件路径
   */
  public String knowHost;

  /**
   * ca 证书密码
   */
  public String storePass;

  /**
   * ca证书路径
   */
  public String storePath;

  /**
   * ca证书别名
   */
  public String keyAlias;
}
