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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import aria.apache.commons.net.util.Base64;

/**
 * Experimental attempt at FTP client that tunnels over an HTTP proxy connection.
 *
 * @since 2.2
 */
public class FTPHTTPClient extends FTPClient {
  private final String proxyHost;
  private final int proxyPort;
  private final String proxyUsername;
  private final String proxyPassword;

  private static final byte[] CRLF = { '\r', '\n' };
  private final Base64 base64 = new Base64();

  private String tunnelHost; // Save the host when setting up a tunnel (needed for EPSV)

  public FTPHTTPClient(String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    this.proxyUsername = proxyUser;
    this.proxyPassword = proxyPass;
    this.tunnelHost = null;
  }

  public FTPHTTPClient(String proxyHost, int proxyPort) {
    this(proxyHost, proxyPort, null, null);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if connection mode is not passive
   * @deprecated (3.3) Use {@link FTPClient#_openDataConnection_(FTPCmd, String)} instead
   */
  // Kept to maintain binary compatibility
  // Not strictly necessary, but Clirr complains even though there is a super-impl
  @Override @Deprecated protected Socket _openDataConnection_(int command, String arg)
      throws IOException {
    return super._openDataConnection_(command, arg);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if connection mode is not passive
   * @since 3.1
   */
  @Override protected Socket _openDataConnection_(String command, String arg) throws IOException {
    //Force local passive mode, active mode not supported by through proxy
    if (getDataConnectionMode() != PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
      throw new IllegalStateException("Only passive connection mode supported");
    }

    final boolean isInet6Address = getRemoteAddress() instanceof Inet6Address;
    String passiveHost = null;

    boolean attemptEPSV = isUseEPSVwithIPv4() || isInet6Address;
    if (attemptEPSV && epsv() == FTPReply.ENTERING_EPSV_MODE) {
      _parseExtendedPassiveModeReply(_replyLines.get(0));
      passiveHost = this.tunnelHost;
    } else {
      if (isInet6Address) {
        return null; // Must use EPSV for IPV6
      }
      // If EPSV failed on IPV4, revert to PASV
      if (pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
        return null;
      }
      _parsePassiveModeReply(_replyLines.get(0));
      passiveHost = this.getPassiveHost();
    }

    Socket socket = _socketFactory_.createSocket(proxyHost, proxyPort);
    InputStream is = socket.getInputStream();
    OutputStream os = socket.getOutputStream();
    tunnelHandshake(passiveHost, this.getPassivePort(), is, os);
    if ((getRestartOffset() > 0) && !restart(getRestartOffset())) {
      socket.close();
      return null;
    }

    if (!FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
      socket.close();
      return null;
    }

    return socket;
  }

  @Override public void connect(String host, int port) throws SocketException, IOException {

    _socket_ = _socketFactory_.createSocket(proxyHost, proxyPort);
    _input_ = _socket_.getInputStream();
    _output_ = _socket_.getOutputStream();
    Reader socketIsReader;
    try {
      socketIsReader = tunnelHandshake(host, port, _input_, _output_);
    } catch (Exception e) {
      IOException ioe = new IOException("Could not connect to " + host + " using port " + port);
      ioe.initCause(e);
      throw ioe;
    }
    super._connectAction_(socketIsReader);
  }

  private BufferedReader tunnelHandshake(String host, int port, InputStream input,
      OutputStream output) throws IOException, UnsupportedEncodingException {
    final String connectString = "CONNECT " + host + ":" + port + " HTTP/1.1";
    final String hostString = "Host: " + host + ":" + port;

    this.tunnelHost = host;
    output.write(connectString.getBytes("UTF-8")); // TODO what is the correct encoding?
    output.write(CRLF);
    output.write(hostString.getBytes("UTF-8"));
    output.write(CRLF);

    if (proxyUsername != null && proxyPassword != null) {
      final String auth = proxyUsername + ":" + proxyPassword;
      final String header =
          "Proxy-Authorization: Basic " + base64.encodeToString(auth.getBytes("UTF-8"));
      output.write(header.getBytes("UTF-8"));
    }
    output.write(CRLF);

    List<String> response = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(input, getCharset()));

    for (String line = reader.readLine(); line != null && line.length() > 0;
        line = reader.readLine()) {
      response.add(line);
    }

    int size = response.size();
    if (size == 0) {
      throw new IOException("No response from proxy");
    }

    String code = null;
    String resp = response.get(0);
    if (resp.startsWith("HTTP/") && resp.length() >= 12) {
      code = resp.substring(9, 12);
    } else {
      throw new IOException("Invalid response from proxy: " + resp);
    }

    if (!"200".equals(code)) {
      StringBuilder msg = new StringBuilder();
      msg.append("HTTPTunnelConnector: connection failed\r\n");
      msg.append("Response received from the proxy:\r\n");
      for (String line : response) {
        msg.append(line);
        msg.append("\r\n");
      }
      throw new IOException(msg.toString());
    }
    return reader;
  }
}


