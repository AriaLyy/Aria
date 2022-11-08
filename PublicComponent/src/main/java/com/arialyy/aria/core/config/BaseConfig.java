package com.arialyy.aria.core.config;

import android.text.TextUtils;
import com.arialyy.aria.core.AriaConfig;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import java.io.Serializable;

abstract class BaseConfig implements Serializable {
  private static final String TAG = "BaseConfig";
  static final int TYPE_DOWNLOAD = 1;
  static final int TYPE_UPLOAD = 2;
  static final int TYPE_APP = 3;
  static final int TYPE_DGROUP = 4;

  /**
   * 类型
   *
   * @return {@link #TYPE_DOWNLOAD}、{@link #TYPE_UPLOAD}、{@link #TYPE_APP}、{@link #TYPE_DGROUP}
   */
  abstract int getType();

  /**
   * 保存配置
   */
  void save() {
    String basePath = AriaConfig.getInstance().getAPP().getFilesDir().getPath();
    String path = null;
    switch (getType()) {
      case TYPE_DOWNLOAD:
        path = Configuration.DOWNLOAD_CONFIG_FILE;
        break;
      case TYPE_UPLOAD:
        path = Configuration.UPLOAD_CONFIG_FILE;
        break;
      case TYPE_APP:
        path = Configuration.APP_CONFIG_FILE;
        break;
      case TYPE_DGROUP:
        path = Configuration.DGROUP_CONFIG_FILE;
        break;
    }
    if (!TextUtils.isEmpty(path)) {
      String tempPath = String.format("%s%s", basePath, path);
      FileUtil.deleteFile(tempPath);
      FileUtil.writeObjToFile(tempPath, this);
    } else {
      ALog.e(TAG, String.format("保存配置失败，配置类型：%s，原因：路径错误", getType()));
    }
  }
}