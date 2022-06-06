package com.example.aidltest

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import com.bear.processbus.Bus
import com.bear.processbus.BusListener
import com.bear.processbus.Event

class SecondActivity : AppCompatActivity() {

    val TAG = "SecondActivity"

    val testBtn by lazy<Button> {
        findViewById(R.id.testBtn_back)
    }

    val connectBtn by lazy<Button> {
        findViewById(R.id.testBtn_connect)
    }

    val postBtn by lazy<Button> {
        findViewById(R.id.testBtn_post)
    }

    val eventbusBtn by lazy<Button>{
        findViewById(R.id.testBtn_eventbus)
    }

    val registerBtn by lazy<Button> {
        findViewById(R.id.testBtn_regist)
    }

    val unregisterBtn by lazy<Button> {
        findViewById(R.id.testBtn_unregist)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        testBtn.setOnClickListener {
            this.finish()
        }
        connectBtn.setOnClickListener {
//            val intent = Intent(this,CatService::class.java)
//            bindService(intent,catConnection, Service.BIND_AUTO_CREATE)
        }
        eventbusBtn.setOnClickListener {
            Bus.init(this)
//            val intent = Intent(this,EventService::class.java)
//            bindService(intent,eventConnection,Service.BIND_AUTO_CREATE)
        }
        postBtn.setOnClickListener {
            Bus.post(Event("testCmd","新的内容"))
//            val event = Event("testCmd","event是发送的内容")
//            eventSS?.post(event)
        }
        unregisterBtn.setOnClickListener {
            Bus.unRegister("testCmd")
//            eventSS?.unRegister("testCmd")
        }
        registerBtn.setOnClickListener {
            Bus.register("testCmd",object:BusListener{
                override fun onEvent(event: Event) {
                    Log.i(TAG,"收到了："+event.cmd+","+event.content)
                }

            })
//            eventSS?.register("testCmd",recieve)
        }
    }

//    private var eventSS:IEventBus? = null
//    private var recieve = object:ICallBack.Stub(){
//        override fun onReceived(code: Int, event: Event?) {
//            Log.i(TAG,"收到了recived cmd:"+event?.cmd+",code:"+code+",content:"+event?.content)
//        }
//    }

//    private val eventConnection = object : ServiceConnection{
//        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
//            Log.i(TAG,"event onServiceConnected")
//            eventSS = IEventBus.Stub.asInterface(p1)
////            eventSS!!.register("testCmd",recieve)
//        }
//
//        override fun onServiceDisconnected(p0: ComponentName?) {
//            Log.i(TAG,"event onServiceDisconnected")
//            eventSS = null
//        }
//
//    }


}