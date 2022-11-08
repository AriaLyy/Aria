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
package com.arialyy.aria.ftp.download;

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.loader.AbsNormalTTBuilderAdapter;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.task.IThreadTaskAdapter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import java.io.File;

final class FtpDTTBuilderAdapter extends AbsNormalTTBuilderAdapter {

  @Override public IThreadTaskAdapter getAdapter(SubThreadConfig config) {
    return new FtpDThreadTaskAdapter(config);
  }

  @Override public boolean handleNewTask(TaskRecord record, int totalThreadNum) {
    if (!record.isBlock) {
      if (getTempFile().exists()) {
        FileUtil.deleteFile(getTempFile());
      }
      //CommonUtil.createFile(mTempFile.getPath());
    } else {
      for (int i = 0; i < totalThreadNum; i++) {
        File blockFile =
            new File(String.format(IRecordHandler.SUB_PATH, getTempFile().getPath(), i));
        if (blockFile.exists()) {
          ALog.d(TAG, String.format("分块【%s】已经存在，将删除该分块", i));
          FileUtil.deleteFile(blockFile);
        }
      }
    }
    return true;
  }
}
