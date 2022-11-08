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
import aria.apache.commons.net.ftp.FTPFile;
import aria.apache.commons.net.ftp.FTPFileEntryParser;
import java.text.ParseException;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for OS2 Systems.
 *
 * @version $Id: OS2FTPEntryParser.java 1752660 2016-07-14 13:25:39Z sebb $
 * @see FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class OS2FTPEntryParser extends ConfigurableFTPFileEntryParserImpl

{

  private static final String DEFAULT_DATE_FORMAT = "MM-dd-yy HH:mm"; //11-09-01 12:30
  /**
   * this is the regular expression used by this parser.
   */
  private static final String REGEX = "\\s*([0-9]+)\\s*"
      + "(\\s+|[A-Z]+)\\s*"
      + "(DIR|\\s+)\\s*"
      + "(\\S+)\\s+(\\S+)\\s+" /* date stuff */
      + "(\\S.*)";

  /**
   * The default constructor for a OS2FTPEntryParser object.
   *
   * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not
   * be seen
   * under normal conditions.  It it is seen, this is a sign that
   * <code>REGEX</code> is  not a valid regular expression.
   */
  public OS2FTPEntryParser() {
    this(null);
  }

  /**
   * This constructor allows the creation of an OS2FTPEntryParser object
   * with something other than the default configuration.
   *
   * @param config The {@link FTPClientConfig configuration} object used to
   * configure this parser.
   * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not
   * be seen
   * under normal conditions.  It it is seen, this is a sign that
   * <code>REGEX</code> is  not a valid regular expression.
   * @since 1.4
   */
  public OS2FTPEntryParser(FTPClientConfig config) {
    super(REGEX);
    configure(config);
  }

  /**
   * Parses a line of an OS2 FTP server file listing and converts it into a
   * usable format in the form of an <code> FTPFile </code> instance.  If the
   * file listing line doesn't describe a file, <code> null </code> is
   * returned, otherwise a <code> FTPFile </code> instance representing the
   * files in the directory is returned.
   *
   * @param entry A line of text from the file listing
   * @return An FTPFile instance corresponding to the supplied entry
   */
  @Override public FTPFile parseFTPEntry(String entry) {

    FTPFile f = new FTPFile();
    if (matches(entry)) {
      String size = group(1);
      String attrib = group(2);
      String dirString = group(3);
      String datestr = group(4) + " " + group(5);
      String name = group(6);
      try {
        f.setTimestamp(super.parseTimestamp(datestr));
      } catch (ParseException e) {
        // intentionally do nothing
      }

      //is it a DIR or a file
      if (dirString.trim().equals("DIR") || attrib.trim().equals("DIR")) {
        f.setType(FTPFile.DIRECTORY_TYPE);
      } else {
        f.setType(FTPFile.FILE_TYPE);
      }

      //set the name
      f.setName(name.trim());

      //set the size
      f.setSize(Long.parseLong(size.trim()));

      return (f);
    }
    return null;
  }

  /**
   * Defines a default configuration to be used when this class is
   * instantiated without a {@link  FTPClientConfig  FTPClientConfig}
   * parameter being specified.
   *
   * @return the default configuration for this parser.
   */
  @Override protected FTPClientConfig getDefaultConfiguration() {
    return new FTPClientConfig(FTPClientConfig.SYST_OS2, DEFAULT_DATE_FORMAT, null);
  }
}
