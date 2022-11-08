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

package aria.apache.commons.net.util;

import java.nio.charset.Charset;

/**
 * Helps dealing with Charsets.
 *
 * @since 3.3
 */
public class Charsets {

  /**
   * Returns a charset object for the given charset name.
   *
   * @param charsetName The name of the requested charset; may be a canonical name, an alias, or
   * null. If null, return the
   * default charset.
   * @return A charset object for the named charset
   */
  public static Charset toCharset(String charsetName) {
    return charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName);
  }

  /**
   * Returns a charset object for the given charset name.
   *
   * @param charsetName The name of the requested charset; may be a canonical name, an alias, or
   * null.
   * If null, return the default charset.
   * @param defaultCharsetName the charset name to use if the requested charset is null
   * @return A charset object for the named charset
   */
  public static Charset toCharset(String charsetName, String defaultCharsetName) {
    return charsetName == null ? Charset.forName(defaultCharsetName) : Charset.forName(charsetName);
  }
}
