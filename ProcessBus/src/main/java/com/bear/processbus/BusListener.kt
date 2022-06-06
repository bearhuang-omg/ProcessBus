package com.bear.processbus

interface BusListener {
    fun onEvent(event: Event)
}