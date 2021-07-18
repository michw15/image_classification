package com.example.imager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class DatabaseActivity : AppCompatActivity() {

    private var mFishListView: ListView? = null
    private var listMap: ArrayList<HashMap<String, String>>? = null
    private var mAdapter: SimpleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.database_activity)
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.data_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateUI() {
        mFishListView = findViewById(R.id.list_fish) as ListView?
        val db = DbHelper(this)
        listMap = db.allFish
        mAdapter = SimpleAdapter(
            this,
            listMap,
            R.layout.fish_item,
            arrayOf("line1", "line2"),
            intArrayOf(R.id.fish_name, R.id.fish_species)
        )
        mFishListView!!.adapter = mAdapter
        db.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.switch_to_live -> {
                Log.d(TAG, "Switching to Live Mode")
                val liveIntent = Intent(this, ClassifierActivity::class.java)
                startActivity(liveIntent)
                finish()
                true
            }
            R.id.database -> {
                Log.d(TAG, "Switching to Database")
                val databaseIntent = Intent(this, DatabaseActivity::class.java)
                startActivity(databaseIntent)
                finish()
                true
            }
            R.id.about -> {
                Log.d(TAG, "Switching to About page")
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

    companion object {
        private const val TAG = "DatabaseActivity"
    }
}