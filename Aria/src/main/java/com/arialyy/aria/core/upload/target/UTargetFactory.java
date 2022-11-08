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
package com.arialyy.aria.core.upload.target;

import com.arialyy.aria.core.common.AbsBuilderTarget;
import com.arialyy.aria.core.common.AbsNormalTarget;

/**
 * @author aria
 * Date: 2019-09-05
 */
public class UTargetFactory {

  public static volatile UTargetFactory INSTANCE;

  private UTargetFactory() {

  }

  public static UTargetFactory getInstance() {

    if (INSTANCE == null) {
      synchronized (UTargetFactory.class) {
        if (INSTANCE == null) {
          INSTANCE = new UTargetFactory();
        }
      }
    }

    return INSTANCE;
  }

  public <T extends AbsNormalTarget> T generateNormalTarget(Class<T> clazz, long taskId) {
    T target = null;
    if (clazz == HttpNormalTarget.class) {
      target = (T) new HttpNormalTarget(taskId);
    } else if (clazz == FtpNormalTarget.class) {
      target = (T) new FtpNormalTarget(taskId);
    }

    return target;
  }

  public <T extends AbsBuilderTarget> T generateBuilderTarget(Class<T> clazz, String url) {
    T target = null;
    if (clazz == HttpBuilderTarget.class) {
      target = (T) new HttpBuilderTarget(url);
    } else if (clazz == FtpBuilderTarget.class) {
      target = (T) new FtpBuilderTarget(url);
    }

    return target;
  }
}
