package com.example.runnerapp.models

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.Date

class TrackModel(
    var id: Int? = null,
    var firebaseKey: String? = null,
    var startAt: Date? = null,
    var routeList: ArrayList<LatLng>? = null,
    var distance: Int? = null,
    var duration: Long? = null,
) : Serializable