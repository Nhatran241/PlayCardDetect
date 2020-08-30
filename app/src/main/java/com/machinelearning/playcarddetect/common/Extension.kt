package com.machinelearning.playcarddetect.common

import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.graphics.Color
import android.os.Build

@TargetApi(Build.VERSION_CODES.O)
@Synchronized
fun Service.createChannel(): String {
    val mNotificationManager = this.getSystemService(AccessibilityService.NOTIFICATION_SERVICE) as NotificationManager
    val name = "snap map fake location "
    val importance = NotificationManager.IMPORTANCE_LOW
    val mChannel = NotificationChannel("snap map channel", name, importance)
    mChannel.enableLights(true)
    mChannel.lightColor = Color.BLUE
    mNotificationManager.createNotificationChannel(mChannel)
    return "snap map channel"
}

fun Service.setNotification(){
    val channel: String
    channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel() else {
        ""
    }
    var notification: Notification? = null
    notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification.Builder(applicationContext, channel)
                .setContentTitle("Running")
                .build()
    } else {
        Notification.Builder(applicationContext)
                .setContentTitle("Running")
                .build()
    }
    startForeground(1, notification)
}
fun String.intOrString(): Any {
    val v = toIntOrNull()
    return when(v) {
        null -> this
        else -> v
    }
}
