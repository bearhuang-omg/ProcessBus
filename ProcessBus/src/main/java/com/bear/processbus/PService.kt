package com.bear.processbus

import com.bear.processbus.service.*

class PService(val eventSS: IEventBus?) {

    fun registerService(service: IProcessService) {
        eventSS?.registerService(service.getServiceName(), object : IService.Stub() {
            override fun call(request: Request?): Response {
                if (request == null) {
                    return Response("request is null", Constant.REQUEST_NULL)
                }
                return service.call(request)
            }
        })
    }

    fun unRegisterService(serviceName: String) {
        if (!serviceName.isNullOrEmpty()) {
            eventSS?.unRegisterService(serviceName)
        }
    }

    fun callService(request: Request): Response {
        if (eventSS == null) {
            return Response("service not connected", Constant.SERVICE_ERROR)
        }
        return eventSS.callService(request)
    }

}