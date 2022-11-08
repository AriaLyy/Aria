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

import aria.apache.commons.net.ftp.FTPFile;
import aria.apache.commons.net.ftp.FTPFileEntryParser;
import java.util.Calendar;

/**
 * Parser for the Connect Enterprise Unix FTP Server From Sterling Commerce.
 * Here is a sample of the sort of output line this parser processes:
 * "-C--E-----FTP B QUA1I1      18128       41 Aug 12 13:56 QUADTEST"
 * <P><B>
 * Note: EnterpriseUnixFTPEntryParser can only be instantiated through the
 * DefaultFTPParserFactory by classname.  It will not be chosen
 * by the autodetection scheme.
 * </B>
 *
 * @version $Id: EnterpriseUnixFTPEntryParser.java 1741829 2016-05-01 00:24:44Z sebb $
 * @see FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 * @see DefaultFTPFileEntryParserFactory
 */
public class EnterpriseUnixFTPEntryParser extends RegexFTPFileEntryParserImpl {

  /**
   * months abbreviations looked for by this parser.  Also used
   * to determine <b>which</b> month has been matched by the parser.
   */
  private static final String MONTHS = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";

  /**
   * this is the regular expression used by this parser.
   */
  private static final String REGEX =
      "(([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])"
          + "([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z]))"
          + "(\\S*)\\s*"
          // 12
          + "(\\S+)\\s*"
          // 13
          + "(\\S*)\\s*"
          // 14 user
          + "(\\d*)\\s*"
          // 15 group
          + "(\\d*)\\s*"
          // 16 filesize
          + MONTHS
          // 17 month
          + "\\s*"
          // TODO should the space be optional?
          // TODO \\d* should be \\d? surely ? Otherwise 01111 is allowed
          + "((?:[012]\\d*)|(?:3[01]))\\s*"
          // 18 date [012]\d* or 3[01]
          + "((\\d\\d\\d\\d)|((?:[01]\\d)|(?:2[0123])):([012345]\\d))\\s"
          // 20 \d\d\d\d  = year  OR
          // 21 [01]\d or 2[0123] hour + ':'
          // 22 [012345]\d = minute
          + "(\\S*)(\\s*.*)"; // 23 name

  /**
   * The sole constructor for a EnterpriseUnixFTPEntryParser object.
   */
  public EnterpriseUnixFTPEntryParser() {
    super(REGEX);
  }

  /**
   * Parses a line of a unix FTP server file listing and converts  it into a
   * usable format in the form of an <code> FTPFile </code>  instance.  If
   * the file listing line doesn't describe a file,  <code> null </code> is
   * returned, otherwise a <code> FTPFile </code>  instance representing the
   * files in the directory is returned.
   *
   * @param entry A line of text from the file listing
   * @return An FTPFile instance corresponding to the supplied entry
   */
  @Override public FTPFile parseFTPEntry(String entry) {

    FTPFile file = new FTPFile();
    file.setRawListing(entry);

    if (matches(entry)) {
      String usr = group(14);
      String grp = group(15);
      String filesize = group(16);
      String mo = group(17);
      String da = group(18);
      String yr = group(20);
      String hr = group(21);
      String min = group(22);
      String name = group(23);

      file.setType(FTPFile.FILE_TYPE);
      file.setUser(usr);
      file.setGroup(grp);
      try {
        file.setSize(Long.parseLong(filesize));
      } catch (NumberFormatException e) {
        // intentionally do nothing
      }

      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.MILLISECOND, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.HOUR_OF_DAY, 0);

      int pos = MONTHS.indexOf(mo);
      int month = pos / 4;
      final int missingUnit; // the first missing unit
      try {

        if (yr != null) {
          // it's a year; there are no hours and minutes
          cal.set(Calendar.YEAR, Integer.parseInt(yr));
          missingUnit = Calendar.HOUR_OF_DAY;
        } else {
          // it must be  hour/minute or we wouldn't have matched
          missingUnit = Calendar.SECOND;
          int year = cal.get(Calendar.YEAR);

          // if the month we're reading is greater than now, it must
          // be last year
          if (cal.get(Calendar.MONTH) < month) {
            year--;
          }
          cal.set(Calendar.YEAR, year);
          cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hr));
          cal.set(Calendar.MINUTE, Integer.parseInt(min));
        }
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(da));
        cal.clear(missingUnit);
        file.setTimestamp(cal);
      } catch (NumberFormatException e) {
        // do nothing, date will be uninitialized
      }
      file.setName(name);

      return file;
    }
    return null;
  }
}
