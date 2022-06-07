package com.example.aidltest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.bear.processbus.Bus
import com.bear.processbus.Event

class ThirdActivity : AppCompatActivity() {

    val TAG = "ThirdActivity"

    val testBtnFinish by lazy<Button> {
        findViewById(R.id.testBtn_finish)
    }

    val connectBtnPost by lazy<Button> {
        findViewById(R.id.testBtn_post)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
//        Bus.init(this)
        testBtnFinish.setOnClickListener {
            this.finish()
        }
        connectBtnPost.setOnClickListener {
            Bus.post(Event("testCmd","thirdActivity发出来的消息"))
        }
    }


}