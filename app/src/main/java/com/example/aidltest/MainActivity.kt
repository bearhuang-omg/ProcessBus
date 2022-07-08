package com.example.aidltest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bear.processbus.Bus
import com.bear.processbus.service.IProcessService
import com.bear.processbus.service.Request
import com.bear.processbus.service.Response

class MainActivity : AppCompatActivity() {

    val TAG = "MMMMMMM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var testBtn: Button = findViewById(R.id.testBtn)
        testBtn.setOnClickListener {
            var intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

        Bus.registerService(object : IProcessService {
            override fun call(request: Request): Response {
                Log.i(TAG, "request params:${request.params}")
                return Response("hello world!!!")
            }

            override fun getServiceName(): String {
                return "testService"
            }
        })
    }
}