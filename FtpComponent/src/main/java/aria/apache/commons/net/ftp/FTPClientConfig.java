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

package aria.apache.commons.net.ftp;

import aria.apache.commons.net.ftp.parser.ConfigurableFTPFileEntryParserImpl;
import aria.apache.commons.net.ftp.parser.FTPTimestampParserImpl;
import java.text.DateFormatSymbols;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * <p>
 * This class implements an alternate means of configuring the
 * {@link  FTPClient  FTPClient} object and
 * also subordinate objects which it uses.  Any class implementing the
 * {@link  Configurable  Configurable }
 * interface can be configured by this object.
 * </p><p>
 * In particular this class was designed primarily to support configuration
 * of FTP servers which express file timestamps in formats and languages
 * other than those for the US locale, which although it is the most common
 * is not universal.  Unfortunately, nothing in the FTP spec allows this to
 * be determined in an automated way, so manual configuration such as this
 * is necessary.
 * </p><p>
 * This functionality was designed to allow existing clients to work exactly
 * as before without requiring use of this component.  This component should
 * only need to be explicitly invoked by the user of this package for problem
 * cases that previous implementations could not solve.
 * </p>
 * <h3>Examples of use of FTPClientConfig</h3>
 * Use cases:
 * You are trying to access a server that
 * <ul>
 * <li>lists files with timestamps that use month names in languages other
 * than English</li>
 * <li>lists files with timestamps that use date formats other
 * than the American English "standard" <code>MM dd yyyy</code></li>
 * <li>is in different timezone and you need accurate timestamps for
 * dependency checking as in Ant</li>
 * </ul>
 * <p>
 * Unpaged (whole list) access on a UNIX server that uses French month names
 * but uses the "standard" <code>MMM d yyyy</code> date formatting
 * <pre>
 *    FTPClient f=FTPClient();
 *    FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
 *    conf.setServerLanguageCode("fr");
 *    f.configure(conf);
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = listFiles(directory);
 * </pre>
 * <p>
 * Paged access on a UNIX server that uses Danish month names
 * and "European" date formatting in Denmark's time zone, when you
 * are in some other time zone.
 * <pre>
 *    FTPClient f=FTPClient();
 *    FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
 *    conf.setServerLanguageCode("da");
 *    conf.setDefaultDateFormat("d MMM yyyy");
 *    conf.setRecentDateFormat("d MMM HH:mm");
 *    conf.setTimeZoneId("Europe/Copenhagen");
 *    f.configure(conf);
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPListParseEngine engine =
 *       f.initiateListParsing("com.whatever.YourOwnParser", directory);
 *
 *    while (engine.hasNext()) {
 *       FTPFile[] files = engine.getNext(25);  // "page size" you want
 *       //do whatever you want with these files, display them, etc.
 *       //expensive FTPFile objects not created until needed.
 *    }
 * </pre>
 * <p>
 * Unpaged (whole list) access on a VMS server that uses month names
 * in a language not {@link #getSupportedLanguageCodes() supported} by the system.
 * but uses the "standard" <code>MMM d yyyy</code> date formatting
 * <pre>
 *    FTPClient f=FTPClient();
 *    FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_VMS);
 *    conf.setShortMonthNames(
 *        "jan|feb|mar|apr|ma\u00ED|j\u00FAn|j\u00FAl|\u00e1g\u00FA|sep|okt|n\u00F3v|des");
 *    f.configure(conf);
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = listFiles(directory);
 * </pre>
 * <p>
 * Unpaged (whole list) access on a Windows-NT server in a different time zone.
 * (Note, since the NT Format uses numeric date formatting, language issues
 * are irrelevant here).
 * <pre>
 *    FTPClient f=FTPClient();
 *    FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
 *    conf.setTimeZoneId("America/Denver");
 *    f.configure(conf);
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = listFiles(directory);
 * </pre>
 * Unpaged (whole list) access on a Windows-NT server in a different time zone
 * but which has been configured to use a unix-style listing format.
 * <pre>
 *    FTPClient f=FTPClient();
 *    FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
 *    conf.setTimeZoneId("America/Denver");
 *    f.configure(conf);
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = listFiles(directory);
 * </pre>
 *
 * @see Configurable
 * @see FTPClient
 * @see FTPTimestampParserImpl#configure(FTPClientConfig)
 * @see ConfigurableFTPFileEntryParserImpl
 * @since 1.4
 */
