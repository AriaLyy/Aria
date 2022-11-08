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

package com.arialyy.simple.core.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadEntity
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.listener.ISchedulers
import com.arialyy.aria.util.ALog
import com.arialyy.aria.util.CommonUtil
import com.arialyy.frame.util.show.T
import com.arialyy.simple.R
import com.arialyy.simple.R.string
import com.arialyy.simple.base.BaseActivity
import com.arialyy.simple.common.ModifyPathDialog
import com.arialyy.simple.common.ModifyUrlDialog
import com.arialyy.simple.databinding.ActivitySingleKotlinBinding
import com.arialyy.simple.util.AppUtil
import com.pddstudio.highlightjs.models.Language
import java.io.IOException

class KotlinDownloadActivity : BaseActivity<ActivitySingleKotlinBinding>() {

  private var mUrl: String? = null
  private var mFilePath: String? = null
  private var mModule: HttpDownloadModule? = null
  private var mTaskId: Long = -1

  internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(
      context: Context,
      intent: Intent
    ) {
      if (intent.action == ISchedulers.ARIA_TASK_INFO_ACTION) {
        ALog.d(
            TAG,
            "state = " + intent.getIntExtra(ISchedulers.TASK_STATE, -1)
        )
        ALog.d(
            TAG, "type = " + intent.getIntExtra(ISchedulers.TASK_TYPE, -1)
        )
        ALog.d(
            TAG,
            "speed = " + intent.getLongExtra(ISchedulers.TASK_SPEED, -1)
        )
        ALog.d(
            TAG, "percent = " + intent.getIntExtra(
            ISchedulers.TASK_PERCENT, -1
        )
        )
        ALog.d(
            TAG, "entity = " + intent.getParcelableExtra<DownloadEntity>(
            ISchedulers.TASK_ENTITY
        ).toString()
        )
      }
    }
  }

  override fun onResume() {
    super.onResume()
    //registerReceiver(receiver, new IntentFilter(ISchedulers.ARIA_TASK_INFO_ACTION));
  }

  override fun onDestroy() {
    super.onDestroy()
    //unregisterReceiver(receiver);
    Aria.download(this)
        .unRegister()
  }

  override fun init(savedInstanceState: Bundle?) {
    super.init(savedInstanceState)
    title = "单任务下载"
    Aria.download(this)
        .register()
    mModule = ViewModelProviders.of(this)
        .get(HttpDownloadModule::class.java)
    mModule!!.getHttpDownloadInfo(this)
        .observe(this, Observer { entity ->
          if (entity == null) {
            return@Observer
          }
          if (entity.state == IEntity.STATE_STOP) {
            binding.stateStr = getString(string.resume)
          }

          if (Aria.download(this).load(entity.id).isRunning) {
            binding.stateStr = getString(string.stop)
          }

          if (entity.fileSize != 0L) {
            binding.fileSize = CommonUtil.formatFileSize(entity.fileSize.toDouble())
            binding.progress = if (entity.isComplete)
              100
            else
              (entity.currentProgress * 100 / entity.fileSize).toInt()
          }
          binding.url = entity.url
          binding.filePath = entity.filePath
          mUrl = entity.url
          mFilePath = entity.filePath
        })
    binding.viewModel = this
    try {
      binding.codeView.setSource(AppUtil.getHelpCode(this, "KotlinHttpDownload.kt"), Language.JAVA)
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  fun chooseUrl() {
    val dialog = ModifyUrlDialog(this, getString(R.string.modify_url_dialog_title), mUrl)
    dialog.show(supportFragmentManager, "ModifyUrlDialog")
  }

  fun chooseFilePath() {
    val dialog = ModifyPathDialog(this, getString(R.string.modify_file_path), mFilePath)
    dialog.show(supportFragmentManager, "ModifyPathDialog")
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_single_task_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onMenuItemClick(item: MenuItem): Boolean {
    var speed = -1
    var msg = ""
    when (item.itemId) {
      R.id.help -> {
        msg = ("一些小知识点：\n"
            + "1、你可以在注解中增加链接，用于指定被注解的方法只能被特定的下载任务回调，以防止progress乱跳\n"
            + "2、当遇到网络慢的情况时，你可以先使用onPre()更新UI界面，待连接成功时，再在onTaskPre()获取完整的task数据，然后给UI界面设置正确的数据\n"
            + "3、你可以在界面初始化时通过Aria.download(this).load(URL).getPercent()等方法快速获取相关任务的一些数据")
        showMsgDialog("tip", msg)
      }
      R.id.speed_0 -> speed = 0
      R.id.speed_128 -> speed = 128
      R.id.speed_256 -> speed = 256
      R.id.speed_512 -> speed = 512
      R.id.speed_1m -> speed = 1024
    }
    if (speed > -1) {
      msg = item.title.toString()
      Aria.download(this)
          .setMaxSpeed(speed)
      T.showShort(this, msg)
    }
    return true
  }

  @Download.onWait
  fun onWait(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == mUrl) {
      Log.d(TAG, "wait ==> " + task.downloadEntity.fileName)
    }
  }

  @Download.onPre
  fun onPre(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == mUrl) {
      binding.stateStr = getString(R.string.stop)
    }
  }

  @Download.onTaskStart
  fun taskStart(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == mUrl) {
      binding.fileSize = task.convertFileSize
    }
  }

  @Download.onTaskRunning
  fun running(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == mUrl) {
      //Log.d(TAG, task.getKey());
      val len = task.fileSize
      if (len == 0L) {
        binding.progress = 0
      } else {
        binding.progress = task.percent
      }
      binding.speed = task.convertSpeed
    }
  }

  @Download.onTaskResume
  fun taskResume(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == mUrl) {
      binding.stateStr = getString(R.string.stop)
    }
  }

  @Download.onTaskStop
  fun taskStop(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == mUrl) {
      binding.stateStr = getString(R.string.resume)
      binding.speed = ""
    }
  }

  @Download.onTaskCancel
  fun taskCancel(task: com.arialyy.aria.core.task.DownloadTask) {
    if (task.key == mUrl) {
      binding.progress = 0
      binding.stateStr = getString(R.string.start)
      binding.speed = ""
      Log.d(TAG, "cancel")
    }
  }

  /**
   *
   */
  @Download.onTaskFail
  fun taskFail(
    task: com.arialyy.aria.core.task.DownloadTask,
    e: Exception
  ) {
    if (task.key == mUrl) {
      Toast.makeText(this, getString(R.string.download_fail), Toast.LENGTH_SHORT)
          .show()
      binding.stateStr = getString(R.string.start)
    }
  }

  @Download.onTaskComplete
  fun taskComplete(task: com.arialyy.aria.core.task.DownloadTask) {

    if (task.key == mUrl) {
      binding.progress = 100
      Toast.makeText(
          this, getString(R.string.download_success),
          Toast.LENGTH_SHORT
      )
          .show()
      binding.stateStr = getString(R.string.re_start)
      binding.speed = ""
    }
  }

  override fun setLayoutId(): Int {
    return R.layout.activity_single_kotlin
  }

  fun onClick(view: View) {
    when (view.id) {
      R.id.start -> if (Aria.download(this).load(mTaskId).isRunning) {
        Aria.download(this)
            .load(mTaskId)
            .stop()
      } else {
        startD()
      }
      R.id.stop -> Aria.download(this).load(mTaskId).stop()
      R.id.cancel -> Aria.download(this).load(mTaskId).cancel(true)
    }
  }

  private fun startD() {
    if (mTaskId == -1L) {

      mTaskId = Aria.download(this)
          .load(mUrl!!)
          //.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
          //.addHeader("Accept-Encoding", "gzip, deflate")
          //.addHeader("DNT", "1")
          //.addHeader("Cookie", "BAIDUID=648E5FF020CC69E8DD6F492D1068AAA9:FG=1; BIDUPSID=648E5FF020CC69E8DD6F492D1068AAA9; PSTM=1519099573; BD_UPN=12314753; locale=zh; BDSVRTM=0")
          .setFilePath(mFilePath!!, true)
          .create()
    } else {
      Aria.download(this)
          .load(mTaskId)
          .resume()
    }
  }

  override fun onStop() {
    super.onStop()
    //Aria.download(this).unRegister();
  }

  override fun dataCallback(
    result: Int,
    data: Any
  ) {
    super.dataCallback(result, data)
    if (result == ModifyUrlDialog.MODIFY_URL_DIALOG_RESULT) {
      mModule!!.uploadUrl(this, data.toString())
    } else if (result == ModifyPathDialog.MODIFY_PATH_RESULT) {
      mModule!!.updateFilePath(this, data.toString())
    }
  }
}