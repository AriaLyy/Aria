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

/**
 * FTPS-specific commands.
 *
 * @since 2.0
 * @deprecated 3.0 DO NOT USE
 */
@Deprecated public final class FTPSCommand {
  public static final int AUTH = 0;
  public static final int ADAT = 1;
  public static final int PBSZ = 2;
  public static final int PROT = 3;
  public static final int CCC = 4;

  public static final int AUTHENTICATION_SECURITY_MECHANISM = AUTH;
  public static final int AUTHENTICATION_SECURITY_DATA = ADAT;
  public static final int PROTECTION_BUFFER_SIZE = PBSZ;
  public static final int DATA_CHANNEL_PROTECTION_LEVEL = PROT;
  public static final int CLEAR_COMMAND_CHANNEL = CCC;

  private static final String[] _commands = { "AUTH", "ADAT", "PBSZ", "PROT", "CCC" };

  /**
   * Retrieve the FTPS command string corresponding to a specified
   * command code.
   *
   * @param command The command code.
   * @return The FTPS command string corresponding to a specified
   * command code.
   */
  public static final String getCommand(int command) {
    return _commands[command];
  }
}