public class FTPClientConfig {

  /**
   * Identifier by which a unix-based ftp server is known throughout
   * the commons-net ftp system.
   */
  public static final String SYST_UNIX = "UNIX";

  /**
   * Identifier for alternate UNIX parser; same as {@link #SYST_UNIX} but leading spaces are
   * trimmed from file names. This is to maintain backwards compatibility with
   * the original behaviour of the parser which ignored multiple spaces between the date
   * and the start of the file name.
   *
   * @since 3.4
   */
  public static final String SYST_UNIX_TRIM_LEADING = "UNIX_LTRIM";

  /**
   * Identifier by which a vms-based ftp server is known throughout
   * the commons-net ftp system.
   */
  public static final String SYST_VMS = "VMS";

  /**
   * Identifier by which a WindowsNT-based ftp server is known throughout
   * the commons-net ftp system.
   */
  public static final String SYST_NT = "WINDOWS";

  /**
   * Identifier by which an OS/2-based ftp server is known throughout
   * the commons-net ftp system.
   */
  public static final String SYST_OS2 = "OS/2";

  /**
   * Identifier by which an OS/400-based ftp server is known throughout
   * the commons-net ftp system.
   */
  public static final String SYST_OS400 = "OS/400";

  /**
   * Identifier by which an AS/400-based ftp server is known throughout
   * the commons-net ftp system.
   */
  public static final String SYST_AS400 = "AS/400";

  /**
   * Identifier by which an MVS-based ftp server is known throughout
   * the commons-net ftp system.
   */
  public static final String SYST_MVS = "MVS";

  /**
   * Some servers return an "UNKNOWN Type: L8" message
   * in response to the SYST command. We set these to be a Unix-type system.
   * This may happen if the ftpd in question was compiled without system
   * information.
   *
   * NET-230 - Updated to be UPPERCASE so that the check done in
   * createFileEntryParser will succeed.
   *
   * @since 1.5
   */
  public static final String SYST_L8 = "TYPE: L8";

  /**
   * Identifier by which an Netware-based ftp server is known throughout
   * the commons-net ftp system.
   *
   * @since 1.5
   */
  public static final String SYST_NETWARE = "NETWARE";

  /**
   * Identifier by which a Mac pre OS-X -based ftp server is known throughout
   * the commons-net ftp system.
   *
   * @since 3.1
   */
  // Full string is "MACOS Peter's Server"; the substring below should be enough
  public static final String SYST_MACOS_PETER = "MACOS PETER"; // NET-436

  private final String serverSystemKey;
  private String defaultDateFormatStr = null;
  private String recentDateFormatStr = null;
  private boolean lenientFutureDates = true; // NET-407
  private String serverLanguageCode = null;
  private String shortMonthNames = null;
  private String serverTimeZoneId = null;
  private boolean saveUnparseableEntries = false;

  /**
   * The main constructor for an FTPClientConfig object
   *
   * @param systemKey key representing system type of the  server being
   * connected to. See {@link #getServerSystemKey() serverSystemKey}
   * If set to the empty string, then FTPClient uses the system type returned by the server.
   * However this is not recommended for general use;
   * the correct system type should be set if it is known.
   */
  public FTPClientConfig(String systemKey) {
    this.serverSystemKey = systemKey;
  }

  /**
   * Convenience constructor mainly for use in testing.
   * Constructs a UNIX configuration.
   */
  public FTPClientConfig() {
    this(SYST_UNIX);
  }

  /**
   * Constructor which allows setting of the format string member fields
   *
   * @param systemKey key representing system type of the  server being
   * connected to. See
   * {@link #getServerSystemKey() serverSystemKey}
   * @param defaultDateFormatStr See
   * {@link  #setDefaultDateFormatStr(String)  defaultDateFormatStr}
   * @param recentDateFormatStr See
   * {@link  #setRecentDateFormatStr(String)  recentDateFormatStr}
   * @since 3.6
   */
  public FTPClientConfig(String systemKey, String defaultDateFormatStr,
      String recentDateFormatStr) {
    this(systemKey);
    this.defaultDateFormatStr = defaultDateFormatStr;
    this.recentDateFormatStr = recentDateFormatStr;
  }

