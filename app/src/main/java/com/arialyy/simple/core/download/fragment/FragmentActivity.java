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

package com.arialyy.simple.core.download.fragment;

import android.os.Bundle;
import com.arialyy.aria.core.Aria;
import com.arialyy.simple.MainActivity;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.FragmentDownloadBinding;
import com.arialyy.simple.to.NormalTo;

/**
 * Created by lyy on 2017/1/4.
 */

public class FragmentActivity extends BaseActivity<FragmentDownloadBinding> {
  @Override protected int setLayoutId() {
    return R.layout.activity_fragment;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);

    final NormalTo to = getIntent().getParcelableExtra(MainActivity.KEY_MAIN_DATA);
    setTitle(to.title);
  }
}
