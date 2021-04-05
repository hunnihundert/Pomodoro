package com.hooni.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.hooni.pomodoro.ui.notification.NotificationUtil
import com.hooni.pomodoro.util.PrefUtil
import com.hooni.pomodoro.util.Util
import com.hooni.pomodoro.util.Util.TimerState
import java.util.*

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationUtil = NotificationUtil(context)

        if (PrefUtil.getAutoStart(context)) {
            PrefUtil.setCurrentCycle((PrefUtil.getCurrentCycle(context) + 1), context)

            val lengthInMinutes =
                when (PrefUtil.getCurrentCycle(context)) {
                    0, 2, 4, 6 -> {
                        // study cycle
                        PrefUtil.getTimerLengthInMinutes(context)
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
                        PrefUtil.getTimerLengthInMinutes(context)
                    }
                }
            PrefUtil.setMillisecondsRemaining(lengthInMinutes * 60L * 1000L, context)
            val secondsRemaining = PrefUtil.getMillisecondsRemaining(context)
            val currentTimeInMilliseconds = Calendar.getInstance().timeInMillis
            val wakeUpTime = Util.setAlarm(context, currentTimeInMilliseconds, secondsRemaining)
            notificationUtil.showTimerRunning(wakeUpTime, secondsRemaining)
            Util.playNotification(context)
            Util.vibratePhone(context)
            Toast.makeText(context,
                Util.returnCurrentCycle(PrefUtil.getCurrentCycle(context)),Toast.LENGTH_SHORT).show()
        } else {
            notificationUtil.showTimerExpired()
            PrefUtil.setTimerState(TimerState.Stopped, context)
            PrefUtil.setAlarmSetTime(0, context)
        }

    }
}
