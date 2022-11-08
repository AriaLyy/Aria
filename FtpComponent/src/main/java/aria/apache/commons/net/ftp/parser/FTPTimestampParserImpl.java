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
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Default implementation of the {@link  FTPTimestampParser  FTPTimestampParser}
 * interface also implements the {@link  Configurable  Configurable}
 * interface to allow the parsing to be configured from the outside.
 *
 * @see ConfigurableFTPFileEntryParserImpl
 * @since 1.4
 */
public class FTPTimestampParserImpl implements FTPTimestampParser, Configurable {

  /** The date format for all dates, except possibly recent dates. Assumed to include the year. */
  private SimpleDateFormat defaultDateFormat;
  /* The index in CALENDAR_UNITS of the smallest time unit in defaultDateFormat */
  private int defaultDateSmallestUnitIndex;

  /** The format used for recent dates (which don't have the year). May be null. */
  private SimpleDateFormat recentDateFormat;
  /* The index in CALENDAR_UNITS of the smallest time unit in recentDateFormat */
  private int recentDateSmallestUnitIndex;

  private boolean lenientFutureDates = false;

  /*
   * List of units in order of increasing significance.
   * This allows the code to clear all units in the Calendar until it
   * reaches the least significant unit in the parse string.
   * The date formats are analysed to find the least significant
   * unit (e.g. Minutes or Milliseconds) and the appropriate index to
   * the array is saved.
   * This is done by searching the array for the unit specifier,
   * and returning the index. When clearing the Calendar units,
   * the code loops through the array until the previous entry.
   * e.g. for MINUTE it would clear MILLISECOND and SECOND
   */
  private static final int[] CALENDAR_UNITS = {
      Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR_OF_DAY,
      Calendar.DAY_OF_MONTH, Calendar.MONTH, Calendar.YEAR
  };

  /*
   * Return the index to the array representing the least significant
   * unit found in the date format.
   * Default is 0 (to avoid dropping precision)
   */
  private static int getEntry(SimpleDateFormat dateFormat) {
    if (dateFormat == null) {
      return 0;
    }
    final String FORMAT_CHARS = "SsmHdM";
    final String pattern = dateFormat.toPattern();
    for (char ch : FORMAT_CHARS.toCharArray()) {
      if (pattern.indexOf(ch) != -1) { // found the character
        switch (ch) {
          case 'S':
            return indexOf(Calendar.MILLISECOND);
          case 's':
            return indexOf(Calendar.SECOND);
          case 'm':
            return indexOf(Calendar.MINUTE);
          case 'H':
            return indexOf(Calendar.HOUR_OF_DAY);
          case 'd':
            return indexOf(Calendar.DAY_OF_MONTH);
          case 'M':
            return indexOf(Calendar.MONTH);
        }
      }
    }
    return 0;
  }

  /*
   * Find the entry in the CALENDAR_UNITS array.
   */
  private static int indexOf(int calendarUnit) {
    int i;
    for (i = 0; i < CALENDAR_UNITS.length; i++) {
      if (calendarUnit == CALENDAR_UNITS[i]) {
        return i;
      }
    }
    return 0;
  }

  /*
   * Sets the Calendar precision (used by FTPFile#toFormattedDate) by clearing
   * the immediately preceeding unit (if any).
   * Unfortunately the clear(int) method results in setting all other units.
   */
  private static void setPrecision(int index, Calendar working) {
    if (index <= 0) { // e.g. MILLISECONDS
      return;
    }
    final int field = CALENDAR_UNITS[index - 1];
    // Just in case the analysis is wrong, stop clearing if
    // field value is not the default.
    final int value = working.get(field);
    if (value != 0) { // don't reset if it has a value
      //            new Throwable("Unexpected value "+value).printStackTrace(); // DEBUG
    } else {
      working.clear(field); // reset just the required field
    }
  }

  /**
   * The only constructor for this class.
   */
  public FTPTimestampParserImpl() {
    setDefaultDateFormat(DEFAULT_SDF, null);
    setRecentDateFormat(DEFAULT_RECENT_SDF, null);
  }

  /**
   * Implements the one {@link  FTPTimestampParser#parseTimestamp(String)  method}
   * in the {@link  FTPTimestampParser  FTPTimestampParser} interface
   * according to this algorithm:
   *
   * If the recentDateFormat member has been defined, try to parse the
   * supplied string with that.  If that parse fails, or if the recentDateFormat
   * member has not been defined, attempt to parse with the defaultDateFormat
   * member.  If that fails, throw a ParseException.
   *
   * This method assumes that the server time is the same as the local time.
   *
   * @param timestampStr The timestamp to be parsed
   * @return a Calendar with the parsed timestamp
   * @see FTPTimestampParserImpl#parseTimestamp(String, Calendar)
   */
  @Override public Calendar parseTimestamp(String timestampStr) throws ParseException {
    Calendar now = Calendar.getInstance();
    return parseTimestamp(timestampStr, now);
  }

