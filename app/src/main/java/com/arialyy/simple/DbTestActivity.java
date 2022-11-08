package com.arialyy.simple;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityDbTestBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DbTestActivity extends BaseActivity<ActivityDbTestBinding> {
  @Override
  protected int setLayoutId() {
    return R.layout.activity_db_test;
  }

  @Override
  protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.insert:
        insertManyRecord(10000);
        break;
      case R.id.search:
        break;
      case R.id.search_all:
        searchAll();
        break;
    }
  }

  private void searchAll() {
    long startT = System.currentTimeMillis();
    //List<DTaskWrapper> data = DownloadEntity.findRelationData(DownloadEntity.class);

    long endT = System.currentTimeMillis();
    Log.d(TAG, "search_time=" + (endT - startT));
  }

  private void insertManyRecord(int len) {
    long startT = System.currentTimeMillis();
    List<DbEntity> datas = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      String key = UUID.randomUUID().toString();
      String url = "https://blog.csdn.net/carefree31441/article/details/3998553";

      DownloadEntity entity = new DownloadEntity();
      entity.setUrl(url);
      entity.setFileName("ssssssssssssssssss");
      entity.setFilePath(key);

      DTaskWrapper dte = new DTaskWrapper(entity);

      datas.add(entity);
    }

    //AbsEntity.insertManyData(DownloadEntity.class, datas);
    AbsEntity.saveAll(datas);
    long endT = System.currentTimeMillis();

    Log.d(TAG, "insert_time=" + (endT - startT));
  }
}
