package com.bear.processbus

import android.app.Application
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

object Bus {

    private val TAG = "ProcessBus"
    private var isInit = false
    private var eventSS: IEventBus? = null
    private var context: Context? = null

    public fun init(context: Context) {
        if (!isInit) {
            this.context = context.applicationContext
            this.isInit = true
        }
    }

    public fun post(event: Event) {
        if (event == null) {
            return
        }
        bindService()
        eventSS?.post(event)
    }

    public fun register(cmd: String, listener: BusListener) {
        if (cmd == null || listener == null) {
            return
        }
        bindService()
        eventSS?.register(cmd, object : ICallBack.Stub() {
            override fun onReceived(code: Int, event: Event?) {
                if (event != null) {
                    listener.onEvent(event)
                }
            }
        })
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
            eventSS = IEventBus.Stub.asInterface(p1)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(TAG, "service disConnected")
            eventSS = null
        }

    }

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
}