package com.hooni.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hooni.pomodoro.util.NotificationUtil
import com.hooni.pomodoro.util.PrefUtil

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            AppConstants.ACTION_STOP -> {
                MainActivity_old.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity_old.TimerState.Stopped, context)
                NotificationUtil.hideTimerNotification(context)
            }

            AppConstants.ACTION_PAUSE -> {
                var secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)
                val nowSeconds = MainActivity_old.nowSeconds

                secondsRemaining -= nowSeconds - alarmSetTime
                PrefUtil.setSecondsRemaining(secondsRemaining, context)

                MainActivity_old.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity_old.TimerState.Paused,context)
                NotificationUtil.showTimerPaused(context)
            }

            AppConstants.ACTION_RESUME -> {
                val secondsRemaining = PrefUtil.getSecondsRemaining(context)
                val wakeUpTime = MainActivity_old.setAlarm(context, MainActivity_old.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(MainActivity_old.TimerState.Running, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime, secondsRemaining)
            }

            AppConstants.ACTION_START -> {
                val minutesRemaining = PrefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaining * 60L
                val wakeUpTime = MainActivity_old.setAlarm(context, MainActivity_old.nowSeconds, secondsRemaining)
                PrefUtil.setTimerState(MainActivity_old.TimerState.Running, context)
                PrefUtil.setSecondsRemaining(secondsRemaining,context)
                NotificationUtil.showTimerRunning(context, wakeUpTime, secondsRemaining)
            }
        }
    }
}
