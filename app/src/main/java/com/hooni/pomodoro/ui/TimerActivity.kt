package com.hooni.pomodoro.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.hooni.pomodoro.SettingsActivity
import com.hooni.pomodoro.ui.notification.NotificationUtil
import com.hooni.pomodoro.ui.theme.PomodoroTheme
import com.hooni.pomodoro.util.PrefUtil
import com.hooni.pomodoro.util.Util
import com.hooni.pomodoro.util.Util.TimerState
import java.util.*

class TimerActivity:ComponentActivity() {

    private val timerViewModel by viewModels<TimerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PomodoroTheme {
                TimerScreen(
                    timerViewModel.minutesRemaining,
                    timerViewModel.secondsRemaining,
                    timerViewModel.currentPomodoro,
                    timerViewModel.progress,
                    timerViewModel::pausePlay,
                    timerViewModel::onAutostart,
                    { timerViewModel.onResetTimer(this) },
                    timerViewModel.timerState,
                    timerViewModel.isAutostart,
                    { openPreferences() }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val notificationUtil = NotificationUtil(applicationContext)
        val currentTimeInMilliseconds = Calendar.getInstance().timeInMillis
        if(timerViewModel.timerState == TimerState.Running) {
            val wakeUpTime = Util.setAlarm(applicationContext,currentTimeInMilliseconds,timerViewModel.timeLeftMilliseconds)
            notificationUtil.showTimerRunning(wakeUpTime, timerViewModel.timeLeftMilliseconds)
        } else {
            notificationUtil.showTimerPaused()
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerViewModel.timerLengthMilliseconds,this)
        PrefUtil.setMillisecondsRemaining(timerViewModel.timeLeftMilliseconds, this)
        PrefUtil.setTimerState(timerViewModel.timerState, this)
        PrefUtil.setCurrentCycle(timerViewModel.currentPomodoro, this)
    }

    override fun onStart() {
        super.onStart()
        val notificationUtil = NotificationUtil(applicationContext)
        timerViewModel.initTimer(this)
        Util.removeAlarm(this)
        notificationUtil.hideTimerNotification()
    }

    private fun openPreferences() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}