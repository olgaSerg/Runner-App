package com.example.runnerapp.providers

import android.database.sqlite.SQLiteDatabase
import bolts.Task
import com.example.runnerapp.models.TrackModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GetTracksProvider {

    fun getTracksExecute(db: SQLiteDatabase): Task<ArrayList<TrackModel>> {
        return Task.callInBackground {
            val cursor = db.rawQuery(
                """SELECT id, start_time, distance, running_time FROM track""",
                null
            )
            val tracks = arrayListOf<TrackModel>()
            with(cursor) {
                while (moveToNext()) {
                    val track = TrackModel()
                    track.id = getInt(getColumnIndexOrThrow("id"))
                    val date = getString(getColumnIndexOrThrow("start_time"))
                    val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                    track.startTime = format.parse(date)
                    track.distance = getInt(getColumnIndexOrThrow("distance"))
                    track.duration = getLong(getColumnIndexOrThrow("running_time"))
                    tracks.add(track)
                }
            }
            cursor.close()
            tracks
        }
    }
}