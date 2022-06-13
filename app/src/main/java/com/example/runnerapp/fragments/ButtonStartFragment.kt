package com.example.runnerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.example.runnerapp.R

class ButtonStartFragment : Fragment(R.layout.fragment_button_start) {

    private var buttonStartClick: OnButtonStartClick? = null

    interface OnButtonStartClick {
        fun clickButtonStart()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        buttonStartClick = try {
            activity as OnButtonStartClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnButtonStartClick")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val button: Button = view.findViewById(R.id.button_start)
        button.setOnClickListener {
            buttonStartClick?.clickButtonStart()
        }
    }
}