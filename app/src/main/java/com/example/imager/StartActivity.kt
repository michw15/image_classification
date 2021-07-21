package com.example.imager

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    private lateinit var infoImage: ImageView
    private lateinit var focusImage: ImageView
    private lateinit var dbImage: ImageView
    private lateinit var cameraImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        infoImage = findViewById(R.id.info_image)
        focusImage = findViewById(R.id.focus_image)
        dbImage = findViewById(R.id.db_image)
        cameraImage = findViewById(R.id.camera_image)

        infoImage.setOnClickListener {
            startActivity(intent.setClass(this, AboutActivity::class.java))
        }
        focusImage.setOnClickListener {
            startActivity(intent.setClass(this, CamActivity::class.java))
        }
        cameraImage.setOnClickListener {
            startActivity(intent.setClass(this, MainActivity::class.java))
        }
        dbImage.setOnClickListener {
            startActivity(intent.setClass(this, DatabaseActivity::class.java))
        }
    }
}