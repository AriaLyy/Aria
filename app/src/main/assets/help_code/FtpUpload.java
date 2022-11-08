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
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import com.arialyy.annotations.Upload;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.ftp.FtpInterceptHandler;
import com.arialyy.aria.core.common.ftp.IFtpUploadInterceptor;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.task.UploadTask;
import com.arialyy.frame.util.FileUtil;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import java.io.File;
import java.util.List;

/**
 * Created by lyy on 2019/5/28. Ftp 文件上传demo
 * <a href="https://aria.laoyuyu.me/aria_doc/">文档</>
 */
public class FtpUpload extends Activity {
  private static final String TAG = "FtpUpload";
  private String mFilePath = Environment.getExternalStorageDirectory().getPath() + "/AriaPrj.rar";
  private String mUrl = "ftp://172.168.1.2:2121/aa/你好";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Aria.upload(this).register();
    // 读取历史记录信息
    UploadEntity entity = Aria.upload(this).getUploadEntity(mFilePath);
    if (entity != null) {
      // 设置界面的进度、文件大小等信息
    }
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        if (Aria.upload(this).load(mFilePath).isRunning()) {
          Aria.upload(this).loadFtp(mFilePath).stop(); // 停止任务
        } else {
          Aria.upload(this)
              .loadFtp(mFilePath) // 需要上传的文件
              .setUploadUrl(mUrl) // 服务器地址
              // 如果ftp服务器端有同名文件，可通过拦截器处理是覆盖服务器端文件，还是修改文件名
              .setUploadInterceptor(
                  new IFtpUploadInterceptor() {

                    @Override
                    public FtpInterceptHandler onIntercept(UploadEntity entity,
                        List<String> fileList) {
                      FtpInterceptHandler.Builder builder = new FtpInterceptHandler.Builder();
                      //builder.coverServerFile();
                      builder.resetFileName("test.zip");
                      return builder.build();
                    }
                  })
              .login("N0rI", "0qcK")
              .start();
        }
        break;
      case R.id.cancel:
        Aria.upload(this).loadFtp(mFilePath).cancel();
        break;
    }
  }

  @Upload.onWait void onWait(UploadTask task) {
    Log.d(TAG, task.getTaskName() + "_wait");
  }

  @Upload.onPre public void onPre(UploadTask task) {
    setFileSize(task.getConvertFileSize());
  }

  @Upload.onTaskStart public void taskStart(UploadTask task) {
    Log.d(TAG, "开始上传，md5：" + FileUtil.getFileMD5(new File(task.getEntity().getFilePath())));
  }

  @Upload.onTaskResume public void taskResume(UploadTask task) {
    Log.d(TAG, "恢复上传");
  }

  @Upload.onTaskStop public void taskStop(UploadTask task) {
    setSpeed("");
    Log.d(TAG, "停止上传");
  }

  @Upload.onTaskCancel public void taskCancel(UploadTask task) {
    setSpeed("");
    setFileSize("");
    setProgress(0);
    Log.d(TAG, "删除任务");
  }

  @Upload.onTaskFail public void taskFail(UploadTask task) {
    Log.d(TAG, "上传失败");
  }

  @Upload.onTaskRunning public void taskRunning(UploadTask task) {
    Log.d(TAG, "PP = " + task.getPercent());
    setProgress(task.getPercent());
    setSpeed(task.getConvertSpeed());
  }

  @Upload.onTaskComplete public void taskComplete(UploadTask task) {
    setProgress(100);
    setSpeed("");
    T.showShort(this, "文件：" + task.getEntity().getFileName() + "，上传完成");
  }
}
