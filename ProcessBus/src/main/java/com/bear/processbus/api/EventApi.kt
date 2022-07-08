package com.bear.processbus.api

import com.bear.processbus.Bus
import com.bear.processbus.eventbus.Event

//事件Api
interface EventApi {
    fun post(event: Event)
    fun register(cmd: String, block: (Event) -> Unit): Bus.Releasable?
    fun unRegister(observerKey: String)
}