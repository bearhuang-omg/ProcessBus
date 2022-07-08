package com.bear.processbus.api

import com.bear.processbus.service.IProcessService
import com.bear.processbus.service.Request
import com.bear.processbus.service.Response

//服务Api
interface ServiceApi {
    fun registerService(service: IProcessService)
    fun unRegisterService(serviceName: String)
    suspend fun callService(request: Request): Response
}