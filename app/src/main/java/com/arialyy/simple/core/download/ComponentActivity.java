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
package com.arialyy.simple.core.download;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.arialyy.simple.MainActivity;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.base.adapter.RvItemClickSupport;
import com.arialyy.simple.common.NormalToAdapter;
import com.arialyy.simple.core.download.fragment.FragmentActivity;
import com.arialyy.simple.databinding.ActivityComponentBinding;
import com.arialyy.simple.modlue.CommonModule;
import com.arialyy.simple.to.NormalTo;
import java.util.ArrayList;
import java.util.List;

public class ComponentActivity extends BaseActivity<ActivityComponentBinding> {
  @Override protected int setLayoutId() {
    return R.layout.activity_component;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    NormalTo to = getIntent().getParcelableExtra(MainActivity.KEY_MAIN_DATA);
    setTitle(to.title);

    final List<NormalTo> data = new ArrayList<>();
    getBinding().list.setLayoutManager(new GridLayoutManager(this, 2));
    final NormalToAdapter adapter = new NormalToAdapter(this, data);
    getBinding().list.setAdapter(adapter);
    final CommonModule module = ViewModelProviders.of(this).get(CommonModule.class);

    module.getComponentData(this).observe(this,
        new Observer<List<NormalTo>>() {
          @Override public void onChanged(@Nullable List<NormalTo> normalTos) {
            if (normalTos != null) {
              data.addAll(normalTos);
              adapter.notifyDataSetChanged();
            }
          }
        });
    RvItemClickSupport.addTo(getBinding().list).setOnItemClickListener(
        new RvItemClickSupport.OnItemClickListener() {
          @Override public void onItemClicked(RecyclerView recyclerView, int position, View v) {
            switch (position) {
              case 0:
                module.startNextActivity(ComponentActivity.this, data.get(position),
                    FragmentActivity.class);
                break;
              case 1:
                DownloadDialog dialog = new DownloadDialog(ComponentActivity.this);
                dialog.show();
                break;
              case 2:
                new DownloadDialogFragment(ComponentActivity.this).show(getSupportFragmentManager(),
                    "df");
                break;
            }
          }
        });
  }
}
