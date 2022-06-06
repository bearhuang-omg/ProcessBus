// IEventBus.aidl
package com.example.aidltest;
import com.example.aidltest.ICallBack;
import com.example.aidltest.Event;

// Declare any non-default types here with import statements

interface IEventBus {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void register(String cmd,ICallBack callback);
    void unRegister(String cmd);
    void post(in Event event);
}