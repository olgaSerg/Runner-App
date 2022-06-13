package com.example.runnerapp.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.TimerService
import com.example.runnerapp.activities.FASTEST_INTERVAL
import com.example.runnerapp.activities.UPDATE_INTERVAL
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.example.runnerapp.providers.RecordTrackProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.math.roundToInt

class ButtonFinishFragment : Fragment(R.layout.fragment_button_finish) {

    private var textViewTimer: TextView? = null
    private var buttonFinish: Button? = null
    private var buttonFinishClick: OnButtonFinishClick? = null
    private var time = 0.0
    private var timerStarted = false
    private var routeList = arrayListOf<LatLng>()
    private var totalDistance = 0.0
    private var startTime: Date? = null
    private var serviceIntent: Intent? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var database: DatabaseReference

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

    interface OnButtonFinishClick {
        fun clickFinishButton(time: String, totalDistance: Double)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        buttonFinishClick = try {
            activity as OnButtonFinishClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnButtonFinishClick")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        serviceIntent = Intent(context, TimerService::class.java)
        requireActivity().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        textViewTimer = view.findViewById(R.id.text_view_timer)
        buttonFinish = view.findViewById(R.id.button_finish)


        startTime = Date()
        createLocationRequest()
        startTimer()

        setButtonFinishClickListener()
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()
        if (mLocationRequest != null) {
            mLocationRequest!!.interval = UPDATE_INTERVAL
            mLocationRequest!!.fastestInterval = FASTEST_INTERVAL
            mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest!!,
                mLocationCallback,
                Looper.myLooper()!!
            )
        }
    }

    private fun startTimer() {
        serviceIntent?.putExtra(TimerService.TIME_EXTRA, time)
        requireActivity().startService(serviceIntent)
        timerStarted = true
    }

    private fun stopTimer() {
        requireActivity().stopService(serviceIntent)
        timerStarted = false
    }

    private fun setButtonFinishClickListener() {
        val buttonFinish = buttonFinish ?: return
        buttonFinish.setOnClickListener {

            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            stopTimer()
            val track = TrackModel()
            track.duration = time.toLong() / 1000
            track.startTime = startTime
            track.routeList = routeList
            track.distance = totalDistance.toInt(

            )
            val db = App.instance?.dBHelper?.writableDatabase ?: return@setOnClickListener

            val recordTrackProvider = RecordTrackProvider()
            recordTrackProvider.recordTrackExecute(db, track)
                .onSuccess {
                    writeTracksToFirebase(db)
                }

            buttonFinishClick?.clickFinishButton(getTimeStringFromDouble(time), totalDistance)
        }
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!timerStarted) return
            val textViewTimer = textViewTimer ?: return
            val currentTime = Date()
            if (startTime != null) {
                time = 1.0 * (currentTime.time - startTime!!.time)
                textViewTimer.text = getTimeStringFromDouble(time)
            }
        }
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt() / 1000
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60
        val millis = time.roundToInt() % 1000

        return makeTimeString(hours, minutes, seconds, millis)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int, millis: Int): String =
        String.format("%02d:%02d:%02d.%03d", hour, min, sec, millis)

    private fun writeTracksToFirebase(db: SQLiteDatabase) {
        val getTracksProvider = GetTracksProvider()
        var tracks: ArrayList<TrackModel>? = null
        val uid = Firebase.auth.uid ?: return
        database = Firebase.database.reference
        val recordTrackProvider = RecordTrackProvider()

        getTracksProvider.getTracksAsync(db).onSuccess { tracks = it.result }.onSuccess {
            if (tracks != null) {
                for (track in tracks!!) {
                    if (track.firebaseKey == null) {
                        track.routeList = arrayListOf(
                            LatLng(50.34, 23.43),
                            LatLng(50.87, 23.67),
                            LatLng(51.00, 23.20)
                        )
                        val key = database.child("track").push().key
                        val firebaseTrack = TrackModel(
                            null,
                            null,
                            track.startTime,
                            track.routeList,
                            track.distance,
                            track.duration
                        )
                        val childUpdates = mutableMapOf<String, Any>(
                            "/$uid/tracks/$key/" to firebaseTrack
                        )
                        database.updateChildren(childUpdates)
                        track.firebaseKey = key
                        if (key != null && track.id != null) {
                            recordTrackProvider.recordFirebaseKeyAsync(db, key, track.id!!)
                        }
                    }
                }
            }
        }
    }
}
