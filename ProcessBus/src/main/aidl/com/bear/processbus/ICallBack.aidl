// ICallBack.aidl
package com.bear.processbus;

import com.bear.processbus.Event;

interface ICallBack {
    void onReceived(inout Event event);
}