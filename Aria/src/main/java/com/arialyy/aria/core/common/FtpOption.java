/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.common;

import android.text.TextUtils;
import com.arialyy.aria.core.FtpUrlEntity;
import com.arialyy.aria.core.IdEntity;
import com.arialyy.aria.core.ProtocolType;
import com.arialyy.aria.core.processor.IFtpUploadInterceptor;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import java.text.DateFormatSymbols;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Created by laoyuyu on 2018/3/9.
 */
public class FtpOption extends BaseOption {

  private String charSet, userName, password, account;
  private boolean isNeedLogin = false;
  private FtpUrlEntity urlEntity;
  private String protocol;
  private boolean isImplicit = true;
  private IFtpUploadInterceptor uploadInterceptor;
  private int connMode = FtpConnectionMode.DATA_CONNECTION_MODE_PASV;
  private int minPort, maxPort;
  private String activeExternalIPAddress;

  //---------------- ftp client 配置信息 start
  private String defaultDateFormatStr = null;
  private String recentDateFormatStr = null;
  private String serverLanguageCode = null;
  private String shortMonthNames = null;
  private String serverTimeZoneId = null;
  private String systemKey = FTPServerIdentifier.SYST_UNIX;
  //---------------- ftp client 配置信息 end
  private IdEntity idEntity = new IdEntity();

  public FtpOption() {
    super();
  }

  public FtpOption charSet(String charSet) {
    if (TextUtils.isEmpty(charSet)) {
      throw new NullPointerException("字符编码为空");
    }
    this.charSet = charSet;
    return this;
  }

  public FtpOption login(String userName, String password) {
    return login(userName, password, null);
  }

  public FtpOption login(String userName, String password, String account) {
    if (TextUtils.isEmpty(userName)) {
      ALog.e(TAG, "用户名不能为null");
      return this;
    } else if (TextUtils.isEmpty(password)) {
      ALog.e(TAG, "密码不能为null");
      return this;
    }
    this.userName = userName;
    this.password = password;
    this.account = account;
    isNeedLogin = true;
    return this;
  }

  /**
   * 设置协议类型
   *
   * @param protocol {@link ProtocolType}
   */
  public FtpOption setProtocol(String protocol) {
    if (TextUtils.isEmpty(protocol)) {
      ALog.e(TAG, "设置协议失败，协议信息为空");
      return this;
    }
    this.protocol = protocol;
    return this;
  }

  /**
   * 设置私钥证书别名
   *
   * @param keyAlias 别名
   */
  public FtpOption setAlias(String keyAlias) {
    if (TextUtils.isEmpty(keyAlias)) {
      ALog.e(TAG, "设置证书别名失败，证书别名为空");
      return this;
    }
    idEntity.keyAlias = keyAlias;
    return this;
  }

  /**
   * 设置ca证书密码
   *
   * @param storePass ca证书密码
   */
  public FtpOption setStorePass(String storePass) {
    if (TextUtils.isEmpty(storePass)) {
      ALog.e(TAG, "设置证书密码失败，证书密码为空");
      return this;
    }
    idEntity.storePass = storePass;
    return this;
  }

  /**
   * 设置cer证书路径
   *
   * @param storePath 证书路径
   */
  public FtpOption setStorePath(String storePath) {
    if (TextUtils.isEmpty(storePath)) {
      ALog.e(TAG, "设置证书路径失败，证书路径为空");
      return this;
    }
    idEntity.storePath = storePath;
    return this;
  }

  /**
   * 设置安全模式，默认true
   *
   * @param isImplicit true 隐式，false 显式
   */
  public FtpOption setImplicit(boolean isImplicit) {
    this.isImplicit = isImplicit;
    return this;
  }

  /**
   * FTP文件上传拦截器，如果远端已有同名文件，可使用该拦截器控制覆盖文件或修改该文件上传到服务器端端文件名
   */
  public FtpOption setUploadInterceptor(IFtpUploadInterceptor uploadInterceptor) {
    if (uploadInterceptor == null) {
      throw new NullPointerException("ftp拦截器为空");
    }
    CheckUtil.checkMemberClass(uploadInterceptor.getClass());
    this.uploadInterceptor = uploadInterceptor;
    return this;
  }

  /**
   * 设置数据传输模式，默认为被动模式。
   * 主动模式：传输文件时，客户端开启一个端口，ftp服务器连接到客户端的该端口，ftp服务器推送数据到客户端
   * 被动模式：传输文件时，ftp服务器开启一个端口，客户端连接到ftp服务器的这个端口，客户端请求ftp服务器的数据
   * 请注意：主动模式是服务器主动连接到android，如果使用住的模式，请确保ftp服务器能ping通android
   *
   * @param connMode {@link FtpConnectionMode#DATA_CONNECTION_MODE_PASV},
   * {@link FtpConnectionMode#DATA_CONNECTION_MODE_ACTIVITY}
   */
  public FtpOption setConnectionMode(int connMode) {
    if (connMode != FtpConnectionMode.DATA_CONNECTION_MODE_PASV
        && connMode != FtpConnectionMode.DATA_CONNECTION_MODE_ACTIVITY) {
      ALog.e(TAG, "连接模式设置失败，默认启用被动模式");
      return this;
    }
    this.connMode = connMode;
    return this;
  }

