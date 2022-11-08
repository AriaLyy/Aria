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

import com.arialyy.aria.core.common.AbsEntity;

/**
 * 删除记录
 */
public interface IDeleteRecord {

  /**
   * 删除记录
   *
   * @param key 记录关联的key
   * @param needRemoveFile 是否需要删除文件
   * @param needRemoveEntity 是否需要删除实体，true 删除实体
   */
  void deleteRecord(String key, boolean needRemoveFile, boolean needRemoveEntity);

  /**
   * 删除记录
   *
   * @param absEntity 记录关联的实体
   * @param needRemoveFile 是否需要删除文件
   * @param needRemoveEntity 是否需要删除实体，true 删除实体
   */
  void deleteRecord(AbsEntity absEntity, boolean needRemoveFile, boolean needRemoveEntity);
}
