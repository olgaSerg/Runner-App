package com.example.runnerapp.fragments.running

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import bolts.Task
import com.example.runnerapp.App
import com.example.runnerapp.R
import com.example.runnerapp.State
import com.example.runnerapp.activities.STATE
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLngBounds

class SelectedTrackFragment : Fragment(R.layout.fragment_selected_track),
    GoogleMap.OnMyLocationButtonClickListener,
    OnMapReadyCallback {

    private var textViewDistance: TextView? = null
    private var textViewDuration: TextView? = null
    private var mMap: GoogleMap? = null
    private var selectedTrack: TrackModel? = null
    private var state: State? = null

    companion object {
        fun newInstance(state: State): SelectedTrackFragment {
            val args = Bundle()
            args.putSerializable(STATE, state)
            val selectedTrackFragment = SelectedTrackFragment()
            selectedTrackFragment.arguments = args
            return selectedTrackFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewDistance = view.findViewById(R.id.text_view_distance)
        textViewDuration = view.findViewById(R.id.text_view_running_time)

        val onMapReadyCallback = this
        getSelectedTrack(onMapReadyCallback)
    }

    private fun getSelectedTrack(onMapReadyCallback: OnMapReadyCallback) {
        state = arguments?.getSerializable(STATE) as State
        val db = App.instance?.db ?: return
        val tracksProvider = GetTracksProvider()
        val state = state ?: return
        val selectedTrackId = state.trackId
        if (selectedTrackId != null) {
            state.trackId = selectedTrackId
            tracksProvider.getTrackAsync(db, selectedTrackId).onSuccess({
                selectedTrack = it.result
            }, Task.BACKGROUND_EXECUTOR).onSuccess({
                val mapFragment: SupportMapFragment = childFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(onMapReadyCallback)
            }, Task.UI_THREAD_EXECUTOR)
        }
    }


    private fun displayTrack(track: TrackModel) {
        val textViewDistance = textViewDistance ?: return
        val textViewDuration = textViewDuration ?: return
        if (track.distance != null && track.duration != null) {
            val distance = track.distance!!
            val duration = timeToString(track.duration!!)
            textViewDistance.text = formatDistance(distance)
            textViewDuration.text = duration
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        mMap = map
        val mMap = mMap ?: return
        val routeList = selectedTrack?.routeList ?: return
        mMap.setOnMyLocationButtonClickListener(this)
        if (selectedTrack != null) {
            displayTrack(selectedTrack!!)
            mMap.addPolyline(
                PolylineOptions()
                    .clickable(true)
                    .color(R.color.track_color)
                    .addAll(
                        routeList
                    )
            )
        }

        val builder = LatLngBounds.Builder()
        for (latLng in routeList) {
            builder.include(latLng)
        }

        mMap.setOnMapLoadedCallback {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    builder.build(),
                    100
                )
            )
        }

        mMap.addMarker(
            MarkerOptions()
                .position(routeList.first())
                .title(getString(R.string.start))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        mMap.addMarker(
            MarkerOptions()
                .position(routeList.last())
                .title(getString(R.string.finish))
        )
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        val mMap = mMap ?: return
        mMap.isMyLocationEnabled = true
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    private fun timeToString(secs: Long): String {
        val hour = secs / 3600
        val min = secs / 60 % 60
        val sec = secs / 1 % 60
        return String.format("%02d:%02d:%02d", hour, min, sec)
    }

    private fun formatDistance(distance: Int): String {
        val result = distance.toDouble() / 1000
        return result.toString()
    }

    override fun onDestroyView() {
        textViewDistance = null
        textViewDuration = null
        selectedTrack = null
        state = null
        mMap?.clear()
        mMap = null
        super.onDestroyView()
    }
}
