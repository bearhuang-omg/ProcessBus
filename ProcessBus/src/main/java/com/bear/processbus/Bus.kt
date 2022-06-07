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

object Bus {

    private val TAG = "ProcessBus"
    private var isInit = false
    private var isConnected = false
    private var eventSS: IEventBus? = null
    private var context: Context? = null
    private val callbackMap = HashMap<String, ArrayList<() -> Unit>>()
    private val handler: Util.ProcessHandler by lazy {
        Util.getHandler(TAG)!!
    }

    public fun init(context: Context) {
        if (!isInit) {
            innerInit(context)
        }
    }

    private fun innerInit(context: Context? = null) {
        if (!isInit) {
            synchronized(Bus::class.java) {
                if (!isInit) {
                    if (context != null) {
                        this.context = context
                    } else {
                        getContext()
                    }
                    isInit = true
                }
            }
        }
    }

    public fun post(event: Event) {
        if (event == null || event.cmd.isEmpty()) {
            return
        }
        innerInit()
        handler.post {
            if (context == null) {
                event.fromProcess = Util.DEFAULT_PROCESS_NAME
            } else {
                event.fromProcess = Util.getProcessName(context!!)
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
    }

    //注册
    public fun register(cmd: String, block: (Event) -> Unit): Releasable? {
        if (cmd.isEmpty() || block == null) {
            return null
        }
        innerInit()
        if (context == null) {
            return null
        }
        val key = Util.getKey(context!!, cmd)
        handler.post {
            if (eventSS != null) {
                realRegister(cmd, key, block)
            } else {
                addCallBack(key) {
                    realRegister(cmd, key, block)
                }
                bindService()
            }
        }
        return Releasable(key)
    }

    private fun realRegister(cmd: String, key: String, block: (Event) -> Unit) {
        Log.i(TAG, "register cmd:" + cmd)
        eventSS?.register(cmd, key, object : ICallBack.Stub() {
            override fun onReceived(event: Event?) {
                if (event != null) {
                    block(event)
                }
            }
        })
    }

    //反注册
    public fun unRegister(key: String) {
        if (key.isEmpty()) {
            return
        }
        Log.i(TAG, "unregister key:" + key)
        innerInit()
        handler.post {
            removeCallBack(key)
            eventSS?.unRegister(key)
        }
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
    class Releasable(val key: String) {

        public fun autoRelease(lifecycle: Lifecycle) {
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        Log.i(TAG, "release ${key},event onDeStroy")
                        unRegister(key)
                    }
                }
            })
        }
    }

}