  /**
   * If the recentDateFormat member has been defined, try to parse the
   * supplied string with that.  If that parse fails, or if the recentDateFormat
   * member has not been defined, attempt to parse with the defaultDateFormat
   * member.  If that fails, throw a ParseException.
   *
   * This method allows a {@link Calendar} instance to be passed in which represents the
   * current (system) time.
   *
   * @param timestampStr The timestamp to be parsed
   * @param serverTime The current time for the server
   * @return the calendar
   * @throws ParseException if timestamp cannot be parsed
   * @see FTPTimestampParser#parseTimestamp(String)
   * @since 1.5
   */
  public Calendar parseTimestamp(String timestampStr, Calendar serverTime) throws ParseException {
    Calendar working = (Calendar) serverTime.clone();
    working.setTimeZone(getServerTimeZone()); // is this needed?

    Date parsed = null;

    if (recentDateFormat != null) {
      Calendar now = (Calendar) serverTime.clone();// Copy this, because we may change it
      now.setTimeZone(this.getServerTimeZone());
      if (lenientFutureDates) {
        // add a day to "now" so that "slop" doesn't cause a date
        // slightly in the future to roll back a full year.  (Bug 35181 => NET-83)
        now.add(Calendar.DAY_OF_MONTH, 1);
      }
      // The Java SimpleDateFormat class uses the epoch year 1970 if not present in the input
      // As 1970 was not a leap year, it cannot parse "Feb 29" correctly.
      // Java 1.5+ returns Mar 1 1970
      // Temporarily add the current year to the short date time
      // to cope with short-date leap year strings.
      // Since Feb 29 is more that 6 months from the end of the year, this should be OK for
      // all instances of short dates which are +- 6 months from current date.
      // TODO this won't always work for systems that use short dates +0/-12months
      // e.g. if today is Jan 1 2001 and the short date is Feb 29
      String year = Integer.toString(now.get(Calendar.YEAR));
      String timeStampStrPlusYear = timestampStr + " " + year;
      SimpleDateFormat hackFormatter = new SimpleDateFormat(recentDateFormat.toPattern() + " yyyy",
          recentDateFormat.getDateFormatSymbols());
      hackFormatter.setLenient(false);
      hackFormatter.setTimeZone(recentDateFormat.getTimeZone());
      ParsePosition pp = new ParsePosition(0);
      parsed = hackFormatter.parse(timeStampStrPlusYear, pp);
      // Check if we parsed the full string, if so it must have been a short date originally
      if (parsed != null && pp.getIndex() == timeStampStrPlusYear.length()) {
        working.setTime(parsed);
        if (working.after(now)) { // must have been last year instead
          working.add(Calendar.YEAR, -1);
        }
        setPrecision(recentDateSmallestUnitIndex, working);
        return working;
      }
    }

    ParsePosition pp = new ParsePosition(0);
    parsed = defaultDateFormat.parse(timestampStr, pp);
    // note, length checks are mandatory for us since
    // SimpleDateFormat methods will succeed if less than
    // full string is matched.  They will also accept,
    // despite "leniency" setting, a two-digit number as
    // a valid year (e.g. 22:04 will parse as 22 A.D.)
    // so could mistakenly confuse an hour with a year,
    // if we don't insist on full length parsing.
    if (parsed != null && pp.getIndex() == timestampStr.length()) {
      working.setTime(parsed);
    } else {
      throw new ParseException("Timestamp '"
          + timestampStr
          + "' could not be parsed using a server time of "
          + serverTime.getTime().toString(), pp.getErrorIndex());
    }
    setPrecision(defaultDateSmallestUnitIndex, working);
    return working;
  }

  /**
   * @return Returns the defaultDateFormat.
   */
  public SimpleDateFormat getDefaultDateFormat() {
    return defaultDateFormat;
  }

  /**
   * @return Returns the defaultDateFormat pattern string.
   */
  public String getDefaultDateFormatString() {
    return defaultDateFormat.toPattern();
  }

  /**
   * @param format The defaultDateFormat to be set.
   * @param dfs the symbols to use (may be null)
   */
  private void setDefaultDateFormat(String format, DateFormatSymbols dfs) {
    if (format != null) {
      if (dfs != null) {
        this.defaultDateFormat = new SimpleDateFormat(format, dfs);
      } else {
        this.defaultDateFormat = new SimpleDateFormat(format);
      }
      this.defaultDateFormat.setLenient(false);
    } else {
      this.defaultDateFormat = null;
    }
    this.defaultDateSmallestUnitIndex = getEntry(this.defaultDateFormat);
  }

