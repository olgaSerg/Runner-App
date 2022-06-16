package com.example.runnerapp.fragments

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.runnerapp.*
import com.example.runnerapp.R
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.example.runnerapp.providers.RecordTrackProvider
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class RunningFinishFragment : Fragment(R.layout.fragment_running_finish) {

    private var textViewTimer: TextView? = null
    private var buttonFinish: Button? = null
    private var buttonFinishClick: OnButtonFinishClick? = null
    private var time = 0.0
    private var routeList = arrayListOf<LatLng>()
    private var totalDistance = 0.0
    private var startTime: Date? = null
    private var serviceTimerIntent: Intent? = null
    private var serviceLocationIntent: Intent? = null
    private lateinit var database: DatabaseReference
    private var errorDialogClick: OnErrorDialogClick? = null


    interface OnButtonFinishClick {
        fun clickFinishButton(time: String, totalDistance: Double)
    }

    interface OnErrorDialogClick {
        fun onErrorDialogClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        buttonFinishClick = try {
            activity as OnButtonFinishClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnButtonFinishClick")
        }

        errorDialogClick = try {
            activity as OnErrorDialogClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnErrorDialogClick")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        serviceTimerIntent = Intent(context, TimerService::class.java)
        requireActivity().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        textViewTimer = view.findViewById(R.id.text_view_timer)
        buttonFinish = view.findViewById(R.id.button_finish)

        startTime = Date()

        startTimer()

        setButtonFinishClickListener()
    }

    private fun startTimer() {
        serviceTimerIntent?.putExtra(TimerService.TIME_EXTRA, time)
        requireActivity().startService(serviceTimerIntent)
        serviceLocationIntent = Intent(context, LocationService::class.java)
        requireActivity().registerReceiver(updateLocation, IntentFilter(LOCATION_UPDATE))
        serviceLocationIntent?.putExtra(ROUTE_LIST, routeList)
        requireActivity().startService(serviceLocationIntent)
    }

    private fun stopTimer() {
        requireActivity().stopService(serviceTimerIntent)
        requireActivity().unregisterReceiver(updateTime)
        requireActivity().stopService(serviceLocationIntent)
    }

    private fun setButtonFinishClickListener() {
        val buttonFinish = buttonFinish ?: return
        buttonFinish.setOnClickListener {

            stopTimer()

            buttonFinishClick?.clickFinishButton(getTimeStringFromDouble(time), totalDistance)
        }
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val textViewTimer = textViewTimer ?: return
            val currentTime = Date()
            if (startTime != null) {
                time = 1.0 * (currentTime.time - startTime!!.time)
                textViewTimer.text = getTimeStringFromDouble(time)
            }
        }
    }

    private val updateLocation: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            routeList = intent.getParcelableArrayListExtra<LatLng>(ROUTE_LIST) as ArrayList<LatLng>
            totalDistance = intent.getDoubleExtra(DISTANCE, 0.0)
            if (routeList.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setMessage("Трек не будет сохранен, так как маршрут отсутсвует")
                    .setPositiveButton(
                        "ок"
                    ) { dialog, id -> errorDialogClick?.onErrorDialogClick() }
                    .setCancelable(false)
                    .create()
                    .show()
                return
            }
            recordTrack()
        }
    }

    private fun recordTrack() {
        val track = TrackModel()
        track.duration = time.toLong() / 1000
        track.startTime = startTime
        track.routeList = routeList
        track.distance = totalDistance.toInt()
        val db = App.instance?.dBHelper?.writableDatabase ?: return

        val recordTrackProvider = RecordTrackProvider()
        recordTrackProvider.recordTrackExecute(db, track)
            .onSuccess {
                writeTracksToFirebase(db)
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
            .onSuccess { requireActivity().unregisterReceiver(updateLocation) }
    }
}
