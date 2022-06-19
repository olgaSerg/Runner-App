package com.example.runnerapp

import com.example.runnerapp.models.NotificationModel
import com.example.runnerapp.models.TrackModel
import java.io.Serializable
import java.util.*

// TODO: Split into separate states
class State(
    var fragment: String = "login",

    // auth
    var email: String = "",
    var firstName: String? = null,
    var lastName: String? = null,
    var password: String = "",
    var passwordConfirmation: String? = null,
    var isTaskRegistrationStarted: Boolean = false,
    var isTaskLoginStarted: Boolean = false,

    // notifications
    var notification: NotificationModel? = null,
    var isNewNotification: Boolean = false,

    // running
    var trackId: Int? = null,
    var timeStart: Date? = null,
    var runningTime: String? = null,
    var totalDistance: Double? = null,
    var isInitialSynchronizationDone: Boolean = false,
    var currentTrack: TrackModel? = null
) : Serializable
