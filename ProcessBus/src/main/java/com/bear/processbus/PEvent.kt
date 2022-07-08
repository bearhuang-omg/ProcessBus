package com.bear.processbus

import android.content.Context
import android.util.Log
import com.bear.processbus.eventbus.Event
import com.bear.processbus.eventbus.ICallBack
import com.bear.processbus.service.Constant

class PEvent(val eventSS: IEventBus?, val context: Context?) {

    val TAG = "PEvent"
    private val registedCmd = HashMap<String, HashSet<String>>() // 注册的事件和key，cmd->observerKey
    private val registedBlock = HashMap<String, (Event) -> Unit>()//注册的key和监听方法，observerKey->block
    private val handler: Util.ProcessHandler by lazy {
        Util.getHandler(Constant.PROCESS_HANDLER)!!
    }
    private val bindCallBack = object : ICallBack.Stub() {
        override fun onReceived(event: Event?) {
            handler.post {
                Log.i(
                    TAG,
                    "received event, cmd:${event?.cmd} , content:${event?.content} , fromProcess:${event?.fromProcess}"
                )
                if (event != null && !event.cmd.isNullOrEmpty()) {
                    registedCmd[event?.cmd]?.forEach { observerKey ->
                        val block = registedBlock[observerKey]
                        try {
                            block?.invoke(event)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            Log.e(
                                TAG,
                                "block run error , observerkey: ${observerKey} , error:${ex}"
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        eventSS?.bind(Util.getProcessKey(context), bindCallBack)
    }

    public fun post(event: Event) {
        eventSS?.post(event)
    }

    public fun register(cmd: String, observerKey: String, block: (Event) -> Unit) {
        if (registedCmd.containsKey(cmd)) {
            registedCmd[cmd]?.add(observerKey)
        } else {
            val set = HashSet<String>()
            set.add(observerKey)
            registedCmd[cmd] = set
            eventSS?.register(Util.getProcessKey(context), cmd)
        }
        registedBlock[observerKey] = block
        Log.i(TAG, "register key:{$observerKey} , cmd:${cmd}")
    }

    fun unRegister(observerKey: String) {
        if (observerKey.isEmpty()) {
            return
        }
        Log.i(TAG, "unregister key:${observerKey}")
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