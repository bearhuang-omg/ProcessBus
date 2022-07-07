// ICallBack.aidl
package com.bear.processbus.eventbus;

import com.bear.processbus.eventbus.Event;

interface ICallBack {
    oneway void onReceived(in Event event);
}