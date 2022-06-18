package com.example.runnerapp

import com.example.runnerapp.models.NotificationModel
import java.io.Serializable
import java.util.*

class State(
    var email: String = "",
    var firstName: String? = null,
    var lastName: String? = null,
    var password: String = "",
    var confirmPassword: String? = null,
    var isTaskRegistrationStarted: Boolean = false,
    var isTaskLoginStarted: Boolean = false,
    var fragment: String = "login",
    var trackId: Int? = null,
    var notification: NotificationModel? = null,
    var runningTime: String? = null,
    var totalDistance: Double? = null,
    var timeStart: Date? = null
) : Serializable