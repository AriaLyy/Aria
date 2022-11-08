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

/**
 * The CopyStreamListener class can accept CopyStreamEvents to keep track
 * of the progress of a stream copying operation.  However, it is currently
 * not used that way within NetComponents for performance reasons.  Rather
 * the bytesTransferred(long, int) method is called directly rather than
 * passing an event to bytesTransferred(CopyStreamEvent), saving the creation
 * of a CopyStreamEvent instance.  Also, the only place where
 * CopyStreamListener is currently used within NetComponents is in the
 * static methods of the uninstantiable org.apache.commons.io.Util class, which
 * would preclude the use of addCopyStreamListener and
 * removeCopyStreamListener methods.  However, future additions may use the
 * JavaBean event model, which is why the hooks have been included from the
 * beginning.
 *
 * @version $Id: CopyStreamListener.java 1652801 2015-01-18 17:10:05Z sebb $
 * @see CopyStreamEvent
 * @see CopyStreamAdapter
 * @see Util
 */
public interface CopyStreamListener extends EventListener {
  /**
   * This method is invoked by a CopyStreamEvent source after copying
   * a block of bytes from a stream.  The CopyStreamEvent will contain
   * the total number of bytes transferred so far and the number of bytes
   * transferred in the last write.
   *
   * @param event The CopyStreamEvent fired by the copying of a block of
   * bytes.
   */
  public void bytesTransferred(CopyStreamEvent event);

  /**
   * This method is not part of the JavaBeans model and is used by the
   * static methods in the org.apache.commons.io.Util class for efficiency.
   * It is invoked after a block of bytes to inform the listener of the
   * transfer.
   *
   * @param totalBytesTransferred The total number of bytes transferred
   * so far by the copy operation.
   * @param bytesTransferred The number of bytes copied by the most recent
   * write.
   * @param streamSize The number of bytes in the stream being copied.
   * This may be equal to CopyStreamEvent.UNKNOWN_STREAM_SIZE if
   * the size is unknown.
   */
  public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize);
}
