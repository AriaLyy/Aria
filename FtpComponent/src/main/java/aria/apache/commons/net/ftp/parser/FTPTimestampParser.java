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

import java.text.ParseException;
import java.util.Calendar;

/**
 * This interface specifies the concept of parsing an FTP server's
 * timestamp.
 *
 * @since 1.4
 */
public interface FTPTimestampParser {

  /**
   * the default default date format.
   */
  public static final String DEFAULT_SDF = UnixFTPEntryParser.DEFAULT_DATE_FORMAT;
  /**
   * the default recent date format.
   */
  public static final String DEFAULT_RECENT_SDF = UnixFTPEntryParser.DEFAULT_RECENT_DATE_FORMAT;

  /**
   * Parses the supplied datestamp parameter.  This parameter typically would
   * have been pulled from a longer FTP listing via the regular expression
   * mechanism
   *
   * @param timestampStr - the timestamp portion of the FTP directory listing
   * to be parsed
   * @return a <code>java.util.Calendar</code> object initialized to the date
   * parsed by the parser
   * @throws ParseException if none of the parser mechanisms belonging to
   * the implementor can parse the input.
   */
  public Calendar parseTimestamp(String timestampStr) throws ParseException;
}
