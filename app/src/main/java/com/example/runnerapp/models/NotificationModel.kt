package com.example.runnerapp.models

import java.io.Serializable

data class NotificationModel(
    var id: Int? = null,
    var notifyAt: Long? = null
) : Serializable
