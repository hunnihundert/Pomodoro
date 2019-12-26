package com.hooni.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hooni.pomodoro.util.NotificationUtil
import com.hooni.pomodoro.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // depending on if auto continue is open
        // a new alarm should be started or the alarm should be stopped

        if (PrefUtil.getAutoStart(context)) {
            // need to get the information which cycle is next, to know which time should be set
            // secondsRemaining retrieves now information, that is not correct
            // which cycle the app is in right now, should be passed back to mainactivity,
            // when user returns

            // retrieve the next pomodoro cycle
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

        } else {
            NotificationUtil.showTimerExpired(context)
            PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
            PrefUtil.setAlarmSetTime(0, context)
        }

    }
}
