package com.example.imager

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Created by Eric on 3/2/2018.
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, DatabaseActivity::class.java)
        startActivity(intent)
        finish()
    }
}