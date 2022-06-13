package com.example.runnerapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.runnerapp.R
import com.example.runnerapp.fragments.ButtonFinishFragment
import com.example.runnerapp.fragments.ButtonStartFragment
import com.example.runnerapp.fragments.ResultScreenFragment

const val UPDATE_INTERVAL = (10 * 1000).toLong()
const val FASTEST_INTERVAL: Long = 2000

class RunningActivity : AppCompatActivity(), ButtonStartFragment.OnButtonStartClick,
    ButtonFinishFragment.OnButtonFinishClick {

    private var totalDistance = 0.0
    private var containerNumber = 1

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
}



