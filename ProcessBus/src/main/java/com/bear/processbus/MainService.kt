package com.bear.processbus

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.bear.processbus.eventbus.Event
import com.bear.processbus.eventbus.ICallBack
import com.bear.processbus.service.Constant
import com.bear.processbus.service.IService
import com.bear.processbus.service.Request
import com.bear.processbus.service.Response

class MainService : Service() {
    private val TAG = "MainService"
    private val handler: Util.ProcessHandler by lazy {
        Util.getHandler(TAG)!!
    }

    //event
    private val callbackMap = HashMap<String, ICallBack>() //每个进程的callback回调
    private val cmdMap = HashMap<String, HashSet<String>>()//每个命令对应的进程

    //service
    private val serviceMap = HashMap<String,IService>() //进程注册的服务

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

        override fun registerService(serviceName: String?, service: IService?) {
            handler.post {
                if (service != null && service != null){
                    if (serviceMap.containsKey(serviceName)){
                        Log.i(TAG,"already has ${serviceName}")
                    }
                    serviceMap[serviceName!!] = service
                }
            }
        }

        override fun unRegisterService(serviceName: String?) {
            handler.post {
                if (serviceName != null && serviceMap.containsKey(serviceName)){
                    serviceMap.remove(serviceName)
                }
            }
        }

        override fun callService(request: Request?): Response {
            if (request!= null && !request.serviceName.isNullOrEmpty() && serviceMap.containsKey(request.serviceName)){
                Log.i(TAG,"request serviceName:${ request.serviceName},params:${request.params}")
                val response = serviceMap[request.serviceName]!!.call(request)
                Log.i(TAG,"response : ${response.content}")
                return response
            }
            return Response("cannot find the service", Constant.SERVICE_NOT_FOUND)
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
        serviceMap.clear()
        handler.quitSafely()
    }
}