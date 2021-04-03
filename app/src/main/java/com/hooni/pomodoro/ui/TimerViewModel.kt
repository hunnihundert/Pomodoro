package com.hooni.pomodoro.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TimerViewModel: ViewModel() {

    var minutesRemaining by mutableStateOf("")
        private set

    var secondsRemaining by mutableStateOf("")
        private set

    var currentPomodoro by mutableStateOf(0)
        private set

    var progress by mutableStateOf(1.0f)
        private set

    fun pausePlay(isRunning: Boolean) {
        if(isRunning) {
            // pause timer
        } else {
            // start timer
        }
    }

    fun onAutostart(isAutostart: Boolean) {

    }


}