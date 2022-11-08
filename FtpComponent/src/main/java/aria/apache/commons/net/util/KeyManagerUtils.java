/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package aria.apache.commons.net.util;

import aria.apache.commons.net.io.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509ExtendedKeyManager;

/**
 * General KeyManager utilities
 * <p>
 * How to use with a client certificate:
 * <pre>
 * KeyManager km = KeyManagerUtils.createClientKeyManager("JKS",
 *     "/path/to/privatekeystore.jks","storepassword",
 *     "privatekeyalias", "keypassword");
 * FTPSClient cl = new FTPSClient();
 * cl.setKeyManager(km);
 * cl.connect(...);
 * </pre>
 * If using the default store type and the key password is the same as the
 * store password, these parameters can be omitted. <br>
 * If the desired key is the first or only key in the keystore, the keyAlias parameter
 * can be omitted, in which case the code becomes:
 * <pre>
 * KeyManager km = KeyManagerUtils.createClientKeyManager(
 *     "/path/to/privatekeystore.jks","storepassword");
 * FTPSClient cl = new FTPSClient();
 * cl.setKeyManager(km);
 * cl.connect(...);
 * </pre>
 *
 * @since 3.0
 */
public final class KeyManagerUtils {

  private static final String DEFAULT_STORE_TYPE = KeyStore.getDefaultType();

  private KeyManagerUtils() {
    // Not instantiable
  }

  /**
   * Create a client key manager which returns a particular key.
   * Does not handle server keys.
   *
   * @param ks the keystore to use
   * @param keyAlias the alias of the key to use, may be {@code null} in which case the first key
   * entry alias is used
   * @param keyPass the password of the key to use
   * @return the customised KeyManager
   * @throws GeneralSecurityException if there is a problem creating the keystore
   */
  public static KeyManager createClientKeyManager(KeyStore ks, String keyAlias, String keyPass)
      throws GeneralSecurityException {
    ClientKeyStore cks =
        new ClientKeyStore(ks, keyAlias != null ? keyAlias : findAlias(ks), keyPass);
    return new X509KeyManager(cks);
  }

  /**
   * Create a client key manager which returns a particular key.
   * Does not handle server keys.
   *
   * @param storeType the type of the keyStore, e.g. "JKS"
   * @param storePath the path to the keyStore
   * @param storePass the keyStore password
   * @param keyAlias the alias of the key to use, may be {@code null} in which case the first key
   * entry alias is used
   * @param keyPass the password of the key to use
   * @return the customised KeyManager
   * @throws GeneralSecurityException if there is a problem creating the keystore
   * @throws IOException if there is a problem creating the keystore
   */
  public static KeyManager createClientKeyManager(String storeType, File storePath,
      String storePass, String keyAlias, String keyPass)
      throws IOException, GeneralSecurityException {
    KeyStore ks = loadStore(storeType, storePath, storePass);
    return createClientKeyManager(ks, keyAlias, keyPass);
  }

  /**
   * Create a client key manager which returns a particular key.
   * Does not handle server keys.
   * Uses the default store type and assumes the key password is the same as the store password
   *
   * @param storePath the path to the keyStore
   * @param storePass the keyStore password
   * @param keyAlias the alias of the key to use, may be {@code null} in which case the first key
   * entry alias is used
   * @return the customised KeyManager
   * @throws IOException if there is a problem creating the keystore
   * @throws GeneralSecurityException if there is a problem creating the keystore
   */
  public static KeyManager createClientKeyManager(File storePath, String storePass, String keyAlias)
      throws IOException, GeneralSecurityException {
    return createClientKeyManager(DEFAULT_STORE_TYPE, storePath, storePass, keyAlias, storePass);
  }

  /**
   * Create a client key manager which returns a particular key.
   * Does not handle server keys.
   * Uses the default store type and assumes the key password is the same as the store password.
   * The key alias is found by searching the keystore for the first private key entry
   *
   * @param storePath the path to the keyStore
   * @param storePass the keyStore password
   * @return the customised KeyManager
   * @throws IOException if there is a problem creating the keystore
   * @throws GeneralSecurityException if there is a problem creating the keystore
   */
  public static KeyManager createClientKeyManager(File storePath, String storePass)
      throws IOException, GeneralSecurityException {
    return createClientKeyManager(DEFAULT_STORE_TYPE, storePath, storePass, null, storePass);
  }

  private static KeyStore loadStore(String storeType, File storePath, String storePass)
      throws KeyStoreException, IOException, GeneralSecurityException {
    KeyStore ks = KeyStore.getInstance(storeType);
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(storePath);
      ks.load(stream, storePass.toCharArray());
    } finally {
      Util.closeQuietly(stream);
    }
    return ks;
  }

  private static String findAlias(KeyStore ks) throws KeyStoreException {
    Enumeration<String> e = ks.aliases();
    while (e.hasMoreElements()) {
      String entry = e.nextElement();
      if (ks.isKeyEntry(entry)) {
        return entry;
      }
    }
    throw new KeyStoreException("Cannot find a private key entry");
  }

  private static class ClientKeyStore {

    private final X509Certificate[] certChain;
    private final PrivateKey key;
    private final String keyAlias;

    ClientKeyStore(KeyStore ks, String keyAlias, String keyPass) throws GeneralSecurityException {
      this.keyAlias = keyAlias;
      this.key = (PrivateKey) ks.getKey(this.keyAlias, keyPass.toCharArray());
      Certificate[] certs = ks.getCertificateChain(this.keyAlias);
      X509Certificate[] X509certs = new X509Certificate[certs.length];
      for (int i = 0; i < certs.length; i++) {
        X509certs[i] = (X509Certificate) certs[i];
      }
      this.certChain = X509certs;
    }

    final X509Certificate[] getCertificateChain() {
      return this.certChain;
    }

    final PrivateKey getPrivateKey() {
      return this.key;
    }

    final String getAlias() {
      return this.keyAlias;
    }
  }

  private static class X509KeyManager extends X509ExtendedKeyManager {

    private final ClientKeyStore keyStore;

    X509KeyManager(final ClientKeyStore keyStore) {
      this.keyStore = keyStore;
    }

    // Call sequence: 1
    @Override public String chooseClientAlias(String[] keyType, Principal[] issuers,
        Socket socket) {
      return keyStore.getAlias();
    }

    // Call sequence: 2
    @Override public X509Certificate[] getCertificateChain(String alias) {
      return keyStore.getCertificateChain();
    }

    @Override public String[] getClientAliases(String keyType, Principal[] issuers) {
      return new String[] { keyStore.getAlias() };
    }

    // Call sequence: 3
    @Override public PrivateKey getPrivateKey(String alias) {
      return keyStore.getPrivateKey();
    }

    @Override public String[] getServerAliases(String keyType, Principal[] issuers) {
      return null;
    }

    @Override public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
      return null;
    }
  }
}
