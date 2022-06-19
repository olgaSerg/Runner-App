package com.example.runnerapp

import android.app.Application
import android.database.sqlite.SQLiteDatabase

class App : Application() {
    var dBHelper: DBHelper? = null
    var db: SQLiteDatabase? = null
        private set

    override fun onCreate() {
        super.onCreate()

        instance = this
        dBHelper = DBHelper(this)
        db = dBHelper?.writableDatabase
    }

    companion object {
        var instance: App? = null
            private set
    }
}