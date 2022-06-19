package com.example.runnerapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

const val DB_VERSION = 1
const val TRACKS_TABLE_NAME = "track"
const val NOTIFICATIONS_TABLE_NAME = "notification"
const val ID = "id"
const val START_TIME = "start_at"
const val DISTANCE = "distance"
const val RUNNING_TIME = "running_time"
const val ROUTE = "route"
const val FIREBASE_KEY = "firebase_key"
const val DATE_TIME = "notify_at"
const val UID = "user_id"

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
            CREATE TABLE $TRACKS_TABLE_NAME (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $START_TIME STRING,
                $DISTANCE INTEGER,
                $RUNNING_TIME LONG,
                $ROUTE JSONArray,
                $FIREBASE_KEY STRING,
                $UID STRING
            );
            """
                )
                db.execSQL(
                    """
            CREATE TABLE $NOTIFICATIONS_TABLE_NAME (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $DATE_TIME STRING,
                $UID STRING
            );
            """
                )
            }
        }
    }
}