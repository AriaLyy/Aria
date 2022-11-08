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
import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.IdEntity;
import com.arialyy.aria.core.ProtocolType;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.ComponentUtil;

/**
 * Created by laoyuyu on 2018/3/9.
 */
public class SFtpOption extends BaseOption {

  private String charSet, userName, password;
  private boolean isNeedLogin = false;
  private FtpUrlEntity urlEntity;
  private String protocol;
  private IdEntity idEntity = new IdEntity();

  public SFtpOption() {
    super();
    ComponentUtil.getInstance().checkComponentExist(ComponentUtil.COMPONENT_TYPE_SFTP);
  }

  public SFtpOption charSet(String charSet) {
    if (TextUtils.isEmpty(charSet)) {
      throw new NullPointerException("字符编码为空");
    }
    this.charSet = charSet;
    return this;
  }

  public SFtpOption login(String userName, String password) {
    if (TextUtils.isEmpty(userName)) {
      ALog.e(TAG, "用户名不能为null");
      return this;
    } else if (TextUtils.isEmpty(password)) {
      ALog.e(TAG, "密码不能为null");
      return this;
    }
    this.userName = userName;
    this.password = password;
    isNeedLogin = true;
    return this;
  }

  /**
   * 设置协议类型
   *
   * @param protocol {@link ProtocolType}
   */
  public SFtpOption setProtocol(String protocol) {
    if (TextUtils.isEmpty(protocol)) {
      ALog.e(TAG, "设置协议失败，协议信息为空");
      return this;
    }
    this.protocol = protocol;
    return this;
  }

  /**
   * 设置私钥证书路径
   *
   * @param prvKey 证书路径
   */
  public SFtpOption setPrvKey(String prvKey) {
    if (TextUtils.isEmpty(prvKey)) {
      ALog.e(TAG, "设置私钥证书失败，证书内容为空");
      return this;
    }
    idEntity.prvKey = prvKey;
    return this;
  }

  /**
   * 设置私钥密码
   *
   * @param prvKeyPass 私钥密码
   */
  public SFtpOption setPrvKeyPass(String prvKeyPass) {
    if (TextUtils.isEmpty(prvKeyPass)) {
      ALog.e(TAG, "设置证书密码失败，证书密码为空");
      return this;
    }
    idEntity.prvPass = prvKeyPass;
    return this;
  }

  /**
   * 设置公钥证书
   *
   * @param pubKey 公钥证书内容
   */
  public SFtpOption setPubKey(String pubKey) {
    if (TextUtils.isEmpty(pubKey)) {
      ALog.e(TAG, "设置公钥失败，证书内容为空");
      return this;
    }
    idEntity.pubKey = pubKey;
    return this;
  }

  public SFtpOption setKnowHostPath(String knowHostPath) {
    if (TextUtils.isEmpty(knowHostPath)) {
      ALog.e(TAG, "knowhost 文件路径为空");
      return this;
    }
    idEntity.knowHost = knowHostPath;
    return this;
  }

  public void setUrlEntity(FtpUrlEntity urlEntity) {
    this.urlEntity = urlEntity;
    urlEntity.needLogin = isNeedLogin;
    urlEntity.user = userName;
    urlEntity.password = password;
    urlEntity.idEntity = idEntity;
  }
}