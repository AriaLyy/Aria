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
import com.arialyy.simple.core.download.fragment.MultiFragmentActivity;
import com.arialyy.simple.core.download.mutil.MultiTaskListActivity;
import com.arialyy.simple.databinding.ActivityDownloadMeanBinding;
import com.arialyy.simple.modlue.CommonModule;
import com.arialyy.simple.to.NormalTo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyy on 2016/10/13.
 */
public class DownloadActivity extends BaseActivity<ActivityDownloadMeanBinding> {
  private NormalTo mTo;

  @Override protected int setLayoutId() {
    return R.layout.activity_download_mean;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    mTo = getIntent().getParcelableExtra(MainActivity.KEY_MAIN_DATA);
    setTitle(mTo.title);

    final List<NormalTo> data = new ArrayList<>();
    getBinding().list.setLayoutManager(new GridLayoutManager(this, 2));
    final NormalToAdapter adapter = new NormalToAdapter(this, data);
    getBinding().list.setAdapter(adapter);
    final CommonModule module = ViewModelProviders.of(this).get(CommonModule.class);

    module.getDownloadData(this).observe(this,
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
                module.startNextActivity(DownloadActivity.this, data.get(position),
                    SingleTaskActivity.class);
                break;
              case 1:
                module.startNextActivity(DownloadActivity.this, data.get(position),
                    MultiTaskListActivity.class);
                break;
              case 2:
                module.startNextActivity(DownloadActivity.this, data.get(position),
                    HighestPriorityActivity.class);
                break;
              case 3:
                module.startNextActivity(DownloadActivity.this, data.get(position),
                    KotlinDownloadActivity.class);
                break;
              case 4:
                // 多fragment
                module.startNextActivity(DownloadActivity.this, data.get(position),
                    MultiFragmentActivity.class);
                break;
              case 5:
                // 组件中使用
                module.startNextActivity(DownloadActivity.this, data.get(position),
                    ComponentActivity.class);
                break;
            }
          }
        });
  }
}