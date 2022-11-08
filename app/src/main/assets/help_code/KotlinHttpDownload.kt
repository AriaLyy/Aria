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
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.target.HttpNormalTarget
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.inf.IEntity
import com.arialyy.simple.R
import com.arialyy.simple.base.BaseActivity
import com.arialyy.simple.databinding.ActivitySingleBinding

/**
 * Created by lyy on 2017/10/23.
 */
class KotlinDownloadActivity : BaseActivity<ActivitySingleBinding>() {

  private val DOWNLOAD_URL =
    "http://static.gaoshouyou.com/d/22/94/822260b849944492caadd2983f9bb624.apk"

  private lateinit var mStart: Button
  private lateinit var mStop: Button
  private lateinit var mCancel: Button
  private lateinit var target: HttpNormalTarget

  override fun setLayoutId(): Int {
    return R.layout.activity_single
  }

  override fun init(savedInstanceState: Bundle?) {
    title = "kotlin测试"
    Aria.get(this)
        .downloadConfig.maxTaskNum = 2
    Aria.download(this)
        .register()
    mStart = findViewById(R.id.start)
    mStop = findViewById(R.id.stop)
    mCancel = findViewById(R.id.cancel)
    mStop.visibility = View.GONE

    target = Aria.download(this)
        .load(DOWNLOAD_URL)
    binding.progress = target.percent
    if (target.taskState == com.arialyy.aria.inf.IEntity.STATE_STOP) {
      mStart.text = "恢复"
    } else if (target.isRunning) {
      mStart.text = "停止"
    }
    binding.fileSize = target.convertFileSize
  }

  /**
   * 注解方法不能添加internal修饰符，否则会出现e: [kapt] An exception occurred: java.lang.IllegalArgumentException: peerIndex 1 for '$a' not in range (received 0 arguments)错误
   */
  @Download.onTaskRunning
  fun running(task: com.arialyy.aria.core.task.DownloadTask) {
    Log.d(TAG, task.percent.toString())
    val len = task.fileSize
    if (len == 0L) {
      binding.progress = 0
    } else {
      binding.progress = task.percent
    }
    binding.speed = task.convertSpeed
  }

  @Download.onWait
  fun onWait(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == DOWNLOAD_URL) {
      Log.d(TAG, "wait ==> " + task.downloadEntity.fileName)
    }
  }

  @Download.onPre
  fun onPre(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == DOWNLOAD_URL) {
      mStart.text = "停止"
    }
  }

  @Download.onTaskStart
  fun taskStart(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == DOWNLOAD_URL) {
      binding.fileSize = task.convertFileSize
    }
  }

  @Download.onTaskComplete
  fun complete(task: com.arialyy.aria.core.task.DownloadTask) {
    Log.d(TAG, "完成")
  }

  @Download.onTaskResume
  fun taskResume(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == DOWNLOAD_URL) {
      mStart.text = "停止"
    }
  }

  @Download.onTaskStop
  fun taskStop(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == DOWNLOAD_URL) {
      mStart.text = "恢复"
      binding.speed = ""
    }
  }

  @Download.onTaskCancel
  fun taskCancel(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == DOWNLOAD_URL) {
      binding.progress = 0
      Toast.makeText(this@KotlinDownloadActivity, "取消下载", Toast.LENGTH_SHORT)
          .show()
      mStart.text = "开始"
      binding.speed = ""
      Log.d(TAG, "cancel")
    }
  }

  @Download.onTaskFail
  fun taskFail(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == DOWNLOAD_URL) {
      Toast.makeText(this@KotlinDownloadActivity, "下载失败", Toast.LENGTH_SHORT)
          .show()
      mStart.text = "开始"
    }
  }

  fun onClick(view: View) {
    when (view.id) {
      R.id.start -> {
        if (target.isRunning) {
          Aria.download(this)
              .load(DOWNLOAD_URL)
              .stop()
        } else {
          startD()
        }
      }
      R.id.stop -> Aria.download(this).load(DOWNLOAD_URL).stop()
      R.id.cancel -> Aria.download(this).load(DOWNLOAD_URL).cancel()
    }
  }

  private fun startD() {
    Aria.download(this)
        .load(DOWNLOAD_URL)
        .addHeader("groupHash", "value")
        .setFilePath(Environment.getExternalStorageDirectory().path + "/kotlin.apk")
        .start()
  }
}