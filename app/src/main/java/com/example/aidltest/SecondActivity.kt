package com.example.aidltest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bear.processbus.Bus
import com.bear.processbus.Event
import com.bear.processbus.Util

class SecondActivity : AppCompatActivity() {

    val TAG = "ProcessSecondActivity"

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

    var key = ""

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
            Bus.unRegister(key)
        }
        registerBtn.setOnClickListener {
            key = Bus.register("testCmd") { event ->
                Log.i(TAG, "当前的进程${Util.getProcessName(this)},收到的event：${event.cmd},${event.content},${event.fromProcess} ")
                this.runOnUiThread{
                    Toast.makeText(this,"当前的进程${Util.getProcessName(this)},收到的event：${event.cmd},${event.content},${event.fromProcess}",Toast.LENGTH_SHORT).show()
                }
            }?.key!!
           Log.i(TAG,"key = $key")
        }
    }


}