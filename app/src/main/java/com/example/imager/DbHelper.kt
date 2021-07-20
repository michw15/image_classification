package com.example.imager

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper
import java.util.*

class DbHelper(context: Context?) : SQLiteAssetHelper(context, DB_NAME, null, DB_VERSION) {

    val allFish: ArrayList<HashMap<String, String>>
        get() {
            val list = ArrayList<HashMap<String, String>>()
            var item: HashMap<String, String>
            val db: SQLiteDatabase = readableDatabase
            val cursor: Cursor = db.rawQuery("SELECT * FROM FISH_LIST ORDER BY NAME", null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                item = HashMap()
                Log.d(TAG, cursor.getString(0) + cursor.getString(1) + cursor.getString(2))
                item["line1"] = cursor.getString(1)
                item["line2"] = cursor.getString(2)
                item["line3"] = cursor.getString(3)
                item["line4"] = cursor.getString(4)
                list.add(item)
                cursor.moveToNext()
            }
            cursor.close()
            return list
        }

    companion object {
        private const val DB_NAME = "fish.db"
        private const val DB_VERSION = 2
        private const val TAG = "DatabaseHelper"
    }

    init {
        setForcedUpgrade()
    }
}