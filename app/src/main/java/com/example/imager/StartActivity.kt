package com.example.imager

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        button = findViewById(R.id.start_button)
        button.setOnClickListener {
            startActivity(intent.setClass(this, DatabaseActivity::class.java))
        }
    }
}