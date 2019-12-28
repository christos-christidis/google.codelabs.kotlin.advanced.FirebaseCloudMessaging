package com.kotlin.cloudmessaging.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.kotlin.cloudmessaging.R
import com.kotlin.cloudmessaging.databinding.FragmentEggTimerBinding

class EggTimerFragment : Fragment() {

    private val _topic = "breakfast"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentEggTimerBinding.inflate(inflater)

        val viewModel = ViewModelProvider(this).get(EggTimerViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        createChannel(
            getString(R.string.egg_notification_channel_id),
            getString(R.string.egg_notification_channel_name)
        )

        // SOS: create another channel for the Firebase notifications
        createChannel(
            getString(R.string.breakfast_notification_channel_id),
            getString(R.string.breakfast_notification_channel_name)
        )

        subscribeToTopic()

        return binding.root
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.apply {
                setShowBadge(false)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = getString(R.string.breakfast_notification_channel_description)
            }

            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(_topic)
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful)
                    getString(R.string.message_subscribed)
                else
                    getString(R.string.message_subscribe_failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance() = EggTimerFragment()
    }
}
