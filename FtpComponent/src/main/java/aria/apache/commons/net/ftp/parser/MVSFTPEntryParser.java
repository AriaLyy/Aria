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
import java.util.List;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for IBM zOS/MVS
 * Systems.
 *
 * @version $Id: MVSFTPEntryParser.java 1752660 2016-07-14 13:25:39Z sebb $
 * @see FTPFileEntryParser FTPFileEntryParser (for
 * usage instructions)
 */
public class MVSFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {

  static final int UNKNOWN_LIST_TYPE = -1;
  static final int FILE_LIST_TYPE = 0;
  static final int MEMBER_LIST_TYPE = 1;
  static final int UNIX_LIST_TYPE = 2;
  static final int JES_LEVEL_1_LIST_TYPE = 3;
  static final int JES_LEVEL_2_LIST_TYPE = 4;

  private int isType = UNKNOWN_LIST_TYPE;

  /**
   * Fallback parser for Unix-style listings
   */
  private UnixFTPEntryParser unixFTPEntryParser;

  /**
   * Dates are ignored for file lists, but are used for member lists where
   * possible
   */
  static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm"; // 2001/09/18
  // 13:52

  /**
   * Matches these entries:
   * <pre>
   *  Volume Unit    Referred Ext Used Recfm Lrecl BlkSz Dsorg Dsname
   *  B10142 3390   2006/03/20  2   31  F       80    80  PS   MDI.OKL.WORK
   * </pre>
   */
  static final String FILE_LIST_REGEX = "\\S+\\s+" + // volume
      // ignored
      "\\S+\\s+" + // unit - ignored
      "\\S+\\s+" + // access date - ignored
      "\\S+\\s+" + // extents -ignored
      "\\S+\\s+" + // used - ignored
      "[FV]\\S*\\s+" + // recfm - must start with F or V
      "\\S+\\s+" + // logical record length -ignored
      "\\S+\\s+" + // block size - ignored
      "(PS|PO|PO-E)\\s+" + // Dataset organisation. Many exist
      // but only support: PS, PO, PO-E
      "(\\S+)\\s*"; // Dataset Name (file name)

  /**
   * Matches these entries:
   * <pre>
   *   Name      VV.MM   Created       Changed      Size  Init   Mod   Id
   *   TBSHELF   01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001
   * </pre>
   */
  static final String MEMBER_LIST_REGEX = "(\\S+)\\s+" + // name
      "\\S+\\s+" + // version, modification (ignored)
      "\\S+\\s+" + // create date (ignored)
      "(\\S+)\\s+" + // modification date
      "(\\S+)\\s+" + // modification time
      "\\S+\\s+" + // size in lines (ignored)
      "\\S+\\s+" + // size in lines at creation(ignored)
      "\\S+\\s+" + // lines modified (ignored)
      "\\S+\\s*"; // id of user who modified (ignored)

  /**
   * Matches these entries, note: no header:
   * <pre>
   *   IBMUSER1  JOB01906  OUTPUT    3 Spool Files
   *   012345678901234567890123456789012345678901234
   *             1         2         3         4
   * </pre>
   */
  static final String JES_LEVEL_1_LIST_REGEX = "(\\S+)\\s+" + // job name ignored
      "(\\S+)\\s+" + // job number
      "(\\S+)\\s+" + // job status (OUTPUT,INPUT,ACTIVE)
      "(\\S+)\\s+" + // number of spool files
      "(\\S+)\\s+" + // Text "Spool" ignored
      "(\\S+)\\s*" // Text "Files" ignored
      ;

