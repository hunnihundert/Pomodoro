package com.hooni.pomodoro.util

import android.content.Context
import android.preference.PreferenceManager
import com.hooni.pomodoro.MainActivity

class PrefUtil {

    companion object {
        private const val TIMER_LENGTH_ID = "com.hooni.pomodoro.timer_length"
        private const val SHORT_BREAK_LENGTH_ID = "com.hooni.pomodoro.short_break_length"
        private const val LONG_BREAK_LENGTH_ID = "com.hooni.pomodoro.long_break_length"
        private const val PLAY_SOUND_ID="com.hooni.pomodoro.sound"
        private const val VIBRATE_ID="com.hooni.pomodoro.vibration"

        fun getTimerLength(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(TIMER_LENGTH_ID,25)
        }

        fun getShortBreakLength(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(SHORT_BREAK_LENGTH_ID,5)
        }

        fun getLongBreakLength(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(LONG_BREAK_LENGTH_ID,20)
        }

        fun getPlaySound(context: Context): Boolean {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getBoolean(PLAY_SOUND_ID, true)
        }

        fun getVibrate(context: Context): Boolean {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getBoolean(VIBRATE_ID,true)
        }


        private const val AUTO_START_ID = "com.hooni.pomodoro.auto_start"

        fun getAutoStart(context: Context): Boolean {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getBoolean(AUTO_START_ID,true)
        }

        fun setAutoStart(context: Context, value: Boolean) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putBoolean(AUTO_START_ID,value)
            editor.apply()
        }

        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.hooni.pomodoro.previous_timer_length"

        fun getPreviousTimerLengthSeconds(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        fun setPreviousTimerLengthSeconds(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID = "com.hooni.pomodoro.timer_state"

        fun getTimerState(context: Context): MainActivity.TimerState {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return MainActivity.TimerState.values()[ordinal]
        }

        fun setTimerState(timerState: MainActivity.TimerState, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = timerState.ordinal
            editor.putInt(TIMER_STATE_ID,ordinal)
            editor.apply()
        }

        private const val SECONDS_REMAINING = "com.hooni.pomodoro.seconds_remaining"

        fun getSecondsRemaining(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING, seconds)
            editor.apply()
        }

        private const val ALARM_SET_TIME_ID = "com.hooni.pomodoro.backgrounded_time"

        fun getAlarmSetTime(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(ALARM_SET_TIME_ID,0)
        }

        fun setAlarmSetTime(time: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID,time)
            editor.apply()
        }

        private const val CURRENT_CYCLE = "com.hooni.pomodoro.current_cycle"

        fun setCurrentCycle(context: Context, currentCycle: Int) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(CURRENT_CYCLE,currentCycle)
            editor.apply()
        }

        fun getCurrentCycle(context: Context): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return if(preferences.getInt(CURRENT_CYCLE,0) >= 7) 0
            else preferences.getInt(CURRENT_CYCLE,0)
        }

    }
}