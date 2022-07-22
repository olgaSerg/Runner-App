package com.example.runnerapp.fragments.running
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.runnerapp.R

const val TRACK_DISTANCE = "track_distance"
const val TIME = "time"

class RunningResultFragment : Fragment(R.layout.fragment_result_running_screen) {

    private var textViewDuration: TextView? = null
    private var textViewDistance: TextView? = null

    companion object {
        fun newInstance(time: String, trackDistance: Double): RunningResultFragment {
            val args = Bundle()
            args.putDouble(TRACK_DISTANCE, trackDistance)
            args.putString(TIME, time)
            val resultScreenFragment = RunningResultFragment()
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

        if (arguments != null) {
            textViewDuration.text = requireArguments().getString(TIME)
            val distance = requireArguments().getDouble(TRACK_DISTANCE).toInt() / 1000.0
            textViewDistance.text = distance.toString()
        }
    }

    override fun onDestroyView() {
        textViewDuration = null
        textViewDistance = null
        super.onDestroyView()
    }
}