package com.kotlin.firebasenotifications.util

import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter

// Converts milliseconds to formatted mm:ss
@BindingAdapter("elapsedTime")
fun TextView.setElapsedTime(value: Long) {
    val seconds = value / 1000
    text = if (seconds < 60) seconds.toString() else DateUtils.formatElapsedTime(seconds)
}