  /**
   * JES INTERFACE LEVEL 2 parser
   * Matches these entries:
   * <pre>
   * JOBNAME  JOBID    OWNER    STATUS CLASS
   * IBMUSER1 JOB01906 IBMUSER  OUTPUT A        RC=0000 3 spool files
   * IBMUSER  TSU01830 IBMUSER  OUTPUT TSU      ABEND=522 3 spool files
   * </pre>
   * Sample output from FTP session:
   * <pre>
   * ftp> quote site filetype=jes
   * 200 SITE command was accepted
   * ftp> ls
   * 200 Port request OK.
   * 125 List started OK for JESJOBNAME=IBMUSER*, JESSTATUS=ALL and JESOWNER=IBMUSER
   * JOBNAME  JOBID    OWNER    STATUS CLASS
   * IBMUSER1 JOB01906 IBMUSER  OUTPUT A        RC=0000 3 spool files
   * IBMUSER  TSU01830 IBMUSER  OUTPUT TSU      ABEND=522 3 spool files
   * 250 List completed successfully.
   * ftp> ls job01906
   * 200 Port request OK.
   * 125 List started OK for JESJOBNAME=IBMUSER*, JESSTATUS=ALL and JESOWNER=IBMUSER
   * JOBNAME  JOBID    OWNER    STATUS CLASS
   * IBMUSER1 JOB01906 IBMUSER  OUTPUT A        RC=0000
   * --------
   * ID  STEPNAME PROCSTEP C DDNAME   BYTE-COUNT
   * 001 JES2              A JESMSGLG       858
   * 002 JES2              A JESJCL         128
   * 003 JES2              A JESYSMSG       443
   * 3 spool files
   * 250 List completed successfully.
   * </pre>
   */

  static final String JES_LEVEL_2_LIST_REGEX = "(\\S+)\\s+" + // job name ignored
      "(\\S+)\\s+" + // job number
      "(\\S+)\\s+" + // owner ignored
      "(\\S+)\\s+" + // job status (OUTPUT,INPUT,ACTIVE) ignored
      "(\\S+)\\s+" + // job class ignored
      "(\\S+).*" // rest ignored
      ;

    /*
     * ---------------------------------------------------------------------
     * Very brief and incomplete description of the zOS/MVS-filesystem. (Note:
     * "zOS" is the operating system on the mainframe, and is the new name for
     * MVS)
     *
     * The filesystem on the mainframe does not have hierarchal structure as for
     * example the unix filesystem. For a more comprehensive description, please
     * refer to the IBM manuals
     *
     * @LINK:
     * http://publibfp.boulder.ibm.com/cgi-bin/bookmgr/BOOKS/dgt2d440/CONTENTS
     *
     *
     * Dataset names =============
     *
     * A dataset name consist of a number of qualifiers separated by '.', each
     * qualifier can be at most 8 characters, and the total length of a dataset
     * can be max 44 characters including the dots.
     *
     *
     * Dataset organisation ====================
     *
     * A dataset represents a piece of storage allocated on one or more disks.
     * The structure of the storage is described with the field dataset
     * organinsation (DSORG). There are a number of dataset organisations, but
     * only two are usable for FTP transfer.
     *
     * DSORG: PS: sequential, or flat file PO: partitioned dataset PO-E:
     * extended partitioned dataset
     *
     * The PS file is just a flat file, as you would find it on the unix file
     * system.
     *
     * The PO and PO-E files, can be compared to a single level directory
     * structure. A PO file consist of a number of dataset members, or files if
     * you will. It is possible to CD into the file, and to retrieve the
     * individual members.
     *
     *
     * Dataset record format =====================
     *
     * The physical layout of the dataset is described on the dataset itself.
     * There are a number of record formats (RECFM), but just a few is relavant
     * for the FTP transfer.
     *
     * Any one beginning with either F or V can safely used by FTP transfer. All
     * others should only be used with great care, so this version will just
     * ignore the other record formats. F means a fixed number of records per
     * allocated storage, and V means a variable number of records.
     *
     *
     * Other notes ===========
     *
     * The file system supports automatically backup and retrieval of datasets.
     * If a file is backed up, the ftp LIST command will return: ARCIVE Not
     * Direct Access Device KJ.IOP998.ERROR.PL.UNITTEST
     *
     *
     * Implementation notes ====================
     *
     * Only datasets that have dsorg PS, PO or PO-E and have recfm beginning
     * with F or V, is fully parsed.
     *
     * The following fields in FTPFile is used: FTPFile.Rawlisting: Always set.
     * FTPFile.Type: DIRECTORY_TYPE or FILE_TYPE or UNKNOWN FTPFile.Name: name
     * FTPFile.Timestamp: change time or null
     *
     *
     *
     * Additional information ======================
     *
     * The MVS ftp server supports a number of features via the FTP interface.
     * The features are controlled with the FTP command quote site filetype=<SEQ|JES|DB2>
     * SEQ is the default and used for normal file transfer JES is used to
     * interact with the Job Entry Subsystem (JES) similar to a job scheduler
     * DB2 is used to interact with a DB2 subsystem
     *
     * This parser supports SEQ and JES.
     *
     *
     *
     *
     *
     *
     */

