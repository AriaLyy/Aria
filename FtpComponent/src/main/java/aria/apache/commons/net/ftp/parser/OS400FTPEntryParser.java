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
import java.io.File;
import java.text.ParseException;

/**
 * @version $Id: OS400FTPEntryParser.java 1752660 2016-07-14 13:25:39Z sebb $
 *          <pre>
 *          Example *FILE/*MEM FTP entries, when the current
 *          working directory is a file of file system QSYS:
 *          ------------------------------------------------
 *
 *          $ cwd /qsys.lib/rpgunit.lib/rpgunitc1.file
 *            250-NAMEFMT set to 1.
 *            250 "/QSYS.LIB/RPGUNIT.LIB/RPGUNITC1.FILE" is current directory.
 *          $ dir
 *            227 Entering Passive Mode (10,200,36,33,40,249).
 *            125 List started.
 *            QPGMR          135168 22.06.13 13:18:19 *FILE
 *            QPGMR                                   *MEM       MKCMD.MBR
 *            QPGMR                                   *MEM       RUCALLTST.MBR
 *            QPGMR                                   *MEM       RUCMDHLP.MBR
 *            QPGMR                                   *MEM       RUCRTTST.MBR
 *            250 List completed.
 *
 *
 *          Example *FILE entry of an OS/400 save file:
 *          ---------------------------------------------------
 *
 *          $ cwd /qsys.lib/rpgunit.lib
 *            250 "/QSYS.LIB/RPGUNIT.LIB" is current library.
 *          $ dir rpgunit.file
 *            227 Entering Passive Mode (10,200,36,33,188,106).
 *            125 List started.
 *            QPGMR        16347136 29.06.13 15:45:09 *FILE      RPGUNIT.SAVF
 *            250 List completed.
 *
 *
 *          Example *STMF/*DIR FTP entries, when the
 *          current working directory is in file system "root":
 *          ---------------------------------------------------
 *
 *          $ cwd /home/raddatz
 *            250 "/home/raddatz" is current directory.
 *          $ dir test*
 *            227 Entering Passive Mode (10,200,36,33,200,189).
 *            125 List started.
 *            RADDATZ           200 21.05.11 12:31:18 *STMF      TEST_RG_02_CRLF.properties
 *            RADDATZ           187 08.05.11 12:31:40 *STMF      TEST_RG_02_LF.properties
 *            RADDATZ           187 08.05.11 12:31:52 *STMF      TEST_RG_02_CR.properties
 *            RADDATZ          8192 04.07.13 09:04:14 *DIR       testDir1/
 *            RADDATZ          8192 04.07.13 09:04:17 *DIR       testDir2/
 *            250 List completed.
 *
 *
 *          Example 1, using ANT to list specific members of a file:
 *          --------------------------------------------------------
 *
 *               &lt;echo/&gt;
 *               &lt;echo&gt;Listing members of a file:&lt;/echo&gt;
 *
 *               &lt;ftp action="list"
 *                    server="${ftp.server}"
 *                    userid="${ftp.user}"
 *                    password="${ftp.password}"
 *                    binary="false"
 *                    verbose="true"
 *                    remotedir="/QSYS.LIB/RPGUNIT.LIB/RPGUNITY1.FILE"
 *                    systemTypeKey="OS/400"
 *                    listing="ftp-listing.txt"
 *                    &gt;
 *                   &lt;fileset dir="./i5-downloads-file" casesensitive="false"&gt;
 *                       &lt;include name="run*.mbr" /&gt;
 *                   &lt;/fileset&gt;
 *               &lt;/ftp&gt;
 *
 *          Output:
 *          -------
 *
 *            [echo] Listing members of a file:
 *             [ftp] listing files
 *             [ftp] listing RUN.MBR
 *             [ftp] listing RUNNER.MBR
 *             [ftp] listing RUNNERBND.MBR
 *             [ftp] 3 files listed
 *
 *
 *          Example 2, using ANT to list specific members of all files of a library:
 *          ------------------------------------------------------------------------
 *
 *               &lt;echo/&gt;
 *               &lt;echo&gt;Listing members of all files of a library:&lt;/echo&gt;
 *
 *               &lt;ftp action="list"
 *                    server="${ftp.server}"
 *                    userid="${ftp.user}"
 *                    password="${ftp.password}"
 *                    binary="false"
 *                    verbose="true"
 *                    remotedir="/QSYS.LIB/RPGUNIT.LIB"
 *                    systemTypeKey="OS/400"
 *                    listing="ftp-listing.txt"
 *                    &gt;
 *                   &lt;fileset dir="./i5-downloads-lib" casesensitive="false"&gt;
 *                       &lt;include name="**\run*.mbr" /&gt;
 *                   &lt;/fileset&gt;
 *               &lt;/ftp&gt;
 *
 *          Output:
 *          -------
 *
 *            [echo] Listing members of all files of a library:
 *             [ftp] listing files
 *             [ftp] listing RPGUNIT1.FILE\RUN.MBR
 *             [ftp] listing RPGUNIT1.FILE\RUNRMT.MBR
 *             [ftp] listing RPGUNITT1.FILE\RUNT.MBR
 *             [ftp] listing RPGUNITY1.FILE\RUN.MBR
 *             [ftp] listing RPGUNITY1.FILE\RUNNER.MBR
 *             [ftp] listing RPGUNITY1.FILE\RUNNERBND.MBR
 *             [ftp] 6 files listed
 *
 *
 *          Example 3, using ANT to download specific members of a file:
 *          ------------------------------------------------------------
 *
 *               &lt;echo/&gt;
 *               &lt;echo&gt;Downloading members of a file:&lt;/echo&gt;
 *
 *               &lt;ftp action="get"
 *                    server="${ftp.server}"
 *                    userid="${ftp.user}"
 *                    password="${ftp.password}"
 *                    binary="false"
 *                    verbose="true"
 *                    remotedir="/QSYS.LIB/RPGUNIT.LIB/RPGUNITY1.FILE"
 *                    systemTypeKey="OS/400"
 *                    &gt;
 *                   &lt;fileset dir="./i5-downloads-file" casesensitive="false"&gt;
 *                       &lt;include name="run*.mbr" /&gt;
 *                   &lt;/fileset&gt;
 *               &lt;/ftp&gt;
 *
 *          Output:
 *          -------
 *
 *            [echo] Downloading members of a file:
 *             [ftp] getting files
 *             [ftp] transferring RUN.MBR to C:\workspaces\rdp_080\workspace\ANT -
 *          FTP\i5-downloads-file\RUN.MBR
 *             [ftp] transferring RUNNER.MBR to C:\workspaces\rdp_080\workspace\ANT -
 *          FTP\i5-downloads-file\RUNNER.MBR
 *             [ftp] transferring RUNNERBND.MBR to C:\workspaces\rdp_080\workspace\ANT -
 *          FTP\i5-downloads-file\RUNNERBND.MBR
 *             [ftp] 3 files retrieved
 *
 *
 *          Example 4, using ANT to download specific members of all files of a library:
 *          ----------------------------------------------------------------------------
 *
 *               &lt;echo/&gt;
 *               &lt;echo&gt;Downloading members of all files of a library:&lt;/echo&gt;
 *
 *               &lt;ftp action="get"
 *                    server="${ftp.server}"
 *                    userid="${ftp.user}"
 *                    password="${ftp.password}"
 *                    binary="false"
 *                    verbose="true"
 *                    remotedir="/QSYS.LIB/RPGUNIT.LIB"
 *                    systemTypeKey="OS/400"
 *                    &gt;
 *                   &lt;fileset dir="./i5-downloads-lib" casesensitive="false"&gt;
 *                       &lt;include name="**\run*.mbr" /&gt;
 *                   &lt;/fileset&gt;
 *               &lt;/ftp&gt;
 *
 *          Output:
 *          -------
 *
 *            [echo] Downloading members of all files of a library:
 *             [ftp] getting files
 *             [ftp] transferring RPGUNIT1.FILE\RUN.MBR to C:\work\rdp_080\space\ANT -
 *          FTP\i5-downloads\RPGUNIT1.FILE\RUN.MBR
 *             [ftp] transferring RPGUNIT1.FILE\RUNRMT.MBR to C:\work\rdp_080\space\ANT -
 *          FTP\i5-downloads\RPGUNIT1.FILE\RUNRMT.MBR
 *             [ftp] transferring RPGUNITT1.FILE\RUNT.MBR to C:\work\rdp_080\space\ANT -
 *          FTP\i5-downloads\RPGUNITT1.FILE\RUNT.MBR
 *             [ftp] transferring RPGUNITY1.FILE\RUN.MBR to C:\work\rdp_080\space\ANT -
 *          FTP\i5-downloads\RPGUNITY1.FILE\RUN.MBR
 *             [ftp] transferring RPGUNITY1.FILE\RUNNER.MBR to C:\work\rdp_080\space\ANT -
 *          FTP\i5-downloads\RPGUNITY1.FILE\RUNNER.MBR
 *             [ftp] transferring RPGUNITY1.FILE\RUNNERBND.MBR to C:\work\rdp_080\space\ANT -
 *          FTP\i5-downloads\RPGUNITY1.FILE\RUNNERBND.MBR
 *             [ftp] 6 files retrieved
 *
 *
 *          Example 5, using ANT to download a save file of a library:
 *          ----------------------------------------------------------
 *
 *               &lt;ftp action="get"
 *                    server="${ftp.server}"
 *                    userid="${ftp.user}"
 *                    password="${ftp.password}"
 *                    binary="true"
 *                    verbose="true"
 *                    remotedir="/QSYS.LIB/RPGUNIT.LIB"
 *                    systemTypeKey="OS/400"
 *                    &gt;
 *                 &lt;fileset dir="./i5-downloads-savf" casesensitive="false"&gt;
 *                     &lt;include name="RPGUNIT.SAVF" /&gt;
 *                 &lt;/fileset&gt;
 *               &lt;/ftp&gt;
 *
 *          Output:
 *          -------
 *            [echo] Downloading save file:
 *             [ftp] getting files
 *             [ftp] transferring RPGUNIT.SAVF to C:\workspaces\rdp_080\workspace\net-Test\i5-downloads-lib\RPGUNIT.SAVF
 *             [ftp] 1 files retrieved
 *
 *          </pre>
 */