  /**
   * Constructor which allows setting of most member fields
   *
   * @param systemKey key representing system type of the  server being
   * connected to. See
   * {@link #getServerSystemKey() serverSystemKey}
   * @param defaultDateFormatStr See
   * {@link  #setDefaultDateFormatStr(String)  defaultDateFormatStr}
   * @param recentDateFormatStr See
   * {@link  #setRecentDateFormatStr(String)  recentDateFormatStr}
   * @param serverLanguageCode See
   * {@link  #setServerLanguageCode(String)  serverLanguageCode}
   * @param shortMonthNames See
   * {@link  #setShortMonthNames(String)  shortMonthNames}
   * @param serverTimeZoneId See
   * {@link  #setServerTimeZoneId(String)  serverTimeZoneId}
   */
  public FTPClientConfig(String systemKey, String defaultDateFormatStr, String recentDateFormatStr,
      String serverLanguageCode, String shortMonthNames, String serverTimeZoneId) {
    this(systemKey);
    this.defaultDateFormatStr = defaultDateFormatStr;
    this.recentDateFormatStr = recentDateFormatStr;
    this.serverLanguageCode = serverLanguageCode;
    this.shortMonthNames = shortMonthNames;
    this.serverTimeZoneId = serverTimeZoneId;
  }

  /**
   * Constructor which allows setting of all member fields
   *
   * @param systemKey key representing system type of the  server being
   * connected to. See
   * {@link #getServerSystemKey() serverSystemKey}
   * @param defaultDateFormatStr See
   * {@link  #setDefaultDateFormatStr(String)  defaultDateFormatStr}
   * @param recentDateFormatStr See
   * {@link  #setRecentDateFormatStr(String)  recentDateFormatStr}
   * @param serverLanguageCode See
   * {@link  #setServerLanguageCode(String)  serverLanguageCode}
   * @param shortMonthNames See
   * {@link  #setShortMonthNames(String)  shortMonthNames}
   * @param serverTimeZoneId See
   * {@link  #setServerTimeZoneId(String)  serverTimeZoneId}
   * @param lenientFutureDates See
   * {@link  #setLenientFutureDates(boolean)  lenientFutureDates}
   * @param saveUnparseableEntries See
   * {@link  #setUnparseableEntries(boolean)  saveUnparseableEntries}
   */
  public FTPClientConfig(String systemKey, String defaultDateFormatStr, String recentDateFormatStr,
      String serverLanguageCode, String shortMonthNames, String serverTimeZoneId,
      boolean lenientFutureDates, boolean saveUnparseableEntries) {
    this(systemKey);
    this.defaultDateFormatStr = defaultDateFormatStr;
    this.lenientFutureDates = lenientFutureDates;
    this.recentDateFormatStr = recentDateFormatStr;
    this.saveUnparseableEntries = saveUnparseableEntries;
    this.serverLanguageCode = serverLanguageCode;
    this.shortMonthNames = shortMonthNames;
    this.serverTimeZoneId = serverTimeZoneId;
  }

  // Copy constructor, intended for use by FTPClient only
  FTPClientConfig(String systemKey, FTPClientConfig config) {
    this.serverSystemKey = systemKey;
    this.defaultDateFormatStr = config.defaultDateFormatStr;
    this.lenientFutureDates = config.lenientFutureDates;
    this.recentDateFormatStr = config.recentDateFormatStr;
    this.saveUnparseableEntries = config.saveUnparseableEntries;
    this.serverLanguageCode = config.serverLanguageCode;
    this.serverTimeZoneId = config.serverTimeZoneId;
    this.shortMonthNames = config.shortMonthNames;
  }

  /**
   * Copy constructor
   *
   * @param config source
   * @since 3.6
   */
  public FTPClientConfig(FTPClientConfig config) {
    this.serverSystemKey = config.serverSystemKey;
    this.defaultDateFormatStr = config.defaultDateFormatStr;
    this.lenientFutureDates = config.lenientFutureDates;
    this.recentDateFormatStr = config.recentDateFormatStr;
    this.saveUnparseableEntries = config.saveUnparseableEntries;
    this.serverLanguageCode = config.serverLanguageCode;
    this.serverTimeZoneId = config.serverTimeZoneId;
    this.shortMonthNames = config.shortMonthNames;
  }

  private static final Map<String, Object> LANGUAGE_CODE_MAP = new TreeMap<String, Object>();

