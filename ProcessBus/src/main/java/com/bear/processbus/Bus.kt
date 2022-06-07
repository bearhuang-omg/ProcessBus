package com.bear.processbus

import android.app.Application
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentHashMap

object Bus {

    private val TAG = "ProcessBus"
    private var isInit = false
    private var isConnected = false
    private var eventSS: IEventBus? = null
    private var context: Context? = null
    private val callbackMap = ConcurrentHashMap<String, ArrayList<() -> Unit>>()

    public fun init(context: Context) {
        if (!isInit) {
            this.context = context.applicationContext
            this.isInit = true
        }
    }

    public fun post(event: Event) {
        if (event == null || event.cmd.isEmpty()) {
            return
        }
        if (eventSS != null) {
            eventSS!!.post(event)
        } else {
            addCallBack(event.cmd) {
                eventSS!!.post(event)
            }
            bindService()
        }
    }

    //注册
    public fun register(cmd: String, block: (Event) -> Unit): Releasable? {
        if (cmd.isEmpty() || block == null) {
            return null
        }
        if (eventSS != null) {
            realRegister(cmd, block)
        } else {
            addCallBack(cmd) {
                realRegister(cmd, block)
            }
            bindService()
        }
        return Releasable(cmd)
    }

    private fun realRegister(cmd: String, block: (Event) -> Unit) {
        Log.i(TAG, "register cmd:" + cmd)
        eventSS?.register(cmd, object : ICallBack.Stub() {
            override fun onReceived(code: Int, event: Event?) {
                if (event != null) {
                    block(event)
                }
            }
        })
    }

    //反注册
    public fun unRegister(cmd: String) {
        if (cmd.isEmpty()) {
            return
        }
        Log.i(TAG, "unregister cmd:" + cmd)
        removeCallBack(cmd)
        eventSS?.unRegister(cmd)
    }

    private fun bindService() {
        if (eventSS != null) {
            return
        }
        if (context == null) {
            getContext()
        }
        if (context == null) {
            return
        }
        val intent = Intent(context, MainService::class.java)
        context?.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)
    }

    //监听服务
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.i(TAG, "service connected")
            isConnected = true
            eventSS = IEventBus.Stub.asInterface(p1)
            runCallBack()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(TAG, "service disConnected")
            isConnected = false
            eventSS = null
        }
    }

    private fun addCallBack(cmd: String, block: () -> Unit) {
        if (callbackMap.contains(cmd)) {
            callbackMap[cmd]?.add(block)
        } else {
            val list = ArrayList<() -> Unit>()
            list.add(block)
            callbackMap[cmd] = list
        }
    }

    private fun removeCallBack(cmd: String) {
        callbackMap.remove(cmd)
    }

    private fun runCallBack() {
        if (!callbackMap.isEmpty()) {
            callbackMap.forEach {
                for (block in it.value) {
                    block()
                }
            }
            callbackMap.clear()
        }
    }

    //反射获取Application
    private fun getContext() {
        try {
            if (context == null) {
                context =
                    Class.forName("android.app.ActivityThread").getMethod("currentApplication")
                        .invoke(null) as Application
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    //自动释放
    class Releasable(val cmd: String) {

        public fun autoRelease(lifecycle: Lifecycle) {
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        Log.i(TAG, "release cmd:" + cmd)
                        unRegister(cmd)
                    }
                }
            })
        }
    }
}