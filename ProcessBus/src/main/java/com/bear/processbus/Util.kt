package com.bear.processbus

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
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

        try {
            val pid = Process.myPid()
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

    var count:AtomicInteger = AtomicInteger(0)

    //获取每个监听者的key
    fun getObserverKey(context: Context, cmd: String): String {
        val current = count.getAndIncrement()
        return getProcessKey(context) + current + "_" + cmd
    }

    fun getContext(): Context? {
        try {
            return Class.forName("android.app.ActivityThread").getMethod("currentApplication")
                .invoke(null) as Application
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}