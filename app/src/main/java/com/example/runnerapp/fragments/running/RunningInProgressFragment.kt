package com.example.runnerapp.fragments.running

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import bolts.Task
import com.example.runnerapp.activities.STATE

class RunningInProgressFragment : Fragment(R.layout.fragment_running_finish) {

    private var textViewTimer: TextView? = null
    private var buttonFinish: Button? = null
    private var buttonFinishClick: OnButtonFinishClick? = null
    private var time = 0.0
    private var routeList = arrayListOf<LatLng>()
    private var totalDistance = 0.0
    private var startTime: Date? = null
    private var serviceTimerIntent: Intent? = null
    private var serviceLocationIntent: Intent? = null
    private var errorDialogClick: OnErrorDialogClick? = null
    private var state: State? = null


    interface OnButtonFinishClick {
        fun clickFinishButton(time: String, totalDistance: Double)
    }

    interface OnErrorDialogClick {
        fun onErrorDialogClick()
    }

    companion object {
        fun newInstance(state: State): RunningInProgressFragment {
            val args = Bundle()
            args.putSerializable(STATE, state)
            val runningFinishFragment = RunningInProgressFragment()
            runningFinishFragment.arguments = args
            return runningFinishFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerReceivers()

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
        serviceLocationIntent = Intent(context, LocationService::class.java)

        textViewTimer = view.findViewById(R.id.text_view_timer)
        buttonFinish = view.findViewById(R.id.button_finish)

        state = arguments?.getSerializable(STATE) as State
        val state = state ?: return

        if (state.timeStart == null) {
            startTime = Date()
            state.timeStart = startTime
            state.currentTrack = TrackModel()
            startTimer()
        } else {
            startTime = state.timeStart!!
        }

        setButtonFinishClickListener()
    }

    private fun startTimer() {
        requireActivity().startService(serviceTimerIntent)
        serviceLocationIntent?.putExtra(ROUTE_LIST, routeList)
        requireActivity().startService(serviceLocationIntent)
    }

    private fun stopTimer() {
        requireActivity().stopService(serviceTimerIntent)
        requireActivity().stopService(serviceLocationIntent)
    }

    private fun registerReceivers() {
        requireActivity().registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
        requireActivity().registerReceiver(updateLocation, IntentFilter(LOCATION_UPDATE))
    }

    private fun unregisterReceivers() {
        if (updateTime != null) {
            requireActivity().unregisterReceiver(updateTime)
            updateTime = null
        }
        if (updateLocation != null) {
            requireActivity().unregisterReceiver(updateLocation)
            updateLocation = null
        }
    }

    private fun setButtonFinishClickListener() {
        val buttonFinish = buttonFinish ?: return
        buttonFinish.setOnClickListener {
            stopTimer()

            if (state!!.currentTrack!!.routeList!!.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setMessage("Трек не будет сохранен, так как маршрут отсутсвует")
                    .setPositiveButton(
                        "ок"
                    ) { dialog, id -> errorDialogClick?.onErrorDialogClick() }
                    .setCancelable(false)
                    .create()
                    .show()
                return@setOnClickListener
            }

            buttonFinishClick?.clickFinishButton(getTimeStringFromDouble(time), totalDistance)

            recordTrackToLocalDb().onSuccess {
                val db = App.instance?.db ?: return@onSuccess
                val tracksSynchronizer =
                    TracksSynchronizer(db, this@RunningInProgressFragment.requireContext())
                tracksSynchronizer.synchronizeTracks { }
            }

            Log.v("!!!!", "click finish")
//            buttonFinishClick?.clickFinishButton(getTimeStringFromDouble(time), totalDistance)
        }
    }

    private var updateTime: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val textViewTimer = textViewTimer ?: return
            val currentTime = Date()
            if (startTime != null) {
                time = 1.0 * (currentTime.time - startTime!!.time)
                textViewTimer.text = getTimeStringFromDouble(time)
            }
        }
    }

    private var updateLocation: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = state ?: return
            routeList = intent.getParcelableArrayListExtra<LatLng>(ROUTE_LIST) as ArrayList<LatLng>
            totalDistance = intent.getDoubleExtra(DISTANCE, 0.0)
            state.currentTrack!!.duration = time.toLong() / 1000
            state.currentTrack!!.startAt = startTime
            state.currentTrack!!.routeList = routeList
            state.currentTrack!!.distance = totalDistance.toInt()
        }
    }

    private fun recordTrackToLocalDb(): Task<TrackModel> {
        val db = App.instance?.dBHelper?.writableDatabase!!

        val recordTrackProvider = RecordTrackProvider()
        return recordTrackProvider.recordTrackAsync(db, state?.currentTrack!!)
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
        String.format("%02d:%02d:%02d,%02d", hour, min, sec, millis / 10)

    override fun onDestroy() {
        super.onDestroy()
        Log.v("!!!!onDestroy", "onDestroy")
        unregisterReceivers()
//        stopTimer()
    }
}
