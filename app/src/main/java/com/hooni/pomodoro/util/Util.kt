package com.hooni.pomodoro.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.hooni.pomodoro.AppConstants
import com.hooni.pomodoro.TimerExpiredReceiver
import java.util.*

class Util {
    companion object {
        fun setAlarm(context: Context, currentTimeInMilliseconds: Long, millisecondsRemaining: Long): Long {
            val wakeUpTime = (currentTimeInMilliseconds + millisecondsRemaining)
            val alarmManager = context.getSystemService((Context.ALARM_SERVICE)) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(currentTimeInMilliseconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService((Context.ALARM_SERVICE)) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        fun playNotification(context: Context) {
            if (PrefUtil.getPlaySound(context)) {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val player = MediaPlayer.create(context, notification)
                player.start()
                player.release()
            }
        }

        fun vibratePhone(context: Context) {
            if (PrefUtil.getVibrate(context)) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) { //
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                500,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(500)
                    }
                }
            }
        }

        fun returnCurrentCycle(cycleNumber: Int): String {
            return when (cycleNumber) {
                0 -> AppConstants.FIRST_STUDY
                1 -> AppConstants.FIRST_BREAK
                2 -> AppConstants.SECOND_STUDY
                3 -> AppConstants.SECOND_BREAK
                4 -> AppConstants.THIRD_STUDY
                5 -> AppConstants.THIRD_BREAK
                6 -> AppConstants.FOURTH_STUDY
                7 -> AppConstants.FOURTH_BREAK
                else -> "Error"
            }
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000

    }

    enum class TimerState {
        Stopped, Paused, Running
    }
}