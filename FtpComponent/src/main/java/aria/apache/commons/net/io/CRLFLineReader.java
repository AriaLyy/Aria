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

package aria.apache.commons.net.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * CRLFLineReader implements a readLine() method that requires
 * exactly CRLF to terminate an input line.
 * This is required for IMAP, which allows bare CR and LF.
 *
 * @since 3.0
 */
public final class CRLFLineReader extends BufferedReader {
  private static final char LF = '\n';
  private static final char CR = '\r';

  /**
   * Creates a CRLFLineReader that wraps an existing Reader
   * input source.
   *
   * @param reader The Reader input source.
   */
  public CRLFLineReader(Reader reader) {
    super(reader);
  }

  /**
   * Read a line of text.
   * A line is considered to be terminated by carriage return followed immediately by a linefeed.
   * This contrasts with BufferedReader which also allows other combinations.
   *
   * @since 3.0
   */
  @Override public String readLine() throws IOException {
    StringBuilder sb = new StringBuilder();
    int intch;
    boolean prevWasCR = false;
    synchronized (lock) { // make thread-safe (hopefully!)
      while ((intch = read()) != -1) {
        if (prevWasCR && intch == LF) {
          return sb.substring(0, sb.length() - 1);
        }
        if (intch == CR) {
          prevWasCR = true;
        } else {
          prevWasCR = false;
        }
        sb.append((char) intch);
      }
    }
    String string = sb.toString();
    if (string.length() == 0) { // immediate EOF
      return null;
    }
    return string;
  }
}
