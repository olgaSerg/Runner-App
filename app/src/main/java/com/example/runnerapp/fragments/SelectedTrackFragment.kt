package com.example.runnerapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import bolts.Task
import com.example.runnerapp.App
import com.example.runnerapp.PermissionUtils
import com.example.runnerapp.R
import com.example.runnerapp.models.TrackModel
import com.example.runnerapp.providers.GetTracksProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLngBounds

const val TRACK_ID = "track_id"
const val LOCATION_PERMISSION_REQUEST_CODE = 1

class SelectedTrackFragment : Fragment(R.layout.fragment_selected_track),
    GoogleMap.OnMyLocationButtonClickListener,
    OnMapReadyCallback {

    private var textViewDistance: TextView? = null
    private var textViewDuration: TextView? = null
    private lateinit var mMap: GoogleMap
    private var selectedTrack: TrackModel? = null

    companion object {
        fun newInstance(trackId: Int): SelectedTrackFragment {
            val args = Bundle()
            args.putSerializable(TRACK_ID, trackId)
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

    private fun getSelectedTrack(onMapReadyCallback: SelectedTrackFragment) {
        val selectedTrackId: Int = arguments?.getSerializable(TRACK_ID) as Int
        val db = App.instance?.db ?: return
        val tracksProvider = GetTracksProvider()
        tracksProvider.getSelectedTrackAsync(db, selectedTrackId).onSuccess({
//            Thread.sleep(10000)
            selectedTrack = it.result
        }, Task.BACKGROUND_EXECUTOR).onSuccess({
            val mapFragment: SupportMapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(onMapReadyCallback)
        }, Task.UI_THREAD_EXECUTOR)
    }

    private fun displayTrack(track: TrackModel) {
        val textViewDistance = textViewDistance ?: return
        val textViewDuration = textViewDuration ?: return
        textViewDistance.text = track.distance.toString()
        textViewDuration.text = track.duration.toString()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.setOnMyLocationButtonClickListener(this)
        displayTrack(selectedTrack!!)
        mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .color(R.color.track_color)
                .addAll(
                    selectedTrack!!.routeList!!
                )
        )

        val builder = LatLngBounds.Builder()
        for (latLng in selectedTrack!!.routeList!!) {
            builder.include(latLng)
        }
        val bounds = builder.build()
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngBounds(bounds, 100)
        )

        mMap.addMarker(
            MarkerOptions()
                .position(selectedTrack!!.routeList!!.first())
                .title("Старт")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        mMap.addMarker(
            MarkerOptions()
                .position(selectedTrack!!.routeList!!.last())
                .title("Финиш")
        )
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(requireActivity().supportFragmentManager, "dialog")
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }


}