  static {

    // if there are other commonly used month name encodings which
    // correspond to particular locales, please add them here.

    // many locales code short names for months as all three letters
    // these we handle simply.
    LANGUAGE_CODE_MAP.put("en", Locale.ENGLISH);
    LANGUAGE_CODE_MAP.put("de", Locale.GERMAN);
    LANGUAGE_CODE_MAP.put("it", Locale.ITALIAN);
    LANGUAGE_CODE_MAP.put("es", new Locale("es", "", "")); // spanish
    LANGUAGE_CODE_MAP.put("pt", new Locale("pt", "", "")); // portuguese
    LANGUAGE_CODE_MAP.put("da", new Locale("da", "", "")); // danish
    LANGUAGE_CODE_MAP.put("sv", new Locale("sv", "", "")); // swedish
    LANGUAGE_CODE_MAP.put("no", new Locale("no", "", "")); // norwegian
    LANGUAGE_CODE_MAP.put("nl", new Locale("nl", "", "")); // dutch
    LANGUAGE_CODE_MAP.put("ro", new Locale("ro", "", "")); // romanian
    LANGUAGE_CODE_MAP.put("sq", new Locale("sq", "", "")); // albanian
    LANGUAGE_CODE_MAP.put("sh", new Locale("sh", "", "")); // serbo-croatian
    LANGUAGE_CODE_MAP.put("sk", new Locale("sk", "", "")); // slovak
    LANGUAGE_CODE_MAP.put("sl", new Locale("sl", "", "")); // slovenian

    // some don't
    LANGUAGE_CODE_MAP.put("fr",
        "jan|f\u00e9v|mar|avr|mai|jun|jui|ao\u00fb|sep|oct|nov|d\u00e9c");  //french
  }

  /**
   * Getter for the serverSystemKey property.  This property
   * specifies the general type of server to which the client connects.
   * Should be either one of the <code>FTPClientConfig.SYST_*</code> codes
   * or else the fully qualified class name of a parser implementing both
   * the <code>FTPFileEntryParser</code> and <code>Configurable</code>
   * interfaces.
   *
   * @return Returns the serverSystemKey property.
   */
  public String getServerSystemKey() {
    return serverSystemKey;
  }

  /**
   * getter for the {@link  #setDefaultDateFormatStr(String)  defaultDateFormatStr}
   * property.
   *
   * @return Returns the defaultDateFormatStr property.
   */
  public String getDefaultDateFormatStr() {
    return defaultDateFormatStr;
  }

  /**
   * getter for the {@link  #setRecentDateFormatStr(String)  recentDateFormatStr} property.
   *
   * @return Returns the recentDateFormatStr property.
   */

  public String getRecentDateFormatStr() {
    return recentDateFormatStr;
  }

  /**
   * getter for the {@link  #setServerTimeZoneId(String)  serverTimeZoneId} property.
   *
   * @return Returns the serverTimeZoneId property.
   */
  public String getServerTimeZoneId() {
    return serverTimeZoneId;
  }

  /**
   * <p>
   * getter for the {@link  #setShortMonthNames(String)  shortMonthNames}
   * property.
   * </p>
   *
   * @return Returns the shortMonthNames.
   */
  public String getShortMonthNames() {
    return shortMonthNames;
  }

  /**
   * <p>
   * getter for the {@link  #setServerLanguageCode(String)  serverLanguageCode} property.
   * </p>
   *
   * @return Returns the serverLanguageCode property.
   */
  public String getServerLanguageCode() {
    return serverLanguageCode;
  }

  /**
   * <p>
   * getter for the {@link  #setLenientFutureDates(boolean)  lenientFutureDates} property.
   * </p>
   *
   * @return Returns the lenientFutureDates.
   * @since 1.5
   */
  public boolean isLenientFutureDates() {
    return lenientFutureDates;
  }

  /**
   * <p>
   * setter for the defaultDateFormatStr property.  This property
   * specifies the main date format that will be used by a parser configured
   * by this configuration to parse file timestamps.  If this is not
   * specified, such a parser will use as a default value, the most commonly
   * used format which will be in as used in <code>en_US</code> locales.
   * </p><p>
   * This should be in the format described for
   * <code>java.text.SimpleDateFormat</code>.
   * property.
   * </p>
   *
   * @param defaultDateFormatStr The defaultDateFormatStr to set.
   */
  public void setDefaultDateFormatStr(String defaultDateFormatStr) {
    this.defaultDateFormatStr = defaultDateFormatStr;
  }

