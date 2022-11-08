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

import aria.apache.commons.net.ftp.Configurable;
import aria.apache.commons.net.ftp.FTPClientConfig;
import aria.apache.commons.net.ftp.FTPFile;
import aria.apache.commons.net.ftp.FTPFileEntryParser;
import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for NT Systems.
 *
 * @version $Id: NTFTPEntryParser.java 1752660 2016-07-14 13:25:39Z sebb $
 * @see FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class NTFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {

  private static final String DEFAULT_DATE_FORMAT = "MM-dd-yy hh:mma"; //11-09-01 12:30PM

  private static final String DEFAULT_DATE_FORMAT2 = "MM-dd-yy kk:mm"; //11-09-01 18:30

  private final FTPTimestampParser timestampParser;

  /**
   * this is the regular expression used by this parser.
   */
  private static final String REGEX =
      "(\\S+)\\s+(\\S+)\\s+"          // MM-dd-yy whitespace hh:mma|kk:mm; swallow trailing spaces
          + "(?:(<DIR>)|([0-9]+))\\s+"    // <DIR> or ddddd; swallow trailing spaces
          + "(\\S.*)";                    // First non-space followed by rest of line (name)

  /**
   * The sole constructor for an NTFTPEntryParser object.
   *
   * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not
   * be seen
   * under normal conditions.  It it is seen, this is a sign that
   * <code>REGEX</code> is  not a valid regular expression.
   */
  public NTFTPEntryParser() {
    this(null);
  }

  /**
   * This constructor allows the creation of an NTFTPEntryParser object
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
  public NTFTPEntryParser(FTPClientConfig config) {
    super(REGEX, Pattern.DOTALL);
    configure(config);
    FTPClientConfig config2 =
        new FTPClientConfig(FTPClientConfig.SYST_NT, DEFAULT_DATE_FORMAT2, null);
    config2.setDefaultDateFormatStr(DEFAULT_DATE_FORMAT2);
    this.timestampParser = new FTPTimestampParserImpl();
    ((Configurable) this.timestampParser).configure(config2);
  }

  /**
   * Parses a line of an NT FTP server file listing and converts it into a
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
    f.setRawListing(entry);

    if (matches(entry)) {
      String datestr = group(1) + " " + group(2);
      String dirString = group(3);
      String size = group(4);
      String name = group(5);
      try {
        f.setTimestamp(super.parseTimestamp(datestr));
      } catch (ParseException e) {
        // parsing fails, try the other date format
        try {
          f.setTimestamp(timestampParser.parseTimestamp(datestr));
        } catch (ParseException e2) {
          // intentionally do nothing
        }
      }

      if (null == name || name.equals(".") || name.equals("..")) {
        return (null);
      }
      f.setName(name);

      if ("<DIR>".equals(dirString)) {
        f.setType(FTPFile.DIRECTORY_TYPE);
        f.setSize(0);
      } else {
        f.setType(FTPFile.FILE_TYPE);
        if (null != size) {
          f.setSize(Long.parseLong(size));
        }
      }
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
  @Override public FTPClientConfig getDefaultConfiguration() {
    return new FTPClientConfig(FTPClientConfig.SYST_NT, DEFAULT_DATE_FORMAT, null);
  }
}
