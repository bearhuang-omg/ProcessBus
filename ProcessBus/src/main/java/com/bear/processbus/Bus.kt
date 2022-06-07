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
    private val callbackList = ArrayList<ConnectCallBack>()

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
            callbackList.add(object : ConnectCallBack {
                override fun onConnected() {
                    eventSS!!.post(event)
                }
            })
            bindService()
        }
    }

    public fun register(cmd: String, listener: BusListener): Releasable? {
        if (cmd.isEmpty() || listener == null) {
            return null
        }
        if (eventSS != null) {
            eventSS?.register(cmd, object : ICallBack.Stub() {
                override fun onReceived(code: Int, event: Event?) {
                    if (event != null) {
                        listener.onEvent(event)
                    }
                }
            })
        } else {
            callbackList.add(object :ConnectCallBack{
                override fun onConnected() {
                    eventSS?.register(cmd, object : ICallBack.Stub() {
                        override fun onReceived(code: Int, event: Event?) {
                            if (event != null) {
                                listener.onEvent(event)
                            }
                        }
                    })
                }
            })
            bindService()
        }
        return Releasable(cmd)
    }

    public fun unRegister(cmd: String) {
        if (cmd == null) {
            return
        }
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
        val intent = Intent(context, com.bear.processbus.MainService::class.java)
        context?.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.i(TAG, "service connected")
            isConnected = true
            eventSS = IEventBus.Stub.asInterface(p1)
            if (callbackList.size > 0){
                val iterator = callbackList.iterator()
                while (iterator.hasNext()){
                    iterator.next().onConnected()
                }
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(TAG, "service disConnected")
            isConnected = false
            eventSS = null
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

    //连接上之后的回调
    interface ConnectCallBack {
        fun onConnected()
    }

    //自动释放
    class Releasable(val cmd: String) {

        public fun autoRelease(lifecycle: Lifecycle) {
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        Log.i(TAG,"release cmd:"+cmd)
                        Bus.unRegister(cmd)
                    }
                }
            })
        }
    }
}