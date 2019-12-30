package com.hooni.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.hooni.pomodoro.util.NotificationUtil
import com.hooni.pomodoro.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (PrefUtil.getAutoStart(context)) {
            PrefUtil.setCurrentCycle(context, (PrefUtil.getCurrentCycle(context) + 1))

            val lengthInMinutes =
                when (PrefUtil.getCurrentCycle(context)) {
                    0, 2, 4, 6 -> {
                        // study cycle
                        PrefUtil.getTimerLength(context)
                    }
                    1, 3, 5 -> {
                        // short break
                        PrefUtil.getShortBreakLength(context)
                    }
                    7 -> {
                        // long break
                        PrefUtil.getLongBreakLength(context)
                    }
                    else -> {
                        // error
                        PrefUtil.getTimerLength(context)
                    }
                }
            PrefUtil.setSecondsRemaining(lengthInMinutes * 60L, context)
            val secondsRemaining = PrefUtil.getSecondsRemaining(context)
            val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(context, wakeUpTime, secondsRemaining)
            MainActivity.playNotification(context)
            MainActivity.vibratePhone(context)
            Toast.makeText(context,"Next Cycle started: ${PrefUtil.getCurrentCycle(context)}",Toast.LENGTH_SHORT).show()
        } else {
            NotificationUtil.showTimerExpired(context)
            PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
            PrefUtil.setAlarmSetTime(0, context)
        }

    }
}
