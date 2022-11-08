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
package com.arialyy.aria.exception;

public class ExceptionFactory {
  public static final int TYPE_FTP = 1;
  public static final int TYPE_HTTP = 2;
  public static final int TYPE_M3U8 = 3;
  public static final int TYPE_SFTP = 4;
  public static final int TYPE_GROUP = 5;

  public static AriaException getException(int type, String msg, Exception e) {
    switch (type) {
      case TYPE_FTP: {
        return e == null ? new AriaFTPException(msg) : new AriaFTPException(msg, e);
      }
      case TYPE_HTTP: {
        return e == null ? new AriaHTTPException(msg) : new AriaHTTPException(msg, e);
      }
      case TYPE_M3U8: {
        return e == null ? new AriaM3U8Exception(msg) : new AriaM3U8Exception(msg, e);
      }
      case TYPE_SFTP: {
        return e == null ? new AriaSFTPException(msg) : new AriaSFTPException(msg, e);
      }
      case TYPE_GROUP: {
        return e == null ? new AriaGroupException(msg) : new AriaGroupException(msg, e);
      }
    }
    return e == null ? new AriaException(msg) : new AriaException(msg, e);
  }
}
