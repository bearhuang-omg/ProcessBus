// ICallBack.aidl
package com.bear.processbus;

import com.bear.processbus.Event;

interface ICallBack {
    void onReceived(int code,inout Event event);
}