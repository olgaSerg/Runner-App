package com.example.runnerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.RecordTrackProvider
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class RunningActivity : AppCompatActivity() {

    private var textViewTimer: TextView? = null
    private var buttonStart: Button? = null
    private var buttonFinish: Button? = null
    private var timerStarted = false
    private var time = 0.0
    private var serviceIntent: Intent? = null
    var currentDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        textViewTimer = findViewById(R.id.text_view_timer)
        buttonStart = findViewById(R.id.button_start)
        buttonFinish = findViewById(R.id.button_finish)


        val buttonStart = buttonStart ?: return
        val buttonFinish = buttonFinish ?: return
        val db = App.instance?.dBHelper?.writableDatabase ?: return


        buttonStart.setOnClickListener {
            buttonStart.isInvisible = true
            buttonFinish.isInvisible = false
            currentDate = Date()
            startTimer()
        }

        buttonFinish.setOnClickListener {
            stopTimer()
            buttonFinish.isInvisible = true
            val track = TrackModel()
            track.duration = time.toLong()
            track.startTime = currentDate

            track.distance = 1759
            val recordTrackProvider = RecordTrackProvider()
            recordTrackProvider.recordTrackExecute(db, track).onSuccess {
                Toast.makeText(this, "Трек записан", Toast.LENGTH_SHORT).show()
            }
        }

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }

    private fun startTimer() {
        serviceIntent?.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        timerStarted = true
    }

    private fun stopTimer() {
        stopService(serviceIntent)
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val textViewTimer = textViewTimer ?: return
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            textViewTimer.text = getTimeStringFromDouble(time)
        }
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d:%02d", hour, min, sec)

    override fun onBackPressed() {
        if (timerStarted) {
            Toast.makeText(applicationContext, "Нажмите на кнопку \"Финиш\"", Toast.LENGTH_SHORT).show()
        }
        else {
            super.onBackPressed()
        }
    }
}

