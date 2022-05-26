package com.example.runnerapp.providers

import android.database.sqlite.SQLiteDatabase
import bolts.Task
import com.example.runnerapp.models.TrackModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordTrackProvider {

    fun recordTrackExecute(db: SQLiteDatabase, track: TrackModel): Task<TrackModel> {
        val date = formatDate(track.startTime!!)
        val args = arrayOf(date, track.duration)

        return Task.callInBackground {
            db.execSQL(
                """INSERT INTO "track" (start_time, distance, running_time)
                    VALUES (?, 1247, ?)
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
