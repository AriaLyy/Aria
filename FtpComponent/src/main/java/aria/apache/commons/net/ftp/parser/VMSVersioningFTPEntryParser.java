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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Special implementation VMSFTPEntryParser with versioning turned on.
 * This parser removes all duplicates and only leaves the version with the highest
 * version number for each filename.
 *
 * This is a sample of VMS LIST output
 *
 * "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]
 * (RWED,RWED,RWED,RE)",
 * "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]
 * (RWED,RWED,RWED,RE)",
 * "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]
 * (RWED,RWED,RWED,RE)",
 * <P>
 *
 * @version $Id: VMSVersioningFTPEntryParser.java 1747119 2016-06-07 02:22:24Z ggregory $
 * @see FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class VMSVersioningFTPEntryParser extends VMSFTPEntryParser {

  private final Pattern _preparse_pattern_;
  private static final String PRE_PARSE_REGEX = "(.*?);([0-9]+)\\s*.*";

  /**
   * Constructor for a VMSFTPEntryParser object.
   *
   * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not
   * be seen
   * under normal conditions.  It it is seen, this is a sign that
   * <code>REGEX</code> is  not a valid regular expression.
   */
  public VMSVersioningFTPEntryParser() {
    this(null);
  }

  /**
   * This constructor allows the creation of a VMSVersioningFTPEntryParser
   * object with something other than the default configuration.
   *
   * @param config The {@link FTPClientConfig configuration} object used to
   * configure this parser.
   * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not
   * be seen
   * under normal conditions.  It it is seen, this is a sign that
   * <code>REGEX</code> is  not a valid regular expression.
   * @since 1.4
   */
  public VMSVersioningFTPEntryParser(FTPClientConfig config) {
    super();
    configure(config);
    try {
      //_preparse_matcher_ = new Perl5Matcher();
      _preparse_pattern_ = Pattern.compile(PRE_PARSE_REGEX);
    } catch (PatternSyntaxException pse) {
      throw new IllegalArgumentException("Unparseable regex supplied:  " + PRE_PARSE_REGEX);
    }
  }

  /**
   * Implement hook provided for those implementers (such as
   * VMSVersioningFTPEntryParser, and possibly others) which return
   * multiple files with the same name to remove the duplicates ..
   *
   * @param original Original list
   * @return Original list purged of duplicates
   */
  @Override public List<String> preParse(List<String> original) {
    HashMap<String, Integer> existingEntries = new HashMap<String, Integer>();
    ListIterator<String> iter = original.listIterator();
    while (iter.hasNext()) {
      String entry = iter.next().trim();
      MatchResult result = null;
      Matcher _preparse_matcher_ = _preparse_pattern_.matcher(entry);
      if (_preparse_matcher_.matches()) {
        result = _preparse_matcher_.toMatchResult();
        String name = result.group(1);
        String version = result.group(2);
        Integer nv = Integer.valueOf(version);
        Integer existing = existingEntries.get(name);
        if (null != existing) {
          if (nv.intValue() < existing.intValue()) {
            iter.remove();  // removes older version from original list.
            continue;
          }
        }
        existingEntries.put(name, nv);
      }
    }
    // we've now removed all entries less than with less than the largest
    // version number for each name that were listed after the largest.
    // we now must remove those with smaller than the largest version number
    // for each name that were found before the largest
    while (iter.hasPrevious()) {
      String entry = iter.previous().trim();
      MatchResult result = null;
      Matcher _preparse_matcher_ = _preparse_pattern_.matcher(entry);
      if (_preparse_matcher_.matches()) {
        result = _preparse_matcher_.toMatchResult();
        String name = result.group(1);
        String version = result.group(2);
        Integer nv = Integer.valueOf(version);
        Integer existing = existingEntries.get(name);
        if (null != existing) {
          if (nv.intValue() < existing.intValue()) {
            iter.remove(); // removes older version from original list.
          }
        }
      }
    }
    return original;
  }

  @Override protected boolean isVersioning() {
    return true;
  }
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
