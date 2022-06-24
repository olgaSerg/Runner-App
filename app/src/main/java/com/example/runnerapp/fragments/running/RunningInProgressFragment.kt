package com.example.runnerapp.fragments.running

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.runnerapp.*
import com.example.runnerapp.R
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.RecordTrackProvider
import com.google.android.gms.maps.model.LatLng
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import bolts.Task
import com.example.runnerapp.activities.STATE

class RunningInProgressFragment : Fragment(R.layout.fragment_running_in_progress) {

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
    private var currentTrack: TrackModel? = null
    private var isReceiverUnregistered = false

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
            currentTrack = TrackModel()
            startTimer()
        } else {
            startTime = state.timeStart!!
        }

        if (currentTrack == null) {
            currentTrack = TrackModel()
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
        requireActivity().unregisterReceiver(updateTime)
        isReceiverUnregistered = true
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
            val currentTrack = currentTrack ?: return@setOnClickListener
            if (currentTrack.routeList == null || currentTrack.routeList!!.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.error_saving_track))
                    .setPositiveButton(
                        "ок"
                    ) { dialog, id -> errorDialogClick?.onErrorDialogClick() }
                    .setCancelable(false)
                    .create()
                    .show()
                return@setOnClickListener
            }

            buttonFinishClick?.clickFinishButton(getTimeStringFromDouble(time), totalDistance)
            currentTrack.duration = time.toLong() / 1000

            recordTrackToLocalDb().onSuccess {
                val db = App.instance?.db ?: return@onSuccess
                val tracksSynchronizer =
                    TracksSynchronizer(db, this@RunningInProgressFragment.requireContext())
                tracksSynchronizer.synchronizeTracks { }
            }
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
            routeList = intent.getParcelableArrayListExtra<LatLng>(ROUTE_LIST) as ArrayList<LatLng>
            totalDistance = intent.getDoubleExtra(DISTANCE, 0.0)
            val currentTrack = currentTrack ?: return
            currentTrack.duration = time.toLong() / 1000
            currentTrack.startAt = startTime
            currentTrack.routeList = routeList
            currentTrack.distance = totalDistance.toInt()
        }
    }

    private fun recordTrackToLocalDb(): Task<TrackModel> {
        val db = App.instance?.dBHelper?.writableDatabase

        val recordTrackProvider = RecordTrackProvider()
        return recordTrackProvider.recordTrackAsync(db!!, currentTrack!!)
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
        if (!isReceiverUnregistered) {
            unregisterReceivers()
        }
    }
}
