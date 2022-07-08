// IEventBus.aidl
package com.bear.processbus;

import com.bear.processbus.eventbus.ICallBack;
import com.bear.processbus.eventbus.Event;
import com.bear.processbus.service.IService;

interface IEventBus {

    //event
    oneway void bind(String key,ICallBack callback);//key是processName，callback为通信的通路
    oneway void register(String key,String cmd);
    oneway void unRegister(String key,String cmd);
    oneway void post(in Event event);

    //服务
    oneway void registerService(String serviceName,IService service);
    oneway void unRegisterService(String serviceName);
    Response callService(in Request request);
}