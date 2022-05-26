package com.example.runnerapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.runnerapp.PermissionUtils
import com.example.runnerapp.R
import com.example.runnerapp.models.TrackModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

const val TRACK = "track"
const val LOCATION_PERMISSION_REQUEST_CODE = 1

class SelectedTrackFragment : Fragment(R.layout.fragment_selected_track), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
    private var textViewDistance: TextView? = null
    private var textViewDuration: TextView? = null
    private lateinit var mMap: GoogleMap
    private var routeList = arrayListOf<LatLng>()
    private var totalDistance = 0.0

    private var mLocationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                if (routeList.isEmpty()) {
                    val newLocation = LatLng(location.latitude, location.longitude)
                    routeList.add(newLocation)
                }
                if (routeList.isNotEmpty()) {
                    val lastLocation = routeList[routeList.lastIndex]
                    val newLocation = LatLng(location.latitude, location.longitude)
                    if (lastLocation != newLocation) {
                        val result: FloatArray = floatArrayOf(0.0F)
                        Location.distanceBetween(
                            lastLocation.latitude,
                            lastLocation.longitude,
                            newLocation.latitude,
                            newLocation.longitude,
                            result
                        )
                        if (result[0] >= 5) {
                            routeList.add(newLocation)
                            totalDistance += result[0]
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(track: TrackModel): SelectedTrackFragment {
            val args = Bundle()
            args.putSerializable(TRACK, track)
            val selectedTrackFragment = SelectedTrackFragment()
            selectedTrackFragment.arguments = args
            return selectedTrackFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewDistance = view.findViewById(R.id.text_view_distance)
        textViewDuration = view.findViewById(R.id.text_view_running_time)

        val mapFragment: SupportMapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val selectedTrack = arguments?.getSerializable(TRACK) as TrackModel
        displayTrack(selectedTrack)
    }

    private fun displayTrack(selectedTrack: TrackModel) {
        val textViewDistance = textViewDistance ?: return
        val textViewDuration = textViewDuration ?: return
        textViewDistance.setText(selectedTrack.distance.toString())
        textViewDuration.setText(selectedTrack.duration.toString())
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        TODO("Not yet implemented")
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        mMap = map
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
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
        // [END maps_check_location_permission]
    }
}
