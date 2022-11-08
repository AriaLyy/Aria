package com.arialyy.simple.core.test;

import android.os.Bundle;
import android.view.View;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityTestBinding;
import com.arialyy.simple.modlue.AnyRunnModule;

/**
 * Created by laoyuyu on 2018/4/13.
 */

public class AnyRunActivity extends BaseActivity<ActivityTestBinding> {
  AnyRunnModule module;
  String[] urls;
  int index = 0;
  //String URL = "http://static.gaoshouyou.com/d/12/0d/7f120f50c80d2e7b8c4ba24ece4f9cdd.apk";
  //String URL = "http://58.213.157.242:8081/sims_file/rest/v1/file/mshd_touchscreen_ms/guideFile/41c33556-dc4a-4d78-bb76-b9f627f94448.mp4/%E5%85%AB%E5%8D%A6%E6%B4%B2%E5%8D%97%E4%BA%AC%E5%86%9C%E4%B8%9A%E5%98%89%E5%B9%B4%E5%8D%8E0511.mp4";
  //String URL = "http://d1.showself.com/download/showself_android-s236279_release.apk";
  //String URL = "http://static.gaoshouyou.com/d/22/94/822260b849944492caadd2983f9bb624.apk";
  //private final String URL = "ftp://192.168.29.140:21/download/AriaPrj.rar";
  //String URL = "https://dl.genymotion.com/releases/genymotion-2.12.1/genymotion-2.12.1-vbox.exe";
  //String URL = "ftp://192.168.29.140:21/download/SDK_Demo-release.apk";
  //String URL = "ftps://192.168.29.140:990/download/SDK_Demo-release.apk";
  //String URL = "http://d.quanscreen.com/k/down/resourceDownLoad?resourceId=1994&clientId=A000011106034058176";
  //String URL = "ftp://z:z@dygod18.com:21211/[电影天堂www.dy2018.com]猩球崛起3：终极之战BD国英双语中英双字.mkv";
  //String URL = "https://app-dl.byfen.com//app//201810//ef0400d637e768e9c630b4d38633ee06.zip";
  String URL = "ftps://9.9.9.59:990/download/SDK_Demo-release.apk";
  //private String URL = "https://www.bilibili.com/bangumi/play/ep77693";
  //private String URL = "http://cn-hbsjz-cmcc-v-03.acgvideo.com/upgcxcode/63/82/5108263/5108263-1-80.flv?expires=1530178500&platform=pc&ssig=vr7gLl0duyqWqSMnIpzaDA&oi=3746029570&nfa=BpfiWF+i4mNW8KzjZFHzBQ==&dynamic=1&hfa=2030547937&hfb=Yjk5ZmZjM2M1YzY4ZjAwYTMzMTIzYmIyNWY4ODJkNWI=&trid=3476be01a9254115b15f8cc7198600fe&nfc=1";

  @Override protected int setLayoutId() {
    return R.layout.activity_test;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.init(this);
    mBar.setVisibility(View.GONE);
    module = new AnyRunnModule(this);
    urls = getResources().getStringArray(R.array.group_urls);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        //module.create(URL);
        module.startFtp(URL);
        break;
      case R.id.stop:
        module.stop(URL);
        break;
      case R.id.cancel:
        module.cancel(URL);
        //String newUrl = "ftp://192.168.29.140:21/download/11SDK_Demo-release.apk";
        //Aria.download(this)
        //    .loadFtp(URL)
        //    .updateUrl(newUrl)
        //    .login("lao", "123456")
        //    .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/")
        //    .create();
        break;
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    module.unRegister();
  }
}
