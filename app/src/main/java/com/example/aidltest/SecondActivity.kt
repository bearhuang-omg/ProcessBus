package com.example.aidltest

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bear.processbus.Bus
import com.bear.processbus.eventbus.Event
import com.bear.processbus.Util
import com.bear.processbus.service.Request
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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

    val testImg by lazy<ImageView> {
        findViewById(R.id.image_two)
    }

    val callServiceBtn by lazy<Button> {
        findViewById(R.id.testBtn_callService)
    }

    var key = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        testBtn.setOnClickListener {
            this.finish()
        }
        callServiceBtn.setOnClickListener {
            val request = Request("testService")
            request.addParams("test","hehehe")
            request.addParams("test1","hahaha")
            GlobalScope.launch {
                val response = Bus.callService(request)
                Log.i(TAG,"response : ${response.content}")
            }
        }
        jumpBtn.setOnClickListener {
            val intent = Intent(this,ThirdActivity::class.java)
            startActivity(intent)
        }
        eventbusBtn.setOnClickListener {
//            Bus.init(this)
        }
        postBtn.setOnClickListener {
            Bus.post(Event("testCmd1","新的内容"))
//            ProcessBus.post(Event("testCmd1","新的内容") {
//
//                "12345,上山大老虎".toByteArray()
//            })

        }

        unregisterBtn.setOnClickListener {
            Bus.unRegister(key)
        }

        registerBtn.setOnClickListener {
            this.key = Bus.register("testCmd1") { event ->
                Log.i(
                    TAG, "当前的进程${Util.getProcessName(this)},收到的event：${event.cmd},${event.content},${event.fromProcess} "
                )
                this.runOnUiThread{
                    if (event.getAttachment() != null){
                        val bitmap = byteToBitmap(event.getAttachment()!!.content)
                        testImg.setImageBitmap(bitmap)
                        Toast.makeText(this,"收到了图片",Toast.LENGTH_SHORT).show()
//                        val content = String(event.getAttachment()!!.content)
//                        Toast.makeText(this,content,Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"没有附件",Toast.LENGTH_SHORT).show()
                    }
                }

            }?.key!!
        }
    }


    fun byteToBitmap(byteArray: ByteArray):Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
        return bitmap
    }


}