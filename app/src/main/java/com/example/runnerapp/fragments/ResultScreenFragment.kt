package com.example.runnerapp.fragments
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.runnerapp.R

const val TRACK_DISTANCE = "track_distance"
const val TIME = "time"

class ResultScreenFragment : Fragment(R.layout.fragment_result_running_screen) {

    private var textViewDuration: TextView? = null
    private var textViewDistance: TextView? = null


    companion object {
        fun newInstance(time: String, trackDistance: Double): ResultScreenFragment {
            val args = Bundle()
            args.putDouble(TRACK_DISTANCE, trackDistance)
            args.putString(TIME, time)
            val resultScreenFragment = ResultScreenFragment()
            resultScreenFragment.arguments = args
            return resultScreenFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewDistance = view.findViewById(R.id.text_view_distance)
        textViewDuration = view.findViewById(R.id.text_view_duration)

        val textViewDistance = textViewDistance ?: return
        val textViewDuration = textViewDuration ?: return

        textViewDuration.text = arguments?.getString(TIME)
        textViewDistance.text = arguments?.getDouble(TRACK_DISTANCE).toString()
    }
}