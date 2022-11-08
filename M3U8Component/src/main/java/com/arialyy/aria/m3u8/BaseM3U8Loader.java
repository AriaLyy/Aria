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
package com.arialyy.aria.m3u8;

import android.text.TextUtils;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.loader.AbsNormalLoader;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public abstract class BaseM3U8Loader extends AbsNormalLoader<DTaskWrapper> {
  protected M3U8TaskOption mM3U8Option;

  public BaseM3U8Loader(DTaskWrapper wrapper, IEventListener listener) {
    super(wrapper, listener);
    mM3U8Option = (M3U8TaskOption) wrapper.getM3u8Option();
    mTempFile = new File(wrapper.getEntity().getFilePath());
  }

  @Override protected long delayTimer() {
    return 1000;
  }

  /**
   * 获取ts文件保存路径
   *
   * @param dirCache 缓存目录
   * @param threadId ts文件名
   */
  public static String getTsFilePath(String dirCache, int threadId) {
    return String.format("%s/%s.ts", dirCache, threadId);
  }

  public String getCacheDir() {
    String cacheDir = mM3U8Option.getCacheDir();
    if (TextUtils.isEmpty(cacheDir)) {
      cacheDir = FileUtil.getTsCacheDir(getEntity().getFilePath(), mM3U8Option.getBandWidth());
    }
    if (!new File(cacheDir).exists()) {
      FileUtil.createDir(cacheDir);
    }
    return cacheDir;
  }

  /**
   * 创建索引文件
   */
  public boolean generateIndexFile(boolean isLive) {
    File tempFile =
        new File(String.format(M3U8InfoTask.M3U8_INDEX_FORMAT, getEntity().getFilePath()));
    if (!tempFile.exists()) {
      ALog.e(TAG, "源索引文件不存在");
      return false;
    }
    FileInputStream fis = null;
    FileOutputStream fos = null;
    try {
      String cacheDir = getCacheDir();
      fis = new FileInputStream(tempFile);
      fos = new FileOutputStream(getEntity().getFilePath());
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      String line;
      int i = 0;
      while ((line = reader.readLine()) != null) {
        byte[] bytes;
        if (line.startsWith("#EXTINF")) {
          fos.write(line.concat("\r\n").getBytes(Charset.forName("UTF-8")));
          String tsPath = getTsFilePath(cacheDir, mRecord.threadRecords.get(i).threadId);
          bytes = tsPath.concat("\r\n").getBytes(Charset.forName("UTF-8"));
          reader.readLine(); // 继续读一行，避免写入源索引文件的切片地址
          i++;
        } else if (line.startsWith("#EXT-X-KEY")) {
          M3U8Entity m3U8Entity = getEntity().getM3U8Entity();
          StringBuilder sb = new StringBuilder("#EXT-X-KEY:");
          sb.append("METHOD=").append(m3U8Entity.method);
          sb.append(",URI=\"").append(m3U8Entity.keyPath).append("\"");
          if (!TextUtils.isEmpty(m3U8Entity.iv)) {
            sb.append(",IV=").append(m3U8Entity.iv);
          }
          if (!TextUtils.isEmpty(m3U8Entity.keyFormat)) {
            sb.append(",KEYFORMAT=\"").append(m3U8Entity.keyFormat).append("\"");
            sb.append(",KEYFORMATVERSIONS=\"")
                .append(TextUtils.isEmpty(m3U8Entity.keyFormatVersion) ? "1"
                    : m3U8Entity.keyFormatVersion)
                .append("\"");
          }
          sb.append("\r\n");
          bytes = sb.toString().getBytes(Charset.forName("UTF-8"));
        } else {
          bytes = line.concat("\r\n").getBytes(Charset.forName("UTF-8"));
        }
        fos.write(bytes, 0, bytes.length);
      }
      // 直播的索引文件需要在结束的时候才写入结束标志
      if (isLive) {
        fos.write("#EXT-X-ENDLIST".concat("\r\n").getBytes(Charset.forName("UTF-8")));
      }

      fos.flush();
      return true;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
        if (fos != null) {
          fos.close();
        }
        if (tempFile.exists()) {
          FileUtil.deleteFile(tempFile);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  @Override public long getCurrentProgress() {
    return isRunning() ? getStateManager().getCurrentProgress() : getEntity().getCurrentProgress();
  }

  protected DownloadEntity getEntity() {
    return mTaskWrapper.getEntity();
  }
}
