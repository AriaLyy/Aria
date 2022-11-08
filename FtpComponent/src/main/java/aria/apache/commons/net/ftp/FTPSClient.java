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

import aria.apache.commons.net.SocketClient;
import aria.apache.commons.net.util.KeyManagerUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import aria.apache.commons.net.util.Base64;
import aria.apache.commons.net.util.SSLContextUtils;
import aria.apache.commons.net.util.SSLSocketUtils;
import aria.apache.commons.net.util.TrustManagerUtils;

/**
 * FTP over SSL processing. If desired, the JVM property -Djavax.net.debug=all can be used to
 * see wire-level SSL details.
 *
 * Warning: the hostname is not verified against the certificate by default, use
 * {@link #setHostnameVerifier(HostnameVerifier)} or {@link #setEndpointCheckingEnabled(boolean)}
 * (on Java 1.7+) to enable verification. Verification is only performed on client mode connections.
 *
 * @version $Id: FTPSClient.java 1747829 2016-06-11 00:57:57Z sebb $
 * @since 2.0
 */
public class FTPSClient extends FTPClient {

  // From http://www.iana.org/assignments/port-numbers

  //    ftps-data   989/tcp    ftp protocol, data, over TLS/SSL
  //    ftps-data   989/udp    ftp protocol, data, over TLS/SSL
  //    ftps        990/tcp    ftp protocol, control, over TLS/SSL
  //    ftps        990/udp    ftp protocol, control, over TLS/SSL

  public static final int DEFAULT_FTPS_DATA_PORT = 989;
  public static final int DEFAULT_FTPS_PORT = 990;

  /** The value that I can set in PROT command  (C = Clear, P = Protected) */
  private static final String[] PROT_COMMAND_VALUE = { "C", "E", "S", "P" };
  /** Default PROT Command */
  private static final String DEFAULT_PROT = "C";
  /** Default secure socket protocol name, i.e. TLS */
  private static final String DEFAULT_PROTOCOL = "TLS";

  /** The AUTH (Authentication/Security Mechanism) command. */
  private static final String CMD_AUTH = "AUTH";
  /** The ADAT (Authentication/Security Data) command. */
  private static final String CMD_ADAT = "ADAT";
  /** The PROT (Data Channel Protection Level) command. */
  private static final String CMD_PROT = "PROT";
  /** The PBSZ (Protection Buffer Size) command. */
  private static final String CMD_PBSZ = "PBSZ";
  /** The MIC (Integrity Protected Command) command. */
  private static final String CMD_MIC = "MIC";
  /** The CONF (Confidentiality Protected Command) command. */
  private static final String CMD_CONF = "CONF";
  /** The ENC (Privacy Protected Command) command. */
  private static final String CMD_ENC = "ENC";
  /** The CCC (Clear Command Channel) command. */
  private static final String CMD_CCC = "CCC";

  /** The security mode. (True - Implicit Mode / False - Explicit Mode) */
  private final boolean isImplicit;
  /** The secure socket protocol to be used, e.g. SSL/TLS. */
  private final String protocol;
  /** The AUTH Command value */
  private String auth = DEFAULT_PROTOCOL;
  /** The context object. */
  private SSLContext context;
  /** The socket object. */
  private Socket plainSocket;
  /** Controls whether a new SSL session may be established by this socket. Default true. */
  private boolean isCreation = true;
  /** The use client mode flag. */
  private boolean isClientMode = true;
  /** The need client auth flag. */
  private boolean isNeedClientAuth = false;
  /** The want client auth flag. */
  private boolean isWantClientAuth = false;
  /** The cipher suites */
  private String[] suites = null;
  /** The protocol versions */
  private String[] protocols = null;

  /**
   * The FTPS {@link TrustManager} implementation, default validate only
   * {@link TrustManagerUtils#getValidateServerCertificateTrustManager()}.
   */
  private TrustManager trustManager = TrustManagerUtils.getValidateServerCertificateTrustManager();

  /** The {@link KeyManager}, default null (i.e. use system default). */
  private KeyManager keyManager = null;

  /** The {@link HostnameVerifier} to use post-TLS, default null (i.e. no verification). */
  private HostnameVerifier hostnameVerifier = null;

