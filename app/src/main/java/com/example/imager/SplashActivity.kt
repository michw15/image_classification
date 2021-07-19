package com.example.imager

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, StartActivity::class.java)
        startActivity(intent)
        finish()
    }
}