  /**
   * The sole constructor for a MVSFTPEntryParser object.
   */
  public MVSFTPEntryParser() {
    super(""); // note the regex is set in preParse.
    super.configure(null); // configure parser with default configurations
  }

  /**
   * Parses a line of an z/OS - MVS FTP server file listing and converts it
   * into a usable format in the form of an <code> FTPFile </code> instance.
   * If the file listing line doesn't describe a file, then
   * <code> null </code> is returned. Otherwise a <code> FTPFile </code>
   * instance representing the file is returned.
   *
   * @param entry A line of text from the file listing
   * @return An FTPFile instance corresponding to the supplied entry
   */
  @Override public FTPFile parseFTPEntry(String entry) {
    boolean isParsed = false;
    FTPFile f = new FTPFile();

    if (isType == FILE_LIST_TYPE) {
      isParsed = parseFileList(f, entry);
    } else if (isType == MEMBER_LIST_TYPE) {
      isParsed = parseMemberList(f, entry);
      if (!isParsed) {
        isParsed = parseSimpleEntry(f, entry);
      }
    } else if (isType == UNIX_LIST_TYPE) {
      isParsed = parseUnixList(f, entry);
    } else if (isType == JES_LEVEL_1_LIST_TYPE) {
      isParsed = parseJeslevel1List(f, entry);
    } else if (isType == JES_LEVEL_2_LIST_TYPE) {
      isParsed = parseJeslevel2List(f, entry);
    }

    if (!isParsed) {
      f = null;
    }

    return f;
  }

  /**
   * Parse entries representing a dataset list. Only datasets with DSORG PS or
   * PO or PO-E and with RECFM F* or V* will be parsed.
   *
   * Format of ZOS/MVS file list: 1 2 3 4 5 6 7 8 9 10 Volume Unit Referred
   * Ext Used Recfm Lrecl BlkSz Dsorg Dsname B10142 3390 2006/03/20 2 31 F 80
   * 80 PS MDI.OKL.WORK ARCIVE Not Direct Access Device
   * KJ.IOP998.ERROR.PL.UNITTEST B1N231 3390 2006/03/20 1 15 VB 256 27998 PO
   * PLU B1N231 3390 2006/03/20 1 15 VB 256 27998 PO-E PLB
   *
   * ----------------------------------- Group within Regex [1] Volume [2]
   * Unit [3] Referred [4] Ext: number of extents [5] Used [6] Recfm: Record
   * format [7] Lrecl: Logical record length [8] BlkSz: Block size [9] Dsorg:
   * Dataset organisation. Many exists but only support: PS, PO, PO-E [10]
   * Dsname: Dataset name
   *
   * Note: When volume is ARCIVE, it means the dataset is stored somewhere in
   * a tape archive. These entries is currently not supported by this parser.
   * A null value is returned.
   *
   * @param file will be updated with Name, Type, Timestamp if parsed.
   * @param entry zosDirectoryEntry
   * @return true: entry was parsed, false: entry was not parsed.
   */
  private boolean parseFileList(FTPFile file, String entry) {
    if (matches(entry)) {
      file.setRawListing(entry);
      String name = group(2);
      String dsorg = group(1);
      file.setName(name);

      // DSORG
      if ("PS".equals(dsorg)) {
        file.setType(FTPFile.FILE_TYPE);
      } else if ("PO".equals(dsorg) || "PO-E".equals(dsorg)) {
        // regex already ruled out anything other than PO or PO-E
        file.setType(FTPFile.DIRECTORY_TYPE);
      } else {
        return false;
      }

      return true;
    }

    return false;
  }

