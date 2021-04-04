package com.hooni.pomodoro.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hooni.pomodoro.AppConstants
import com.hooni.pomodoro.util.PrefUtil
import com.hooni.pomodoro.util.Util
import com.hooni.pomodoro.util.Util.TimerState
import java.util.*

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationUtil = NotificationUtil(context)
        val currentTimeInMilliseconds = Calendar.getInstance().timeInMillis
        val value = when (intent.action) {
            AppConstants.ACTION_STOP -> {
                Util.removeAlarm(context)
                PrefUtil.setTimerState(TimerState.Stopped, context)
                notificationUtil.hideTimerNotification()
            }
            AppConstants.ACTION_PAUSE -> {
                var millisecondsRemaining = PrefUtil.getMillisecondsRemaining(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)

                millisecondsRemaining -= currentTimeInMilliseconds - alarmSetTime
                PrefUtil.setMillisecondsRemaining(millisecondsRemaining, context)

                Util.removeAlarm(context)
                PrefUtil.setTimerState(TimerState.Paused, context)
                notificationUtil.showTimerPaused()
            }

            AppConstants.ACTION_RESUME -> {
                val milliSecondsRemaining = PrefUtil.getMillisecondsRemaining(context)
                val wakeUpTime =
                    Util.setAlarm(context, currentTimeInMilliseconds, milliSecondsRemaining)
                PrefUtil.setTimerState(TimerState.Running, context)
                notificationUtil.showTimerRunning(wakeUpTime, milliSecondsRemaining)
            }

            else -> {
                //AppConstants.ACTION_START
                val minutesRemaining = PrefUtil.getTimerLengthInMinutes(context)
                val millisecondsRemaining = minutesRemaining * 60L * 1000
                val wakeUpTime =
                    Util.setAlarm(context, currentTimeInMilliseconds, millisecondsRemaining)
                PrefUtil.setTimerState(TimerState.Running, context)
                PrefUtil.setMillisecondsRemaining(millisecondsRemaining, context)
                notificationUtil.showTimerRunning(wakeUpTime, millisecondsRemaining)
            }
        }
    }
}
