package com.kotlin.cloudmessaging

import android.app.NotificationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kotlin.cloudmessaging.util.sendNotification

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.i(TAG, "From: ${remoteMessage.from}")
        Log.i(TAG, "Message data payload: " + remoteMessage.data)

        // SOS: When the app is in the background, any notification appears automatically. When it's
        // in the background, this method is called and below we choose to show the notification anyway
        remoteMessage.notification?.let {
            Log.i(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body!!)
        }
    }

    // This InstanceID token is how the server knows which device it's talking to. Called automatically
    // when my app first runs if I've registered the Firebase service in the Manifest. Also may be
    // called if the previous token has been compromised.
    override fun onNewToken(token: String) {
        Log.i(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    // I may save this token to a back-end of my choice (either Firebase or not) if I want to send
    // individual messages to this client in the future
    private fun sendRegistrationToServer(token: String?) {
    }

    private fun sendNotification(messageBody: String) {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext, NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendNotification(messageBody, applicationContext)
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
