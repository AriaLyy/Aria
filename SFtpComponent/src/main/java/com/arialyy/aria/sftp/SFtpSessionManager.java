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
package com.arialyy.aria.sftp;

import android.text.TextUtils;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.jcraft.jsch.Session;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SFTP session 管理器
 * 1、管理session
 * 2、定时清除无效的session
 */
public class SFtpSessionManager {
  private String TAG = CommonUtil.getClassName(this);
  private static volatile SFtpSessionManager INSTANCE = null;
  private Map<String, Session> sessionDeque = new HashMap<>();

  public synchronized static SFtpSessionManager getInstance() {
    if (INSTANCE == null) {
      synchronized (SFtpSessionManager.class) {
        INSTANCE = new SFtpSessionManager();
      }
    }
    return INSTANCE;
  }

  private SFtpSessionManager() {

  }

  /**
   * 获取session，获取完成session后，检查map中的所有session，移除所有失效的session
   *
   * @param key md5(host + port + userName + threadId)
   * @return 如果session不可用，返回null
   */
  public Session getSession(String key) {
    if (TextUtils.isEmpty(key)) {
      ALog.e(TAG, "从缓存获取session失败，key为空");
      return null;
    }
    Session session = sessionDeque.get(key);
    if (session == null) {
      ALog.w(TAG, "从缓存获取session失败，key：" + key);
    }
    cleanIdleSession();
    return session;
  }

  /**
   * 添加session
   */
  public void addSession(Session session, int threadId) {
    if (session == null) {
      ALog.e(TAG, "添加session到管理器失败，session 为空");
      return;
    }
    String key =
        CommonUtil.getStrMd5(
            session.getHost() + session.getPort() + session.getUserName() + threadId);
    sessionDeque.put(key, session);
  }

  /**
   * 移除所有闲置的session，闲置条件：
   * 1、session 为空
   * 2、session未连接
   */
  private void cleanIdleSession() {
    if (sessionDeque.size() > 0) {
      Iterator<Map.Entry<String, Session>> i = sessionDeque.entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry<String, Session> entry = i.next();
        Session session = entry.getValue();
        if (session == null || !session.isConnected()) {
          i.remove();
        }
      }
    }
  }
}
