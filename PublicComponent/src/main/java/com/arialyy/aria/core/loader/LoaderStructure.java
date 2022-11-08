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
package com.arialyy.aria.core.loader;

import com.arialyy.aria.core.inf.IThreadStateManager;
import java.util.ArrayList;
import java.util.List;

public class LoaderStructure {
  private List<ILoaderComponent> parts = new ArrayList<>();

  public void accept(ILoaderVisitor visitor) {

    for (ILoaderComponent part : parts) {
      part.accept(visitor);
    }
  }

  /**
   * 将组件加入到集合，必须添加以下集合：
   * 1 {@link IRecordHandler}
   * 2 {@link IInfoTask}
   * 3 {@link IThreadStateManager}
   * 4 {@link IThreadTaskBuilder}
   *
   * @param component 待添加的组件
   */
  public LoaderStructure addComponent(ILoaderComponent component) {
    parts.add(component);
    return this;
  }
}
