package com.bear.processbus

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
import com.bear.processbus.api.EventApi
import com.bear.processbus.api.ServiceApi
import com.bear.processbus.eventbus.Event
import com.bear.processbus.service.Constant
import com.bear.processbus.service.IProcessService
import com.bear.processbus.service.Request
import com.bear.processbus.service.Response
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Bus : ServiceApi, EventApi {

    private val TAG = "ProcessBus"
    private var isInit = false
    private var eventSS: IEventBus? = null
    private var context: Context? = null

    private val INIT = 0 //初始状态
    private val CONNECTED = 1 //连接状态
    private val DISCONNECTED = 2 //断开连接状态
    private var status = INIT

    private var processEvent: PEvent? = null

    private var processService: PService? = null

    private val handler: Util.ProcessHandler by lazy {
        Util.getHandler(Constant.PROCESS_HANDLER)!!
    }

    fun init(context: Context? = null) {
        if (!isInit) {
            synchronized(Bus::class.java) {
                if (!isInit) {
                    if (context != null) {
                        this.context = context
                    } else {
                        this.context = Util.getContext()
                    }
                    isInit = true
                }
            }
        }
    }

    //绑定服务
    private suspend fun bindService() {
        return suspendCoroutine { continuation ->
            if (status == CONNECTED) {
                continuation.resume(Unit)
                return@suspendCoroutine
            }
            if (context == null) {
                this.context = Util.getContext()
            }
            if (context == null) {
                continuation.resume(Unit)
                return@suspendCoroutine
            }
            val intent = Intent(context, MainService::class.java)
            context?.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                    Log.i(TAG, "service connected")
                    eventSS = IEventBus.Stub.asInterface(p1)
                    processEvent = PEvent(eventSS, context)
                    processService = PService(eventSS)

                    status = CONNECTED
                    continuation.resume(Unit)
                }

                override fun onServiceDisconnected(p0: ComponentName?) {
                    Log.i(TAG, "service disConnected")
                    status = DISCONNECTED
                    processService = null
                    processEvent = null
                    eventSS = null
                }
            }, Service.BIND_AUTO_CREATE)
        }
    }


    override fun post(event: Event) {
        checkAndRun {
            processEvent?.post(event)
        }
    }

    override fun register(cmd: String, block: (Event) -> Unit): Releasable? {
        init()
        if (context == null) {
            return null
        }
        val observerKey = Util.getObserverKey(context!!, cmd)
        checkAndRun {
            processEvent?.register(cmd, observerKey, block)
        }
        return Releasable(observerKey)
    }

    override fun unRegister(observerKey: String) {
        checkAndRun {
            processEvent?.unRegister(observerKey)
        }
    }

    override fun registerService(service: IProcessService) {
        checkAndRun {
            processService?.registerService(service)
        }
    }

    override fun unRegisterService(serviceName: String) {
        checkAndRun {
            processService?.unRegisterService(serviceName)
        }
    }

    override suspend fun callService(request: Request): Response {
        return suspendCoroutine {
            checkAndRun {
                if (processService == null) {
                    it.resume(Response("service not connected", Constant.SERVICE_ERROR))
                } else {
                    it.resume(processService!!.callService(request))
                }
            }
        }
    }

    fun checkAndRun(block: () -> Unit) {
        handler.post {
            GlobalScope.launch {
                init()
                bindService()
                block()
            }
        }
    }

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