package com.example.runnerapp.models

import java.io.Serializable
import java.util.Date

class TrackModel(
    var id: Int? = null,
    var startTime: Date? = null,
    var distance: Int? = null,
    var duration: Long? = null
): Serializable