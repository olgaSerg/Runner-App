package com.example.runnerapp.providers

import android.database.sqlite.SQLiteDatabase
import bolts.Task
import com.example.runnerapp.models.TrackModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import org.json.JSONArray
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordTrackProvider {

    fun recordTrackExecute(db: SQLiteDatabase, track: TrackModel): Task<TrackModel> {
        val date = formatDate(track.startTime!!)
//        track.routeList = arrayListOf(
//            LatLng(50.34, 23.43),
//            LatLng(50.87, 23.67),
//            LatLng(51.00, 23.20)
//        )
        val route = Gson().toJsonTree(track.routeList).asJsonArray
        val args = arrayOf(date, track.distance, track.duration, route)

        return Task.callInBackground {
            db.execSQL(
                """INSERT INTO "track" (start_time, distance, running_time, route)
                    VALUES (?, ?, ?, ?)
                """,
                args
            )
            track

        }
    }

    private fun formatDate(date: Date): String {
        val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }
}
