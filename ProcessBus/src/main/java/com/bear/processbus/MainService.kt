package com.bear.processbus

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MainService : Service() {

    private val eventManager = HashMap<String, ArrayList<ICallBack>>()
    private val TAG = "MainService"
    private val handler: Util.ProcessHandler by lazy {
        Util.getHandler(TAG)!!
    }

    private val mBinder = object : IEventBus.Stub() {
        override fun register(cmd: String?, callback: ICallBack?) {
            if (!cmd.isNullOrEmpty() && callback != null) {
                Log.i(TAG, "register $cmd")
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
            Log.i(TAG, "unregister $cmd")
            if (!cmd.isNullOrEmpty()) {
                eventManager.remove(cmd)
            }
        }

        override fun post(event: Event?) {
            if (event != null && !event.cmd.isNullOrEmpty()) {
                handler.post {
                    if (eventManager.contains(event.cmd) && eventManager[event.cmd] != null) {
                        for (callback in eventManager[event.cmd]!!) {
                            try {
                                callback.onReceived(event)
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                eventManager[event.cmd]?.remove(callback)
                                if (eventManager[event.cmd] != null && eventManager[event.cmd]!!.isEmpty()) {
                                    eventManager.remove(event.cmd)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(TAG, "onBind")
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        Log.i(TAG, "onUnbind")
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.i(TAG, "onRebind")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "main service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "main service destroyed")
        eventManager.clear()
        handler.quitSafely()
    }
}