package com.example.runnerapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

const val DB_VERSION = 1
const val TABLE_NAME = "track"
const val ID = "id"
const val START_TIME = "start_time"
const val DISTANCE = "distance"
const val RUNNING_TIME = "running_time"
const val ROUTE = "route"

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "TracksDB", null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("msg", "--- onCreate database ---")

        for (i in 1..DB_VERSION) {
            migrate(db, i)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newVersion: Int) {
        Log.d("msg", "--- onUpdate database ---")
        var oldVersion = oldV
        while (oldVersion < newVersion) {
            oldVersion++
            migrate(db, oldVersion)
        }
    }

    private fun migrate(db: SQLiteDatabase, dbVersion: Int) {
        when (dbVersion) {
            1 -> {
                db.execSQL(
                    """
            CREATE TABLE $TABLE_NAME (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $START_TIME STRING,
                $DISTANCE INTEGER,
                $RUNNING_TIME LONG,
                $ROUTE JSONArray
            );
            """
                )
                db.execSQL(
                    """
            INSERT INTO "track" ("start_time", "distance", "running_time") 
            VALUES ('13.05.2022 08:13:56', 1523, 683),
                   ('15.05.2022 11:07:35', 2198, 919)
            """
                )
            }
        }
    }
}