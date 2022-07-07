package com.bear.processbus.eventbus

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
import com.bear.processbus.MainService
import com.bear.processbus.Util

object Bus {

    private val TAG = "ProcessBus"
    private var isInit = false
    private var eventSS: IEventBus? = null
    private var context: Context? = null
    private val tempNotPost = ArrayList<() -> Unit>() //发送事件时，service可能还未连接，暂时存在eventCallBack里面
    private val tempNotRegister =
        HashMap<String, () -> Unit>() //注册监听时，service可能还未连接，暂时存在regisetCallBack里面

    private val registedCmd = HashMap<String, HashSet<String>>() // 注册的事件和key，cmd->observerKey
    private val registedBlock = HashMap<String, (Event) -> Unit>()//注册的key和监听方法，observerKey->block

    private val INIT = 0 //初始状态
    private val CONNECTED = 1 //连接状态
    private val DISCONNECTED = 2 //断开连接状态
    private var status = INIT

    private val handler: Util.ProcessHandler by lazy {
        Util.getHandler(TAG)!!
    }

    fun init(context: Context) {
        if (!isInit) {
            innerInit(context)
        }
    }

    private fun innerInit(context: Context? = null) {
        if (!isInit) {
            synchronized(Bus::class.java) {
                if (!isInit) {
                    if (context != null) {
                        Bus.context = context
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
            Log.i(
                TAG,
                "post event , cmd:${event.cmd} , content:${event.content} , fromProcess:${event.fromProcess}"
            )
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
        val observerKey = Util.getObserverKey(context!!, cmd)
        handler.post {
            if (registedCmd.containsKey(cmd)) {
                registedCmd[cmd]?.add(observerKey)
            } else {
                val set = HashSet<String>()
                set.add(observerKey)
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
            registedBlock[observerKey] = block
            Log.i(TAG, "register key:{$observerKey} , cmd:${cmd}")
        }
        return Releasable(observerKey)
    }

    //反注册
    fun unRegister(observerKey: String) {
        if (observerKey.isEmpty()) {
            return
        }
        Log.i(TAG, "unregister key:${observerKey}")
        innerInit()
        handler.post {
            registedBlock.remove(observerKey)
            registedCmd.forEach {
                it.value.remove(observerKey)
                if (it.value.isEmpty()) {
                    val cmd = it.key
                    val processKey = Util.getProcessKey(context)
                    eventSS?.unRegister(processKey, cmd)
                    registedCmd.remove(cmd)
                }
            }
        }
    }

    private fun bindService() {
        if (status == CONNECTED) {
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
    private val bindCallBack = object : ICallBack.Stub() {
        override fun onReceived(event: Event?) {
            handler.post {
                Log.i(TAG, "received event, cmd:${event?.cmd} , content:${event?.content} , fromProcess:${event?.fromProcess}")
                if (event != null && !event.cmd.isNullOrEmpty()) {
                    registedCmd[event?.cmd]?.forEach { observerKey ->
                        val block = registedBlock[observerKey]
                        if (block != null) {
                            try {
                                block(event)
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                Log.e(TAG, "block run error , observerkey: ${observerKey} , error:${ex}")
                            }
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

        fun autoRelease(lifecycle: Lifecycle) {
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        Log.i(TAG, "release ${key} , lifecycle onDeStroy")
                        unRegister(key)
                    }
                }
            })
        }
    }

}