  /**
   * <p>
   * setter for the recentDateFormatStr property.  This property
   * specifies a secondary date format that will be used by a parser
   * configured by this configuration to parse file timestamps, typically
   * those less than a year old.  If this is  not specified, such a parser
   * will not attempt to parse using an alternate format.
   * </p>
   * <p>
   * This is used primarily in unix-based systems.
   * </p>
   * <p>
   * This should be in the format described for
   * <code>java.text.SimpleDateFormat</code>.
   * </p>
   *
   * @param recentDateFormatStr The recentDateFormatStr to set.
   */
  public void setRecentDateFormatStr(String recentDateFormatStr) {
    this.recentDateFormatStr = recentDateFormatStr;
  }

  /**
   * <p>
   * setter for the lenientFutureDates property.  This boolean property
   * (default: false) only has meaning when a
   * {@link  #setRecentDateFormatStr(String)  recentDateFormatStr} property
   * has been set.  In that case, if this property is set true, then the
   * parser, when it encounters a listing parseable with the recent date
   * format, will only consider a date to belong to the previous year if
   * it is more than one day in the future.  This will allow all
   * out-of-synch situations (whether based on "slop" - i.e. servers simply
   * out of synch with one another or because of time zone differences -
   * but in the latter case it is highly recommended to use the
   * {@link  #setServerTimeZoneId(String)  serverTimeZoneId} property
   * instead) to resolve correctly.
   * </p><p>
   * This is used primarily in unix-based systems.
   * </p>
   *
   * @param lenientFutureDates set true to compensate for out-of-synch
   * conditions.
   */
  public void setLenientFutureDates(boolean lenientFutureDates) {
    this.lenientFutureDates = lenientFutureDates;
  }

  /**
   * <p>
   * setter for the serverTimeZoneId property.  This property
   * allows a time zone to be specified corresponding to that known to be
   * used by an FTP server in file listings.  This might be particularly
   * useful to clients such as Ant that try to use these timestamps for
   * dependency checking.
   * </p><p>
   * This should be one of the identifiers used by
   * <code>java.util.TimeZone</code> to refer to time zones, for example,
   * <code>America/Chicago</code> or <code>Asia/Rangoon</code>.
   * </p>
   *
   * @param serverTimeZoneId The serverTimeZoneId to set.
   */
  public void setServerTimeZoneId(String serverTimeZoneId) {
    this.serverTimeZoneId = serverTimeZoneId;
  }

  /**
   * <p>
   * setter for the shortMonthNames property.
   * This property allows the user to specify a set of month names
   * used by the server that is different from those that may be
   * specified using the {@link  #setServerLanguageCode(String)  serverLanguageCode}
   * property.
   * </p><p>
   * This should be a string containing twelve strings each composed of
   * three characters, delimited by pipe (|) characters.  Currently,
   * only 8-bit ASCII characters are known to be supported.  For example,
   * a set of month names used by a hypothetical Icelandic FTP server might
   * conceivably be specified as
   * <code>"jan|feb|mar|apr|ma&#xED;|j&#xFA;n|j&#xFA;l|&#xE1;g&#xFA;|sep|okt|n&#xF3;v|des"</code>.
   * </p>
   *
   * @param shortMonthNames The value to set to the shortMonthNames property.
   */
  public void setShortMonthNames(String shortMonthNames) {
    this.shortMonthNames = shortMonthNames;
  }

  /**
   * <p>
   * setter for the serverLanguageCode property.  This property allows
   * user to specify a
   * <a href="http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt">
   * two-letter ISO-639 language code</a> that will be used to
   * configure the set of month names used by the file timestamp parser.
   * If neither this nor the {@link #setShortMonthNames(String) shortMonthNames}
   * is specified, parsing will assume English month names, which may or
   * may not be significant, depending on whether the date format(s)
   * specified via {@link  #setDefaultDateFormatStr(String)  defaultDateFormatStr}
   * and/or {@link  #setRecentDateFormatStr(String)  recentDateFormatStr} are using
   * numeric or alphabetic month names.
   * </p>
   * <p>If the code supplied is not supported here, <code>en_US</code>
   * month names will be used.  We are supporting here those language
   * codes which, when a <code> java.util.Locale</code> is constucted
   * using it, and a <code>java.text.SimpleDateFormat</code> is
   * constructed using that Locale, the array returned by the
   * SimpleDateFormat's <code>getShortMonths()</code> method consists
   * solely of three 8-bit ASCII character strings.  Additionally,
   * languages which do not meet this requirement are included if a
   * common alternative set of short month names is known to be used.
   * This means that users who can tell us of additional such encodings
   * may get them added to the list of supported languages by contacting
   * the Apache Commons Net team.
   * </p>
   * <p><strong>
   * Please note that this attribute will NOT be used to determine a
   * locale-based date format for the language.  </strong>
   * Experience has shown that many if not most FTP servers outside the
   * United States employ the standard <code>en_US</code> date format
   * orderings of <code>MMM d yyyy</code> and <code>MMM d HH:mm</code>
   * and attempting to deduce this automatically here would cause more
   * problems than it would solve.  The date format must be changed
   * via the {@link  #setDefaultDateFormatStr(String)  defaultDateFormatStr} and/or
   * {@link  #setRecentDateFormatStr(String)  recentDateFormatStr} parameters.
   * </p>
   *
   * @param serverLanguageCode The value to set to the serverLanguageCode property.
   */
  public void setServerLanguageCode(String serverLanguageCode) {
    this.serverLanguageCode = serverLanguageCode;
  }

