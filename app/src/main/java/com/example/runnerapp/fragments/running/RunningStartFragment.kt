package com.example.runnerapp.fragments.running

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.example.runnerapp.R

class RunningStartFragment : Fragment(R.layout.fragment_running_start) {

    private var startButtonClick: OnStartButtonClick? = null

    interface OnStartButtonClick {
        fun onStartButtonClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startButtonClick = try {
            activity as OnStartButtonClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnStartButtonClick")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val button: Button = view.findViewById(R.id.button_start)
        button.setOnClickListener {
            startButtonClick?.onStartButtonClick()
        }
    }
}