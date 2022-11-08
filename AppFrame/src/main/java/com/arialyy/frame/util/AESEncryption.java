package com.arialyy.frame.util;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import java.io.File;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密
 *
 * author lyy
 */
public class AESEncryption {
  /**
   * 4.4的要Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
   */
  private static final int JELLY_BEAN_4_2 = 17;

  /**
   * 加密文件
   *
   * @param filePath 原始文件路径
   * @return 加密后的文件路径
   */
  public static String encryptFile(@NonNull String filePath) {
    File file = new File(filePath);
    return "";
  }

  /**
   * 加密函数
   *
   * @param seed 密钥
   * @param cleartext 说要进行加密的密码
   * @return 返回的是16进制的加密类型
   * @throws Exception
   */
  public static String encryptString(String seed, String cleartext) throws Exception {
    byte[] rawKey = getRawKey(seed.getBytes());
    byte[] result = encryptByte(rawKey, cleartext.getBytes());
    return toHex(result);
  }

  /**
   * 解密函数
   *
   * @param seed 密钥
   * @param encrypted 进行加密后的密码
   * @return 返回原来的密码
   * @throws Exception
   */
  public static String decryptString(String seed, String encrypted) throws Exception {
    byte[] rawKey = getRawKey(seed.getBytes());
    byte[] enc = toByte(encrypted);
    byte[] result = decryptByte(rawKey, enc);
    return new String(result);
  }

  /**
   * 获取key
   *
   * @throws Exception
   */
  @SuppressLint("DeletedProvider") private static byte[] getRawKey(byte[] seed) throws Exception {
    KeyGenerator kgen = KeyGenerator.getInstance("AES");

    SecureRandom sr = null;
    if (android.os.Build.VERSION.SDK_INT >= JELLY_BEAN_4_2) {
      sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
    } else {
      sr = SecureRandom.getInstance("SHA1PRNG");
    }
    // SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
    sr.setSeed(seed);
    kgen.init(128, sr); // 192 and 256 bits may not be available
    SecretKey skey = kgen.generateKey();
    return skey.getEncoded();
  }

  /**
   * 4.3以上的要用cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
   * 加密byte流
   *
   * @throws Exception
   */
  private static byte[] encryptByte(byte[] raw, byte[] clear) throws Exception {
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = null;
    if (android.os.Build.VERSION.SDK_INT > JELLY_BEAN_4_2) {
      cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
    } else {
      cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    }
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
    return cipher.doFinal(clear);
  }

  /**
   * 4.3以上的要用cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
   * 解密byte流
   *
   * @throws Exception
   */
  private static byte[] decryptByte(byte[] raw, byte[] encrypted) throws Exception {
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    Cipher cipher = null;
    if (android.os.Build.VERSION.SDK_INT > JELLY_BEAN_4_2) {
      cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
    } else {
      cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    }
    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
    return cipher.doFinal(encrypted);
  }

  public static String toHex(String txt) {
    return toHex(txt.getBytes());
  }

  public static String fromHex(String hex) {
    return new String(toByte(hex));
  }

  public static byte[] toByte(String hexString) {
    int len = hexString.length() / 2;
    byte[] result = new byte[len];
    for (int i = 0; i < len; i++)
      result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
    return result;
  }

  /**
   * 把用户的key转换为16为的key
   * AES算法的秘钥要求16位
   */
  public static String toHex(byte[] buf) {
    if (buf == null) {
      return "";
    }
    StringBuffer result = new StringBuffer(2 * buf.length);
    for (byte aBuf : buf) {
      appendHex(result, aBuf);
    }
    return result.toString();
  }

  private final static String HEX = "0123456789ABCDEF";

  private static void appendHex(StringBuffer sb, byte b) {
    sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
  }
}
