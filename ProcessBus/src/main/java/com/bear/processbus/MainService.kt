package com.bear.processbus

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MainService : Service() {

    private val eventManager = HashMap<String, ArrayList<MainCallBack>>()
    private val TAG = "MainService"
    private val handler: Util.ProcessHandler by lazy {
        Util.getHandler(TAG)!!
    }

    private val mBinder = object : IEventBus.Stub() {

        override fun register(cmd: String?, key: String?, callback: ICallBack?) {
            if (!cmd.isNullOrEmpty() && callback != null && !key.isNullOrEmpty()) {
                Log.i(TAG, "register $cmd")
                handler.post {
                    if (eventManager.contains(cmd)) {
                        eventManager[cmd]?.add(MainCallBack(key,callback))
                    } else {
                        val list = ArrayList<MainCallBack>()
                        list.add(MainCallBack(key,callback))
                        eventManager[cmd] = list
                    }
                }
            }
        }

        override fun unRegister(key: String?) {
            Log.i(TAG, "unregister $key")
            handler.post {
                if (!key.isNullOrEmpty()) {
                    eventManager.forEach { entry ->
                        entry.value.forEach { mainCallback ->
                            if (mainCallback.key.equals(key)) {
                                entry.value.remove(mainCallback)
                            }
                        }
                    }
                }
            }
        }

        override fun post(event: Event?) {
            if (event != null && !event.cmd.isNullOrEmpty()) {
                handler.post {
                    if (eventManager.contains(event.cmd) && eventManager[event.cmd] != null) {
                        for (mainCallBack in eventManager[event.cmd]!!) {
                            try {
                                mainCallBack.callback.onReceived(event)
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                eventManager[event.cmd]?.remove(mainCallBack)
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

    class MainCallBack(val key:String,val callback:ICallBack)
}