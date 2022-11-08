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
 * DotTerminatedMessageReader is a class used to read messages from a
 * server that are terminated by a single dot followed by a
 * &lt;CR&gt;&lt;LF&gt;
 * sequence and with double dots appearing at the begining of lines which
 * do not signal end of message yet start with a dot.  Various Internet
 * protocols such as NNTP and POP3 produce messages of this type.
 * <p>
 * This class handles stripping of the duplicate period at the beginning
 * of lines starting with a period, and ensures you cannot read past the end of the message.
 * <p>
 * Note: versions since 3.0 extend BufferedReader rather than Reader,
 * and no longer change the CRLF into the local EOL. Also only DOT CR LF
 * acts as EOF.
 *
 * @version $Id: DotTerminatedMessageReader.java 1747119 2016-06-07 02:22:24Z ggregory $
 */
public final class DotTerminatedMessageReader extends BufferedReader {
  private static final char LF = '\n';
  private static final char CR = '\r';
  private static final int DOT = '.';

  private boolean atBeginning;
  private boolean eof;
  private boolean seenCR; // was last character CR?

  /**
   * Creates a DotTerminatedMessageReader that wraps an existing Reader
   * input source.
   *
   * @param reader The Reader input source containing the message.
   */
  public DotTerminatedMessageReader(Reader reader) {
    super(reader);
    // Assumes input is at start of message
    atBeginning = true;
    eof = false;
  }

  /**
   * Reads and returns the next character in the message.  If the end of the
   * message has been reached, returns -1.  Note that a call to this method
   * may result in multiple reads from the underlying input stream to decode
   * the message properly (removing doubled dots and so on).  All of
   * this is transparent to the programmer and is only mentioned for
   * completeness.
   *
   * @return The next character in the message. Returns -1 if the end of the
   * message has been reached.
   * @throws IOException If an error occurs while reading the underlying
   * stream.
   */
  @Override public int read() throws IOException {
    synchronized (lock) {
      if (eof) {
        return -1; // Don't allow read past EOF
      }
      int chint = super.read();
      if (chint == -1) { // True EOF
        eof = true;
        return -1;
      }
      if (atBeginning) {
        atBeginning = false;
        if (chint == DOT) { // Have DOT
          mark(2); // need to check for CR LF or DOT
          chint = super.read();
          if (chint == -1) { // Should not happen
            // new Throwable("Trailing DOT").printStackTrace();
            eof = true;
            return DOT; // return the trailing DOT
          }
          if (chint == DOT) { // Have DOT DOT
            // no need to reset as we want to lose the first DOT
            return chint; // i.e. DOT
          }
          if (chint == CR) { // Have DOT CR
            chint = super.read();
            if (chint == -1) { // Still only DOT CR - should not happen
              //new Throwable("Trailing DOT CR").printStackTrace();
              reset(); // So CR is picked up next time
              return DOT; // return the trailing DOT
            }
            if (chint == LF) { // DOT CR LF
              atBeginning = true;
              eof = true;
              // Do we need to clear the mark somehow?
              return -1;
            }
          }
          // Should not happen - lone DOT at beginning
          //new Throwable("Lone DOT followed by "+(char)chint).printStackTrace();
          reset();
          return DOT;
        } // have DOT
      } // atBeginning

      // Handle CRLF in normal flow
      if (seenCR) {
        seenCR = false;
        if (chint == LF) {
          atBeginning = true;
        }
      }
      if (chint == CR) {
        seenCR = true;
      }
      return chint;
    }
  }

  /**
   * Reads the next characters from the message into an array and
   * returns the number of characters read.  Returns -1 if the end of the
   * message has been reached.
   *
   * @param buffer The character array in which to store the characters.
   * @return The number of characters read. Returns -1 if the
   * end of the message has been reached.
   * @throws IOException If an error occurs in reading the underlying
   * stream.
   */
  @Override public int read(char[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  /**
   * Reads the next characters from the message into an array and
   * returns the number of characters read.  Returns -1 if the end of the
   * message has been reached.  The characters are stored in the array
   * starting from the given offset and up to the length specified.
   *
   * @param buffer The character array in which to store the characters.
   * @param offset The offset into the array at which to start storing
   * characters.
   * @param length The number of characters to read.
   * @return The number of characters read. Returns -1 if the
   * end of the message has been reached.
   * @throws IOException If an error occurs in reading the underlying
   * stream.
   */
  @Override public int read(char[] buffer, int offset, int length) throws IOException {
    if (length < 1) {
      return 0;
    }
    int ch;
    synchronized (lock) {
      if ((ch = read()) == -1) {
        return -1;
      }

      int off = offset;

      do {
        buffer[offset++] = (char) ch;
      } while (--length > 0 && (ch = read()) != -1);

      return (offset - off);
    }
  }

  /**
   * Closes the message for reading.  This doesn't actually close the
   * underlying stream.  The underlying stream may still be used for
   * communicating with the server and therefore is not closed.
   * <p>
   * If the end of the message has not yet been reached, this method
   * will read the remainder of the message until it reaches the end,
   * so that the underlying stream may continue to be used properly
   * for communicating with the server.  If you do not fully read
   * a message, you MUST close it, otherwise your program will likely
   * hang or behave improperly.
   *
   * @throws IOException If an error occurs while reading the
   * underlying stream.
   */
  @Override public void close() throws IOException {
    synchronized (lock) {
      if (!eof) {
        while (read() != -1) {
          // read to EOF
        }
      }
      eof = true;
      atBeginning = false;
    }
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
    synchronized (lock) { // make thread-safe (hopefully!)
      while ((intch = read()) != -1) {
        if (intch == LF && atBeginning) {
          return sb.substring(0, sb.length() - 1);
        }
        sb.append((char) intch);
      }
    }
    String string = sb.toString();
    if (string.length() == 0) { // immediate EOF
      return null;
    }
    // Should not happen - EOF without CRLF
    //new Throwable(string).printStackTrace();
    return string;
  }
}