public class OS400FTPEntryParser extends ConfigurableFTPFileEntryParserImpl {
  private static final String DEFAULT_DATE_FORMAT = "yy/MM/dd HH:mm:ss"; //01/11/09 12:30:24

  private static final String REGEX = "(\\S+)\\s+"                  // user
      + "(?:(\\d+)\\s+)?"           // size, empty for members
      + "(?:(\\S+)\\s+(\\S+)\\s+)?" // date stuff, empty for members
      + "(\\*STMF|\\*DIR|\\*FILE|\\*MEM)\\s+"  // *STMF/*DIR/*FILE/*MEM
      + "(?:(\\S+)\\s*)?";          // filename, missing, when CWD is a *FILE

  /**
   * The default constructor for a OS400FTPEntryParser object.
   *
   * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not
   * be seen
   * under normal conditions.  It it is seen, this is a sign that
   * <code>REGEX</code> is  not a valid regular expression.
   */
  public OS400FTPEntryParser() {
    this(null);
  }

  /**
   * This constructor allows the creation of an OS400FTPEntryParser object
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
  public OS400FTPEntryParser(FTPClientConfig config) {
    super(REGEX);
    configure(config);
  }

  @Override public FTPFile parseFTPEntry(String entry) {

    FTPFile file = new FTPFile();
    file.setRawListing(entry);
    int type;

    if (matches(entry)) {
      String usr = group(1);
      String filesize = group(2);
      String datestr = "";
      if (!isNullOrEmpty(group(3)) || !isNullOrEmpty(group(4))) {
        datestr = group(3) + " " + group(4);
      }
      String typeStr = group(5);
      String name = group(6);

      boolean mustScanForPathSeparator = true;

      try {
        file.setTimestamp(super.parseTimestamp(datestr));
      } catch (ParseException e) {
        // intentionally do nothing
      }

      if (typeStr.equalsIgnoreCase("*STMF")) {
        type = FTPFile.FILE_TYPE;
        if (isNullOrEmpty(filesize) || isNullOrEmpty(name)) {
          return null;
        }
      } else if (typeStr.equalsIgnoreCase("*DIR")) {
        type = FTPFile.DIRECTORY_TYPE;
        if (isNullOrEmpty(filesize) || isNullOrEmpty(name)) {
          return null;
        }
      } else if (typeStr.equalsIgnoreCase("*FILE")) {
        // File, defines the structure of the data (columns of a row)
        // but the data is stored in one or more members. Typically a
        // source file contains multiple members whereas it is
        // recommended (but not enforced) to use one member per data
        // file.
        // Save files are a special type of files which are used
        // to save objects, e.g. for backups.
        if (name != null && name.toUpperCase().endsWith(".SAVF")) {
          mustScanForPathSeparator = false;
          type = FTPFile.FILE_TYPE;
        } else {
          return null;
        }
      } else if (typeStr.equalsIgnoreCase("*MEM")) {
        mustScanForPathSeparator = false;
        type = FTPFile.FILE_TYPE;

        if (isNullOrEmpty(name)) {
          return null;
        }
        if (!(isNullOrEmpty(filesize) && isNullOrEmpty(datestr))) {
          return null;
        }

        // Quick and dirty bug fix to make SelectorUtils work.
        // Class SelectorUtils uses 'File.separator' to splitt
        // a given path into pieces. But actually it had to
        // use the separator of the FTP server, which is a forward
        // slash in case of an AS/400.
        name = name.replace('/', File.separatorChar);
      } else {
        type = FTPFile.UNKNOWN_TYPE;
      }

      file.setType(type);

      file.setUser(usr);

      try {
        file.setSize(Long.parseLong(filesize));
      } catch (NumberFormatException e) {
        // intentionally do nothing
      }

      if (name.endsWith("/")) {
        name = name.substring(0, name.length() - 1);
      }
      if (mustScanForPathSeparator) {
        int pos = name.lastIndexOf('/');
        if (pos > -1) {
          name = name.substring(pos + 1);
        }
      }

      file.setName(name);

      return file;
    }
    return null;
  }

  /**
   * @param string String value that is checked for <code>null</code>
   * or empty.
   * @return <code>true</code> for <code>null</code> or empty values,
   * else <code>false</code>.
   */
  private boolean isNullOrEmpty(String string) {
    if (string == null || string.length() == 0) {
      return true;
    }
    return false;
  }

  /**
   * Defines a default configuration to be used when this class is
   * instantiated without a {@link  FTPClientConfig  FTPClientConfig}
   * parameter being specified.
   *
   * @return the default configuration for this parser.
   */
  @Override protected FTPClientConfig getDefaultConfiguration() {
    return new FTPClientConfig(FTPClientConfig.SYST_OS400, DEFAULT_DATE_FORMAT, null);
  }
}
