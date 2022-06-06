// ICallBack.aidl
package com.example.aidltest;
import com.example.aidltest.Event;

// Declare any non-default types here with import statements

interface ICallBack {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);
    void onReceived(int code,inout Event event);
}