package com.hooni.pomodoro.ui

import android.os.CountDownTimer
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


    private var totalTimeInMilliSeconds = 10000L
    private var currentTimeInMilliSeconds = 10000L

    private lateinit var timer: CountDownTimer

    var isRunning by mutableStateOf(false)
    var isAutostart by mutableStateOf(true)

    fun pausePlay(_isRunning: Boolean) {
        isRunning = _isRunning
        if(isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun onAutostart(_isAutostart: Boolean) {
        isAutostart =_isAutostart
        if(!isRunning) {
            resetTimer()
        }
    }

    private fun pauseTimer() {
        isRunning = false
        timer.cancel()
    }

    private fun startTimer() {
        isRunning = true
        timer = object : CountDownTimer(currentTimeInMilliSeconds,1000) {
            override fun onTick(millisUntilFinished: Long) {
                currentTimeInMilliSeconds = millisUntilFinished
                updateSecondsAndMinutes()
                setProgress()
            }

            override fun onFinish() {
                onTimerFinished()
            }
        }.start()
    }

    private fun updateSecondsAndMinutes() {
        val currentTimeLeftInSeconds = currentTimeInMilliSeconds / 1000
        val currentSecondsLeft = currentTimeLeftInSeconds % 60
        val currentMinutesLeft = (currentTimeLeftInSeconds - currentSecondsLeft) / 60

        val secondsRemainingString = currentSecondsLeft.toString()

        secondsRemaining = if(secondsRemainingString.length == 1) {
            "0$secondsRemainingString"
        } else {
            secondsRemainingString
        }
        minutesRemaining = currentMinutesLeft.toString()
    }

    private fun onTimerFinished() {
        currentTimeInMilliSeconds = 0
        progress = 0f
        updateSecondsAndMinutes()
        setProgress()
        isRunning = false
    }

    private fun setProgress() {
        progress = currentTimeInMilliSeconds.toFloat() / totalTimeInMilliSeconds.toFloat()
    }

    private fun resetTimer() {
        totalTimeInMilliSeconds = 10000L
        currentTimeInMilliSeconds = 10000L
        updateSecondsAndMinutes()
        setProgress()
    }

    companion object {
        private const val TAG = "TimerViewModel"
    }

}