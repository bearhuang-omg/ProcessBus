// IEventBus.aidl
package com.bear.processbus;

import com.bear.processbus.ICallBack;
import com.bear.processbus.Event;

interface IEventBus {

    oneway void bind(String key,ICallBack callback);//key是processName，callback为通信的通路
    oneway void register(String key,String cmd);
    oneway void unRegister(String key,String cmd);
    oneway void post(in Event event);
}