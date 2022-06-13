package com.example.runnerapp.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.TimerService
import com.example.runnerapp.fragments.ButtonFinishFragment
import com.example.runnerapp.fragments.ButtonStartFragment
import com.example.runnerapp.fragments.ResultScreenFragment
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.example.runnerapp.providers.RecordTrackProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

const val UPDATE_INTERVAL = (10 * 1000).toLong()
const val FASTEST_INTERVAL: Long = 2000

class RunningActivity : AppCompatActivity(), ButtonStartFragment.OnButtonStartClick, ButtonFinishFragment.OnButtonFinishClick {

    private var textViewTimer: TextView? = null
    private var buttonStart: Button? = null
    private var buttonFinish: Button? = null
    private var timerStarted = false
    private var time = 0.0
    private var serviceIntent: Intent? = null
    private var startTime: Date? = null
    private var routeList = arrayListOf<LatLng>()
    private var totalDistance = 0.0
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var database: DatabaseReference
    private var containerNumber = 1

    private var mLocationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                if (routeList.isEmpty()) {
                    val newLocation = LatLng(location.latitude, location.longitude)
                    routeList.add(newLocation)
                }
                if (routeList.isNotEmpty()) {
                    val lastLocation = routeList[routeList.lastIndex]
                    val newLocation = LatLng(location.latitude, location.longitude)
                    if (lastLocation != newLocation) {
                        val result: FloatArray = floatArrayOf(0.0F)
                        Location.distanceBetween(
                            lastLocation.latitude,
                            lastLocation.longitude,
                            newLocation.latitude,
                            newLocation.longitude,
                            result
                        )
                        if (result[0] >= 5) {
                            routeList.add(newLocation)
                            totalDistance += result[0]
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, ButtonStartFragment())
                .commit()
        }


    }

    private fun flipCard(time: String? = null, trackDistance: Double? = null) {
        if (containerNumber == 1) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.card_flip_right_in,
                    R.anim.card_flip_right_out,
                    R.anim.card_flip_left_in,
                    R.anim.card_flip_left_out
                )
                .replace(R.id.fragment_container, ButtonFinishFragment())
                .commit()
            containerNumber++
        } else {
            if (time != null && trackDistance != null) {
                if (containerNumber == 2) {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.card_flip_right_in,
                            R.anim.card_flip_right_out,
                            R.anim.card_flip_left_in,
                            R.anim.card_flip_left_out
                        )

                        .replace(
                            R.id.fragment_container,
                            ResultScreenFragment.newInstance(time, totalDistance)
                        )
                        .commit()
                }
            }
        }
    }

    override fun clickButtonStart() {
        containerNumber = 1
        flipCard()
    }

    override fun clickFinishButton(time: String, totalDistance: Double) {
        containerNumber = 2
        flipCard(time, totalDistance)

    }

//    @SuppressLint("MissingPermission")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_running)
//
//        textViewTimer = findViewById(R.id.text_view_timer)
//        buttonStart = findViewById(R.id.button_start)
//        buttonFinish = findViewById(R.id.button_finish)
//
//        val buttonFinish = buttonFinish ?: return
//        val db = App.instance?.dBHelper?.writableDatabase ?: return
//
//        setButtonStartListener(buttonFinish)
//        setButtonFinishListener(buttonFinish, db)
//
//        serviceIntent = Intent(applicationContext, TimerService::class.java)
//        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun setButtonStartListener(buttonFinish: Button) {
//        val buttonStart = buttonStart ?: return
//        buttonStart.setOnClickListener {
//            buttonStart.isInvisible = true
//            buttonFinish.isInvisible = false
//            startTime = Date()
//            mLocationRequest = LocationRequest.create()
//            if (mLocationRequest != null) {
//                mLocationRequest!!.interval = UPDATE_INTERVAL
//                mLocationRequest!!.fastestInterval = FASTEST_INTERVAL
//                mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
//                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//                mFusedLocationClient?.requestLocationUpdates(
//                    mLocationRequest!!,
//                    mLocationCallback,
//                    Looper.myLooper()!!
//                )
//                startTimer()
//            }
//        }
//    }
//
//    private fun startTimer() {
//        serviceIntent?.putExtra(TimerService.TIME_EXTRA, time)
//        startService(serviceIntent)
//        timerStarted = true
//    }
//
//    private fun setButtonFinishListener(buttonFinish: Button, db: SQLiteDatabase) {
//        buttonFinish.setOnClickListener {
//            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
//            stopTimer()
//            buttonFinish.isInvisible = true
//            val track = TrackModel()
//            track.duration = time.toLong() / 1000
//            track.startTime = startTime
//            track.routeList = routeList
//            track.distance = totalDistance.toInt()
//
//            val recordTrackProvider = RecordTrackProvider()
//            recordTrackProvider.recordTrackExecute(db, track)
//                .onSuccess {
//                    writeTracksToFirebase(db)
//                }
//        }
//    }
//
//    private fun stopTimer() {
//        stopService(serviceIntent)
//        timerStarted = false
//    }
//

//

//
//    override fun onBackPressed() {
//        if (timerStarted) {
//            Toast.makeText(applicationContext, "Нажмите кнопку \"Финиш\"", Toast.LENGTH_SHORT)
//                .show()
//        } else {
//            super.onBackPressed()
//        }
//    }
//
//    private fun writeTracksToFirebase(db: SQLiteDatabase) {
//        val getTracksProvider = GetTracksProvider()
//        var tracks: ArrayList<TrackModel>? = null
//        val uid = Firebase.auth.uid ?: return
//        database = Firebase.database.reference
//        val recordTrackProvider = RecordTrackProvider()
//
//        getTracksProvider.getTracksAsync(db).onSuccess { tracks = it.result }.onSuccess {
//            if (tracks != null) {
//                for (track in tracks!!) {
//                    if (track.firebaseKey == null) {
//                        track.routeList = arrayListOf(
//                            LatLng(50.34, 23.43),
//                            LatLng(50.87, 23.67),
//                            LatLng(51.00, 23.20)
//                        )
//                        val key = database.child("track").push().key
//                        val firebaseTrack = TrackModel(
//                            null,
//                            null,
//                            track.startTime,
//                            track.routeList,
//                            track.distance,
//                            track.duration
//                        )
//                        val childUpdates = mutableMapOf<String, Any>(
//                            "/$uid/tracks/$key/" to firebaseTrack
//                        )
//                        database.updateChildren(childUpdates)
//                        track.firebaseKey = key
//                        if (key != null && track.id != null) {
//                            recordTrackProvider.recordFirebaseKeyAsync(db, key, track.id!!)
//                        }
//                    }
//                }
//            }
//        }
//    }
}