  /**
   * 主动模式下的端口范围
   */
  public FtpOption setActivePortRange(int minPort, int maxPort) {

    if (minPort > maxPort) {
      ALog.e(TAG, "设置端口范围错误，minPort > maxPort");
      return this;
    }
    if (minPort <= 0 || minPort >= 65535) {
      ALog.e(TAG, "端口范围错误");
      return this;
    }
    if (maxPort >= 65535) {
      ALog.e(TAG, "端口范围错误");
      return this;
    }
    this.minPort = minPort;
    this.maxPort = maxPort;
    return this;
  }

  /**
   * 主动模式下，对外ip（可被Ftp服务器访问的ip）
   */
  public FtpOption setActiveExternalIPAddress(String ip) {
    if (TextUtils.isEmpty(ip)) {
      ALog.e(TAG, "ip为空");
      return this;
    }
    if (!CheckUtil.checkIp(ip)) {
      ALog.e(TAG, "ip地址错误：" + ip);
      return this;
    }
    this.activeExternalIPAddress = ip;
    return this;
  }

  /**
   * 设置ftp服务器所在的操作系统的标志，如果出现文件获取失败，请设置该标志为
   * 默认使用{@link FTPServerIdentifier#SYST_UNIX}
   *
   * @param identifier {@link FTPServerIdentifier}
   */
  public FtpOption setServerIdentifier(String identifier) {
    this.systemKey = identifier;
    return this;
  }

  /**
   * 解析ftp信息时，默认的文件日期格式，如：setDefaultDateFormatStr("d MMM yyyy")
   *
   * @param defaultDateFormatStr 日期格式
   */
  public FtpOption setDefaultDateFormatStr(String defaultDateFormatStr) {
    this.defaultDateFormatStr = defaultDateFormatStr;
    return this;
  }

  /**
   * 解析ftp信息时，默认的文件修改日期格式，如：setRecentDateFormatStr("d MMM HH:mm")
   *
   * @param recentDateFormatStr 日期格式
   */
  public FtpOption setRecentDateFormatStr(String recentDateFormatStr) {
    this.recentDateFormatStr = recentDateFormatStr;
    return this;
  }

  /**
   * 设置服务器使用的时区，java.util.TimeZone，如：America/Chicago or Asia/Rangoon.
   *
   * @param serverTimeZoneId 时区
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
   * 设置服务器语言代码
   *
   * @param serverLanguageCode {@link #LANGUAGE_CODE_MAP}
   */
  public FtpOption setServerLanguageCode(String serverLanguageCode) {
    this.serverLanguageCode = serverLanguageCode;
    return this;
  }

  public void setUrlEntity(FtpUrlEntity urlEntity) {
    this.urlEntity = urlEntity;
    urlEntity.needLogin = isNeedLogin;
    urlEntity.user = userName;
    urlEntity.password = password;
    urlEntity.account = account;
    urlEntity.idEntity = idEntity;
    if (!TextUtils.isEmpty(idEntity.storePath) || !TextUtils.isEmpty(idEntity.prvKey)) {
      urlEntity.isFtps = true;
      urlEntity.protocol = protocol;
      urlEntity.isImplicit = isImplicit;
    }
  }

  /**
   * ftp 服务器所在的操作系统标志
   */
  public interface FTPServerIdentifier {
    /**
     * Identifier by which a unix-based ftp server is known throughout
     * the commons-net ftp system.
     */
    String SYST_UNIX = "UNIX";

    /**
     * Identifier for alternate UNIX parser; same as {@link #SYST_UNIX} but leading spaces are
     * trimmed from file names. This is to maintain backwards compatibility with
     * the original behaviour of the parser which ignored multiple spaces between the date
     * and the start of the file name.
     *
     * @since 3.4
     */
    String SYST_UNIX_TRIM_LEADING = "UNIX_LTRIM";

    /**
     * Identifier by which a vms-based ftp server is known throughout
     * the commons-net ftp system.
     */
    String SYST_VMS = "VMS";

    /**
     * Identifier by which a WindowsNT-based ftp server is known throughout
     * the commons-net ftp system.
     */
    String SYST_NT = "WINDOWS";

    /**
     * Identifier by which an OS/2-based ftp server is known throughout
     * the commons-net ftp system.
     */
    String SYST_OS2 = "OS/2";

    /**
     * Identifier by which an OS/400-based ftp server is known throughout
     * the commons-net ftp system.
     */
    String SYST_OS400 = "OS/400";

    /**
     * Identifier by which an AS/400-based ftp server is known throughout
     * the commons-net ftp system.
     */
    String SYST_AS400 = "AS/400";

    /**
     * Identifier by which an MVS-based ftp server is known throughout
     * the commons-net ftp system.
     */
    String SYST_MVS = "MVS";

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
    String SYST_L8 = "TYPE: L8";

    /**
     * Identifier by which an Netware-based ftp server is known throughout
     * the commons-net ftp system.
     *
     * @since 1.5
     */
    String SYST_NETWARE = "NETWARE";

    /**
     * Identifier by which a Mac pre OS-X -based ftp server is known throughout
     * the commons-net ftp system.
     *
     * @since 3.1
     */
    // Full string is "MACOS Peter's Server"; the substring below should be enough
    String SYST_MACOS_PETER = "MACOS PETER"; // NET-436
  }

  /**
   * 支持的语言代码
   */
  private static final Map<String, Object> LANGUAGE_CODE_MAP = new TreeMap<>();

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
}
