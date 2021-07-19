package com.example.imager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class DatabaseActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var mFishListView: ListView
    private var listMap: ArrayList<HashMap<String, String>>? = null
    private var mAdapter: SimpleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.database_activity)
        mFishListView = findViewById(R.id.list_fish)
        mFishListView.onItemClickListener = this
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.data_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateUI() {
        val db = DbHelper(this)
        listMap = db.allFish
        mAdapter = SimpleAdapter(
            this,
            listMap,
            R.layout.fish_item,
            arrayOf("line1", "line2"),
            intArrayOf(R.id.fish_name, R.id.fish_species)
        )
        mFishListView.adapter = mAdapter
        db.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.switch_to_live -> {
                Log.d(TAG, "Switching to Live Mode")
                val liveIntent = Intent(this, CamActivity::class.java)
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

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        intent.setClass(this,FishDetailActivity::class.java)
        intent.putExtra("position",p2)
        intent.putExtra("id",p3)
        startActivity(intent)
    }
}