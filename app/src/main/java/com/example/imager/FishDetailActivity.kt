package com.example.imager

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class FishDetailActivity : AppCompatActivity() {

    private lateinit var descriptionText: TextView
    private lateinit var titleText: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_fish)
        descriptionText = findViewById(R.id.textView2)
        titleText = findViewById(R.id.textView3)
        imageView = findViewById(R.id.imageView3)
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.data_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateUI(){
        val db = DbHelper(this)
        val id : Int = intent.extras!!.get("position") as Int
        val fish = db.allFish[id]
        descriptionText.text = fish["line1"]
        titleText.text = fish["line4"]
        imageView.setImageBitmap(decodeImage(fish["line3"]!!))
    }

    private fun decodeImage(encodedImage: String): Bitmap{
        val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.switch_to_live -> {
                val liveIntent = Intent(this, CamActivity::class.java)
                startActivity(liveIntent)
                finish()
                true
            }
            R.id.database -> {
                val databaseIntent = Intent(this, DatabaseActivity::class.java)
                startActivity(databaseIntent)
                finish()
                true
            }
            R.id.about -> {
                val aboutIntent = Intent(this, AboutActivity::class.java)
                startActivity(aboutIntent)
                finish()
                true
            }

            R.id.predict -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}