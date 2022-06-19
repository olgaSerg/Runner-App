package com.example.runnerapp.providers

import android.database.sqlite.SQLiteDatabase
import bolts.Task
import com.example.runnerapp.models.TrackModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordTrackProvider {

    private val userId = Firebase.auth.uid

    fun recordTrackAsync(db: SQLiteDatabase, track: TrackModel): Task<TrackModel> {
        val date = formatDate(track.startAt!!)
        val route = Gson().toJsonTree(track.routeList).asJsonArray
        val args = arrayOf(date, track.distance, track.duration, route, userId, track.firebaseKey)

        return Task.callInBackground {
            db.execSQL(
                """INSERT INTO "track" (start_at, distance, running_time, route, user_id, firebase_key)
                    VALUES (?, ?, ?, ?, ?, ?)
                """,
                args
            )

            val cursor = db.rawQuery("""SELECT MAX(id) AS id FROM track""", null)
            with(cursor) {
                moveToNext()
                track.id = getInt(getColumnIndexOrThrow("id"))
            }
            cursor.close()
            track
        }
    }

    fun recordFirebaseKeyAsync(db: SQLiteDatabase, key: String, id: Int) {
        val args = arrayOf(key, id)
        Task.callInBackground {
            db.execSQL(
                """UPDATE "track" SET firebase_key = ? WHERE id == ?
                """,
                args
            )
        }
    }

    fun recordNewTracksFromFirebase(db: SQLiteDatabase, tracksList: ArrayList<TrackModel>): Task<Unit> {
        return Task.callInBackground {
            for (track in tracksList) {
                recordTrackAsync(db, track)
            }
        }
    }

    private fun formatDate(date: Date): String {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }
}

