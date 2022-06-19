package com.example.runnerapp

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import bolts.Task
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.example.runnerapp.providers.RecordTrackProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TracksSynchronizer(val db: SQLiteDatabase, val context: Context) {

    fun synchronizeTracks(callback: () -> Unit) {
        if (!hasConnection()) {
            Toast.makeText(
                context,
                "Интернет соединение отсутствует",
                Toast.LENGTH_SHORT
            ).show()
            callback()
            return
        }

        val getTracksProvider = GetTracksProvider()
        val recordTrackProvider = RecordTrackProvider()
        // step 1. download new tracks from firebase
        getTracksProvider.getTracksKeysFromFirebase { keysList ->
            Log.i("!!!keysListFirebase", keysList.joinToString(separator = " "))
            // compare keys list from firebase with keys list from local db
            getTracksProvider.getFirebaseKeysListFromDbAsync(db).onSuccess({
                Log.i("!!!keysFromDB", it.result.joinToString(separator = " "))
                // keys of new records from firebase
                val result = getNewTracksKeysFromFirebase(keysList, it.result)
                Log.i("!!!newKeysListFirebase", result.joinToString(separator = " "))
                result
            }, Task.BACKGROUND_EXECUTOR).onSuccess {
                // get tracks from firebase by the list of keys
                getTracksProvider.getNewTracksListFromFirebase(it.result) { tracksList ->
                    Log.i(
                        "newTracksListFromFirebase",
                        tracksList.joinToString(separator = " ")
                    )
                    // write new tracks from firebase to local db
                    recordTrackProvider.recordNewTracksFromFirebase(db, tracksList)
                        .continueWith {
                            // step 2. write non-synchronized tracks from local db to firebase
                            writeNewTracksToFirebase(
                                getTracksProvider,
                                recordTrackProvider,
                                db,
                                callback
                            )
                        }
                }
            }
        }
    }

    private fun getNewTracksKeysFromFirebase(
        keysListFirebase: ArrayList<String>,
        keysListLocalDb: ArrayList<String>
    ): ArrayList<String> {
        val newKeysList = ArrayList<String>()
        val keysListLocalDbSet = keysListLocalDb.toSet()

        for (firebaseKey in keysListFirebase) {
            if (!keysListLocalDbSet.contains(firebaseKey)) {
                newKeysList.add(firebaseKey)
            }
        }
        return newKeysList
    }

    private fun writeNewTracksToFirebase(
        getTracksProvider: GetTracksProvider,
        recordTrackProvider: RecordTrackProvider,
        db: SQLiteDatabase,
        callback: () -> Unit
    ) {
        val uid = Firebase.auth.uid
        val database = Firebase.database.reference
        getTracksProvider.getTracksAsync(db).onSuccess {
            val tracks = it.result ?: return@onSuccess

            for (track in tracks) {
                val trackId = track.id ?: continue
                if (track.firebaseKey != null) {
                    continue
                }

                val key = database.child("track").push().key
                val firebaseTrack = TrackModel(
                    null,
                    track.firebaseKey,
                    track.startAt,
                    track.routeList,
                    track.distance,
                    track.duration
                )
                val childUpdates = mutableMapOf<String, Any>(
                    "/$uid/tracks/$key/" to firebaseTrack
                )
                database.updateChildren(childUpdates)
                track.firebaseKey = key
                recordTrackProvider.recordFirebaseKeyAsync(
                    db,
                    track.firebaseKey!!,
                    trackId
                )
            }
            callback()
        }
    }

    @SuppressLint("MissingPermission")
    private fun hasConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}