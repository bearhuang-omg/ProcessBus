package com.bear.processbus

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MainService : Service() {
    private val TAG = "MainService"
    private val handler: Util.ProcessHandler by lazy {
        Util.getHandler(TAG)!!
    }

    private val callbackMap = HashMap<String, ICallBack>() //每个进程的callback回调
    private val cmdMap = HashMap<String, HashSet<String>>()//每个命令对应的进程

    private val mBinder = object : IEventBus.Stub() {

        override fun bind(key: String?, callback: ICallBack?) {
            Log.i(TAG, "bind key:${key}")
            handler.post {
                if (!key.isNullOrEmpty() && callback != null) {
                    callbackMap[key] = callback
                }
            }
        }

        override fun register(key: String?, cmd: String?) {
            Log.i(TAG, "register key:${key} , cmd:${cmd}")
            handler.post {
                if (!key.isNullOrEmpty() && !cmd.isNullOrEmpty()) {
                    if (cmdMap.containsKey(cmd)) {
                        cmdMap[cmd]?.add(key)
                    } else {
                        val list = HashSet<String>()
                        list.add(key)
                        cmdMap[cmd] = list
                    }
                }
            }
        }

        override fun unRegister(key: String?, cmd: String?) {
            Log.i(TAG, "unregister cmd:${cmd} , key:${key}")
            handler.post {
                if (!key.isNullOrEmpty() && !cmd.isNullOrEmpty()) {
                    if (cmdMap.containsKey(cmd)) {
                        cmdMap[cmd]?.remove(key)
                    }
                }
            }
        }

        override fun post(event: Event?) {
            handler.post {
                if (event != null && !event.cmd.isNullOrEmpty()) {
                    val processSet = cmdMap[event.cmd]
                    processSet?.forEach { processKey ->
                        try {
                            callbackMap[processKey]?.onReceived(event)
                        } catch (ex: Exception) {
                            Log.e(TAG, "post error:${ex}")
                            Log.i(TAG, "remove processKey:${processKey}")
                            callbackMap.remove(processKey)
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
        callbackMap.clear()
        cmdMap.clear()
        handler.quitSafely()
    }
}