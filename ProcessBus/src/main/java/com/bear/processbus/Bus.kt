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
    private var eventSS: IEventBus? = null
    private var context: Context? = null
    private val tempNotPost = ArrayList<() -> Unit>() //发送事件时，service可能还未连接，暂时存在eventCallBack里面
    private val tempNotRegister = HashMap<String, () -> Unit>() //注册监听时，service可能还未连接，暂时存在regisetCallBack里面

    private val registedCmd = HashMap<String, HashSet<String>>() // 注册的事件和key，cmd->key
    private val registedBlock = HashMap<String, (Event) -> Unit>()//注册的key和监听方法，key->block

    private val INIT = 0 //初始状态
    private val CONNECTED = 1 //连接状态
    private val DISCONNECTED = 2 //断开连接状态
    private var status = INIT

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
                tempNotPost.add {
                    eventSS!!.post(event)
                }
                bindService()
            }
            Log.i(TAG,"post event , cmd:${event.cmd},content:${event.content},fromProcess:${event.fromProcess}")
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
        val key = Util.getObserverKey(context!!, cmd)
        handler.post {
            if (registedCmd.containsKey(cmd)) {
                registedCmd[cmd]?.add(key)
            } else {
                val set = HashSet<String>()
                set.add(key)
                registedCmd[cmd] = set
                //registedCmd不存在才需要去service里面注册，否则只需要添加回调就可以了
                if (eventSS != null) {
                    eventSS?.register(Util.getProcessKey(context), cmd)
                } else {
                    tempNotRegister.put(cmd) {
                        eventSS?.register(Util.getProcessKey(context), cmd)
                    }
                    bindService()
                }
            }
            registedBlock[key] = block
            Log.i(TAG, "register key:{$key},cmd:${cmd}")
        }
        return Releasable(key)
    }

    //反注册
    public fun unRegister(key: String) {
        if (key.isEmpty()) {
            return
        }
        Log.i(TAG, "unregister key:" + key)
        innerInit()
        handler.post {
            registedBlock.remove(key)
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

    //进程的回调
    val bindCallBack = object : ICallBack.Stub() {
        override fun onReceived(event: Event?) {
            handler.post {
                Log.i(TAG, "received event, cmd:${event?.cmd},content:${event?.content},fromProcess:${event?.fromProcess}")
                if (event != null && !event.cmd.isNullOrEmpty()) {
                    registedCmd[event?.cmd]?.forEach { processKey ->
                        val block = registedBlock[processKey]
                        if (block != null) {
                            block(event)
                        }
                    }
                }
            }
        }
    }

    //监听服务
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.i(TAG, "service connected")
            eventSS = IEventBus.Stub.asInterface(p1)
            eventSS?.bind(Util.getProcessKey(context), bindCallBack)
            runCallBack()
            status = CONNECTED
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(TAG, "service disConnected")
            status = DISCONNECTED
            eventSS = null
        }
    }

    private fun runCallBack() {
        if (tempNotRegister.size > 0) {
            tempNotRegister.forEach {
                it.value()
            }
            tempNotRegister.clear()
        }
        if (tempNotPost.size > 0) {
            tempNotPost.forEach {
                it()
            }
            tempNotPost.clear()
        }
        //如果是断开重连的状态，则需要重新注册一下
        if (status == DISCONNECTED) {
            registedCmd.forEach {
                val cmd = it.key
                val processKey = Util.getProcessKey(context)
                eventSS?.register(processKey, cmd)
            }
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