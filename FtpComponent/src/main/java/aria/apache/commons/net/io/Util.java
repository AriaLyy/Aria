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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

/***
 * The Util class cannot be instantiated and stores short static convenience
 * methods that are often quite useful.
 *
 *
 * @see CopyStreamException
 * @see CopyStreamListener
 * @see CopyStreamAdapter
 ***/

public final class Util {
  /**
   * The default buffer size ({@value}) used by
   * {@link #copyStream  copyStream } and {@link #copyReader  copyReader}
   * and by the copyReader/copyStream methods if a zero or negative buffer size is supplied.
   */
  public static final int DEFAULT_COPY_BUFFER_SIZE = 1024;

  // Cannot be instantiated
  private Util() {
  }

  /***
   * Copies the contents of an InputStream to an OutputStream using a
   * copy buffer of a given size and notifies the provided
   * CopyStreamListener of the progress of the copy operation by calling
   * its bytesTransferred(long, int) method after each write to the
   * destination.  If you wish to notify more than one listener you should
   * use a CopyStreamAdapter as the listener and register the additional
   * listeners with the CopyStreamAdapter.
   * <p>
   * The contents of the InputStream are
   * read until the end of the stream is reached, but neither the
   * source nor the destination are closed.  You must do this yourself
   * outside of the method call.  The number of bytes read/written is
   * returned.
   *
   * @param source  The source InputStream.
   * @param dest    The destination OutputStream.
   * @param bufferSize  The number of bytes to buffer during the copy.
   *            A zero or negative value means to use {@link #DEFAULT_COPY_BUFFER_SIZE}.
   * @param streamSize  The number of bytes in the stream being copied.
   *          Should be set to CopyStreamEvent.UNKNOWN_STREAM_SIZE if unknown.
   * Not currently used (though it is passed to {@link CopyStreamListener#bytesTransferred(long, int, long)}
   * @param listener  The CopyStreamListener to notify of progress.  If
   *      this parameter is null, notification is not attempted.
   * @param flush Whether to flush the output stream after every
   *        write.  This is necessary for interactive sessions that rely on
   *        buffered streams.  If you don't flush, the data will stay in
   *        the stream buffer.
   * @return number of bytes read/written
   * @throws CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyStream(InputStream source, OutputStream dest, int bufferSize,
      long streamSize, CopyStreamListener listener, boolean flush) throws CopyStreamException {
    int numBytes;
    long total = 0;
    byte[] buffer = new byte[bufferSize > 0 ? bufferSize : DEFAULT_COPY_BUFFER_SIZE];

    try {
      while (!Thread.currentThread().isInterrupted() && (numBytes = source.read(buffer)) != -1) {
        // Technically, some read(byte[]) methods may return 0 and we cannot
        // accept that as an indication of EOF.

        if (numBytes == 0) {
          int singleByte = source.read();
          if (singleByte < 0) {
            break;
          }
          dest.write(singleByte);
          if (flush) {
            dest.flush();
          }
          ++total;
          if (listener != null) {
            listener.bytesTransferred(total, 1, streamSize);
          }
          continue;
        }

        dest.write(buffer, 0, numBytes);
        if (flush) {
          dest.flush();
        }
        total += numBytes;
        if (listener != null) {
          listener.bytesTransferred(total, numBytes, streamSize);
        }
      }
    } catch (IOException e) {
      throw new CopyStreamException("IOException caught while copying.", total, e);
    }

    return total;
  }


  /***
   * Copies the contents of an InputStream to an OutputStream using a
   * copy buffer of a given size and notifies the provided
   * CopyStreamListener of the progress of the copy operation by calling
   * its bytesTransferred(long, int) method after each write to the
   * destination.  If you wish to notify more than one listener you should
   * use a CopyStreamAdapter as the listener and register the additional
   * listeners with the CopyStreamAdapter.
   * <p>
   * The contents of the InputStream are
   * read until the end of the stream is reached, but neither the
   * source nor the destination are closed.  You must do this yourself
   * outside of the method call.  The number of bytes read/written is
   * returned.
   *
   * @param source  The source InputStream.
   * @param dest    The destination OutputStream.
   * @param bufferSize  The number of bytes to buffer during the copy.
   *            A zero or negative value means to use {@link #DEFAULT_COPY_BUFFER_SIZE}.
   * @param streamSize  The number of bytes in the stream being copied.
   *          Should be set to CopyStreamEvent.UNKNOWN_STREAM_SIZE if unknown.
   * Not currently used (though it is passed to {@link CopyStreamListener#bytesTransferred(long, int, long)}
   * @param listener  The CopyStreamListener to notify of progress.  If
   *      this parameter is null, notification is not attempted.
   * @return number of bytes read/written
   * @throws CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyStream(InputStream source, OutputStream dest, int bufferSize,
      long streamSize, CopyStreamListener listener) throws CopyStreamException {
    return copyStream(source, dest, bufferSize, streamSize, listener, true);
  }

  /***
   * Copies the contents of an InputStream to an OutputStream using a
   * copy buffer of a given size.  The contents of the InputStream are
   * read until the end of the stream is reached, but neither the
   * source nor the destination are closed.  You must do this yourself
   * outside of the method call.  The number of bytes read/written is
   * returned.
   *
   * @param source  The source InputStream.
   * @param dest    The destination OutputStream.
   * @param bufferSize  The number of bytes to buffer during the copy.
   *            A zero or negative value means to use {@link #DEFAULT_COPY_BUFFER_SIZE}.
   * @return The number of bytes read/written in the copy operation.
   * @throws CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyStream(InputStream source, OutputStream dest, int bufferSize)
      throws CopyStreamException {
    return copyStream(source, dest, bufferSize, CopyStreamEvent.UNKNOWN_STREAM_SIZE, null);
  }

  /***
   * Same as <code> copyStream(source, dest, DEFAULT_COPY_BUFFER_SIZE); </code>
   * @param source where to copy from
   * @param dest  where to copy to
   * @return number of bytes copied
   * @throws CopyStreamException on error
   ***/
  public static final long copyStream(InputStream source, OutputStream dest)
      throws CopyStreamException {
    return copyStream(source, dest, DEFAULT_COPY_BUFFER_SIZE);
  }

