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

import java.util.EventListener;

import aria.apache.commons.net.util.ListenerList;

/**
 * The CopyStreamAdapter will relay CopyStreamEvents to a list of listeners
 * when either of its bytesTransferred() methods are called.  Its purpose
 * is to facilitate the notification of the progress of a copy operation
 * performed by one of the static copyStream() methods in
 * org.apache.commons.io.Util to multiple listeners.  The static
 * copyStream() methods invoke the
 * bytesTransfered(long, int) of a CopyStreamListener for performance
 * reasons and also because multiple listeners cannot be registered given
 * that the methods are static.
 *
 * @version $Id: CopyStreamAdapter.java 1741829 2016-05-01 00:24:44Z sebb $
 * @see CopyStreamEvent
 * @see CopyStreamListener
 * @see Util
 */
public class CopyStreamAdapter implements CopyStreamListener {
  private final ListenerList internalListeners;

  /**
   * Creates a new copyStreamAdapter.
   */
  public CopyStreamAdapter() {
    internalListeners = new ListenerList();
  }

  /**
   * This method is invoked by a CopyStreamEvent source after copying
   * a block of bytes from a stream.  The CopyStreamEvent will contain
   * the total number of bytes transferred so far and the number of bytes
   * transferred in the last write.  The CopyStreamAdapater will relay
   * the event to all of its registered listeners, listing itself as the
   * source of the event.
   *
   * @param event The CopyStreamEvent fired by the copying of a block of
   * bytes.
   */
  @Override public void bytesTransferred(CopyStreamEvent event) {
    for (EventListener listener : internalListeners) {
      ((CopyStreamListener) (listener)).bytesTransferred(event);
    }
  }

  /**
   * This method is not part of the JavaBeans model and is used by the
   * static methods in the org.apache.commons.io.Util class for efficiency.
   * It is invoked after a block of bytes to inform the listener of the
   * transfer.  The CopyStreamAdapater will create a CopyStreamEvent
   * from the arguments and relay the event to all of its registered
   * listeners, listing itself as the source of the event.
   *
   * @param totalBytesTransferred The total number of bytes transferred
   * so far by the copy operation.
   * @param bytesTransferred The number of bytes copied by the most recent
   * write.
   * @param streamSize The number of bytes in the stream being copied.
   * This may be equal to CopyStreamEvent.UNKNOWN_STREAM_SIZE if
   * the size is unknown.
   */
  @Override public void bytesTransferred(long totalBytesTransferred, int bytesTransferred,
      long streamSize) {
    for (EventListener listener : internalListeners) {
      ((CopyStreamListener) (listener)).bytesTransferred(totalBytesTransferred, bytesTransferred,
          streamSize);
    }
  }

  /**
   * Registers a CopyStreamListener to receive CopyStreamEvents.
   * Although this method is not declared to be synchronized, it is
   * implemented in a thread safe manner.
   *
   * @param listener The CopyStreamlistener to register.
   */
  public void addCopyStreamListener(CopyStreamListener listener) {
    internalListeners.addListener(listener);
  }

  /**
   * Unregisters a CopyStreamListener.  Although this method is not
   * synchronized, it is implemented in a thread safe manner.
   *
   * @param listener The CopyStreamlistener to unregister.
   */
  public void removeCopyStreamListener(CopyStreamListener listener) {
    internalListeners.removeListener(listener);
  }
}
