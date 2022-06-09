package com.example.aidltest

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bear.processbus.Bus
import com.bear.processbus.Event
import com.bear.processbus.Util
import java.io.ByteArrayOutputStream

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

    val imageBtn by lazy<ImageView> {
        findViewById(R.id.image_three)
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
            Bus.post(Event("testCmd1","新的内容,thridActivity"){
//                "34567，abcde".toByteArray()
                bitmapToByte(getBitmapFromImgView(imageBtn))
            })
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


    private fun getBitmapFromImgView(mImageView: ImageView): Bitmap {
        mImageView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(mImageView.drawingCache)
        mImageView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun bitmapToByte(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);//将流对象与Bitmap对象进行关联。
        val bytes = byteArrayOutputStream.toByteArray()//使用流对象，将Bitmap对象转换为byte[]数组
        return bytes
    }
}