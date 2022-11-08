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

package com.arialyy.aria.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.core.FtpUrlEntity;
import dalvik.system.DexFile;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lyy on 2016/1/22. 通用工具
 */
public class CommonUtil {
  private static final String TAG = "CommonUtil";
  public static final String SERVER_CHARSET = "ISO-8859-1";
  private static long lastClickTime;
  /**
   * android、androidx、support的fragment、dialogFragment类名
   */
  private static List<String> mFragmentClassName = new ArrayList<>();
  private static List<String> mDialogFragmentClassName = new ArrayList<>();

  static {
    mFragmentClassName.add("androidx.fragment.app.Fragment");
    mFragmentClassName.add("androidx.fragment.app.DialogFragment");
    mFragmentClassName.add("android.app.Fragment");
    mFragmentClassName.add("android.app.DialogFragment");
    mFragmentClassName.add("android.support.v4.app.Fragment");
    mFragmentClassName.add("android.support.v4.app.DialogFragment");

    mDialogFragmentClassName.add("androidx.fragment.app.DialogFragment");
    mDialogFragmentClassName.add("android.app.DialogFragment");
    mDialogFragmentClassName.add("android.support.v4.app.DialogFragment");
  }

  /**
   * 获取fragment的activityz
   *
   * @return 获取失败，返回null
   */
  public static Activity getFragmentActivity(Object obj) {
    try {
      Method method = obj.getClass().getMethod("getActivity");
      return (Activity) method.invoke(obj);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 判断注解对象是否是fragment
   *
   * @return true 对象是fragment
   */
  public static boolean isFragment(Class subClazz) {
    Class parentClass = subClazz.getSuperclass();
    if (parentClass == null) {
      return false;
    } else {
      String parentName = parentClass.getName();
      if (mFragmentClassName.contains(parentName)) {
        return true;
      } else {
        return isFragment(parentClass);
      }
    }
  }

  /**
   * 判断对象是否是DialogFragment
   *
   * @return true 对象是DialogFragment
   */
  public static boolean isDialogFragment(Class subClazz) {
    Class parentClass = subClazz.getSuperclass();
    if (parentClass == null) {
      return false;
    } else {
      String parentName = parentClass.getName();
      if (mDialogFragmentClassName.contains(parentName)) {
        return true;
      } else {
        return isDialogFragment(parentClass);
      }
    }
  }

  public static boolean isLocalOrAnonymousClass(Class clazz) {
    // JVM Spec 4.8.6: A class must have an EnclosingMethod
    // attribute if and only if it is a local class or an
    // anonymous class.
    return clazz.isLocalClass() || clazz.isAnonymousClass();
  }

  public static String getTargetName(Object obj) {
    String targetName;
    if (isLocalOrAnonymousClass(obj.getClass())) {
      Log.w(TAG, String.format("%s 是匿名内部类或局部类，将使用其主类的对象", obj.getClass().getName()));
      String clsName = obj.getClass().getName();
      int $Index = clsName.lastIndexOf("$");
      targetName = clsName.substring(0, $Index);
    } else {
      targetName = obj.getClass().getName();
    }
    return targetName;
  }

  /**
   * 获取线程名称，命名规则：md5(任务地址 + 线程id)
   *
   * @param url 任务地址
   * @param threadId 线程id
   */
  public static String getThreadName(String url, int threadId) {
    return getStrMd5(url.concat(String.valueOf(threadId)));
  }

  /**
   * 检查sql的expression是否合法
   *
   * @return false 不合法
   */
  public static boolean checkSqlExpression(String... expression) {
    if (expression.length == 0) {
      ALog.e(TAG, "sql语句表达式不能为null");
      return false;
    }
    //if (expression.length == 1) {
    //  ALog.e(TAG, String.format("表达式需要写入参数，参数信息：%s", Arrays.toString(expression)));
    //  return false;
    //}
    String where = expression[0];
    //if (!where.contains("?")) {
    //  ALog.e(TAG, String.format("请在where语句的'='后编写?，参数信息：%s", Arrays.toString(expression)));
    //  return false;
    //}
    Pattern pattern = Pattern.compile("\\?");
    Matcher matcher = pattern.matcher(where);
    int count = 0;
    while (matcher.find()) {
      count++;
    }
    if (count < expression.length - 1) {
      ALog.e(TAG, String.format("条件语句的?个数不能小于参数个数，参数信息：%s", Arrays.toString(expression)));
      return false;
    }
    if (count > expression.length - 1) {
      ALog.e(TAG, String.format("条件语句的?个数不能大于参数个数， 参数信息：%s", Arrays.toString(expression)));
      return false;
    }
    return true;
  }

  /**
   * 是否是快速点击，500ms内快速点击无效
   *
   * @return true 快速点击
   */
  public static boolean isFastDoubleClick() {
    long time = System.currentTimeMillis();
    long timeD = time - lastClickTime;
    if (0 < timeD && timeD < 500) {
      ALog.i(TAG, "操作太频繁了，缓一下吧～");
      return true;
    }
    lastClickTime = time;
    return false;
  }

  /**
   * 将字符串转换为Ftp服务器默认的ISO-8859-1编码
   *
   * @param charSet 原字符串编码s
   * @param str 需要转换的字符串
   * @return 转换后的字符串
   */
  public static String convertFtpChar(String charSet, String str)
      throws UnsupportedEncodingException {
    return new String(str.getBytes(charSet), SERVER_CHARSET);
  }

  /**
   * 将字符串转换为Ftp服务器默认的ISO-8859-1编码
   *
   * @param charSet 字符串编码
   * @param str 需要转换的字符串
   * @return 转换后的字符串
   */
  public static String convertSFtpChar(String charSet, String str)
      throws UnsupportedEncodingException {
    return new String(str.getBytes(), charSet);
  }

  /**
   * 获取某包下所有类
   *
   * @param className 过滤的类名
   * @return 类的完整名称
   */
  public static List<String> getPkgClassNames(Context context, String className) {
    List<String> classNameList = new ArrayList<>();
    String pPath = context.getPackageCodePath();
    File dir = new File(pPath).getParentFile();
    String[] paths = dir.list();
    if (paths == null) {
      classNameList.addAll(getPkgClassName(pPath, className));
    } else {
      String dPath = dir.getPath();
      for (String path : dir.list()) {
        String fPath = dPath + "/" + path;
        if (!fPath.endsWith(".apk")) {
          continue;
        }
        classNameList.addAll(getPkgClassName(fPath, className));
      }
    }
    return classNameList;
  }

  /**
   * 获取指定包名下的所有类
   *
   * @param path dex路径
   * @param filterClass 需要过滤的类
   */
  public static List<String> getPkgClassName(String path, String filterClass) {
    List<String> list = new ArrayList<>();
    try {
      File file = new File(path);
      if (!file.exists()) {
        ALog.w(TAG, String.format("路径【%s】下的Dex文件不存在", path));
        return list;
      }

      DexFile df = new DexFile(path);//通过DexFile查找当前的APK中可执行文件
      Enumeration<String> enumeration = df.entries();//获取df中的元素  这里包含了所有可执行的类名 该类名包含了包名+类名的方式
      while (enumeration.hasMoreElements()) {
        String _className = enumeration.nextElement();
        if (!_className.contains(filterClass)) {
          continue;
        }
        if (_className.contains(filterClass)) {
          list.add(_className);
        }
      }
      df.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * 拦截window.location.replace数据
   *
   * @return 重定向url
   */
  public static String getWindowReplaceUrl(String text) {
    if (TextUtils.isEmpty(text)) {
      ALog.e(TAG, "拦截数据为null");
      return null;
    }
    String reg = Regular.REG_WINLOD_REPLACE;
    Pattern p = Pattern.compile(reg);
    Matcher m = p.matcher(text);
    if (m.find()) {
      String s = m.group();
      s = s.substring(9, s.length() - 2);
      return s;
    }
    return null;
  }

  /**
   * 获取sdcard app的缓存目录
   *
   * @return "/mnt/sdcard/Android/data/{package_name}/files/"
   */
  public static String getAppPath(Context context) {
    //判断是否存在sd卡
    boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(
        android.os.Environment.getExternalStorageState());
    if (!sdExist) {
      return null;
    } else {
      //获取sd卡路径
      File file = context.getExternalFilesDir(null);
      String dir;
      if (file != null) {
        dir = file.getPath() + "/";
      } else {
        dir = Environment.getExternalStorageDirectory().getPath()
            + "/Android/data/"
            + context.getPackageName()
            + "/files/";
      }
      return dir;
    }
  }

  /**
   * 获取map泛型类型
   *
   * @param map list类型字段
   * @return 泛型类型
   */
  public static Class[] getMapParamType(Field map) {
    Class type = map.getType();
    if (!type.isAssignableFrom(Map.class)) {
      ALog.d(TAG, "字段类型不是Map");
      return null;
    }

    Type fc = map.getGenericType();

    if (fc == null) {
      ALog.d(TAG, "该字段没有泛型参数");
      return null;
    }

    if (fc instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) fc;
      Type[] types = pt.getActualTypeArguments();
      Class[] clazz = new Class[2];
      clazz[0] = (Class) types[0];
      clazz[1] = (Class) types[1];
      return clazz;
    }
    return null;
  }

  /**
   * 获取list泛型类型
   *
   * @param list list类型字段
   * @return 泛型类型
   */

  public static Class getListParamType(Field list) {
    Class type = list.getType();
    if (!type.isAssignableFrom(List.class)) {
      ALog.d(TAG, "字段类型不是List");
      return null;
    }

    Type fc = list.getGenericType(); // 关键的地方，如果是List类型，得到其Generic的类型

    if (fc == null) {
      ALog.d(TAG, "该字段没有泛型参数");
      return null;
    }

    if (fc instanceof ParameterizedType) { //如果是泛型参数的类型
      ParameterizedType pt = (ParameterizedType) fc;
      return (Class) pt.getActualTypeArguments()[0]; //得到泛型里的class类型对象。
    }
    return null;
  }

  /**
   * 分割获取url，协议，ip/域名，端口，内容
   *
   * @param url 输入的url{@code String url = "ftp://z:z@dygod18.com:21211/[电影天堂www.dy2018.com]猩球崛起3：终极之战BD国英双语中英双字.mkv";}
   */
  public static FtpUrlEntity getFtpUrlInfo(String url) {
    Uri uri = Uri.parse(url);

    String userInfo = uri.getUserInfo(), remotePath = uri.getPath();
    ALog.d(TAG,
        String.format("scheme = %s, user = %s, host = %s, port = %s, path = %s", uri.getScheme(),
            userInfo, uri.getHost(), uri.getPort(), remotePath));

    FtpUrlEntity entity = new FtpUrlEntity();
    entity.url = url;
    entity.hostName = uri.getHost();
    entity.port = uri.getPort() == -1 ? "21" : String.valueOf(uri.getPort());
    if (!TextUtils.isEmpty(userInfo)) {
      String[] temp = userInfo.split(":");
      if (temp.length == 2) {
        entity.user = temp[0];
        entity.password = temp[1];
      } else {
        entity.user = userInfo;
      }
    }
    entity.scheme = uri.getScheme();
    entity.remotePath = TextUtils.isEmpty(remotePath) ? "/" : remotePath;
    return entity;
  }

  /**
   * 转换Url
   *
   * @param url 原地址
   * @return 转换后的地址
   */
  public static String convertUrl(String url) {
    Uri uri = Uri.parse(url);
    url = uri.toString();
    if (hasDoubleCharacter(url)) {
      //预先处理空格，URLEncoder只会把空格转换为+
      url = url.replaceAll(" ", "%20");
      //匹配双字节字符(包括汉字在内)
      String regex = Regular.REG_DOUBLE_CHAR_AND_SPACE;
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(url);
      Set<String> strs = new HashSet<>();
      while (m.find()) {
        strs.add(m.group());
      }
      try {
        for (String str : strs) {
          url = url.replaceAll(str, URLEncoder.encode(str, "UTF-8"));
        }
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    return url;
  }

  /**
   * 判断是否有双字节字符(包括汉字在内) 和空格、制表符、回车
   *
   * @param chineseStr 需要进行判断的字符串
   * @return {@code true}有双字节字符，{@code false} 无双字节字符
   */
  public static boolean hasDoubleCharacter(String chineseStr) {
    char[] charArray = chineseStr.toCharArray();
    for (char aCharArray : charArray) {
      if (((aCharArray >= 0x0391) && (aCharArray <= 0xFFE5)) || (aCharArray == 0x0d) || (aCharArray
          == 0x0a) || (aCharArray == 0x20)) {
        return true;
      }
    }
    return false;
  }

  /**
   * base64 解密字符串
   *
   * @param str 被加密的字符串
   * @return 解密后的字符串
   */
  public static String decryptBASE64(String str) {
    return new String(Base64.decode(str.getBytes(), Base64.DEFAULT));
  }

  /**
   * base64 加密字符串
   *
   * @param str 需要加密的字符串
   * @return 加密后的字符串
   */
  public static String encryptBASE64(String str) {
    return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
  }

  /**
   * 字符串编码转换
   */
  public static String strCharSetConvert(String oldStr, String charSet) {
    try {
      return new String(oldStr.getBytes(), charSet);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 根据下载任务组的url创建key
   *
   * @return urls 为 null 或者 size为0，返回""
   */
  public static String getMd5Code(List<String> urls) {
    if (urls == null || urls.size() < 1) return "";
    String md5 = "";
    StringBuilder sb = new StringBuilder();
    for (String url : urls) {
      sb.append(url);
    }
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(sb.toString().getBytes());
      md5 = new BigInteger(1, md.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      ALog.e(TAG, e.getMessage());
    }
    return md5;
  }

  /**
   * 获取字符串的md5
   *
   * @return 字符串为空或获取md5失败，则返回""
   */
  public static String getStrMd5(String str) {
    if (TextUtils.isEmpty(str)) return "";
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(str.getBytes());
      return new BigInteger(1, md.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * 获取CPU核心数
   */
  public static int getCoresNum() {
    //Private Class to display only CPU devices in the directory listing
    class CpuFilter implements FileFilter {
      @Override public boolean accept(File pathname) {
        //Check if filename is "cpu", followed by a single digit number
        return Pattern.matches("cpu[0-9]", pathname.getName());
      }
    }

    try {
      //Get directory containing CPU info
      File dir = new File("/sys/devices/system/cpu/");
      //Filter to only list the devices we care about
      File[] files = dir.listFiles(new CpuFilter());
      ALog.d(TAG, "CPU Count: " + files.length);
      //Return the number of cores (virtual CPU devices)
      return files.length;
    } catch (Exception e) {
      //Print exception
      ALog.d(TAG, "CPU Count: Failed.");
      e.printStackTrace();
      //Default to return 1 core
      return 1;
    }
  }

  /**
   * 校验文件MD5码
   */
  public static boolean checkMD5(String md5, File updateFile) {
    if (TextUtils.isEmpty(md5) || updateFile == null) {
      ALog.e(TAG, "MD5 string empty or updateFile null");
      return false;
    }

    String calculatedDigest = getFileMD5(updateFile);
    if (calculatedDigest == null) {
      ALog.e(TAG, "calculatedDigest null");
      return false;
    }
    return calculatedDigest.equalsIgnoreCase(md5);
  }

  /**
   * 校验文件MD5码
   */
  public static boolean checkMD5(String md5, InputStream is) {
    if (TextUtils.isEmpty(md5) || is == null) {
      ALog.e(TAG, "MD5 string empty or updateFile null");
      return false;
    }

    String calculatedDigest = getFileMD5(is);
    if (calculatedDigest == null) {
      ALog.e(TAG, "calculatedDigest null");
      return false;
    }
    return calculatedDigest.equalsIgnoreCase(md5);
  }

  /**
   * 获取文件MD5码
   */
  public static String getFileMD5(File updateFile) {
    InputStream is;
    try {
      is = new FileInputStream(updateFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }

    return getFileMD5(is);
  }

  /**
   * 获取文件MD5码
   */
  public static String getFileMD5(InputStream is) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }

    byte[] buffer = new byte[8192];
    int read;
    try {
      while ((read = is.read(buffer)) > 0) {
        digest.update(buffer, 0, read);
      }
      byte[] md5sum = digest.digest();
      BigInteger bigInt = new BigInteger(1, md5sum);
      String output = bigInt.toString(16);
      // Fill to 32 chars
      output = String.format("%32s", output).replace(' ', '0');
      return output;
    } catch (IOException e) {
      throw new RuntimeException("Unable to process file for MD5", e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 创建隐性的Intent
   */
  public static Intent createIntent(String packageName, String action) {
    Uri.Builder builder = new Uri.Builder();
    builder.scheme(packageName);
    Uri uri = builder.build();
    Intent intent = new Intent(action);
    intent.setData(uri);
    return intent;
  }

  /**
   * 存储字符串到配置文件
   *
   * @param preName 配置文件名
   * @param key 存储的键值
   * @param value 需要存储的字符串
   * @return 成功标志
   */
  public static Boolean putString(String preName, Context context, String key, String value) {
    SharedPreferences pre = context.getSharedPreferences(preName, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = pre.edit();
    editor.putString(key, value);
    return editor.commit();
  }

  /**
   * 从配置文件读取字符串
   *
   * @param preName 配置文件名
   * @param key 字符串键值
   * @return 键值对应的字符串, 默认返回""
   */
  public static String getString(String preName, Context context, String key) {
    SharedPreferences pre = context.getSharedPreferences(preName, Context.MODE_PRIVATE);
    return pre.getString(key, "");
  }

  /**
   * 获取所有字段，包括父类的字段
   */
  public static List<Field> getAllFields(Class clazz) {
    List<Field> fields = new ArrayList<>();
    Class personClazz = clazz.getSuperclass();
    if (personClazz != null) {
      Class rootClazz = personClazz.getSuperclass();
      if (rootClazz != null) {
        Collections.addAll(fields, rootClazz.getDeclaredFields());
      }
      Collections.addAll(fields, personClazz.getDeclaredFields());
    }
    Collections.addAll(fields, clazz.getDeclaredFields());
    List<Field> ignore = new ArrayList<>();
    for (Field field : fields) {
      if (field.getName().equals(AriaConfig.IGNORE_CLASS_KLASS) || field.getName()
          .equals(AriaConfig.IGNORE_CLASS_MONITOR)) {
        ignore.add(field);
      }
    }
    if (!ignore.isEmpty()) {
      fields.removeAll(ignore);
    }
    return fields;
  }

  /**
   * 获取当前类里面的所在字段
   */
  public static Field[] getFields(Class clazz) {
    Field[] fields;
    fields = clazz.getDeclaredFields();
    if (fields.length == 0) {
      Class superClazz = clazz.getSuperclass();
      if (superClazz != null) {
        fields = getFields(superClazz);
      }
    }
    return fields;
  }

  /**
   * 获取类里面的指定对象，如果该类没有则从父类查询
   */
  public static Field getField(Class clazz, String name) {
    Field field = null;
    try {
      field = clazz.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      try {
        field = clazz.getField(name);
      } catch (NoSuchFieldException e1) {
        if (clazz.getSuperclass() == null) {
          return null;
        } else {
          field = getField(clazz.getSuperclass(), name);
        }
      }
    }
    if (field != null) {
      field.setAccessible(true);
    }
    return field;
  }

  /**
   * 获取类里面的指定对象，如果该类没有则从父类查询
   */
  public static Method getMethod(Class clazz, String methodName, Class<?>... params){
    Method method = null;
    try {
      method = clazz.getDeclaredMethod(methodName, params);
    } catch (NoSuchMethodException e) {
      try {
        method = clazz.getMethod(methodName, params);
      } catch (NoSuchMethodException ex) {
        if (clazz.getSuperclass() == null) {
          return null;
        } else {
          method = getMethod(clazz.getSuperclass(), methodName, params);
        }
      }
    }

    if (method != null){
      method.setAccessible(true);
    }

    return method;
  }

  /**
   * 字符串转hashcode
   */
  public static int keyToHashCode(String str) {
    int total = 0;
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (ch == '-') ch = (char) 28; // does not contain the same last 5 bits as any letter
      if (ch == '\'') ch = (char) 29; // nor this
      total = (total * 33) + (ch & 0x1F);
    }
    return total;
  }

  /**
   * 将key转换为16进制码
   *
   * @param key 缓存的key
   * @return 转换后的key的值, 系统便是通过该key来读写缓存
   */
  public static String keyToHashKey(String key) {
    String cacheKey;
    try {
      final MessageDigest mDigest = MessageDigest.getInstance("MD5");
      mDigest.update(key.getBytes());
      cacheKey = bytesToHexString(mDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      cacheKey = String.valueOf(key.hashCode());
    }
    return cacheKey;
  }

  /**
   * 将普通字符串转换为16位进制字符串
   */
  public static String bytesToHexString(byte[] src) {
    StringBuilder stringBuilder = new StringBuilder("0x");
    if (src == null || src.length <= 0) {
      return null;
    }
    char[] buffer = new char[2];
    for (byte aSrc : src) {
      buffer[0] = Character.forDigit((aSrc >>> 4) & 0x0F, 16);
      buffer[1] = Character.forDigit(aSrc & 0x0F, 16);
      stringBuilder.append(buffer);
    }
    return stringBuilder.toString();
  }

  /**
   * 获取对象名
   *
   * @param obj 对象
   * @return 对象名
   */
  public static String getClassName(Object obj) {
    String[] arrays = obj.getClass().getName().split("\\.");
    return arrays[arrays.length - 1];
  }

  /**
   * 获取对象名
   *
   * @param clazz clazz
   * @return 对象名
   */
  public static String getClassName(Class clazz) {
    String[] arrays = clazz.getName().split("\\.");
    return arrays[arrays.length - 1];
  }

  /**
   * 格式化文件大小
   *
   * @param size file.length() 获取文件大小
   */
  public static String formatFileSize(double size) {
    if (size < 0) {
      return "0kb";
    }
    double kiloByte = size / 1024;
    if (kiloByte < 1) {
      return size + "b";
    }

    double megaByte = kiloByte / 1024;
    if (megaByte < 1) {
      BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
      return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "kb";
    }

    double gigaByte = megaByte / 1024;
    if (gigaByte < 1) {
      BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
      return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "mb";
    }

    double teraBytes = gigaByte / 1024;
    if (teraBytes < 1) {
      BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
      return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "gb";
    }
    BigDecimal result4 = new BigDecimal(teraBytes);
    return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "tb";
  }

  /**
   * 转换时间
   * 时间＜1 小时，显示分秒，显示样式 00:20
   * 时间 ≥1 小时，显示时分秒，显示样式 01:11:12
   * 时间 ≥1 天，显示天时分，显示样式 1d 01:11
   * 时间 ≥7 天，显示样式 ∞
   *
   * @param seconds 单位为s的时间
   */
  public static String formatTime(int seconds) {
    String standardTime;
    if (seconds <= 0) {
      standardTime = "00:00";
    } else if (seconds < 60) {
      standardTime = String.format(Locale.getDefault(), "00:%02d", seconds % 60);
    } else if (seconds < 3600) {
      standardTime = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
    } else if (seconds < 86400) {
      standardTime =
          String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60,
              seconds % 60);
    } else if (seconds < 604800) {
      standardTime =
          String.format(Locale.getDefault(), "%dd %02d:%02d", seconds / 86400,
              seconds % 86400 / 3600,
              seconds % 3600);
    } else {
      standardTime = "∞";
    }

    return standardTime;
  }

  /**
   * 通过文件名获取下载配置文件路径
   *
   * @param fileName 文件名
   */
  public static String getFileConfigPath(boolean isDownload, String fileName) {
    return AriaConfig.getInstance().getAPP().getFilesDir().getPath() + (isDownload
        ? AriaConfig.DOWNLOAD_TEMP_DIR
        : AriaConfig.UPLOAD_TEMP_DIR) + fileName + ".properties";
  }
}