package com.example.runnerapp.providers

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import bolts.Task
import com.example.runnerapp.models.TrackModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
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

    fun getTracksAsync(db: SQLiteDatabase): Task<ArrayList<TrackModel>> {
        return Task.callInBackground {
            val cursor = db.rawQuery(
                """SELECT id, start_time, distance, running_time, firebase_key FROM track ORDER BY start_time DESC""",
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
                    track.firebaseKey = getString(getColumnIndexOrThrow("firebase_key"))
                    tracks.add(track)
                }
            }
            cursor.close()
            tracks
        }
    }

    fun getSelectedTrackAsync(db: SQLiteDatabase, id: Int) : Task<TrackModel> {
        val args = id.toString()
        return Task.callInBackground {

            val cursor = db.rawQuery(
                """SELECT distance, running_time, route FROM track WHERE id == ?""",
                arrayOf(
                    args
                )
            )
            val track = TrackModel()
            with(cursor) {
                moveToNext()
                track.distance = getInt(getColumnIndexOrThrow("distance"))
                track.duration = getLong(getColumnIndexOrThrow("running_time"))
                val jsonString = getString(getColumnIndexOrThrow("route"))
                track.routeList = parseJSON(jsonString)
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

    fun getFirebaseKeyFromDbAsync(db: SQLiteDatabase): Task<ArrayList<String>> {
        return Task.callInBackground {
            val cursor = db.rawQuery(
                """SELECT firebase_key FROM track""",
                null
            )
            val localDbKeysList = arrayListOf<String>()
            with(cursor) {
                while(moveToNext()) {
                    val key = getString(getColumnIndexOrThrow("firebase_key"))
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
            val uid = Firebase.auth.uid

                if (uid != null) {
                    database.child(uid).child("tracks").get().addOnSuccessListener {
                        val dataSnapshot = it.value as HashMap<String, Any>

                        for (data in dataSnapshot) {
                            keysListFirebase.add(data.key)
                        }
                        callback(keysListFirebase)
                    }
                }
            keysListFirebase
        }
    }

    fun getNewTracksList(keysList: ArrayList<String>, callback: (ArrayList<TrackModel>) -> Unit): ArrayList<TrackModel> {
        val database = Firebase.database.reference
        val tracksList = arrayListOf<TrackModel>()
        val uid = Firebase.auth.uid ?: return tracksList
        database.child(uid).child("tracks").get().addOnSuccessListener {
            val dataSnapshot = it.children
            for (key in keysList) {
                for (shot in dataSnapshot) {
                    if (shot.key == key) {
                        val track = TrackModel()
                        val hashMap = shot.value as HashMap<String, Any>
                        track.duration = hashMap.getValue("duration") as Long
                        val distance = hashMap.getValue("distance") as Long
                        track.distance = distance.toInt()
                        val startTime = hashMap.getValue("startTime") as HashMap<String, Any>
                        val time = startTime.getValue("time") as Long
                        track.startTime = formatTime(time)
                        track.routeList = hashMap.getValue("routeList") as ArrayList<LatLng>
                        track.firebaseKey = shot.key
                        tracksList.add(track)
                    }
                }
            }
            callback(tracksList)
            Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        return tracksList
    }

    private fun formatTime(time: Long): Date {
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
        val date = dateFormat.format(time)
        return dateFormat.parse(date)!!
    }
}