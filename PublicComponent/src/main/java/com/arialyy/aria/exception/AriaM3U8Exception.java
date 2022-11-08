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

public class AriaM3U8Exception extends AriaException {
  private static final String M3U8_EXCEPTION = "Aria M3U8 Exception:";

  public AriaM3U8Exception(String message) {
    super(String.format("%s\n%s", M3U8_EXCEPTION, message));
  }

  public AriaM3U8Exception(String message, Exception e) {
    super(message, e);
  }
}