  /** Use Java 1.7+ HTTPS Endpoint Identification Algorithim. */
  private boolean tlsEndpointChecking;

  /**
   * Constructor for FTPSClient, calls {@link #FTPSClient(String, boolean)}.
   *
   * Sets protocol to {@link #DEFAULT_PROTOCOL} - i.e. TLS - and security mode to explicit
   * (isImplicit = false)
   */
  public FTPSClient() {
    this(DEFAULT_PROTOCOL, false);
  }

  /**
   * Constructor for FTPSClient, using {@link #DEFAULT_PROTOCOL} - i.e. TLS
   * Calls {@link #FTPSClient(String, boolean)}
   *
   * @param isImplicit The security mode (Implicit/Explicit).
   */
  public FTPSClient(boolean isImplicit) {
    this(DEFAULT_PROTOCOL, isImplicit);
  }

  /**
   * Constructor for FTPSClient, using explict mode, calls {@link #FTPSClient(String, boolean)}.
   *
   * @param protocol the protocol to use
   */
  public FTPSClient(String protocol) {
    this(protocol, false);
  }

  /**
   * Constructor for FTPSClient allowing specification of protocol
   * and security mode. If isImplicit is true, the port is set to
   * {@link #DEFAULT_FTPS_PORT} i.e. 990.
   * The default TrustManager is set from {@link TrustManagerUtils#getValidateServerCertificateTrustManager()}
   *
   * @param protocol the protocol
   * @param isImplicit The security mode(Implicit/Explicit).
   */
  public FTPSClient(String protocol, boolean isImplicit) {
    super();
    this.protocol = protocol;
    this.isImplicit = isImplicit;
    if (isImplicit) {
      setDefaultPort(DEFAULT_FTPS_PORT);
    }
  }

  /**
   * Constructor for FTPSClient, using {@link #DEFAULT_PROTOCOL} - i.e. TLS
   * The default TrustManager is set from {@link TrustManagerUtils#getValidateServerCertificateTrustManager()}
   *
   * @param isImplicit The security mode(Implicit/Explicit).
   * @param context A pre-configured SSL Context
   */
  public FTPSClient(boolean isImplicit, SSLContext context) {
    this(DEFAULT_PROTOCOL, isImplicit);
    this.context = context;
  }

  /**
   * Constructor for FTPSClient, using {@link #DEFAULT_PROTOCOL} - i.e. TLS
   * and isImplicit {@code false}
   * Calls {@link #FTPSClient(boolean, SSLContext)}
   *
   * @param context A pre-configured SSL Context
   */
  public FTPSClient(SSLContext context) {
    this(false, context);
  }

  /**
   * Set AUTH command use value.
   * This processing is done before connected processing.
   *
   * @param auth AUTH command use value.
   */
  public void setAuthValue(String auth) {
    this.auth = auth;
  }

  /**
   * Return AUTH command use value.
   *
   * @return AUTH command use value.
   */
  public String getAuthValue() {
    return this.auth;
  }

  /**
   * Because there are so many connect() methods,
   * the _connectAction_() method is provided as a means of performing
   * some action immediately after establishing a connection,
   * rather than reimplementing all of the connect() methods.
   *
   * @throws IOException If it throw by _connectAction_.
   * @see SocketClient#_connectAction_()
   */
  @Override protected void _connectAction_() throws IOException {
    // Implicit mode.
    if (isImplicit) {
      sslNegotiation();
    }
    super._connectAction_();
    // Explicit mode.
    if (!isImplicit) {
      execAUTH();
      sslNegotiation();
    }
  }

  /**
   * AUTH command.
   *
   * @throws SSLException If it server reply code not equal "234" and "334".
   * @throws IOException If an I/O error occurs while either sending
   * the command.
   */
  protected void execAUTH() throws SSLException, IOException {
    int replyCode = sendCommand(CMD_AUTH, auth);
    if (FTPReply.SECURITY_MECHANISM_IS_OK == replyCode) {
      // replyCode = 334
      // I carry out an ADAT command.
    } else if (FTPReply.SECURITY_DATA_EXCHANGE_COMPLETE != replyCode) {
      throw new SSLException(getReplyString());
    }
  }

