// IEventBus.aidl
package com.bear.processbus;

import com.bear.processbus.ICallBack;
import com.bear.processbus.Event;

interface IEventBus {

    void register(String cmd,ICallBack callback);
    void unRegister(String cmd);
    void post(in Event event);
}