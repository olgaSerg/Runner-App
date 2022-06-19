package com.example.runnerapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.runnerapp.*
import com.example.runnerapp.fragments.running.RunningInProgressFragment
import com.example.runnerapp.fragments.running.RunningStartFragment
import com.example.runnerapp.fragments.running.RunningResultFragment
import com.google.android.gms.location.LocationServices

const val FASTEST_INTERVAL: Long = 2000
const val START_SCREEN = "start"
const val FINISH_SCREEN = "finish"
const val RESULT_SCREEN = "result"

class RunningActivity : AppCompatActivity(), RunningStartFragment.OnStartButtonClick,
    RunningInProgressFragment.OnButtonFinishClick, RunningInProgressFragment.OnErrorDialogClick {

    private var state = State()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, RunningStartFragment())
                .commit()
            state.fragment = START_SCREEN
        } else {
            state = savedInstanceState.getSerializable(STATE) as State
            loadFragments(state)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(STATE, state)
    }

    private fun loadFragments(state: State) {
        when (state.fragment) {
            START_SCREEN -> loadStartRunningFragment()
            FINISH_SCREEN -> loadRunningInProgressFragment()
            RESULT_SCREEN -> loadResultRunningFragment()
        }
    }

    private fun loadStartRunningFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.card_flip_right_in,
                R.anim.card_flip_right_out,
                R.anim.card_flip_left_in,
                R.anim.card_flip_left_out
            )
            .replace(R.id.fragment_container, RunningStartFragment())
            .commit()
    }

    private fun loadRunningInProgressFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.card_flip_right_in,
                R.anim.card_flip_right_out,
                R.anim.card_flip_left_in,
                R.anim.card_flip_left_out
            )
            .replace(R.id.fragment_container, RunningInProgressFragment.newInstance(state))
            .commit()
    }

    private fun loadResultRunningFragment() {
        if (state.runningTime != null && state.totalDistance != null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.card_flip_right_in,
                    R.anim.card_flip_right_out,
                    R.anim.card_flip_left_in,
                    R.anim.card_flip_left_out
                )
                .replace(
                    R.id.fragment_container,
                    RunningResultFragment.newInstance(
                        state.runningTime!!,
                        state.totalDistance!!
                    )
                )
                .commit()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartButtonClick() {
        state.fragment = FINISH_SCREEN
        loadFragments(state)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
    }

    override fun clickFinishButton(time: String, totalDistance: Double) {
        state.fragment = RESULT_SCREEN
        state.runningTime = time
        state.totalDistance = totalDistance
        loadFragments(state)
    }

    override fun onBackPressed() {
        if (state.fragment == FINISH_SCREEN) {
            Toast.makeText(this, getText(R.string.back_pressed_running_screen_toast), Toast.LENGTH_SHORT)
                .show()
        } else {
            startActivity(Intent(this, MainScreenActivity::class.java))
        }
    }

    override fun onErrorDialogClick() {
        startActivity(Intent(this, MainScreenActivity::class.java))
    }
}




