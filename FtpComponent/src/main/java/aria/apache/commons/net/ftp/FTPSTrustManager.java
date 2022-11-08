/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aria.apache.commons.net.ftp;

import aria.apache.commons.net.util.TrustManagerUtils;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Do not use.
 *
 * @since 2.0
 * @deprecated 3.0 use
 * {@link TrustManagerUtils#getValidateServerCertificateTrustManager()
 * TrustManagerUtils#getValidateServerCertificateTrustManager()} instead
 */
@Deprecated public class FTPSTrustManager implements X509TrustManager {
  private static final X509Certificate[] EMPTY_X509CERTIFICATE_ARRAY = new X509Certificate[] {};

  /**
   * No-op
   */
  @Override public void checkClientTrusted(X509Certificate[] certificates, String authType) {
    return;
  }

  @Override public void checkServerTrusted(X509Certificate[] certificates, String authType)
      throws CertificateException {
    for (X509Certificate certificate : certificates) {
      certificate.checkValidity();
    }
  }

  @Override public X509Certificate[] getAcceptedIssuers() {
    return EMPTY_X509CERTIFICATE_ARRAY;
  }
}
