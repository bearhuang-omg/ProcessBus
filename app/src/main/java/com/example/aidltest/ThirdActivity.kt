package com.example.aidltest

import android.os.Bundle
import android.os.Process
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bear.processbus.Bus
import com.bear.processbus.Event
import com.bear.processbus.Util

class ThirdActivity : AppCompatActivity() {

    val TAG = "ThirdActivityProcess"

    val testBtnFinish by lazy<Button> {
        findViewById(R.id.testBtn_finish)
    }

    val connectBtnPost by lazy<Button> {
        findViewById(R.id.testBtn_post)
    }

    val registerBtn by lazy<Button> {
        findViewById(R.id.testBtn_register)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
//        Bus.init(this)
        testBtnFinish.setOnClickListener {
            this.finish()
//            Process.killProcess(Process.myPid())
        }
        connectBtnPost.setOnClickListener {
            Bus.post(Event("testCmd2", "thirdActivity333333发出来的消息"))
            Bus.post(Event("testCmd1", "thirdActivity11111发出来的消息"))
        }
        registerBtn.setOnClickListener {
            Bus.register("testCmd2") { event ->
                Log.i(
                    TAG,
                    "当前的进程${Util.getProcessName(this)},收到的event：${event.cmd},${event.content},${event.fromProcess} "
                )
            }?.autoRelease(lifecycle)
        }
    }


}