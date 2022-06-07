package com.bear.processbus

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
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
            if (mActivityManager == null) {
                currentProcessName = app.applicationInfo.packageName
                return currentProcessName
            }
            val infoList = mActivityManager.runningAppProcesses
            if (infoList == null) {
                currentProcessName = app.applicationInfo.packageName
                return currentProcessName
            }
            for (appProcess in mActivityManager
                .runningAppProcesses) {
                if (appProcess.pid == pid) {
                    currentProcessName =
                        if (appProcess.processName == null || appProcess.processName.isEmpty()) {
                            app.applicationInfo.packageName
                        } else {
                            appProcess.processName
                        }
                    return currentProcessName
                }
            }
            currentProcessName = app.applicationInfo.packageName
            return currentProcessName
        } catch (e: Throwable) {
        }
        return DEFAULT_PROCESS_NAME
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

    fun getKey(context: Context, cmd: String): String {
        val current = count.getAndIncrement()
//        return md5(getProcessName(context) + current) + "_" + cmd
        return getProcessName(context) + current + "_" + cmd
    }
}