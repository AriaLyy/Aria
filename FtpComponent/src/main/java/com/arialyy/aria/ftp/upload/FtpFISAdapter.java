/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.ftp.upload;

import com.arialyy.aria.util.BufferedRandomAccessFile;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lyy on 2017/9/26.
 * BufferedRandomAccessFile 转 InputStream 适配器
 */
final class FtpFISAdapter extends InputStream {

  private BufferedRandomAccessFile mIs;
  private ProgressCallback mCallback;
  private int count;

  interface ProgressCallback {
    void onProgressCallback(byte[] buffer, int byteOffset, int byteCount) throws IOException;
  }

  FtpFISAdapter(BufferedRandomAccessFile is, ProgressCallback callback) {
    mIs = is;
    mCallback = callback;
  }

  FtpFISAdapter(BufferedRandomAccessFile is) {
    mIs = is;
  }

  @Override public void close() throws IOException {
    mIs.close();
  }

  @Override public int read() throws IOException {
    return mIs.read();
  }

  @Override public int read(byte[] buffer) throws IOException {
    count = mIs.read(buffer);
    if (mCallback != null) {
      mCallback.onProgressCallback(buffer, 0, count);
    }
    return count;
  }

  @Override public int read(byte[] buffer, int byteOffset, int byteCount)
      throws IOException {
    count = mIs.read(buffer, byteOffset, byteCount);
    if (mCallback != null) {
      mCallback.onProgressCallback(buffer, byteOffset, byteCount);
    }
    return count;
  }

  @Override public long skip(long byteCount) throws IOException {
    return mIs.skipBytes((int) byteCount);
  }
}