  /**
   * @return Returns the recentDateFormat.
   */
  public SimpleDateFormat getRecentDateFormat() {
    return recentDateFormat;
  }

  /**
   * @return Returns the recentDateFormat.
   */
  public String getRecentDateFormatString() {
    return recentDateFormat.toPattern();
  }

  /**
   * @param format The recentDateFormat to set.
   * @param dfs the symbols to use (may be null)
   */
  private void setRecentDateFormat(String format, DateFormatSymbols dfs) {
    if (format != null) {
      if (dfs != null) {
        this.recentDateFormat = new SimpleDateFormat(format, dfs);
      } else {
        this.recentDateFormat = new SimpleDateFormat(format);
      }
      this.recentDateFormat.setLenient(false);
    } else {
      this.recentDateFormat = null;
    }
    this.recentDateSmallestUnitIndex = getEntry(this.recentDateFormat);
  }

  /**
   * @return returns an array of 12 strings representing the short
   * month names used by this parse.
   */
  public String[] getShortMonths() {
    return defaultDateFormat.getDateFormatSymbols().getShortMonths();
  }

  /**
   * @return Returns the serverTimeZone used by this parser.
   */
  public TimeZone getServerTimeZone() {
    return this.defaultDateFormat.getTimeZone();
  }

  /**
   * sets a TimeZone represented by the supplied ID string into all
   * of the parsers used by this server.
   *
   * @param serverTimeZone Time Id java.util.TimeZone id used by
   * the ftp server.  If null the client's local time zone is assumed.
   */
  private void setServerTimeZone(String serverTimeZoneId) {
    TimeZone serverTimeZone = TimeZone.getDefault();
    if (serverTimeZoneId != null) {
      serverTimeZone = TimeZone.getTimeZone(serverTimeZoneId);
    }
    this.defaultDateFormat.setTimeZone(serverTimeZone);
    if (this.recentDateFormat != null) {
      this.recentDateFormat.setTimeZone(serverTimeZone);
    }
  }

  /**
   * Implementation of the {@link  Configurable  Configurable}
   * interface. Configures this <code>FTPTimestampParser</code> according
   * to the following logic:
   * <p>
   * Set up the {@link  FTPClientConfig#setDefaultDateFormatStr(String) defaultDateFormat}
   * and optionally the {@link  FTPClientConfig#setRecentDateFormatStr(String) recentDateFormat}
   * to values supplied in the config based on month names configured as follows:
   * </p>
   * <ul>
   * <li>If a {@link  FTPClientConfig#setShortMonthNames(String) shortMonthString}
   * has been supplied in the <code>config</code>, use that to parse  parse timestamps.</li>
   * <li>Otherwise, if a {@link  FTPClientConfig#setServerLanguageCode(String) serverLanguageCode}
   * has been supplied in the <code>config</code>, use the month names represented
   * by that {@link  FTPClientConfig#lookupDateFormatSymbols(String) language}
   * to parse timestamps.</li>
   * <li>otherwise use default English month names</li>
   * </ul><p>
   * Finally if a {@link  FTPClientConfig#setServerTimeZoneId(String)
   * serverTimeZoneId}
   * has been supplied via the config, set that into all date formats that have
   * been configured.
   * </p>
   */
  @Override public void configure(FTPClientConfig config) {
    DateFormatSymbols dfs = null;

    String languageCode = config.getServerLanguageCode();
    String shortmonths = config.getShortMonthNames();
    if (shortmonths != null) {
      dfs = FTPClientConfig.getDateFormatSymbols(shortmonths);
    } else if (languageCode != null) {
      dfs = FTPClientConfig.lookupDateFormatSymbols(languageCode);
    } else {
      dfs = FTPClientConfig.lookupDateFormatSymbols("en");
    }

    String recentFormatString = config.getRecentDateFormatStr();
    setRecentDateFormat(recentFormatString, dfs);

    String defaultFormatString = config.getDefaultDateFormatStr();
    if (defaultFormatString == null) {
      throw new IllegalArgumentException("defaultFormatString cannot be null");
    }
    setDefaultDateFormat(defaultFormatString, dfs);

    setServerTimeZone(config.getServerTimeZoneId());

    this.lenientFutureDates = config.isLenientFutureDates();
  }

  /**
   * @return Returns the lenientFutureDates.
   */
  boolean isLenientFutureDates() {
    return lenientFutureDates;
  }

  /**
   * @param lenientFutureDates The lenientFutureDates to set.
   */
  void setLenientFutureDates(boolean lenientFutureDates) {
    this.lenientFutureDates = lenientFutureDates;
  }
}