  /**
   * Parse entries within a partitioned dataset.
   *
   * Format of a memberlist within a PDS:
   * <pre>
   *    0         1        2          3        4     5     6      7    8
   *   Name      VV.MM   Created       Changed      Size  Init   Mod   Id
   *   TBSHELF   01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001
   *   TBTOOL    01.12 2002/09/12 2004/11/26 19:54    51    28     0 KIL001
   *
   * -------------------------------------------
   * [1] Name
   * [2] VV.MM: Version . modification
   * [3] Created: yyyy / MM / dd
   * [4,5] Changed: yyyy / MM / dd HH:mm
   * [6] Size: number of lines
   * [7] Init: number of lines when first created
   * [8] Mod: number of modified lines a last save
   * [9] Id: User id for last update
   * </pre>
   *
   * @param file will be updated with Name, Type and Timestamp if parsed.
   * @param entry zosDirectoryEntry
   * @return true: entry was parsed, false: entry was not parsed.
   */
  private boolean parseMemberList(FTPFile file, String entry) {
    if (matches(entry)) {
      file.setRawListing(entry);
      String name = group(1);
      String datestr = group(2) + " " + group(3);
      file.setName(name);
      file.setType(FTPFile.FILE_TYPE);
      try {
        file.setTimestamp(super.parseTimestamp(datestr));
      } catch (ParseException e) {
        e.printStackTrace();
        // just ignore parsing errors.
        // TODO check this is ok
        return false; // this is a parsing failure too.
      }
      return true;
    }

    return false;
  }

  /**
   * Assigns the name to the first word of the entry. Only to be used from a
   * safe context, for example from a memberlist, where the regex for some
   * reason fails. Then just assign the name field of FTPFile.
   *
   * @return true if the entry string is non-null and non-empty
   */
  private boolean parseSimpleEntry(FTPFile file, String entry) {
    if (entry != null && entry.trim().length() > 0) {
      file.setRawListing(entry);
      String name = entry.split(" ")[0];
      file.setName(name);
      file.setType(FTPFile.FILE_TYPE);
      return true;
    }
    return false;
  }

  /**
   * Parse the entry as a standard unix file. Using the UnixFTPEntryParser.
   *
   * @return true: entry is parsed, false: entry could not be parsed.
   */
  private boolean parseUnixList(FTPFile file, String entry) {
    file = unixFTPEntryParser.parseFTPEntry(entry);
    if (file == null) {
      return false;
    }
    return true;
  }

  /**
   * Matches these entries, note: no header:
   * <pre>
   * [1]      [2]      [3]   [4] [5]
   * IBMUSER1 JOB01906 OUTPUT 3 Spool Files
   * 012345678901234567890123456789012345678901234
   *           1         2         3         4
   * -------------------------------------------
   * Group in regex
   * [1] Job name
   * [2] Job number
   * [3] Job status (INPUT,ACTIVE,OUTPUT)
   * [4] Number of sysout files
   * [5] The string "Spool Files"
   * </pre>
   *
   * @param file will be updated with Name, Type and Timestamp if parsed.
   * @param entry zosDirectoryEntry
   * @return true: entry was parsed, false: entry was not parsed.
   */
  private boolean parseJeslevel1List(FTPFile file, String entry) {
    if (matches(entry)) {
      if (group(3).equalsIgnoreCase("OUTPUT")) {
        file.setRawListing(entry);
        String name = group(2); /* Job Number, used by GET */
        file.setName(name);
        file.setType(FTPFile.FILE_TYPE);
        return true;
      }
    }

    return false;
  }

