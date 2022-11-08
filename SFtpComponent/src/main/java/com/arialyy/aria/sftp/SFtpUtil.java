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
import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.IdEntity;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.FileUtil;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * sftp工具类
 *
 * @author lyy
 */
public class SFtpUtil {
  private final String TAG = CommonUtil.getClassName(getClass());

  private static SFtpUtil INSTANCE;

  private SFtpUtil() {

  }

  public synchronized static SFtpUtil getInstance() {
    if (INSTANCE == null) {
      synchronized (SFtpUtil.class) {
        INSTANCE = new SFtpUtil();
      }
    }
    return INSTANCE;
  }

  /**
   * 创建jsch 的session
   *
   * @param threadId 线程id，默认0
   * @throws JSchException
   * @throws UnsupportedEncodingException
   */
  public Session getSession(FtpUrlEntity entity, int threadId) throws JSchException,
      UnsupportedEncodingException {

    JSch jSch = new JSch();

    IdEntity idEntity = entity.idEntity;

    if (idEntity.prvKey != null) {
      if (idEntity.pubKey == null) {
        jSch.addIdentity(idEntity.prvKey,
            entity.password == null ? null : idEntity.prvPass.getBytes("UTF-8"));
      } else {
        jSch.addIdentity(idEntity.prvKey, idEntity.pubKey,
            entity.password == null ? null : idEntity.prvPass.getBytes("UTF-8"));
      }
    }

    setKnowHost(jSch, entity);

    Session session;
    if (TextUtils.isEmpty(entity.user)) {
      session = jSch.getSession(null, entity.hostName, Integer.parseInt(entity.port));
    } else {
      session = jSch.getSession(entity.user, entity.hostName, Integer.parseInt(entity.port));
    }
    if (!TextUtils.isEmpty(entity.password)) {
      session.setPassword(entity.password);
    }
    Properties config = new Properties();

    // 不检查公钥，需要在connect之前配置，但是不安全，no 模式会自动将配对信息写入know_host文件
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);// 为Session对象设置properties
    session.setTimeout(5000);// 设置超时
    session.setIdentityRepository(jSch.getIdentityRepository());
    session.connect();
    SFtpSessionManager.getInstance().addSession(session, threadId);
    return session;
  }

  private void setKnowHost(JSch jSch, FtpUrlEntity entity) throws JSchException {
    IdEntity idEntity = entity.idEntity;
    if (idEntity.knowHost != null) {
      File knowFile = new File(idEntity.knowHost);
      if (!knowFile.exists()) {
        FileUtil.createFile(knowFile);
      }
      jSch.setKnownHosts(idEntity.knowHost);

      //HostKeyRepository hkr = jSch.getHostKeyRepository();
      //hkr.add(new HostKey(entity.hostName, HostKey.SSHRSA, getPubKey(idEntity.pubKey)), new JschUserInfo());
      //
      //HostKey[] hks = hkr.getHostKey();
      //if (hks != null) {
      //  System.out.println("Host keys in " + hkr.getKnownHostsRepositoryID());
      //  for (int i = 0; i < hks.length; i++) {
      //    HostKey hk = hks[i];
      //    System.out.println(hk.getHost() + " " +
      //        hk.getType() + " " +
      //        hk.getFingerPrint(jSch));
      //  }
      //}
    }
  }

  private byte[] getPubKey(String pubKeyPath) {
    try {
      File f = new File(pubKeyPath);
      FileInputStream fis = new FileInputStream(f);
      byte[] buf = new byte[(int) f.length()];
      int len = fis.read(buf);
      fis.close();
      return buf;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static class JschUserInfo implements UserInfo {

    @Override public String getPassphrase() {
      return null;
    }

    @Override public String getPassword() {
      return null;
    }

    @Override public boolean promptPassword(String message) {
      System.out.println(message);
      return true;
    }

    @Override public boolean promptPassphrase(String message) {
      System.out.println(message);
      return false;
    }

    @Override public boolean promptYesNo(String message) {
      System.out.println(message);
      return false;
    }

    @Override public void showMessage(String message) {
      System.out.println(message);
    }
  }
}
