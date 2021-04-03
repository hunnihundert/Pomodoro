package com.hooni.pomodoro.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.Surface
import androidx.activity.compose.setContent
import androidx.activity.viewModels

class TimerActivity:ComponentActivity() {

    private val timerViewModel by viewModels<TimerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                TimerScreen(
                    timerViewModel.minutesRemaining,
                    timerViewModel.secondsRemaining,
                    timerViewModel.currentPomodoro,
                    timerViewModel.progress,
                    timerViewModel::pausePlay,
                    timerViewModel::onAutostart,
                    {/*TODO: openSettings */}
                )
            }
        }
    }
}