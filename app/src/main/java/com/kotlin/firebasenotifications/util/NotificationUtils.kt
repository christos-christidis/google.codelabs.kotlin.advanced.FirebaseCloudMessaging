package com.kotlin.firebasenotifications.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.kotlin.firebasenotifications.MainActivity
import com.kotlin.firebasenotifications.R
import com.kotlin.firebasenotifications.receiver.SnoozeReceiver

private const val NOTIFICATION_ID = 0
private const val REQUEST_CODE = 0
private const val FLAGS = 0

fun NotificationManager.sendNotification(messageBody: String, appContext: Context) {
    val contentIntent = Intent(appContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        appContext, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    val eggImage = BitmapFactory.decodeResource(appContext.resources, R.drawable.cooked_egg)
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(eggImage)
        .bigLargeIcon(null)

    val snoozeIntent = Intent(appContext, SnoozeReceiver::class.java)
    val snoozePendingIntent = PendingIntent.getBroadcast(
        appContext, REQUEST_CODE, snoozeIntent, FLAGS
    )

    val builder = NotificationCompat.Builder(
        appContext, appContext.getString(R.string.egg_notification_channel_id)
    )
        .setSmallIcon(R.drawable.cooked_egg)
        .setContentTitle(appContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setStyle(bigPicStyle)
        .setLargeIcon(eggImage)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(R.drawable.egg_icon, appContext.getString(R.string.snooze), snoozePendingIntent)

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}
