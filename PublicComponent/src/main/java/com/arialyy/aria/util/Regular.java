package com.arialyy.aria.util;

/**
 * Created by Aria.Lao on 2017/10/24.
 * 正则表达式
 */
public interface Regular {

  /**
   * 获取文件名
   */
  String REG_FILE_NAME = "[/|\\\\|//]";

  /**
   * IPV4地址匹配
   */
  String REG_IP_V4 = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";

  /**
   * 匹配双字节字符、空格、制表符、换行符
   */
  String REG_DOUBLE_CHAR_AND_SPACE = "[^\\x00-\\xff]|[\\s\\p{Zs}]";

  /**
   * 匹配window.location.replace
   */
  String REG_WINLOD_REPLACE = "replace\\(\".*\"\\)";

  /**
   * 匹配BANDWIDTH
   */
  String BANDWIDTH = "[0-9]{3,}";
}
