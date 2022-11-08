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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/***
 * This class wraps an input stream, replacing all singly occurring
 * &lt;LF&gt; (linefeed) characters with &lt;CR&gt;&lt;LF&gt; (carriage return
 * followed by linefeed), which is the NETASCII standard for representing
 * a newline.
 * You would use this class to implement ASCII file transfers requiring
 * conversion to NETASCII.
 *
 *
 ***/

public final class ToNetASCIIInputStream extends FilterInputStream {
  private static final int __NOTHING_SPECIAL = 0;
  private static final int __LAST_WAS_CR = 1;
  private static final int __LAST_WAS_NL = 2;
  private int __status;

  /***
   * Creates a ToNetASCIIInputStream instance that wraps an existing
   * InputStream.
   *
   * @param input  The InputStream to wrap.
   ***/
  public ToNetASCIIInputStream(InputStream input) {
    super(input);
    __status = __NOTHING_SPECIAL;
  }

  /***
   * Reads and returns the next byte in the stream.  If the end of the
   * message has been reached, returns -1.
   *
   * @return The next character in the stream. Returns -1 if the end of the
   *          stream has been reached.
   * @throws IOException If an error occurs while reading the underlying
   *            stream.
   ***/
  @Override public int read() throws IOException {
    int ch;

    if (__status == __LAST_WAS_NL) {
      __status = __NOTHING_SPECIAL;
      return '\n';
    }

    ch = in.read();

    switch (ch) {
      case '\r':
        __status = __LAST_WAS_CR;
        return '\r';
      case '\n':
        if (__status != __LAST_WAS_CR) {
          __status = __LAST_WAS_NL;
          return '\r';
        }
        //$FALL-THROUGH$
      default:
        __status = __NOTHING_SPECIAL;
        return ch;
    }
    // statement not reached
    //return ch;
  }

  /***
   * Reads the next number of bytes from the stream into an array and
   * returns the number of bytes read.  Returns -1 if the end of the
   * stream has been reached.
   *
   * @param buffer  The byte array in which to store the data.
   * @return The number of bytes read. Returns -1 if the
   *          end of the message has been reached.
   * @throws IOException If an error occurs in reading the underlying
   *            stream.
   ***/
  @Override public int read(byte buffer[]) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  /***
   * Reads the next number of bytes from the stream into an array and returns
   * the number of bytes read.  Returns -1 if the end of the
   * message has been reached.  The characters are stored in the array
   * starting from the given offset and up to the length specified.
   *
   * @param buffer The byte array in which to store the data.
   * @param offset  The offset into the array at which to start storing data.
   * @param length   The number of bytes to read.
   * @return The number of bytes read. Returns -1 if the
   *          end of the stream has been reached.
   * @throws IOException If an error occurs while reading the underlying
   *            stream.
   ***/
  @Override public int read(byte buffer[], int offset, int length) throws IOException {
    int ch, off;

    if (length < 1) {
      return 0;
    }

    ch = available();

    if (length > ch) {
      length = ch;
    }

    // If nothing is available, block to read only one character
    if (length < 1) {
      length = 1;
    }

    if ((ch = read()) == -1) {
      return -1;
    }

    off = offset;

    do {
      buffer[offset++] = (byte) ch;
    } while (--length > 0 && (ch = read()) != -1);

    return (offset - off);
  }

  /*** Returns false.  Mark is not supported. ***/
  @Override public boolean markSupported() {
    return false;
  }

  @Override public int available() throws IOException {
    int result;

    result = in.available();

    if (__status == __LAST_WAS_NL) {
      return (result + 1);
    }

    return result;
  }
}
