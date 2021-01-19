package com.arul.camerax

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    val camIntentCode = 100

    val imageFile: File by lazy {
        File(getExternalFilesDir(null)?.absolutePath + "/${System.currentTimeMillis()}.jpg")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun OnGetImage(view: View) {
        val camIntent = Intent(this@MainActivity, CameraXActivity::class.java)
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile.absolutePath)
        startActivityForResult(camIntent, camIntentCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == camIntentCode) {
            if (resultCode == RESULT_OK) {
                val myBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                findViewById<ImageView>(R.id.image).setImageBitmap(myBitmap)
            }
        }
    }
}
