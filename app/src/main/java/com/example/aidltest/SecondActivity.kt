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

class SecondActivity : AppCompatActivity() {

    val TAG = "SecondActivity"

    val testBtn by lazy<Button> {
        findViewById(R.id.testBtn_back)
    }

    val connectBtn by lazy<Button> {
        findViewById(R.id.testBtn_connect)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        testBtn.setOnClickListener {
            this.finish()
        }
        connectBtn.setOnClickListener {
            connect()
        }
    }

    private val connection = object : ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.i(TAG,"onServiceConnected")
            var cat = ICat.Stub.asInterface(p1)
            try {
                val color = cat.getColor(1)
                val weight = cat.getWeight(2)
                Log.i(TAG,"color:"+color)
                Log.i(TAG,"weight:"+weight)
            }catch (ex : Exception){
                ex.printStackTrace()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(TAG,"onServiceDisconnected")
        }

    }

    fun connect(){
        val intent = Intent(this,CatService::class.java)
        bindService(intent,connection, Service.BIND_AUTO_CREATE)
    }
}