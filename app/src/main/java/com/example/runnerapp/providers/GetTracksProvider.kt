package com.example.runnerapp.providers

import android.database.sqlite.SQLiteDatabase
import bolts.Task
import com.example.runnerapp.models.TrackModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.util.Log
import java.lang.reflect.Type

class GetTracksProvider {

    fun getTracksAsync(db: SQLiteDatabase): Task<ArrayList<TrackModel>> {
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
}