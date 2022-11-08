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

/***
 * FTPConnectionClosedException is used to indicate the premature or
 * unexpected closing of an FTP connection resulting from a
 * {@link FTPReply#SERVICE_NOT_AVAILABLE FTPReply.SERVICE_NOT_AVAILABLE }
 *  response (FTP reply code 421) to a
 * failed FTP command.  This exception is derived from IOException and
 * therefore may be caught either as an IOException or specifically as an
 * FTPConnectionClosedException.
 *
 * @see FTP
 * @see FTPClient
 ***/

public class FTPConnectionClosedException extends IOException {

  private static final long serialVersionUID = 3500547241659379952L;

  /*** Constructs a FTPConnectionClosedException with no message ***/
  public FTPConnectionClosedException() {
    super();
  }

  /***
   * Constructs a FTPConnectionClosedException with a specified message.
   *
   * @param message  The message explaining the reason for the exception.
   ***/
  public FTPConnectionClosedException(String message) {
    super(message);
  }
}