  /***
   * Copies the contents of a Reader to a Writer using a
   * copy buffer of a given size and notifies the provided
   * CopyStreamListener of the progress of the copy operation by calling
   * its bytesTransferred(long, int) method after each write to the
   * destination.  If you wish to notify more than one listener you should
   * use a CopyStreamAdapter as the listener and register the additional
   * listeners with the CopyStreamAdapter.
   * <p>
   * The contents of the Reader are
   * read until its end is reached, but neither the source nor the
   * destination are closed.  You must do this yourself outside of the
   * method call.  The number of characters read/written is returned.
   *
   * @param source  The source Reader.
   * @param dest    The destination writer.
   * @param bufferSize  The number of characters to buffer during the copy.
   *            A zero or negative value means to use {@link #DEFAULT_COPY_BUFFER_SIZE}.
   * @param streamSize  The number of characters in the stream being copied.
   *          Should be set to CopyStreamEvent.UNKNOWN_STREAM_SIZE if unknown.
   * Not currently used (though it is passed to {@link CopyStreamListener#bytesTransferred(long, int, long)}
   * @param listener  The CopyStreamListener to notify of progress.  If
   *      this parameter is null, notification is not attempted.
   * @return The number of characters read/written in the copy operation.
   * @throws CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyReader(Reader source, Writer dest, int bufferSize, long streamSize,
      CopyStreamListener listener) throws CopyStreamException {
    int numChars;
    long total = 0;
    char[] buffer = new char[bufferSize > 0 ? bufferSize : DEFAULT_COPY_BUFFER_SIZE];

    try {
      while ((numChars = source.read(buffer)) != -1) {
        // Technically, some read(char[]) methods may return 0 and we cannot
        // accept that as an indication of EOF.
        if (numChars == 0) {
          int singleChar = source.read();
          if (singleChar < 0) {
            break;
          }
          dest.write(singleChar);
          dest.flush();
          ++total;
          if (listener != null) {
            listener.bytesTransferred(total, 1, streamSize);
          }
          continue;
        }

        dest.write(buffer, 0, numChars);
        dest.flush();
        total += numChars;
        if (listener != null) {
          listener.bytesTransferred(total, numChars, streamSize);
        }
      }
    } catch (IOException e) {
      throw new CopyStreamException("IOException caught while copying.", total, e);
    }

    return total;
  }

  /***
   * Copies the contents of a Reader to a Writer using a
   * copy buffer of a given size.  The contents of the Reader are
   * read until its end is reached, but neither the source nor the
   * destination are closed.  You must do this yourself outside of the
   * method call.  The number of characters read/written is returned.
   *
   * @param source  The source Reader.
   * @param dest    The destination writer.
   * @param bufferSize  The number of characters to buffer during the copy.
   *            A zero or negative value means to use {@link #DEFAULT_COPY_BUFFER_SIZE}.
   * @return The number of characters read/written in the copy operation.
   * @throws CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyReader(Reader source, Writer dest, int bufferSize)
      throws CopyStreamException {
    return copyReader(source, dest, bufferSize, CopyStreamEvent.UNKNOWN_STREAM_SIZE, null);
  }

  /***
   * Same as <code> copyReader(source, dest, DEFAULT_COPY_BUFFER_SIZE); </code>
   * @param source where to copy from
   * @param dest  where to copy to
   * @return number of bytes copied
   * @throws CopyStreamException on error
   ***/
  public static final long copyReader(Reader source, Writer dest) throws CopyStreamException {
    return copyReader(source, dest, DEFAULT_COPY_BUFFER_SIZE);
  }

  /**
   * Closes the object quietly, catching rather than throwing IOException.
   * Intended for use from finally blocks.
   *
   * @param closeable the object to close, may be {@code null}
   * @since 3.0
   */
  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // Ignored
      }
    }
  }

  /**
   * Closes the socket quietly, catching rather than throwing IOException.
   * Intended for use from finally blocks.
   *
   * @param socket the socket to close, may be {@code null}
   * @since 3.0
   */
  public static void closeQuietly(Socket socket) {
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
        // Ignored
      }
    }
  }
}
