# Aria
![图标](https://github.com/AriaLyy/DownloadUtil/blob/master/img/ic_launcher.png)</br>
## [ENGLISH DOC](https://github.com/AriaLyy/Aria/blob/master/ENGLISH_README.md)</br>
## [中文文档](https://aria.laoyuyu.me/aria_doc)
The Aria project originated from the need for file download management encountered in work. At that time, the pain of being downloaded was unhappy. Since then, a simple and easy-to-use, stable and efficient download framework has emerged. Aria has experienced 1.0 to 3.0 development. , It is getting closer and closer to the goal set at the beginning.

Aria has the following characteristics:
 + Simple and convenient
   - Can be used in Activity, Service, Fragment, Dialog, popupWindow, Notification and other components
   - Support HTTP\FTP resumable download, multi-task automatic scheduling
   - Support multi-file package download, multi-file sharing the same progress (eg: video + cover + subtitles)
   - Support downloading FTP folder
   - Support HTTP form upload
   - Support file FTP resumable upload
   - Support FTPS resumable upload, [see](https://aria.laoyuyu.me/aria_doc/api/ftp_params.html#%E4%BA%8C%E3%80%81ftps)
   - Support SFTP resumable upload, [sftp download](https://aria.laoyuyu.me/aria_doc/download/sftp_normal.html), [sftp upload](https://aria.laoyuyu.me/aria_doc/upload /sftp_normal.html)
 + Support https address download
   - It is easy to set the CA certificate information in the configuration file
 + Support [Multi-threaded download in blocks](https://aria.laoyuyu.me/aria_doc/start/config.html), which can more effectively play the machine IO performance
 + Support 300, 301, 302 redirect download link download
 + Support file download of m3u8 and hls protocol [m3u8 download](https://aria.laoyuyu.me/aria_doc/download/m3u8.html)
 + Support m3u8 download support, [click to view details](https://aria.laoyuyu.me/aria_doc/download/m3u8_vod.html)
 + The download support file length increases dynamically, and the file download initialization will no longer occupy too much memory space, see [Dynamic Length Configuration](https://aria.laoyuyu.me/aria_doc/start/config.html#%E4% B8%8B%E8%BD%BD%E5%8A%A8%E6%80%81%E6%96%87%E4%BB%B6%E8%AF%B4%E6%98%8E)

[How to use Aria?] (#use)

If you think Aria is helpful to you, your star and issues will be my greatest support. Of course, you are also very welcome to PR, [PR Method](https://www.zhihu.com/question/21682976/answer /79489643)`^_^`

## Example
* Multitask download

![Multitask download](https://github.com/AriaLyy/DownloadUtil/blob/master/img/download_img.gif)

* Speed limit

![网速下载限制](https://github.com/AriaLyy/DownloadUtil/blob/master/img/max_speed.gif)

* Multi-file package download

<img src="https://github.com/AriaLyy/DownloadUtil/blob/master/img/group_task.gif" width="360" height="640"/>

* m3u8 download

![m3u8点播文件边下边看](https://github.com/AriaLyy/Aria/blob/master/img/m3u8VodDownload.gif)

## Lib
[![license](http://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=flat)](https://github.com/AriaLyy/Aria/blob/master/LICENSE)
[![Core](https://img.shields.io/badge/Core-3.8.10-blue)](https://github.com/AriaLyy/Aria)
[![Compiler](https://img.shields.io/badge/Compiler-3.8.10-blue)](https://github.com/AriaLyy/Aria)
[![FtpComponent](https://img.shields.io/badge/FtpComponent-3.8.10-orange)](https://github.com/AriaLyy/Aria)
[![FtpComponent](https://img.shields.io/badge/SFtpComponent-3.8.10-orange)](https://github.com/AriaLyy/Aria)
[![M3U8Component](https://img.shields.io/badge/M3U8Component-3.8.10-orange)](https://github.com/AriaLyy/Aria)


```java
implementation 'com.arialyy.aria:core:3.8.10'
annotationProcessor 'com.arialyy.aria:compiler:3.8.10'
implementation 'com.arialyy.aria:ftpComponent:3.8.10' # If you need to use ftp, please add this component
implementation 'com.arialyy.aria:sftpComponent:3.8.10' # If you need to use sftp, please add this component
implementation 'com.arialyy.aria:m3u8Component:3.8.10' # If you need to use the m3u8 download function, please add this component
```

If you are using Kotlin, please use the official method provided by Kotlin to configure apt, [kotlin kapt official configuration portal](https://www.kotlincn.net/docs/reference/kapt.html)

__⚠️Note: When the version below 3.5.4 is upgraded, the [configuration file] needs to be updated(https://aria.laoyuyu.me/aria_doc/start/config.html)！！__

__⚠️Note: Version 3.8 and above have been adapted to AndroidX and support libraries can be used

***
## Use
Because Aria involves file and network operations, you need to add the following permissions to the manifest file. If you want to use Aria in a system above 6.0, you need to dynamically apply for file system read and write permissions to the Android system, [How to use Android system permissions](https://developer.android.com/training/permissions/index.html?hl=zh-cn)
```xml
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

## Use Aria
### Basic use
The example is a single-task download, only a very simple code is needed to realize the download function.
* Create task
  ```java
  long taskId = Aria.download(this)
      .load(DOWNLOAD_URL)     //Read download address
      .setFilePath(DOWNLOAD_PATH) //Set the full path where the file is saved
      .create();   //Create and start download
  ```
* Stop\Resume task
  ```java
  Aria.download(this)
      .load(taskId)     //task id
      .stop();       // stop task
      //.resume();    // resume task

  ```

### Obtaining task status
Based on the consideration of decoupling, the download function of Aria is separated from the state acquisition, and the state acquisition will not be integrated into the chain code, but Aria provides another simpler and more flexible solution.
Through annotations, you can easily get all the status of the task.

1. Register the object to Aria
```java
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Aria.download(this).register();
}
```

2. Obtain task execution status through annotations
 **Note: **
  - Annotation back is implemented using Apt, so you don’t need to worry about this affecting the performance of your machine
  - Annotated methods **cannot be modified by private**
  - The annotated method ** can only have one parameter, and the parameter type must be `DownloadTask` or `UploadTask` or `DownloadGroupTask`**
  - Method name can be any string

```java
//Process the status of the task execution here, such as the refresh of the progress bar
@Download.onTaskRunning protected void running(DownloadTask task) {
	if(task.getKey().eques(url)){
		....
		You can judge whether it is the callback of the specified task by url
	}
	int p = task.getPercent();	//Task progress percentage
    String speed = task.getConvertSpeed();	//Download speed after unit conversion, unit conversion needs to be opened in the configuration file
   	String speed1 = task.getSpeed(); //Original byte length speed
}

@Download.onTaskComplete void taskComplete(DownloadTask task) {
	//Process the status of task completion here
}
```

### [Document address](https://aria.laoyuyu.me/aria_doc/)


### Version Log
 + v_3.8.10 (2020/6/26)
    - fix bug https://github.com/AriaLyy/Aria/issues/703
    - fix bug https://github.com/AriaLyy/Aria/issues/702
    - fix bug https://github.com/AriaLyy/Aria/issues/695

[More Version Log](https://github.com/AriaLyy/Aria/blob/master/DEV_LOG.md)

## Confusion configuration
```
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}
-keep class **$$DownloadListenerProxy{ *; }
-keep class **$$UploadListenerProxy{ *; }
-keep class **$$DownloadGroupListenerProxy{ *; }
-keep class **$$DGSubListenerProxy{ *; }
-keepclasseswithmembernames class * {
    @Download.* <methods>;
    @Upload.* <methods>;
    @DownloadGroup.* <methods>;
}

```

## Other
 If you have any questions, you can leave me a feedback at [issues](https://github.com/AriaLyy/Aria/issues). </br>
 Before submitting an issue, I hope you have checked [wiki](https://aria.laoyuyu.me/aria_doc/) or searched [issues](https://github.com/AriaLyy/Aria/issues). </br>

## Donate
 https://paypal.me/arialyy

***

License
-------

    Copyright 2016 AriaLyy(https://github.com/AriaLyy/Aria)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
