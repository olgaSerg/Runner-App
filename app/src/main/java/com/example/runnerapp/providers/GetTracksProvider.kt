package com.example.runnerapp.providers

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.runnerapp.models.TrackModel
import com.google.android.gms.maps.model.LatLng
import bolts.Task
import com.google.android.gms.tasks.Task as FirebaseTask
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import java.lang.reflect.Type
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.HashMap

class GetTracksProvider {
    private val userId = Firebase.auth.uid

    fun getTracksAsync(db: SQLiteDatabase): Task<ArrayList<TrackModel>> {
        return Task.callInBackground {
            val args = arrayOf(userId)
            val cursor = db.rawQuery(
                """
                SELECT id, start_at, distance, route, running_time, firebase_key
                FROM track WHERE user_id = ? ORDER BY start_at DESC
                """,
                args
            )
            val tracks = arrayListOf<TrackModel>()
            with (cursor) {
                while (moveToNext()) {
                    val track = getTrackFromCursor(cursor)
                    tracks.add(track)
                }
            }
            cursor.close()
            tracks
        }
    }

    private fun getTrackFromCursor(cursor: Cursor): TrackModel {
        val track = TrackModel()
        with (cursor) {
            track.id = getInt(getColumnIndexOrThrow("id"))
            val date = getString(getColumnIndexOrThrow("start_at"))
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
            track.startAt = format.parse(date)
            track.distance = getInt(getColumnIndexOrThrow("distance"))
            track.duration = getLong(getColumnIndexOrThrow("running_time"))
            track.firebaseKey = getString(getColumnIndexOrThrow("firebase_key"))
            val jsonString = getString(getColumnIndexOrThrow("route"))
            track.routeList = parseJSON(jsonString)
        }
        return track
    }

    fun getTrackAsync(db: SQLiteDatabase, id: Int) : Task<TrackModel> {
        val args = arrayOf(id.toString())

        return Task.callInBackground {
            var track: TrackModel?
            val cursor = db.rawQuery(
                """SELECT id, start_at, distance, route, running_time, firebase_key 
                    FROM track WHERE id == ?""",
                    args
            )
            with (cursor) {
                moveToNext()
                track = getTrackFromCursor(cursor)
            }
            cursor.close()
            track
        }
    }

    private fun parseJSON(jsonString: String): ArrayList<LatLng> {
        val gson = Gson()
        val type: Type = object : TypeToken<List<LatLng?>?>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun getFirebaseKeysListFromDbAsync(db: SQLiteDatabase): Task<ArrayList<String>> {
        return Task.callInBackground {
            val args = arrayOf(userId.toString())
            val cursor = db.rawQuery(
                """SELECT firebase_key FROM track WHERE user_id = ?""",
                args
            )
            val localDbKeysList = arrayListOf<String>()
            with(cursor) {
                while (moveToNext()) {
                    val key = getString(getColumnIndexOrThrow("firebase_key")) ?: continue
                    localDbKeysList.add(key)
                }
            }
            cursor.close()
            localDbKeysList
        }
    }

    fun getTracksKeysFromFirebase(callback: (ArrayList<String>) -> Unit) : Task<ArrayList<String>> {
        return Task.callInBackground {
            val database = Firebase.database.reference
            val keysListFirebase = arrayListOf<String>()
            if (userId != null) {
                database.child(userId).child("tracks").get().addOnSuccessListener {
                    if (it.value != null) {
                        val dataSnapshot = it.value as HashMap<String, Any>

                        for (data in dataSnapshot) {
                            keysListFirebase.add(data.key)
                        }
                    }
                    callback(keysListFirebase)
                }
            }
            keysListFirebase
        }
    }

    fun getNewTracksListFromFirebase(
        keysList: ArrayList<String>,
        callback: (ArrayList<TrackModel>) -> Unit
    ): ArrayList<TrackModel> {
        val database = Firebase.database.reference
        val tracksList = arrayListOf<TrackModel>()
        val userId = userId ?: return tracksList

        val tasks = ArrayList<FirebaseTask<DataSnapshot>>()
        for (key in keysList) {
            tasks.add(database.child(userId).child("tracks").child(key).get())
        }

        Tasks.whenAllSuccess<DataSnapshot>(tasks).addOnSuccessListener { dataSnapshots ->
            for (dataSnapshot in dataSnapshots) {
                val track = TrackModel()
                val hashMap = dataSnapshot.value as HashMap<String, Any>
                track.duration = hashMap.getValue("duration") as Long
                val distance = hashMap.getValue("distance") as Long
                track.distance = distance.toInt()
                val startTime = hashMap.getValue("startAt") as HashMap<String, Any>
                val time = startTime.getValue("time") as Long
                track.startAt = formatTime(time)
                track.routeList = hashMap.getValue("routeList") as ArrayList<LatLng>
                track.firebaseKey = dataSnapshot.key
                tracksList.add(track)
            }
            callback(tracksList)
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
        return tracksList
    }

    private fun formatTime(time: Long): Date {
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
        val date = dateFormat.format(time)
        return dateFormat.parse(date)
    }
}