  /**
   * Performs a lazy init of the SSL context
   *
   * @throws IOException
   */
  private void initSslContext() throws IOException {
    if (context == null) {
      context = SSLContextUtils.createSSLContext(protocol, getKeyManager(), getTrustManager());
    }
  }

  /**
   * SSL/TLS negotiation. Acquires an SSL socket of a control
   * connection and carries out handshake processing.
   *
   * @throws IOException If server negotiation fails
   */
  protected void sslNegotiation() throws IOException {
    plainSocket = _socket_;
    initSslContext();

    SSLSocketFactory ssf = context.getSocketFactory();
    String host = (_hostname_ != null) ? _hostname_ : getRemoteAddress().getHostAddress();
    int port = _socket_.getPort();
    SSLSocket socket = (SSLSocket) ssf.createSocket(_socket_, host, port, false);
    socket.setEnableSessionCreation(isCreation);
    socket.setUseClientMode(isClientMode);

    // client mode
    if (isClientMode) {
      if (tlsEndpointChecking) {
        SSLSocketUtils.enableEndpointNameVerification(socket);
      }
    } else { // server mode
      socket.setNeedClientAuth(isNeedClientAuth);
      socket.setWantClientAuth(isWantClientAuth);
    }

    if (protocols != null) {
      socket.setEnabledProtocols(protocols);
    }
    if (suites != null) {
      socket.setEnabledCipherSuites(suites);
    }
    socket.startHandshake();

    // TODO the following setup appears to duplicate that in the super class methods
    _socket_ = socket;
    _controlInput_ =
        new BufferedReader(new InputStreamReader(socket.getInputStream(), getControlEncoding()));
    _controlOutput_ =
        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), getControlEncoding()));

    if (isClientMode) {
      if (hostnameVerifier != null && !hostnameVerifier.verify(host, socket.getSession())) {
        throw new SSLHandshakeException("Hostname doesn't match certificate");
      }
    }
  }

  /**
   * Get the {@link KeyManager} instance.
   *
   * @return The {@link KeyManager} instance
   */
  private KeyManager getKeyManager() {
    return keyManager;
  }

  /**
   * Set a {@link KeyManager} to use
   *
   * @param keyManager The KeyManager implementation to set.
   * @see KeyManagerUtils
   */
  public void setKeyManager(KeyManager keyManager) {
    this.keyManager = keyManager;
  }

  /**
   * Controls whether a new SSL session may be established by this socket.
   *
   * @param isCreation The established socket flag.
   */
  public void setEnabledSessionCreation(boolean isCreation) {
    this.isCreation = isCreation;
  }

  /**
   * Returns true if new SSL sessions may be established by this socket.
   * When the underlying {@link Socket} instance is not SSL-enabled (i.e. an
   * instance of {@link SSLSocket} with {@link SSLSocket}{@link #getEnableSessionCreation()})
   * enabled,
   * this returns False.
   *
   * @return true - Indicates that sessions may be created;
   * this is the default.
   * false - indicates that an existing session must be resumed.
   */
  public boolean getEnableSessionCreation() {
    if (_socket_ instanceof SSLSocket) {
      return ((SSLSocket) _socket_).getEnableSessionCreation();
    }
    return false;
  }

  /**
   * Configures the socket to require client authentication.
   *
   * @param isNeedClientAuth The need client auth flag.
   */
  public void setNeedClientAuth(boolean isNeedClientAuth) {
    this.isNeedClientAuth = isNeedClientAuth;
  }

  /**
   * Returns true if the socket will require client authentication.
   * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns false.
   *
   * @return true - If the server mode socket should request
   * that the client authenticate itself.
   */
  public boolean getNeedClientAuth() {
    if (_socket_ instanceof SSLSocket) {
      return ((SSLSocket) _socket_).getNeedClientAuth();
    }
    return false;
  }

  /**
   * Configures the socket to request client authentication,
   * but only if such a request is appropriate to the cipher
   * suite negotiated.
   *
   * @param isWantClientAuth The want client auth flag.
   */
  public void setWantClientAuth(boolean isWantClientAuth) {
    this.isWantClientAuth = isWantClientAuth;
  }

  /**
   * Returns true if the socket will request client authentication.
   * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns false.
   *
   * @return true - If the server mode socket should request
   * that the client authenticate itself.
   */
  public boolean getWantClientAuth() {
    if (_socket_ instanceof SSLSocket) {
      return ((SSLSocket) _socket_).getWantClientAuth();
    }
    return false;
  }

  /**
   * Configures the socket to use client (or server) mode in its first
   * handshake.
   *
   * @param isClientMode The use client mode flag.
   */
  public void setUseClientMode(boolean isClientMode) {
    this.isClientMode = isClientMode;
  }

  /**
   * Returns true if the socket is set to use client mode
   * in its first handshake.
   * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns false.
   *
   * @return true - If the socket should start its first handshake
   * in "client" mode.
   */
  public boolean getUseClientMode() {
    if (_socket_ instanceof SSLSocket) {
      return ((SSLSocket) _socket_).getUseClientMode();
    }
    return false;
  }

  /**
   * Controls which particular cipher suites are enabled for use on this
   * connection. Called before server negotiation.
   *
   * @param cipherSuites The cipher suites.
   */
  public void setEnabledCipherSuites(String[] cipherSuites) {
    suites = new String[cipherSuites.length];
    System.arraycopy(cipherSuites, 0, suites, 0, cipherSuites.length);
  }

  /**
   * Returns the names of the cipher suites which could be enabled
   * for use on this connection.
   * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns null.
   *
   * @return An array of cipher suite names, or <code>null</code>
   */
  public String[] getEnabledCipherSuites() {
    if (_socket_ instanceof SSLSocket) {
      return ((SSLSocket) _socket_).getEnabledCipherSuites();
    }
    return null;
  }

  /**
   * Controls which particular protocol versions are enabled for use on this
   * connection. I perform setting before a server negotiation.
   *
   * @param protocolVersions The protocol versions.
   */
  public void setEnabledProtocols(String[] protocolVersions) {
    protocols = new String[protocolVersions.length];
    System.arraycopy(protocolVersions, 0, protocols, 0, protocolVersions.length);
  }

  /**
   * Returns the names of the protocol versions which are currently
   * enabled for use on this connection.
   * When the underlying {@link Socket} is not an {@link SSLSocket} instance, returns null.
   *
   * @return An array of protocols, or <code>null</code>
   */
  public String[] getEnabledProtocols() {
    if (_socket_ instanceof SSLSocket) {
      return ((SSLSocket) _socket_).getEnabledProtocols();
    }
    return null;
  }

  /**
   * PBSZ command. pbsz value: 0 to (2^32)-1 decimal integer.
   *
   * @param pbsz Protection Buffer Size.
   * @throws SSLException If the server reply code does not equal "200".
   * @throws IOException If an I/O error occurs while sending
   * the command.
   * @see #parsePBSZ(long)
   */
  public void execPBSZ(long pbsz) throws SSLException, IOException {
    if (pbsz < 0 || 4294967295L < pbsz) { // 32-bit unsigned number
      throw new IllegalArgumentException();
    }
    int status = sendCommand(CMD_PBSZ, String.valueOf(pbsz));
    if (FTPReply.COMMAND_OK != status) {
      throw new SSLException(getReplyString());
    }
  }

  /**
   * PBSZ command. pbsz value: 0 to (2^32)-1 decimal integer.
   * Issues the command and parses the response to return the negotiated value.
   *
   * @param pbsz Protection Buffer Size.
   * @return the negotiated value.
   * @throws SSLException If the server reply code does not equal "200".
   * @throws IOException If an I/O error occurs while sending
   * the command.
   * @see #execPBSZ(long)
   * @since 3.0
   */
  public long parsePBSZ(long pbsz) throws SSLException, IOException {
    execPBSZ(pbsz);
    long minvalue = pbsz;
    String remainder = extractPrefixedData("PBSZ=", getReplyString());
    if (remainder != null) {
      long replysz = Long.parseLong(remainder);
      if (replysz < minvalue) {
        minvalue = replysz;
      }
    }
    return minvalue;
  }

  /**
   * PROT command.
   * <ul>
   * <li>C - Clear</li>
   * <li>S - Safe(SSL protocol only)</li>
   * <li>E - Confidential(SSL protocol only)</li>
   * <li>P - Private</li>
   * </ul>
   * <b>N.B.</b> the method calls
   * {@link #setSocketFactory(javax.net.SocketFactory)} and
   * {@link #setServerSocketFactory(javax.net.ServerSocketFactory)}
   *
   * @param prot Data Channel Protection Level, if {@code null}, use {@link #DEFAULT_PROT}.
   * @throws SSLException If the server reply code does not equal  {@code 200}.
   * @throws IOException If an I/O error occurs while sending
   * the command.
   */
  public void execPROT(String prot) throws SSLException, IOException {
    if (prot == null) {
      prot = DEFAULT_PROT;
    }
    if (!checkPROTValue(prot)) {
      throw new IllegalArgumentException();
    }
    if (FTPReply.COMMAND_OK != sendCommand(CMD_PROT, prot)) {
      throw new SSLException(getReplyString());
    }
    if (DEFAULT_PROT.equals(prot)) {
      setSocketFactory(null);
      setServerSocketFactory(null);
    } else {
      setSocketFactory(new FTPSSocketFactory(context));
      setServerSocketFactory(new FTPSServerSocketFactory(context));
      initSslContext();
    }
  }

  /**
   * Check the value that can be set in PROT Command value.
   *
   * @param prot Data Channel Protection Level.
   * @return True - A set point is right / False - A set point is not right
   */
  private boolean checkPROTValue(String prot) {
    for (String element : PROT_COMMAND_VALUE) {
      if (element.equals(prot)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Send an FTP command.
   * A successful CCC (Clear Command Channel) command causes the underlying {@link SSLSocket}
   * instance to be assigned to a plain {@link Socket}
   *
   * @param command The FTP command.
   * @return server reply.
   * @throws IOException If an I/O error occurs while sending the command.
   * @throws SSLException if a CCC command fails
   * @see FTP#sendCommand(String)
   */
  // Would like to remove this method, but that will break any existing clients that are using CCC
  @Override public int sendCommand(String command, String args) throws IOException {
    int repCode = super.sendCommand(command, args);
        /* If CCC is issued, restore socket i/o streams to unsecured versions */
    if (CMD_CCC.equals(command)) {
      if (FTPReply.COMMAND_OK == repCode) {
        _socket_.close();
        _socket_ = plainSocket;
        _controlInput_ = new BufferedReader(
            new InputStreamReader(_socket_.getInputStream(), getControlEncoding()));
        _controlOutput_ = new BufferedWriter(
            new OutputStreamWriter(_socket_.getOutputStream(), getControlEncoding()));
      } else {
        throw new SSLException(getReplyString());
      }
    }
    return repCode;
  }

  /**
   * Returns a socket of the data connection.
   * Wrapped as an {@link SSLSocket}, which carries out handshake processing.
   *
   * @param command The int representation of the FTP command to send.
   * @param arg The arguments to the FTP command.
   * If this parameter is set to null, then the command is sent with
   * no arguments.
   * @return corresponding to the established data connection.
   * Null is returned if an FTP protocol error is reported at any point
   * during the establishment and initialization of the connection.
   * @throws IOException If there is any problem with the connection.
   * @see FTPClient#_openDataConnection_(int, String)
   * @deprecated (3.3) Use {@link FTPClient#_openDataConnection_(FTPCmd, String)} instead
   */
  @Override
  // Strictly speaking this is not needed, but it works round a Clirr bug
  // So rather than invoke the parent code, we do it here
  @Deprecated protected Socket _openDataConnection_(int command, String arg) throws IOException {
    return _openDataConnection_(FTPCommand.getCommand(command), arg);
  }

  /**
   * Returns a socket of the data connection.
   * Wrapped as an {@link SSLSocket}, which carries out handshake processing.
   *
   * @param command The textual representation of the FTP command to send.
   * @param arg The arguments to the FTP command.
   * If this parameter is set to null, then the command is sent with
   * no arguments.
   * @return corresponding to the established data connection.
   * Null is returned if an FTP protocol error is reported at any point
   * during the establishment and initialization of the connection.
   * @throws IOException If there is any problem with the connection.
   * @see FTPClient#_openDataConnection_(int, String)
   * @since 3.2
   */
  @Override protected Socket _openDataConnection_(String command, String arg) throws IOException {
    Socket socket = super._openDataConnection_(command, arg);
    _prepareDataSocket_(socket);
    if (socket instanceof SSLSocket) {
      SSLSocket sslSocket = (SSLSocket) socket;

      sslSocket.setUseClientMode(isClientMode);
      sslSocket.setEnableSessionCreation(isCreation);

      // server mode
      if (!isClientMode) {
        sslSocket.setNeedClientAuth(isNeedClientAuth);
        sslSocket.setWantClientAuth(isWantClientAuth);
      }
      if (suites != null) {
        sslSocket.setEnabledCipherSuites(suites);
      }
      if (protocols != null) {
        sslSocket.setEnabledProtocols(protocols);
      }
      sslSocket.startHandshake();
    }

    return socket;
  }

  /**
   * Performs any custom initialization for a newly created SSLSocket (before
   * the SSL handshake happens).
   * Called by {@link #_openDataConnection_(int, String)} immediately
   * after creating the socket.
   * The default implementation is a no-op
   *
   * @param socket the socket to set up
   * @throws IOException on error
   * @since 3.1
   */
  protected void _prepareDataSocket_(Socket socket) throws IOException {
  }

  /**
   * Get the currently configured {@link TrustManager}.
   *
   * @return A TrustManager instance.
   */
  public TrustManager getTrustManager() {
    return trustManager;
  }

  /**
   * Override the default {@link TrustManager} to use; if set to {@code null},
   * the default TrustManager from the JVM will be used.
   *
   * @param trustManager The TrustManager implementation to set, may be {@code null}
   * @see TrustManagerUtils
   */
  public void setTrustManager(TrustManager trustManager) {
    this.trustManager = trustManager;
  }

  /**
   * Get the currently configured {@link HostnameVerifier}.
   * The verifier is only used on client mode connections.
   *
   * @return A HostnameVerifier instance.
   * @since 3.4
   */
  public HostnameVerifier getHostnameVerifier() {
    return hostnameVerifier;
  }

  /**
   * Override the default {@link HostnameVerifier} to use.
   * The verifier is only used on client mode connections.
   *
   * @param newHostnameVerifier The HostnameVerifier implementation to set or <code>null</code> to
   * disable.
   * @since 3.4
   */
  public void setHostnameVerifier(HostnameVerifier newHostnameVerifier) {
    hostnameVerifier = newHostnameVerifier;
  }

  /**
   * Return whether or not endpoint identification using the HTTPS algorithm
   * on Java 1.7+ is enabled. The default behaviour is for this to be disabled.
   *
   * This check is only performed on client mode connections.
   *
   * @return True if enabled, false if not.
   * @since 3.4
   */
  public boolean isEndpointCheckingEnabled() {
    return tlsEndpointChecking;
  }

  /**
   * Automatic endpoint identification checking using the HTTPS algorithm
   * is supported on Java 1.7+. The default behaviour is for this to be disabled.
   *
   * This check is only performed on client mode connections.
   *
   * @param enable Enable automatic endpoint identification checking using the HTTPS algorithm on
   * Java 1.7+.
   * @since 3.4
   */
  public void setEndpointCheckingEnabled(boolean enable) {
    tlsEndpointChecking = enable;
  }

  /**
   * Closes the connection to the FTP server and restores
   * connection parameters to the default values.
   * <p>
   * Calls {@code setSocketFactory(null)} and {@code setServerSocketFactory(null)}
   * to reset the factories that may have been changed during the session,
   * e.g. by {@link #execPROT(String)}
   *
   * @throws IOException If an error occurs while disconnecting.
   * @since 3.0
   */
  @Override public void disconnect() throws IOException {
    super.disconnect();
    if (plainSocket != null) {
      plainSocket.close();
    }
    setSocketFactory(null);
    setServerSocketFactory(null);
  }

  /**
   * Send the AUTH command with the specified mechanism.
   *
   * @param mechanism The mechanism name to send with the command.
   * @return server reply.
   * @throws IOException If an I/O error occurs while sending
   * the command.
   * @since 3.0
   */
  public int execAUTH(String mechanism) throws IOException {
    return sendCommand(CMD_AUTH, mechanism);
  }

  /**
   * Send the ADAT command with the specified authentication data.
   *
   * @param data The data to send with the command.
   * @return server reply.
   * @throws IOException If an I/O error occurs while sending
   * the command.
   * @since 3.0
   */
  public int execADAT(byte[] data) throws IOException {
    if (data != null) {
      return sendCommand(CMD_ADAT, Base64.encodeBase64StringUnChunked(data));
    } else {
      return sendCommand(CMD_ADAT);
    }
  }

  /**
   * Send the CCC command to the server.
   * The CCC (Clear Command Channel) command causes the underlying {@link SSLSocket} instance  to be
   * assigned
   * to a plain {@link Socket} instances
   *
   * @return server reply.
   * @throws IOException If an I/O error occurs while sending
   * the command.
   * @since 3.0
   */
  public int execCCC() throws IOException {
    int repCode = sendCommand(CMD_CCC);
    // This will be performed by sendCommand(String, String)
    //        if (FTPReply.isPositiveCompletion(repCode)) {
    //            _socket_.close();
    //            _socket_ = plainSocket;
    //            _controlInput_ = new BufferedReader(
    //                new InputStreamReader(
    //                    _socket_.getInputStream(), getControlEncoding()));
    //            _controlOutput_ = new BufferedWriter(
    //                new OutputStreamWriter(
    //                    _socket_.getOutputStream(), getControlEncoding()));
    //        }
    return repCode;
  }

  /**
   * Send the MIC command with the specified data.
   *
   * @param data The data to send with the command.
   * @return server reply.
   * @throws IOException If an I/O error occurs while sending
   * the command.
   * @since 3.0
   */
  public int execMIC(byte[] data) throws IOException {
    if (data != null) {
      return sendCommand(CMD_MIC, Base64.encodeBase64StringUnChunked(data));
    } else {
      return sendCommand(CMD_MIC, ""); // perhaps "=" or just sendCommand(String)?
    }
  }

  /**
   * Send the CONF command with the specified data.
   *
   * @param data The data to send with the command.
   * @return server reply.
   * @throws IOException If an I/O error occurs while sending
   * the command.
   * @since 3.0
   */
  public int execCONF(byte[] data) throws IOException {
    if (data != null) {
      return sendCommand(CMD_CONF, Base64.encodeBase64StringUnChunked(data));
    } else {
      return sendCommand(CMD_CONF, ""); // perhaps "=" or just sendCommand(String)?
    }
  }

  /**
   * Send the ENC command with the specified data.
   *
   * @param data The data to send with the command.
   * @return server reply.
   * @throws IOException If an I/O error occurs while sending
   * the command.
   * @since 3.0
   */
  public int execENC(byte[] data) throws IOException {
    if (data != null) {
      return sendCommand(CMD_ENC, Base64.encodeBase64StringUnChunked(data));
    } else {
      return sendCommand(CMD_ENC, ""); // perhaps "=" or just sendCommand(String)?
    }
  }

  /**
   * Parses the given ADAT response line and base64-decodes the data.
   *
   * @param reply The ADAT reply to parse.
   * @return the data in the reply, base64-decoded.
   * @since 3.0
   */
  public byte[] parseADATReply(String reply) {
    if (reply == null) {
      return null;
    } else {
      return Base64.decodeBase64(extractPrefixedData("ADAT=", reply));
    }
  }

  /**
   * Extract the data from a reply with a prefix, e.g. PBSZ=1234 => 1234
   *
   * @param prefix the prefix to find
   * @param reply where to find the prefix
   * @return the remainder of the string after the prefix, or null if the prefix was not present.
   */
  private String extractPrefixedData(String prefix, String reply) {
    int idx = reply.indexOf(prefix);
    if (idx == -1) {
      return null;
    }
    // N.B. Cannot use trim before substring as leading space would affect the offset.
    return reply.substring(idx + prefix.length()).trim();
  }

  // DEPRECATED - for API compatibility only - DO NOT USE

  /** @deprecated - not used - may be removed in a future release */
  @Deprecated public static String KEYSTORE_ALGORITHM;

  /** @deprecated - not used - may be removed in a future release */
  @Deprecated public static String TRUSTSTORE_ALGORITHM;

  /** @deprecated - not used - may be removed in a future release */
  @Deprecated public static String PROVIDER;

  /** @deprecated - not used - may be removed in a future release */
  @Deprecated public static String STORE_TYPE;
}
/* kate: indent-width 4; replace-tabs on; */
