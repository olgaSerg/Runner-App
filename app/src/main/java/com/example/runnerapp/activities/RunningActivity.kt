package com.example.runnerapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.runnerapp.*
import com.example.runnerapp.fragments.RunningFinishFragment
import com.example.runnerapp.fragments.RunningStartFragment
import com.example.runnerapp.fragments.ResultScreenFragment

const val UPDATE_INTERVAL = (10 * 1000).toLong()
const val FASTEST_INTERVAL: Long = 2000

class RunningActivity : AppCompatActivity(), RunningStartFragment.OnButtonStartClick,
    RunningFinishFragment.OnButtonFinishClick, RunningFinishFragment.OnErrorDialogClick {

    private var totalDistance = 0.0
    private var containerNumber = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, RunningStartFragment())
                .commit()
        }
        else flipCard()
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
                .replace(R.id.fragment_container, RunningFinishFragment())
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
                    containerNumber++
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

    override fun onBackPressed() {
        if (containerNumber == 2) {
            Toast.makeText(this, """Пробежка не завершена, нажмите "Финиш"""", Toast.LENGTH_SHORT)
                .show()
        } else {
            startActivity(Intent(this, MainScreenActivity::class.java))
        }
    }

    override fun onErrorDialogClick() {
        startActivity(Intent(this, MainScreenActivity::class.java))
    }
}




