package com.example.aidltest

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class CatService : Service() {
    val TAG = "CatService"

    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG,"onCreate")
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        Log.i(TAG,"onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG,"onDestroy")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        Log.i(TAG,"onUnbind")
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.i(TAG,"onRebind")
    }

    private val mBinder = object : ICat.Stub() {
        override fun getColor(id: Int): String {
            Log.i(TAG,"收到了getColor,id:"+id)
            return "黄色"
        }

        override fun getWeight(id: Int): Double {
            Log.i(TAG,"收到了getWeight,id:"+id)
            return 10.0
        }

        override fun getMsg(id: Int, callback: ICallBack?) {
//            Log.i(TAG,"收到了getMsg,id:"+id)
//            callback?.onReceived("",10,"返回会去的msg")
        }

    }
}