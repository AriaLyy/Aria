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
package com.arialyy.aria.util;

import android.text.TextUtils;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.ProtocolType;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.WeakHashMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by lyy on 2017/1/11.
 * SSL证书工具
 */
public class SSLContextUtil {
  private static final String TAG = "SSLContextUtil";
  private static Map<String, SSLContext> SSL_CACHE = new WeakHashMap<>();

  /**
   * 从assets目录下加载证书
   *
   * @param caAlias CA证书别名
   * @param caPath 保存在assets目录下的CA证书完整路径
   * @param protocol 连接协议
   */
  public static SSLContext getSSLContextFromAssets(String caAlias, String caPath, String protocol) {
    if (TextUtils.isEmpty(caAlias) || TextUtils.isEmpty(caPath)) {
      return null;
    }
    try {
      String cacheKey = getCacheKey(caAlias, caPath);
      SSLContext sslContext = SSL_CACHE.get(cacheKey);
      if (sslContext != null) {
        return sslContext;
      }
      InputStream caInput = AriaConfig.getInstance().getAPP().getAssets().open(caPath);
      Certificate ca = loadCert(caInput);
      return createContext(caAlias, ca, protocol, cacheKey);
    } catch (IOException | CertificateException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 颁发服务器证书的 CA 未知
   *
   * @param caAlias CA证书别名
   * @param caPath CA证书路径
   * @param protocol 连接协议
   */
  public static SSLContext getSSLContext(String caAlias, String caPath, String protocol) {
    if (TextUtils.isEmpty(caAlias) || TextUtils.isEmpty(caPath)) {
      return null;
    }
    try {
      String cacheKey = getCacheKey(caAlias, caPath);
      SSLContext sslContext = SSL_CACHE.get(cacheKey);
      if (sslContext != null) {
        return sslContext;
      }
      Certificate ca = loadCert(new FileInputStream(caPath));
      return createContext(caAlias, ca, protocol, cacheKey);
    } catch (CertificateException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @param cacheKey 别名 + 证书路径，然后取md5
   */
  private static SSLContext createContext(String caAlias, Certificate ca, String protocol,
      String cacheKey) {
    try {
      String keyStoreType = KeyStore.getDefaultType();
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      keyStore.load(null, null);
      keyStore.setCertificateEntry(caAlias, ca);

      // Create a TrustManager that trusts the CAs in our KeyStore
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(keyStore);
      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore, null);

      // Create an SSLContext that uses our TrustManager
      SSLContext context =
          SSLContext.getInstance(TextUtils.isEmpty(protocol) ? ProtocolType.Default : protocol);
      context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
      SSL_CACHE.put(cacheKey, context);
      return context;
    } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException | KeyManagementException | UnrecoverableKeyException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String getCacheKey(String alias, String path) {
    return CommonUtil.getStrMd5(String.format("%s_%s", alias, path));
  }

  /**
   * 加载CA证书
   *
   * @param is CA证书文件流
   * @throws CertificateException
   */
  private static Certificate loadCert(InputStream is) throws CertificateException, IOException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    Certificate ca = cf.generateCertificate(is);
    ALog.d(TAG, String.format("ca【%s】", ((X509Certificate) ca).getSubjectDN()));
    is.close();
    return ca;
  }

  /**
   * 服务器证书不是由 CA 签署的，而是自签署时，获取默认的SSL
   */
  public static SSLContext getDefaultSLLContext(String protocol) {
    SSLContext sslContext = null;
    try {
      sslContext =
          SSLContext.getInstance(TextUtils.isEmpty(protocol) ? ProtocolType.Default : protocol);
      sslContext.init(null, new TrustManager[] { trustManagers }, new SecureRandom());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sslContext;
  }

  /**
   * 创建自己的 TrustManager，这次直接信任服务器证书。这种方法具有前面所述的将应用与证书直接关联的所有弊端，但可以安全地操作。
   */
  private static TrustManager trustManagers = new X509TrustManager() {

    @Override public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
    }

    @Override public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {

    }

    @Override public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  };

  public static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  };
}
