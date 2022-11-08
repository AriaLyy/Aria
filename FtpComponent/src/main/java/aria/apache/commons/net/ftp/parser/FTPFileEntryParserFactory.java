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

package aria.apache.commons.net.ftp.parser;

import aria.apache.commons.net.ftp.FTPClientConfig;
import aria.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * The interface describes a factory for creating FTPFileEntryParsers.
 *
 * @since 1.2
 */
public interface FTPFileEntryParserFactory {
  /**
   * Implementation should be a method that decodes the
   * supplied key and creates an object implementing the
   * interface FTPFileEntryParser.
   *
   * @param key A string that somehow identifies an
   * FTPFileEntryParser to be created.
   * @return the FTPFileEntryParser created.
   * @throws ParserInitializationException Thrown on any exception in instantiation
   */
  public FTPFileEntryParser createFileEntryParser(String key) throws ParserInitializationException;

  /**
   * <p>
   * Implementation should be a method that extracts
   * a key from the supplied {@link  FTPClientConfig FTPClientConfig}
   * parameter and creates an object implementing the
   * interface FTPFileEntryParser and uses the supplied configuration
   * to configure it.
   * </p><p>
   * Note that this method will generally not be called in scenarios
   * that call for autodetection of parser type but rather, for situations
   * where the user knows that the server uses a non-default configuration
   * and knows what that configuration is.
   * </p>
   *
   * @param config A {@link  FTPClientConfig FTPClientConfig}
   * used to configure the parser created
   * @return the @link  FTPFileEntryParser FTPFileEntryParser} so created.
   * @throws ParserInitializationException Thrown on any exception in instantiation
   * @since 1.4
   */
  public FTPFileEntryParser createFileEntryParser(FTPClientConfig config)
      throws ParserInitializationException;
}
