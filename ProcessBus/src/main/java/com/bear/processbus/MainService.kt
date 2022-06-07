package com.bear.processbus

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log

class MainService : Service() {

    private val eventManager = HashMap<String, ArrayList<ICallBack>>()
    private val TAG = "MainService"
    private val handlerThread = HandlerThread(TAG)
    private val handler: Handler by lazy {
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    private val mBinder = object : IEventBus.Stub() {
        override fun register(cmd: String?, callback: ICallBack?) {
            if (!cmd.isNullOrEmpty() && callback != null) {
                handler.post {
                    if (eventManager.contains(cmd)) {
                        eventManager[cmd]?.add(callback)
                    } else {
                        val list = ArrayList<ICallBack>()
                        list.add(callback)
                        eventManager[cmd] = list
                    }
                }
            }
        }

        override fun unRegister(cmd: String?) {
            if (!cmd.isNullOrEmpty()) {
                eventManager.remove(cmd)
            }
        }

        override fun post(event: Event?) {
            if (event != null && !event.cmd.isNullOrEmpty()) {
                handler.post {
                    if (eventManager.contains(event.cmd) && eventManager[event.cmd] != null) {
                        for (callback in eventManager[event.cmd]!!) {
                            callback.onReceived(event)
                        }
                    }
                }
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "main service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "main service destroyed")
        handlerThread.quitSafely()
    }
}