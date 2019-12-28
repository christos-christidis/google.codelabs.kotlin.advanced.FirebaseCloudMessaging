package com.kotlin.firebasenotifications.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.kotlin.firebasenotifications.BuildConfig
import com.kotlin.firebasenotifications.receiver.AlarmReceiver
import com.kotlin.firebasenotifications.R
import com.kotlin.firebasenotifications.util.cancelNotifications
import kotlinx.coroutines.*

class EggTimerViewModel(private val app: Application) : AndroidViewModel(app) {

    private val _requestCode = 0
    private val _triggerTime = "TRIGGER_AT"

    private val _minuteInMs: Long = 60_000L
    private val _secondInMs: Long = 1_000L

    private val _timerLengthOptions = app.resources.getIntArray(R.array.minutes_array)

    private val _alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var _prefs = app.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    private val _notifyIntent = Intent(app, AlarmReceiver::class.java)
    private val _notifyPendingIntent: PendingIntent

    private val _timeSelection = MutableLiveData<Int>()
    val timeSelection: LiveData<Int>
        get() = _timeSelection

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime

    private var _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean>
        get() = _alarmOn

    private lateinit var _timer: CountDownTimer

    init {
        _alarmOn.value = getPendingIntent(PendingIntent.FLAG_NO_CREATE) != null

        _notifyPendingIntent = getPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT)

        // If alarm is on, resume the timer back for this alarm
        if (_alarmOn.value!!) {
            createAndStartTimer()
        }
    }

    private fun getPendingIntent(flag: Int) =
        PendingIntent.getBroadcast(getApplication(), _requestCode, _notifyIntent, flag)

    fun setAlarm(isChecked: Boolean) {
        when (isChecked) {
            true -> timeSelection.value?.let { setAlarmAndStartTimer(it) }
            false -> cancelNotification()
        }
    }

    // Sets the desired interval for the alarm
    fun setTimeSelected(timerLengthSelection: Int) {
        _timeSelection.value = timerLengthSelection
    }

    // Creates a new alarm, notification and timer
    private fun setAlarmAndStartTimer(timerLengthSelection: Int) {
        _alarmOn.value?.let {
            if (!it) {
                _alarmOn.value = true
                val selectedInterval = when (timerLengthSelection) {
                    0 -> _secondInMs * 10 // For testing only
                    else -> _timerLengthOptions[timerLengthSelection] * _minuteInMs
                }
                val triggerTime = SystemClock.elapsedRealtime() + selectedInterval

                val notificationManager = ContextCompat.getSystemService(
                    app, NotificationManager::class.java
                ) as NotificationManager
                notificationManager.cancelNotifications()

                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    _alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    _notifyPendingIntent
                )

                viewModelScope.launch {
                    saveTime(triggerTime)
                }
            }
        }
        createAndStartTimer()
    }

    private fun createAndStartTimer() {
        viewModelScope.launch {
            val triggerTime = loadTime()
            _timer = object : CountDownTimer(triggerTime, _secondInMs) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = triggerTime - SystemClock.elapsedRealtime()
                    if (_elapsedTime.value!! <= 0) {
                        resetTimer()
                    }
                }

                override fun onFinish() {
                    resetTimer()
                }
            }
            _timer.start()
        }
    }

    // Cancels the alarm, notification and resets the timer
    private fun cancelNotification() {
        resetTimer()
        _alarmManager.cancel(_notifyPendingIntent)
    }

    private fun resetTimer() {
        _timer.cancel()
        _elapsedTime.value = 0
        _alarmOn.value = false
    }

    private suspend fun saveTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            _prefs.edit().putLong(_triggerTime, triggerTime).apply()
        }

    private suspend fun loadTime(): Long =
        withContext(Dispatchers.IO) {
            _prefs.getLong(_triggerTime, 0)
        }
}