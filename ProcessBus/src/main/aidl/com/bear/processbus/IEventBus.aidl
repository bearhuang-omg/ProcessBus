// IEventBus.aidl
package com.bear.processbus;

import com.bear.processbus.ICallBack;
import com.bear.processbus.Event;

interface IEventBus {

    void bind(String key,ICallBack callback);//key是processName，callback为通信的通路
    void register(String key,String cmd);
    void unRegister(String key,String cmd);
    void post(in Event event);
}