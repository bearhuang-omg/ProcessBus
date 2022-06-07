package com.example.aidltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    val jumpBtn by lazy<Button> {
        findViewById(R.id.testBtn_jump)
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
        jumpBtn.setOnClickListener {
            val intent = Intent(this,ThirdActivity::class.java)
            startActivity(intent)
        }
        eventbusBtn.setOnClickListener {
//            Bus.init(this)
        }
        postBtn.setOnClickListener {
            Bus.post(Event("testCmd","新的内容"))
        }
        unregisterBtn.setOnClickListener {
            Bus.unRegister("testCmd")
        }
        registerBtn.setOnClickListener {
            Bus.register("testCmd",object:BusListener{
                override fun onEvent(event: Event) {
                    Log.i(TAG,"收到了："+event.cmd+","+event.content)
                }
            })
        }
    }


}