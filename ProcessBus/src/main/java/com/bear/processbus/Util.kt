package com.bear.processbus

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object Util {

    val DEFAULT_PROCESS_NAME = "null"
    private var currentProcessName = ""

    fun getProcessName(context: Context): String {
        if (!currentProcessName.isEmpty()) {
            return currentProcessName
        }
        if (context == null) {
            return DEFAULT_PROCESS_NAME
        }
        val pid = Process.myPid()
        try {
            var app = context!!.applicationContext
            if (app == null) {
                app = context
            }
            val mActivityManager = app
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (mActivityManager != null) {
                for (appProcess in mActivityManager
                    .runningAppProcesses) {
                    if (appProcess.pid == pid) {
                        currentProcessName =
                            if (appProcess.processName == null || appProcess.processName.isEmpty()) {
                                app.applicationInfo.packageName
                            } else {
                                appProcess.processName
                            }
                        break
                    }
                }
            }
            if (!currentProcessName.isNullOrEmpty()) {
                return currentProcessName
            }
        } catch (e: Throwable) {
        }
        return DEFAULT_PROCESS_NAME
    }

    private var PROCESS_KEY = ""
    fun getProcessKey(context: Context?): String {
        if (PROCESS_KEY.isNotEmpty()) {
            return PROCESS_KEY
        }
        if (context == null) {
            PROCESS_KEY = getRandomString(16)
            return PROCESS_KEY
        }
        var processKey = getProcessName(context)
        if (processKey.equals(DEFAULT_PROCESS_NAME)) {
            processKey = getRandomString(16)
        }
        PROCESS_KEY = processKey
        return processKey
    }

    fun getRandomString(length: Int): String {
        val str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val sb = StringBuilder()
        for (i in 0 until length) {
            val number = random.nextInt(62)
            sb.append(str[number])
        }
        return sb.toString()
    }

    class ProcessHandler(tag: String) {
        private val handlerThread = HandlerThread(tag)
        private val handler by lazy<Handler> {
            handlerThread.start()
            Handler(handlerThread.looper)
        }

        fun post(block: () -> Unit) {
            handler.post {
                try {
                    block()
                } catch (ex: Exception) {
                    Log.e("Util", ex.toString())
                }
            }
        }

        fun quitSafely() {
            handlerThread.quitSafely()
        }
    }

    fun getHandler(tag: String): ProcessHandler? {
        if (tag.isNullOrEmpty()) {
            return null
        }
        return ProcessHandler(tag)
    }

    fun md5(text: String): String {
        try {
            //获取md5加密对象
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            //对字符串加密，返回字节数组
            val digest: ByteArray = instance.digest(text.toByteArray())
            var sb = StringBuffer()
            for (b in digest) {
                var i: Int = b.toInt() and 0xff
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    hexString = "0" + hexString
                }
                sb.append(hexString)
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    var count:AtomicInteger = AtomicInteger(0)

    //获取每个监听者的key
    fun getObserverKey(context: Context, cmd: String): String {
        val current = count.getAndIncrement()
        return getProcessKey(context) + current + "_" + cmd
    }
}