package com.example.runnerapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.runnerapp.activities.FASTEST_INTERVAL
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

const val LOCATION_UPDATE = "location_update"
const val ROUTE_LIST = "route list"
const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
const val NOTIFICATION_CHANNEL_NAME = "Tracking"
const val NOTIFICATION_ID = 1

class LocationService : Service() {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var routeList = arrayListOf<LatLng>()
    private var totalDistance = 0.0

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        updateLocationTracking()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        startForegroundService()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking() {
//        if(TrackingUtility.hasLocationPermissions(this)) {
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        fusedLocationProviderClient?.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
//        } else {
//            fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
//        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            val locationList = result.locations
            if (locationList.isEmpty()) {
                return
            }
            val location = locationList.last()
            if (routeList.isEmpty()) {
                val newLocation = LatLng(location.latitude, location.longitude)
                routeList.add(newLocation)
                sendBroadcast()
            } else {
                val lastLocation = routeList.last()
                val newLocation = LatLng(location.latitude, location.longitude)
                    val resultDistance: FloatArray = floatArrayOf(0.0F)
                    Location.distanceBetween(
                        lastLocation.latitude,
                        lastLocation.longitude,
                        newLocation.latitude,
                        newLocation.longitude,
                        resultDistance
                    )
                if (resultDistance[0] >= 5) {
                    routeList.add(newLocation)
                    totalDistance += resultDistance[0]
                    sendBroadcast()
                }
            }
            Log.d(
                "NEW LOC",
                "${result.locations[0].latitude}, ${result.locations[0].longitude}"
            )
            Log.d("NEW DISTANCE", totalDistance.toString())
        }
    }

    private fun sendBroadcast() {
        val intent = Intent(LOCATION_UPDATE)
        intent.putParcelableArrayListExtra(ROUTE_LIST, routeList)
        intent.putExtra(DISTANCE, totalDistance)
        sendBroadcast(intent)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("Running App")
            .setContentText("00:00:00")

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        sendBroadcast()
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}