## 开发日志
  + v_3.8.16 (2021/4/18)
    - 修复单线程下载时，文件已经完成，重复下载回调下载失败的问题
    - 修复一个重新下载文件时，同名路径文件没有被被删除的问题
    - fix bug https://github.com/AriaLyy/Aria/issues/807
    - fix bug https://github.com/AriaLyy/Aria/issues/811
    - fix bug https://github.com/AriaLyy/Aria/issues/851
    - 修复组合任务在获取子任务信息的过程中，手动停止或删除，没有回调的问题
  + v_3.8.15 (2020/11/9)
    - 修复不支持断点的连接下载失败问题，https://github.com/AriaLyy/Aria/issues/771
    - 修复iv不存在时，索引文件异常的问题，https://github.com/AriaLyy/Aria/issues/780
    - fix bug https://github.com/AriaLyy/Aria/issues/799
    - fix bug https://github.com/AriaLyy/Aria/issues/798
  + v_3.8.14 (2020/9/23)
    - 修复spi机制的兼容问题，https://github.com/AriaLyy/Aria/issues/743
    - 增加路径不可以写的判断，下载时，如果路径不可写，将执行失败回调 https://github.com/AriaLyy/Aria/issues/750
  + v_3.8.12 (2020/8/15)
    - 修复一个正则表达式导致的文件名保存号异常问题 https://github.com/AriaLyy/Aria/issues/715
    - 修复一个匿名内部类中的内存溢出的问题 https://github.com/AriaLyy/Aria/issues/705
    - 修复同一个url地址的任务提示路径冲突的问题
    - 修复m3u8任务地址错误时，无法删除实体的问题 https://github.com/AriaLyy/Aria/issues/712
    - 修复m3u8gzip的问题，https://github.com/AriaLyy/Aria/issues/639
    - 修复http表单上传，本地md5和上传的服务的的文件md5不一致的问题 https://github.com/AriaLyy/Aria/issues/730
    - 修复0kb的文件不可下载的问题 https://github.com/AriaLyy/Aria/issues/711
    - 修复加密的m3u8下载，并且使用索引模式时，key的uri没有使用双引号的问题 https://github.com/AriaLyy/Aria/issues/731
    - 修复删除m3u8索引文件后，下载完成回调无法触发的问题
    - 修复删除加密的m3u8文件时，key没有被删除的问题 https://github.com/AriaLyy/Aria/issues/735#issuecomment-673958845
    - 增加m3u8密钥下载地址转换器增加ts列表的url地址 https://github.com/AriaLyy/Aria/issues/718
    - 增加现在http文件下载将使用HEAD请求获取文件大小，配置文件增加 <useHeadRequest value="true"/>。慎用，并不是所有服务器都支持head请求
    - 增加允许不使用apt直接通过实现监听器来回调下载进度更新，该功能由[chenfei0928](https://github.com/chenfei0928)提交，感谢他的pr。如果注解不生效，只需要实现`DownloadListener`接口便可
    - 增加m3u8使用`ignoreFailureTs`后将不会自动重试失败的切片 https://github.com/AriaLyy/Aria/issues/662
  + v_3.8.10 (2020/6/26)
    - fix bug https://github.com/AriaLyy/Aria/issues/703
    - fix bug https://github.com/AriaLyy/Aria/issues/702
    - fix bug https://github.com/AriaLyy/Aria/issues/695
  + v_3.8.9 (2020/6/14)
    - fix bug https://github.com/AriaLyy/Aria/issues/688
    - fix bug https://github.com/AriaLyy/Aria/issues/690
    - m3u8任务增加`setUseDefConvert()`方法，用于处理默认的m3u8任务
  + v_3.8.8 (2020/6/7)
    - 修复设置了cancel(false)，文件还是被删除的问题 https://github.com/AriaLyy/Aria/issues/686
    - 修复错误url的下载任务，无法删除的问题 https://github.com/AriaLyy/Aria/issues/684
  + v_3.8.7 (2020/5/25)
    - 修复组合任务单个子任务失败后，重新恢复组合任务，组合任务状态变为完成的问题
    - 修复40x错误，会继续重试并且无法重试成功的问题 https://github.com/AriaLyy/Aria/issues/619
    - 修复wait模式下，resume(true)无效问题
    - 修复now模式下的一些问题 https://github.com/AriaLyy/Aria/issues/620
    - 修复组任务，其中一个子任务在获取文件长度失败后，重新恢复组合任务，组合任务状态变为完成的问题 https://github.com/AriaLyy/Aria/issues/628
    - 修复组任务中，其中一个子任务是30x地址，导致调度器无法出现该子任务状态的问题
    - 增加组任务groupHash冲突检查 https://github.com/AriaLyy/Aria/issues/635
    - 修复task.cancel(false)还是把本地文件删除的问题 https://github.com/AriaLyy/Aria/issues/646
    - fix bug https://github.com/AriaLyy/Aria/issues/670
    - fix bug https://github.com/AriaLyy/Aria/issues/664
  + v_3.8.6 (2020/2/17)
    - fix bug https://github.com/AriaLyy/Aria/issues/608
    - fix bug https://github.com/AriaLyy/Aria/issues/579#issuecomment-586665035
    - fix bug https://github.com/AriaLyy/Aria/issues/610
    - fix bug https://github.com/AriaLyy/Aria/issues/614
    - 增加文件名适配器（感谢小伙伴[DaveBoy](https://github.com/DaveBoy)的PR）
    - 优化异常提示
  + v_3.8.5 (2020/1/18)
    - fix bug https://github.com/AriaLyy/Aria/issues/599
    - 增加密钥url转换器的参数 https://github.com/AriaLyy/Aria/issues/603
    - 增加sftp，文件上传、下载功能，[sftp下载](https://aria.laoyuyu.me/aria_doc/download/sftp_normal.html)，[sftp上传](https://aria.laoyuyu.me/aria_doc/upload/sftp_normal.html)
    - 使用零拷贝技术，优化了合并分块的效率
  + v_3.8.3 (2020/1/9)
    - fix bug https://github.com/AriaLyy/Aria/issues/573
    - android P适配 https://github.com/AriaLyy/Aria/issues/581
    - 添加ftp服务器标志 https://github.com/AriaLyy/Aria/issues/580
    - 重构loader模块，让loader模块的代码更加清晰，去除一些不必要的线程创建
    - 修复ftp上传完成后，删除服务器端的文件，无法重新下载的问题
    - 增加获取执行中的任务api，详情见：https://aria.laoyuyu.me/aria_doc/api/task_list.html
    - 增加获取剩余时间的api，详情见：https://aria.laoyuyu.me/aria_doc/start/task_explain.html
    - fix bug https://github.com/AriaLyy/Aria/issues/595
  + v_3.8.1 (2019/12/22)
    - 修复一个表创建失败的问题 https://github.com/AriaLyy/Aria/issues/570
    - 修复一个非分块模式下导致下载失败的问题 https://github.com/AriaLyy/Aria/issues/571
    - 修复一个服务器端无法创建socket连接，却没有返回码导致客户端卡住的问题 https://github.com/AriaLyy/Aria/issues/569
    - 修复文件删除后，组合任务没有重新下载的问题 https://github.com/AriaLyy/Aria/issues/574
    - 优化缓存队列和执行队列
  + v_3.8 (2019/12/17)
    - 移除androidx和support的依赖，现在无论是哪个版本的appcompat包都可以使用本框架
    - 修复一个在xml中使用fragment导致的内存泄漏问题
    - m3u8协议的key信息增加了`keyFormat`，`keyFormatVersion`字段
    - m3u8增加了`ignoreFailureTs`方法，忽略下载失败的ts切片
    - 修复在dialogFragment的`onCreateDialog()`注册导致的注解不生效问题
    - 修复组合任务初始化失败时，无法删除的问题
    - 修复`reStart()`后，无法停止的问题
    - ftp增加主动模式，开启主动模式：https://aria.laoyuyu.me/aria_doc/api/ftp_params.html
    - 修复ftp服务器无法响应`abor`命令导致的无法停止上传的问题 https://github.com/AriaLyy/Aria/issues/564
    - 修复ftp上传时，服务器有长度为0的文件导致上传失败的问题
    - 修复下载任务和上传任务的文件路径是同一个时，导致的记录混乱问题
    - 优化提示
  + v_3.7.10 (2019/12/3)
    - fix bug https://github.com/AriaLyy/Aria/issues/543#issuecomment-559733124
    - fix bug https://github.com/AriaLyy/Aria/issues/542
    - fix bug https://github.com/AriaLyy/Azria/issues/547
    - 修复下载失败时，中断重试无效的问题
    - 增加忽略权限检查的api，`ignoreCheckPermissions()`
    - 增加通用的的忽略文件路径被占用的api，`isIgnoreFilePathOccupy()`
  + v_3.7.9 (2019/11/28)
    - fix bug https://github.com/AriaLyy/Aria/issues/537
  + v_3.7.8 (2019/11/28)
    - fix bug https://github.com/AriaLyy/Aria/issues/526
    - fix bug https://github.com/AriaLyy/Aria/issues/533
    - fix bug https://github.com/AriaLyy/Aria/issues/535
    - 修复ftp无法完成下载的问题
    - 修复一个非分块模式下，调用`updateUrl(newUrl)`后无法恢复下载的问题
    - 增加立即恢复任务的接口，正常来说，当执行队列满时，调用恢复任务接口，只能将任务放到缓存队列中。如果希望调用恢复接口，马上进入执行队列，需要调用`resume(true)`这个重载方法。
    - 增加M3U8加密密钥的下载地址转换器 https://github.com/AriaLyy/Aria/issues/522
  + v_3.7.7 (2019/11/20)
    - 修复ftp无法完成下载的问题
    - 修复一个http下载崩溃的问题
  + v_3.7.6 (2019/11/19)
    - fix bug https://github.com/AriaLyy/Aria/issues/505
    - fix bug https://github.com/AriaLyy/Aria/issues/516
    - fix bug https://github.com/AriaLyy/Aria/issues/515
    - 增加强制上传的api`forceUpload()`
    - 修复for循环上传文件出现的问题
    - 移除创建任务的500ms间隔限制
    - 修复多线程读写时可能出现的`database is locked`的问题
  + v_3.7.5 (2019/11/10)
    - fix bug https://github.com/AriaLyy/Aria/issues/500
    - fix bug https://github.com/AriaLyy/Aria/issues/508
    - fix bug https://github.com/AriaLyy/Aria/issues/503
    - 修复m3u8创建索引不成功的问题
  + v_3.7.4 (2019/11/2)
    - 修复一个class被莫名改变的问题
    - 修复非分块模式下导致的一个下载失败问题
    - fix bug https://github.com/AriaLyy/Aria/issues/493
  + v_3.7.3 (2019/10/31)
    - fix bug https://github.com/AriaLyy/Aria/issues/495
    - fix bug https://github.com/AriaLyy/Aria/issues/496
  + v_3.7.2 (2019/10/28)
    - fix bug https://github.com/AriaLyy/Aria/issues/450
    - fix bug https://github.com/AriaLyy/Aria/issues/466
    - fix bug https://github.com/AriaLyy/Aria/issues/454
    - fix bug https://github.com/AriaLyy/Aria/issues/467
    - fix bug https://github.com/AriaLyy/Aria/issues/459
    - fix bug https://github.com/AriaLyy/Aria/issues/487
    - fix bug https://github.com/AriaLyy/Aria/issues/483
    - fix bug https://github.com/AriaLyy/Aria/issues/482
    - fix bug https://github.com/AriaLyy/Aria/issues/473
    - 移除隐藏api的反射 https://github.com/AriaLyy/Aria/issues/456
    - 新增ftp免证书登陆功能 https://github.com/AriaLyy/Aria/issues/455
    - 适配androidX
    - 修复组合任务，恢复下载，会出现进度显示为0的问题
    - m3u8点播下载新增创建ts索引功能
    - 修复多任务的m3u8点播下载时，一个任务调用`jumpIndex`，其它m3u8任务也会自动调用`jumpIndex`的问题
    - 添加权限检查
  + v_3.6.6 (2019/8/7)
    - fix bug https://github.com/AriaLyy/Aria/issues/426
    - fix bug https://github.com/AriaLyy/Aria/issues/429
    - fix bug https://github.com/AriaLyy/Aria/issues/428
    - fix bug https://github.com/AriaLyy/Aria/issues/427
    - fix bug https://github.com/AriaLyy/Aria/issues/431
    - fix bug https://github.com/AriaLyy/Aria/issues/441
    - 修复普通下载任务、组合任务共享执行队列、缓存池的问题
    - 修复组合任务启动失败时，`DownloadGroupEntity`的状态变为执行中的问题
  + v_3.6.5 (2019/7/17)
    - fix bug https://github.com/AriaLyy/Aria/issues/403
    - fix bug https://github.com/AriaLyy/Aria/issues/414
    - fix bug https://github.com/AriaLyy/Aria/issues/406
    - fix bug https://github.com/AriaLyy/Aria/issues/407
    - fix bug https://github.com/AriaLyy/Aria/issues/416
    - fix bug https://github.com/AriaLyy/Aria/issues/420
    - fix bug https://github.com/AriaLyy/Aria/issues/422
    - 新增ftp上传拦截器 https://github.com/AriaLyy/Aria/issues/402
    - 重构线程任务模块
    - 新增m3u8协议的文件下载
    - 修复拦截器可能出现的空指针问题
    - 移除`DownloadGroupEntity`字段`groupHash`的主键约束，`DownloadEntity`字段`groupHash`的外键约束，`TaskRecord`字段`dGroupHash`的外键约束
    - 优化关联查询的性能
    - 修复任务记录删除失败的问题
    - 优化网络连接状态获取的逻辑
    - 修复配置文件的某些配置失效的问题
    - 新增m3u8切片状态注解`@M3U8.onPeerStart`，`@M3U8.onPeerComplete`，`@M3U8.onPeerFail`
    - 新增动态指定m3u8协议视频的下载功能（边下边播下载支持）,[详情](https://aria.laoyuyu.me/aria_doc/download/m3u8_vod.html)
    - 优化`unknownSize`的处理，https://github.com/AriaLyy/Aria/issues/419，需要注意，如果组合任务只任务数过多，将需要更多时间才能进入下载流程
  + v_3.6.4 (2019/5/16)
    - 优化任务接收器的代码结构
    - 修复`DbEntity.saveAll()`失败的问题
    - 修复分块任务重命名失败的问题
    - fix bug https://github.com/AriaLyy/Aria/issues/379
    - 移除`getDownloadTask(String url)`、`getGroupTask(List<String> urls)`、`getFtpDirTask(String path)`
      等获取任务的api，如果你希望获取对应状态，请使用实体的状态判断，如：`getDownloadEntity()`、`getDownloadGroupEntity()`
      `getFtpDirEntity()`
    - fix bug https://github.com/AriaLyy/Aria/issues/388
    - 修复使用`Content-Disposition`的文件名时，第一次下载无法重命名文件的问题
    - 修复使用`Content-Disposition`的文件名时，多次重命名文件的问题
    - 组合任务新增`unknownSize()`，用于处理组合任务大小ø未知的情况，https://github.com/AriaLyy/Aria/issues/380
    - 优化`AbsThreadTask`代码
    - 新增文件长度处理功能 https://github.com/AriaLyy/Aria/issues/393
      ```java
      .setFileLenAdapter(new IHttpFileLenAdapter() {
        @Override public long handleFileLen(Map<String, List<String>> headers) {
          ...
          // 处理header中的文件长度

          return fileLen;
        }
       })
      ```
    - 修复组合任务多次回调`onStop`注解的问题
    - 优化`isRunning()`的逻辑，任务是否在执行的判断将更加准确
    - 修复多次重复快速点击`暂停、开始`时，任务有可能重复下载的问题
    - 修复组合任务中没有等待中的只任务实体保存失败的问题
    - 新增组合任务url重复检查 https://github.com/AriaLyy/Aria/issues/395
    - 初始化任务时，如果url、path有错误将会回调`@Download.onTaskFail`、`@Upload.onTaskFail`、`@DownGroup.onTaskFail`
  + v_3.6.3 (2019/4/2)
    - fix bug https://github.com/AriaLyy/Aria/issues/377
  + v_3.6.2 (2019/4/1)
    - fix bug https://github.com/AriaLyy/Aria/issues/368
    - 增加gradle 5.0支持
    - fix bug https://github.com/AriaLyy/Aria/issues/374
    - 增加分页功能，详情见：https://aria.laoyuyu.me/aria_doc/api/task_list.html#%E4%BB%BB%E5%8A%A1%E5%88%97%E8%A1%A8%E5%88%86%E9%A1%B5%EF%BC%88362%E4%BB%A5%E4%B8%8A%E7%89%88%E6%9C%AC%E6%94%AF%E6%8C%81%EF%BC%89
  + v_3.6.1 (2019/3/5)
    - fix bug https://github.com/AriaLyy/Aria/issues/367
  + v_3.6 (2019/2/27)
    - 优化数据库写入\修改的速度
    - 精减任务实体的存储
    - 增加下载组合任务的配置
    - useBroadcast\notNetRetry这两个配置，统一在AppConfig中配置
    - fix bug https://github.com/AriaLyy/Aria/issues/361
    - fix bug https://github.com/AriaLyy/Aria/issues/365
  + v_3.5.4 (2019/1/8)
    - 修复不支持断点的下载地址，重复下载出现的数据库主键冲突问题
  + v_3.5.3 (2018/12/23)
    - 修复ftps不能加载默认证书的bug https://github.com/AriaLyy/Aria/issues/334
    - 优化注解性能，移除不必要的判断代码
    - 增加广播支持，详情见:http://aria.laoyuyu.me/aria_doc/api/use_broadcast.html
    - 增加get参数支持
      ```java
      Aria.download(SingleTaskActivity.this)
              .load(DOWNLOAD_URL) // url 必须是主体url，也就是?前面的内容
              .setFilePath(path, true)
              .asGet()
              .setParams(params) // 设置参数
              .start();
      ```
      - fix bug https://github.com/AriaLyy/Aria/issues/335
      - 新增进度百分比保存 https://github.com/AriaLyy/Aria/issues/336
      - fix bug https://github.com/AriaLyy/Aria/issues/335
  + v_3.5.2
    - 添加Serializable接口支持 https://github.com/AriaLyy/Aria/issues/320
    - 失败回调增加错误原因 https://github.com/AriaLyy/Aria/issues/310
      ```
      @Download.onTaskFail void taskFail(DownloadTask task, Exception e) {
         e.getMessage();
        ...
      }
      ```
     - fix bug https://github.com/AriaLyy/Aria/issues/322
     - 新增201 重定向支持 https://github.com/AriaLyy/Aria/issues/318
     - 修复使用`useServerFileName(true)`中含有`"`导致的文件后缀名错误问题
     - 优化logcat日志提示
     - 修改下载线程的优先级为`Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);`
     - fix bug https://github.com/AriaLyy/Aria/issues/319
     - 修复分卡下载失败的问题 https://github.com/AriaLyy/Aria/issues/326
     - 初始化Aria时会将所有数据库状态为下载中的任务改为已停止，防止应用被kill后，任务状态错误
     - 初始化时自动判断文件是否被删除，文件被删除的任务将自动重置默认值
     - 修复刷新url后，文件无法删除的 bug
     - fix bug https://github.com/AriaLyy/Aria/issues/309
     - 优化配置文件的读取
  + v_3.5.1
    - 优化`taskExists`方法
    - 添加`post`参数请求支持
      ```java
      Aria.download(SingleTaskActivity.this)
              .load(DOWNLOAD_URL)
              .setFilePath(path)
              .asPost() // post请求
              .setParam("key", "value") //传递参数
              //.setParams(Map<String, String>) // 传递多参数
              .start();
      ```
     - 增加强制设置文件路径的api, https://github.com/AriaLyy/Aria/issues/311
       ```java
       Aria.download(SingleTaskActivity.this)
                     .load(DOWNLOAD_URL)
                     .setFilePath(path, true) // true表示忽略路径是否被占用
                     .start();
       ```
  + v_3.5
    - fix bug https://github.com/AriaLyy/Aria/issues/302
    - fix bug https://github.com/AriaLyy/Aria/issues/283
    - fix bug https://github.com/AriaLyy/Aria/issues/305
    - fix bug https://github.com/AriaLyy/Aria/issues/306
    - fix bug https://github.com/AriaLyy/Aria/issues/272  (现在，停止所有任务，未开始的任务状态将变为停止)
    - fix bug https://github.com/AriaLyy/Aria/issues/277
    - fix bug https://github.com/AriaLyy/Aria/issues/303
    - 优化停止任务的速度
    - 修复组合任务修改子任务文件名失败的问题
  + v_3.4.12
    - fix bug https://github.com/AriaLyy/Aria/issues/286
    - 优化线程池任务
  + v_3.4.11
    - fix bug https://github.com/AriaLyy/Aria/issues/288
    - fix bug https://github.com/AriaLyy/Aria/issues/282
  + v_3.4.10
    - fix bug https://github.com/AriaLyy/Aria/issues/280
  + v_3.4.9
    - fix bug https://github.com/AriaLyy/Aria/issues/276
  + v_3.4.8
    - 组合任务新增`updateUrls(List<String>)`用于修改组合子任务的url，[see](https://aria.laoyuyu.me/aria_doc/api/update_url.html)
    - 出于安全考虑，FTP数据库去掉密码的保存
    - 增加FTPS支持 [see](https://aria.laoyuyu.me/aria_doc/download/ftps.html)
    - 增加速度限制支持[see](https://aria.laoyuyu.me/aria_doc/api/speed_handle.html)
    - 增加内存空间不足验证
  + v_3.4.7
    - 修复分块任务异常操作导致的问题
  + v_3.4.6
    - 修复android 4.4.4 版本多dex下无法进行回调的问题
    - 新增`updateUrl(newUrl)`用于修改任务的url，[see](https://aria.laoyuyu.me/aria_doc/api/task_handle.html#%E6%9B%B4%E6%96%B0%E4%BB%BB%E5%8A%A1url)
    - 优化分块下载
    - 修复了字符串中有特殊字符导致的路径冲突问题；修复ftp分块下载失败问题
    - 修复连接中有`+`导致的地址呗使用问题。
    - 修复表重复创建导致的崩溃问题 https://github.com/AriaLyy/Aria/issues/264
  + v_3.4.4
    - 实现[多线程分块下载](https://aria.laoyuyu.me/aria_doc/start/config.html)
    - 修复`stopAll()`和`resumeAll()`导致的进度为0问题
    - 修复任务组添加header无效的问题
  + v_3.4.3
    - 修复在activity 的onStop中取消注册导致的内存泄露问题
    - fix bug https://github.com/AriaLyy/Aria/issues/258
    - fix bug https://github.com/AriaLyy/Aria/issues/259
  + v_3.4.2
    - fix bug https://github.com/AriaLyy/Aria/issues/248
    - fix bug https://github.com/AriaLyy/Aria/issues/247
    - fix bug https://github.com/AriaLyy/Aria/issues/250
    - 添加任务判断是否存在的api
    - 添加代理api
    - 修复删除所有没有进出等待的问题
    - 进度有时出错的问题
    - FTP添加超时处理
  + v_3.4.1
    - 移除记录配置文件，改用数据库记录任务记录
    - 上传配置添加io超时时间、缓存大小配置
    - 添加没有网络也会重试的开关
    - 修复多次删除记录的bug
    - 文件长度现在可动态增加，详情见 https://aria.laoyuyu.me/aria_doc/start/config.html
    - 修复多module同时引用Aria导致打正式包出错的问题 https://github.com/AriaLyy/Aria/issues/240
  + v_3.4
    - 优化大量代码
    - 重构Aria的ORM模型，提高了数据读取的可靠性和读写速度
    - 现在可在任意类中使用Aria了，[使用方法](http://aria.laoyuyu.me/aria_doc/start/any_java.html)
    - 添加`window.location.replace("http://xxxx")`类型的网页重定向支持
    - 支持gzip、deflate 压缩类型的输入流
    - 添加`useServerFileName`，可使用服务端响应header的`Content-Disposition`携带的文件名
  + v_3.3.16
    - 修复一个activity启动多次，无法进行回掉的bug https://github.com/AriaLyy/Aria/issues/200
    - 优化target代码结构，移除路径被占用的提示
    - 添加支持chunked模式的下载
    - 去掉上一个版本"//"的限制
  + v_3.3.14
    - 修复ftp上传和下载的兼容性问题
    - 如果url中的path有"//"将替换为"/"
    - 修复http上传成功后，如果服务器没有设置返回码导致上传失败的问题
    - 上传实体UploadEntity增加responseStr字段，http上传完成后，在被`@Upload.onComplete`注解的方法中，可通过`task.getEntity().getResponseStr())`获取服务器返回的数据
    - 如果服务器存在已上传了一部分的文件，用户执行删除该FTP上传任务，再次重新上传，会出现550，权限错误；本版本已修复该问题
  + v_3.3.13
    - 添加`@Download.onWait、@Upload.onWait、@DownloadGroup.onWait`三个新注解，队列已经满了，继续创建新任务，任务处于等待中，将会执行被这三个注解标志的方法
    - app被kill，但是还存在等待中的任务A；第二次重新启动，先创建一个新的任务B，Aria会自动把B放进等待队列中，这时再次创建任务A，会导致重复下载，进度错乱的问题；本版本已修复这个问题
  + v_3.3.11
    - 添加进度更新间隔api，在`aria_config.xml`配置`<updateInterval value="1000"/>`或在代码中调用
      `AriaManager.getInstance(AriaManager.APP).getDownloadConfig().setUpdateInterval(3000)`便可以改变进度刷新间隔
    - 修复下载过程中kill进程可能出现的文件错误的问题 https://github.com/AriaLyy/Aria/issues/192
    - 修复http上传的空指针问题 https://github.com/AriaLyy/Aria/issues/193
    - 修复下载地址中含有`'`导致的崩溃问题 https://github.com/AriaLyy/Aria/issues/194
  + v_3.3.10
    - 修复地址切换导致下载失败的问题 https://github.com/AriaLyy/Aria/issues/181
    - 添加重置状态的api，当下载信息不改变，只是替换了服务器的对应的文件，可用`Aria.download(this).load(url).resetState()`重置下载状态 https://github.com/AriaLyy/Aria/issues/182
  + v_3.3.9
    - 添加POST支持
    - 任务执行的过程中，如果调用removeRecord()方法，将会取消任务 https://github.com/AriaLyy/Aria/issues/174
    - 修复一个数据库初始化的问题 https://github.com/AriaLyy/Aria/issues/173
    - 修复head头部信息过长时出现的崩溃问题 https://github.com/AriaLyy/Aria/issues/177
  + v_3.3.7
    - 修复一个线程重启的问题 https://github.com/AriaLyy/Aria/issues/160
    - 修复配置文件异常问题、格式化速度为0问题 https://github.com/AriaLyy/Aria/issues/161
  + v_3.3.6
    - 增加日志输出级别控制
    - 修复公网FTP地址不能下载的问题  https://github.com/AriaLyy/Aria/issues/146
    - 修复http下载地址有空格的时候下载失败的问题 https://github.com/AriaLyy/Aria/issues/131
    - 修复Activity在`onDestroy()`中调用`Aria.download(this).unRegister();`导致回调失效的问题
    - 修复Adapter下载FTP任务问题、任务调度问题 https://github.com/AriaLyy/Aria/issues/157
    - 优化代码，优化了IO性能
  + v_3.3.5 修复任务组、上传任务无法启动的bug
  + v_3.3.4 优化任务代码结构，修复上一个版本暂停后无法自动执行任务的问题
  + v_3.3.3 修复进度条错乱的问题，修复同一时间多次调用start导致重复下载的问题
  + v_3.3.2 新加reTry()，修复上一个版本不会回调失败事件的问题；增加running状态下5秒钟保存一次数据库的功能；修复FTP断点上传文件不完整的问题
  + v_3.3.1 增加网络事件，网络未连接，将不会重试下载，修复删除未开始任务，状态回调错误
  + v_3.3.0 增加任务组子任务暂停和开始控制功能、修复5.0系统以上数据库多生成两个字段的bug、去掉addSchedulerListener事件
  + v_3.2.26 修复任务组有时注解不起作用的问题
  + v_3.2.25 修复删除任务组文件，记录无法删除的问题
  + v_3.2.17 修复一个版本兼容性问题，线程中使用Aria出错问题
  + v_3.2.15 修复大型文件分段下载失败的问题，修复中文URL乱码问题
  + v_3.2.14 修复恢复所有任务的api接口，不能恢复下载组任务的问题
  + v_3.2.13 修复某些服务器头文件返回描述文件格式错误的问题、修复有时删除任务，需要两次删除的问题
  + v_3.2.12 实现FTP多线程断点续传下载，FTP断点续传上传功能
  + v_3.2.9 修复任务组下载完成两次回掉的问题，修复又是获取不到下载状态的问题
  + v_3.2.8 修复下载超过2G大小的文件失败的问题
  + v_3.2.7 移除设置文件名的api接口，修复断开网络时出现的进度条错误的问题
  + v_3.2.6 移除广播事件，增加任务组下载功能
  + v_3.1.9 修复stopAll队列没有任务时崩溃的问题，增加针对单个任务监听的功能
  + v_3.1.7 修复某些文件下载不了的bug，增加apt注解方法，事件获取更加简单了
  + v_3.1.6 取消任务时onTaskCancel回调两次的bug
  + v_3.1.5 优化代码结构，增加优先下载任务功能。
  + v_3.1.4 修复快速切换，暂停、恢复功能时，概率性出现的重新下载问题，添加onPre()回调，onPre()用于请求地址之前执行界面UI更新操作。
  + v_3.1.0 添加Aria配置文件，优化代码
  + v_3.0.3 修复暂停后删除任务，闪退问题，添加删除记录的api
  + v_3.0.2 支持30x重定向链接下载
  + v_3.0.0 添加上传任务支持，修复一些已发现的bug
  + v_2.4.4 修复不支持断点的下载链接拿不到文件大小的问题
  + v_2.4.3 修复404链接卡顿的问题
  + v_2.4.2 修复失败重试无效的bug
  + v_2.4.1 修复下载慢的问题，修复application、service 不能使用的问题
  + v_2.4.0 支持https链接下载
  + v_2.3.8 修复数据错乱的bug、添加fragment支持
  + v_2.3.6 添加dialog、popupWindow支持
  + v_2.3.3 添加断点支持、修改下载逻辑，让使用更加简单、修复一个内存泄露的bug
  + v_2.3.1 重命名为Aria，下载流程简化
  + v_2.1.1 增加，选择最大下载任务数接口