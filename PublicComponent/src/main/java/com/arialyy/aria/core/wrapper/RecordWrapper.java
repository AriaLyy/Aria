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
package com.arialyy.aria.core.wrapper;

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.orm.AbsDbWrapper;
import com.arialyy.aria.orm.annotation.Many;
import com.arialyy.aria.orm.annotation.One;
import com.arialyy.aria.orm.annotation.Wrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by laoyuyu on 2018/3/30.
 * 任务记录和线程记录的关系
 */
@Wrapper
public class RecordWrapper extends AbsDbWrapper {

  @One
  public TaskRecord taskRecord;

  @Many(parentColumn = "filePath", entityColumn = "taskKey")
  public List<ThreadRecord> threadRecords;

  @Override protected void handleConvert() {
    if (threadRecords != null && !threadRecords.isEmpty()) {
      taskRecord.threadRecords = threadRecords;
    } else {
      taskRecord.threadRecords = new ArrayList<>();
    }
  }
}
