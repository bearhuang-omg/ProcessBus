// IEventBus.aidl
package com.bear.processbus;

import com.bear.processbus.ICallBack;
import com.bear.processbus.Event;

interface IEventBus {

    void register(String cmd,String key,ICallBack callback);
    void unRegister(String key);
    void post(in Event event);
}