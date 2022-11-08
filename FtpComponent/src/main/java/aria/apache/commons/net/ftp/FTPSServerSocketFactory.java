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
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

/**
 * Server socket factory for FTPS connections.
 *
 * @since 2.2
 */
public class FTPSServerSocketFactory extends ServerSocketFactory {

  /** Factory for secure socket factories */
  private final SSLContext context;

  public FTPSServerSocketFactory(SSLContext context) {
    this.context = context;
  }

  // Override the default superclass method
  @Override public ServerSocket createServerSocket() throws IOException {
    return init(this.context.getServerSocketFactory().createServerSocket());
  }

  @Override public ServerSocket createServerSocket(int port) throws IOException {
    return init(this.context.getServerSocketFactory().createServerSocket(port));
  }

  @Override public ServerSocket createServerSocket(int port, int backlog) throws IOException {
    return init(this.context.getServerSocketFactory().createServerSocket(port, backlog));
  }

  @Override public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress)
      throws IOException {
    return init(this.context.getServerSocketFactory().createServerSocket(port, backlog, ifAddress));
  }

  /**
   * Sets the socket so newly accepted connections will use SSL client mode.
   *
   * @param socket the SSLServerSocket to initialise
   * @return the socket
   * @throws ClassCastException if socket is not an instance of SSLServerSocket
   */
  public ServerSocket init(ServerSocket socket) {
    ((SSLServerSocket) socket).setUseClientMode(true);
    return socket;
  }
}

