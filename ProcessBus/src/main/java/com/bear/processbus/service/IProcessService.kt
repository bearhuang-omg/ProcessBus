package com.bear.processbus.service

interface IProcessService {

    fun call(request: Request): Response
    fun getServiceName(): String

}