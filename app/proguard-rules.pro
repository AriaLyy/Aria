# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

################################### 混淆配置 start ###########################################
#指定代码的压缩级别
-optimizationpasses 1
#包明不混合大小写
-dontusemixedcaseclassnames
#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses
#优化  不优化输入的类文件
-dontoptimize
#预校验
-dontpreverify
#混淆时是否记录日志
-verbose
# 混淆时所采用的算法
#-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizations !code/simplification/cast,!field/*,!class/merging/*
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#忽略警告
#-ignorewarning
################################### 混淆配置 end ############################################


################## 记录生成的日志数据,gradle build时在本项目根目录输出         #################
####### 输出文件夹 build/outputs/mapping
#apk 包内所有 class 的内部结构
-dump build/outputs/mapping/class_files.txt
#未混淆的类和成员
-printseeds build/outputs/mapping/kpa_seeds.txt
#列出从 apk 中删除的代码
-printusage build/outputs/mapping/kpa_unused.txt
#混淆前后的映射
-printmapping build/outputs/mapping/kpa_mapping.txt
################## 记录生成的日志数据，gradle build时 在本项目根目录输出-end    #################

################## 常用属性配置-start  ##################
# 保护注解
-keepattributes *Annotation*
# 保护support v4 包
-dontwarn android.support.v4.app.**
-keep class android.support.v4.app.**{ *; }
# 保护andorid x
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**
# 保护一些奇葩的问题
-dontwarn org.xmlpull.v1.XmlPullParser
-dontwarn org.xmlpull.v1.XmlSerializer
-keep class org.xmlpull.v1.* {*;}

# 保护JS接口
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
##保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
##保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
##保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable
#
#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#避免混淆泛型 如果混淆报错建议关掉
#–keepattributes Signature
# webview + js
-keepattributes *JavascriptInterface*

################## 常用属性配置-end  ##################

################## kotlin-start  ##################
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
################## kotlin-end  ##################

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
-keep class com.arialyy.aria.ftp.download.FtpDLoaderUtil{*;}
-adaptresourcefilenames **.IUtil
-adaptresourcefilecontents **.IUtil