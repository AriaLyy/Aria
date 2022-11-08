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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/***
 * This class wraps an output stream, replacing all occurrences
 * of &lt;CR&gt;&lt;LF&gt; (carriage return followed by a linefeed),
 * which is the NETASCII standard for representing a newline, with the
 * local line separator representation.  You would use this class to
 * implement ASCII file transfers requiring conversion from NETASCII.
 * <p>
 * Because of the translation process, a call to <code>flush()</code> will
 * not flush the last byte written if that byte was a carriage
 * return.  A call to {@link #close  close() }, however, will
 * flush the carriage return.
 *
 *
 ***/

public final class FromNetASCIIOutputStream extends FilterOutputStream {
  private boolean __lastWasCR;

  /***
   * Creates a FromNetASCIIOutputStream instance that wraps an existing
   * OutputStream.
   *
   * @param output  The OutputStream to wrap.
   ***/
  public FromNetASCIIOutputStream(OutputStream output) {
    super(output);
    __lastWasCR = false;
  }

  private void __write(int ch) throws IOException {
    switch (ch) {
      case '\r':
        __lastWasCR = true;
        // Don't write anything.  We need to see if next one is linefeed
        break;
      case '\n':
        if (__lastWasCR) {
          out.write(FromNetASCIIInputStream._lineSeparatorBytes);
          __lastWasCR = false;
          break;
        }
        __lastWasCR = false;
        out.write('\n');
        break;
      default:
        if (__lastWasCR) {
          out.write('\r');
          __lastWasCR = false;
        }
        out.write(ch);
        break;
    }
  }

  /***
   * Writes a byte to the stream.    Note that a call to this method
   * might not actually write a byte to the underlying stream until a
   * subsequent character is written, from which it can be determined if
   * a NETASCII line separator was encountered.
   * This is transparent to the programmer and is only mentioned for
   * completeness.
   *
   * @param ch The byte to write.
   * @throws IOException If an error occurs while writing to the underlying
   *            stream.
   ***/
  @Override public synchronized void write(int ch) throws IOException {
    if (FromNetASCIIInputStream._noConversionRequired) {
      out.write(ch);
      return;
    }

    __write(ch);
  }

  /***
   * Writes a byte array to the stream.
   *
   * @param buffer  The byte array to write.
   * @throws IOException If an error occurs while writing to the underlying
   *            stream.
   ***/
  @Override public synchronized void write(byte buffer[]) throws IOException {
    write(buffer, 0, buffer.length);
  }

  /***
   * Writes a number of bytes from a byte array to the stream starting from
   * a given offset.
   *
   * @param buffer  The byte array to write.
   * @param offset  The offset into the array at which to start copying data.
   * @param length  The number of bytes to write.
   * @throws IOException If an error occurs while writing to the underlying
   *            stream.
   ***/
  @Override public synchronized void write(byte buffer[], int offset, int length)
      throws IOException {
    if (FromNetASCIIInputStream._noConversionRequired) {
      // FilterOutputStream method is very slow.
      //super.write(buffer, offset, length);
      out.write(buffer, offset, length);
      return;
    }

    while (length-- > 0) {
      __write(buffer[offset++]);
    }
  }

  /***
   * Closes the stream, writing all pending data.
   *
   * @throws IOException  If an error occurs while closing the stream.
   ***/
  @Override public synchronized void close() throws IOException {
    if (FromNetASCIIInputStream._noConversionRequired) {
      super.close();
      return;
    }

    if (__lastWasCR) {
      out.write('\r');
    }
    super.close();
  }
}
