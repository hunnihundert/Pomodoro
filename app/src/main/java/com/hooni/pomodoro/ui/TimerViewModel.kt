package com.hooni.pomodoro.ui

import android.content.Context
import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hooni.pomodoro.AppConstants.Companion.LONG_BREAK_INITIAL_VALUE_IN_MILLISECONDS
import com.hooni.pomodoro.AppConstants.Companion.SHORT_BREAK_INITIAL_VALUE_IN_MILLISECONDS
import com.hooni.pomodoro.AppConstants.Companion.TIMER_INITIAL_VALUE_IN_MILLISECONDS
import com.hooni.pomodoro.util.PrefUtil
import com.hooni.pomodoro.util.Util.TimerState
import java.util.*

class TimerViewModel: ViewModel() {

    var minutesRemaining by mutableStateOf("")
        private set

    var secondsRemaining by mutableStateOf("")
        private set

    var currentPomodoro by mutableStateOf(0)
        private set

    var progress by mutableStateOf(1.0f)
        private set


    var timerLengthMilliseconds by mutableStateOf(TIMER_INITIAL_VALUE_IN_MILLISECONDS)
        private set
    var timeLeftMilliseconds by mutableStateOf(TIMER_INITIAL_VALUE_IN_MILLISECONDS)
        private set
    private var pomodoroLengthMilliseconds = TIMER_INITIAL_VALUE_IN_MILLISECONDS
    private var shortBreakLengthMilliseconds = SHORT_BREAK_INITIAL_VALUE_IN_MILLISECONDS
    private var longBreakLengthMilliseconds = LONG_BREAK_INITIAL_VALUE_IN_MILLISECONDS

    private lateinit var timer: CountDownTimer

    var timerState by mutableStateOf(TimerState.Stopped)
    var isAutostart by mutableStateOf(true)

    fun pausePlay(_isRunning: TimerState) {
        timerState = _isRunning
        if(timerState == TimerState.Running) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun onAutostart(_isAutostart: Boolean) {
        isAutostart =_isAutostart
    }

    private fun pauseTimer() {
        timerState = TimerState.Paused
        timer.cancel()
    }

    private fun startTimer() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(timeLeftMilliseconds,1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftMilliseconds = millisUntilFinished
                updateSecondsAndMinutes()
                setProgress()
            }

            override fun onFinish() {
                onTimerFinished()
            }
        }.start()
    }

    fun initTimer(context: Context) {
        timerState = PrefUtil.getTimerState(context)
        currentPomodoro = PrefUtil.getCurrentCycle(context)
        initTimerLengthValues(context)
        initTimerValues(context)
    }

    private fun setTotalTimeMilliseconds(context: Context) {
        when(timerState) {
            TimerState.Stopped -> {
                setNewTimerLength()
            }
            else -> {
                setPreviousTimerLength(context)
            }
        }
    }

    private fun setNewTimerLength() {
        timerLengthMilliseconds = when(currentPomodoro) {
            1, 3, 5 -> {
                shortBreakLengthMilliseconds
            }
            7 -> {
                longBreakLengthMilliseconds
            }
             else -> {
                 pomodoroLengthMilliseconds
             }
        }
    }

    private fun initTimerLengthValues(context: Context) {
        pomodoroLengthMilliseconds = PrefUtil.getTimerLengthInMinutes(context) * 60L * 1000L
        shortBreakLengthMilliseconds = PrefUtil.getShortBreakLength(context) * 60L * 1000L
        longBreakLengthMilliseconds = PrefUtil.getLongBreakLength(context) * 60L * 1000L
    }

    private fun setPreviousTimerLength(context: Context) {
        timerLengthMilliseconds = PrefUtil.getPreviousTimerLengthSeconds(context)
    }

    private fun setTimeLeftMilliseconds(context: Context) {
        timeLeftMilliseconds = if(timerState != TimerState.Stopped) {
            PrefUtil.getMillisecondsRemaining(context)
        } else {
            timerLengthMilliseconds
        }
        updateSecondsAndMinutes()
    }

    private fun setAlarm(context: Context) {
        val alarmSetTime = PrefUtil.getAlarmSetTime(context)
        val currentTimeMilliSeconds = Calendar.getInstance().timeInMillis

        if (alarmSetTime > 0) {
            timeLeftMilliseconds -= currentTimeMilliSeconds - alarmSetTime

            if(timeLeftMilliseconds <= 0) {
                onTimerFinished()
            } else if(timerState == TimerState.Running) {
                startTimer()
            }
        }
    }

    private fun updateSecondsAndMinutes() {
        val currentTimeLeftInSeconds = timeLeftMilliseconds / 1000
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
        if(timerState != TimerState.Stopped) {
            updatePomodoroCycle()
            // play noti/vibration
        }
        timeLeftMilliseconds = 0
        progress = 0f
        updateSecondsAndMinutes()
        setProgress()

        setNewTimerLength()

        if(isAutostart && timerState == TimerState.Running) {
            timeLeftMilliseconds = timerLengthMilliseconds
            startTimer()
        } else {
            timerState = TimerState.Paused
        }
    }

    private fun updatePomodoroCycle() {
        if(currentPomodoro < 7) {
            currentPomodoro += 1
        } else {
            currentPomodoro = 0
        }
    }

    private fun setProgress() {
        progress = timeLeftMilliseconds.toFloat() / timerLengthMilliseconds.toFloat()
    }

    fun onResetTimer(context: Context) {
        timeLeftMilliseconds = timerLengthMilliseconds
        currentPomodoro = 0
        timerState = TimerState.Stopped
        progress = 1f
        initTimerValues(context)
    }

    private fun initTimerValues(context: Context) {
        setTotalTimeMilliseconds(context)
        setTimeLeftMilliseconds(context)
        setAlarm(context)
    }

    companion object {
        private const val TAG = "TimerViewModel"
    }

}