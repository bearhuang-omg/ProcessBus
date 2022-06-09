// ICallBack.aidl
package com.bear.processbus;

import com.bear.processbus.Event;

interface ICallBack {
    oneway void onReceived(in Event event);
}