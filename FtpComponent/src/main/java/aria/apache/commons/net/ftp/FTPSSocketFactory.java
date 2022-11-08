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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

/**
 * Implementation of org.apache.commons.net.SocketFactory
 *
 * @since 2.0
 */
public class FTPSSocketFactory extends SocketFactory {

  private final SSLContext context;

  public FTPSSocketFactory(SSLContext context) {
    this.context = context;
  }

  // Override the default implementation
  @Override public Socket createSocket() throws IOException {
    return this.context.getSocketFactory().createSocket();
  }

  @Override public Socket createSocket(String address, int port)
      throws UnknownHostException, IOException {
    return this.context.getSocketFactory().createSocket(address, port);
  }

  @Override public Socket createSocket(InetAddress address, int port) throws IOException {
    return this.context.getSocketFactory().createSocket(address, port);
  }

  @Override
  public Socket createSocket(String address, int port, InetAddress localAddress, int localPort)
      throws UnknownHostException, IOException {
    return this.context.getSocketFactory().createSocket(address, port, localAddress, localPort);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return this.context.getSocketFactory().createSocket(address, port, localAddress, localPort);
  }

  // DEPRECATED METHODS - for API compatibility only - DO NOT USE

  /**
   * @param port the port
   * @return the socket
   * @throws IOException on error
   * @deprecated (2.2) use {@link FTPSServerSocketFactory#createServerSocket(int) instead}
   */
  @Deprecated public java.net.ServerSocket createServerSocket(int port) throws IOException {
    return this.init(this.context.getServerSocketFactory().createServerSocket(port));
  }

  /**
   * @param port the port
   * @param backlog the backlog
   * @return the socket
   * @throws IOException on error
   * @deprecated (2.2) use {@link FTPSServerSocketFactory#createServerSocket(int, int) instead}
   */
  @Deprecated public java.net.ServerSocket createServerSocket(int port, int backlog)
      throws IOException {
    return this.init(this.context.getServerSocketFactory().createServerSocket(port, backlog));
  }

  /**
   * @param port the port
   * @param backlog the backlog
   * @param ifAddress the interface
   * @return the socket
   * @throws IOException on error
   * @deprecated (2.2) use {@link FTPSServerSocketFactory#createServerSocket(int, int, InetAddress)
   * instead}
   */
  @Deprecated public java.net.ServerSocket createServerSocket(int port, int backlog,
      InetAddress ifAddress) throws IOException {
    return this.init(
        this.context.getServerSocketFactory().createServerSocket(port, backlog, ifAddress));
  }

  /**
   * @param socket the socket
   * @return the socket
   * @throws IOException on error
   * @deprecated (2.2) use {@link FTPSServerSocketFactory#init(java.net.ServerSocket)}
   */
  @Deprecated public java.net.ServerSocket init(java.net.ServerSocket socket) throws IOException {
    ((javax.net.ssl.SSLServerSocket) socket).setUseClientMode(true);
    return socket;
  }
}
