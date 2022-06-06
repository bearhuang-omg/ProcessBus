package com.example.aidltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var testBtn:Button = findViewById(R.id.testBtn)
        testBtn.setOnClickListener {
//            Toast.makeText(this,"click",Toast.LENGTH_SHORT).show()
            var intent = Intent(this,SecondActivity::class.java)
            startActivity(intent)
        }
    }
}