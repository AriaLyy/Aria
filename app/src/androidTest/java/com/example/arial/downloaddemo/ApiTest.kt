package com.example.arial.downloaddemo

import androidx.test.runner.AndroidJUnit4
import com.arialyy.aria.util.CommonUtil
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.math.pow

@RunWith(AndroidJUnit4::class)
class ApiTest {

  @Test
  fun testAddChar(){
    var str = "\\\\+道+歉+信\u0026感 谢 信"
    str = str.replace("\\+".toRegex(), "%2B")
    println("========")
    println(str)
    println(URLEncoder.encode(str))
    println(URLDecoder.decode(str))
  }

  @Test
  fun testSpeed() {
    val speed = 1024.0.pow(4.0) * 2
    println("speed = ${CommonUtil.formatFileSize(speed)}")

  }
}