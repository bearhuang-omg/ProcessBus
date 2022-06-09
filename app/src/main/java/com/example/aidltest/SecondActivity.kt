package com.example.aidltest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bear.processbus.*

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
//            Bus.post(Event("testCmd1","新的内容"))
            Bus.post(Event("testCmd1","新的内容", Attachment("12345,上山打老虎".toByteArray())))
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
                        val content = String(event.getAttachment()?.content!!)
                        Toast.makeText(this,content,Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"没有附件",Toast.LENGTH_SHORT).show()
                    }
                }

            }?.key!!
        }
    }


}