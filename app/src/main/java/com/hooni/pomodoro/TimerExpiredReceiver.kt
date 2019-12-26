package com.hooni.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hooni.pomodoro.util.NotificationUtil
import com.hooni.pomodoro.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationUtil.showTimerExpired(context)
        // depending on if auto continue is open
        // a new alarm should be started or the alarm should be stopped

        if(PrefUtil.getAutoStart(context)) {
            // need to get the information which cycle is next, to know which time should be set
            // secondsRemaining retrieves now information, that is not correct
            // which cycle the app is in right now, should be passed back to mainactivity,
            // when user returns
            val secondsRemaining = PrefUtil.getSecondsRemaining(context)
            MainActivity.setAlarm(context,MainActivity.nowSeconds,secondsRemaining)
        } else {
            PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
            PrefUtil.setAlarmSetTime(0,context)
        }

    }
}