  /**
   * Matches these entries:
   * <pre>
   * [1]      [2]      [3]     [4]    [5]
   * JOBNAME  JOBID    OWNER   STATUS CLASS
   * IBMUSER1 JOB01906 IBMUSER OUTPUT A       RC=0000 3 spool files
   * IBMUSER  TSU01830 IBMUSER OUTPUT TSU     ABEND=522 3 spool files
   * 012345678901234567890123456789012345678901234
   *           1         2         3         4
   * -------------------------------------------
   * Group in regex
   * [1] Job name
   * [2] Job number
   * [3] Owner
   * [4] Job status (INPUT,ACTIVE,OUTPUT)
   * [5] Job Class
   * [6] The rest
   * </pre>
   *
   * @param file will be updated with Name, Type and Timestamp if parsed.
   * @param entry zosDirectoryEntry
   * @return true: entry was parsed, false: entry was not parsed.
   */
  private boolean parseJeslevel2List(FTPFile file, String entry) {
    if (matches(entry)) {
      if (group(4).equalsIgnoreCase("OUTPUT")) {
        file.setRawListing(entry);
        String name = group(2); /* Job Number, used by GET */
        file.setName(name);
        file.setType(FTPFile.FILE_TYPE);
        return true;
      }
    }

    return false;
  }

  /**
   * preParse is called as part of the interface. Per definition is is called
   * before the parsing takes place.
   * Three kind of lists is recognize:
   * z/OS-MVS File lists
   * z/OS-MVS Member lists
   * unix file lists
   *
   * @since 2.0
   */
  @Override public List<String> preParse(List<String> orig) {
    // simply remove the header line. Composite logic will take care of the
    // two different types of
    // list in short order.
    if (orig != null && orig.size() > 0) {
      String header = orig.get(0);
      if (header.indexOf("Volume") >= 0 && header.indexOf("Dsname") >= 0) {
        setType(FILE_LIST_TYPE);
        super.setRegex(FILE_LIST_REGEX);
      } else if (header.indexOf("Name") >= 0 && header.indexOf("Id") >= 0) {
        setType(MEMBER_LIST_TYPE);
        super.setRegex(MEMBER_LIST_REGEX);
      } else if (header.indexOf("total") == 0) {
        setType(UNIX_LIST_TYPE);
        unixFTPEntryParser = new UnixFTPEntryParser();
      } else if (header.indexOf("Spool Files") >= 30) {
        setType(JES_LEVEL_1_LIST_TYPE);
        super.setRegex(JES_LEVEL_1_LIST_REGEX);
      } else if (header.indexOf("JOBNAME") == 0
          && header.indexOf("JOBID") > 8) {// header contains JOBNAME JOBID OWNER // STATUS CLASS
        setType(JES_LEVEL_2_LIST_TYPE);
        super.setRegex(JES_LEVEL_2_LIST_REGEX);
      } else {
        setType(UNKNOWN_LIST_TYPE);
      }

      if (isType != JES_LEVEL_1_LIST_TYPE) { // remove header is necessary
        orig.remove(0);
      }
    }

    return orig;
  }

  /**
   * Explicitly set the type of listing being processed.
   *
   * @param type The listing type.
   */
  void setType(int type) {
    isType = type;
  }

  /*
   * @return
   */
  @Override protected FTPClientConfig getDefaultConfiguration() {
    return new FTPClientConfig(FTPClientConfig.SYST_MVS, DEFAULT_DATE_FORMAT, null);
  }
}