  /**
   * Looks up the supplied language code in the internally maintained table of
   * language codes.  Returns a DateFormatSymbols object configured with
   * short month names corresponding to the code.  If there is no corresponding
   * entry in the table, the object returned will be that for
   * <code>Locale.US</code>
   *
   * @param languageCode See {@link  #setServerLanguageCode(String)  serverLanguageCode}
   * @return a DateFormatSymbols object configured with short month names
   * corresponding to the supplied code, or with month names for
   * <code>Locale.US</code> if there is no corresponding entry in the internal
   * table.
   */
  public static DateFormatSymbols lookupDateFormatSymbols(String languageCode) {
    Object lang = LANGUAGE_CODE_MAP.get(languageCode);
    if (lang != null) {
      if (lang instanceof Locale) {
        return new DateFormatSymbols((Locale) lang);
      } else if (lang instanceof String) {
        return getDateFormatSymbols((String) lang);
      }
    }
    return new DateFormatSymbols(Locale.US);
  }

  /**
   * Returns a DateFormatSymbols object configured with short month names
   * as in the supplied string
   *
   * @param shortmonths This  should be as described in
   * {@link  #setShortMonthNames(String)  shortMonthNames}
   * @return a DateFormatSymbols object configured with short month names
   * as in the supplied string
   */
  public static DateFormatSymbols getDateFormatSymbols(String shortmonths) {
    String[] months = splitShortMonthString(shortmonths);
    DateFormatSymbols dfs = new DateFormatSymbols(Locale.US);
    dfs.setShortMonths(months);
    return dfs;
  }

  private static String[] splitShortMonthString(String shortmonths) {
    StringTokenizer st = new StringTokenizer(shortmonths, "|");
    int monthcnt = st.countTokens();
    if (12 != monthcnt) {
      throw new IllegalArgumentException("expecting a pipe-delimited string containing 12 tokens");
    }
    String[] months = new String[13];
    int pos = 0;
    while (st.hasMoreTokens()) {
      months[pos++] = st.nextToken();
    }
    months[pos] = "";
    return months;
  }

  /**
   * Returns a Collection of all the language codes currently supported
   * by this class. See {@link  #setServerLanguageCode(String)  serverLanguageCode}
   * for a functional descrption of language codes within this system.
   *
   * @return a Collection of all the language codes currently supported
   * by this class
   */
  public static Collection<String> getSupportedLanguageCodes() {
    return LANGUAGE_CODE_MAP.keySet();
  }

  /**
   * Allow list parsing methods to create basic FTPFile entries if parsing fails.
   * <p>
   * In this case, the FTPFile will contain only the unparsed entry {@link FTPFile#getRawListing()}
   * and {@link FTPFile#isValid()} will return {@code false}
   *
   * @param saveUnparseable if true, then create FTPFile entries if parsing fails
   * @since 3.4
   */
  public void setUnparseableEntries(boolean saveUnparseable) {
    this.saveUnparseableEntries = saveUnparseable;
  }

  /**
   * @return true if list parsing should return FTPFile entries even for unparseable response lines
   * <p>
   * If true, the FTPFile for any unparseable entries will contain only the unparsed entry
   * {@link FTPFile#getRawListing()} and {@link FTPFile#isValid()} will return {@code false}
   * @since 3.4
   */
  public boolean getUnparseableEntries() {
    return this.saveUnparseableEntries;
  }
}
