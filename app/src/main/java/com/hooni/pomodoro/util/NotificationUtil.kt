package com.hooni.pomodoro.util

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hooni.pomodoro.AppConstants
import com.hooni.pomodoro.MainActivity
import com.hooni.pomodoro.R
import com.hooni.pomodoro.TimerNotificationActionReceiver
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

class NotificationUtil {
    companion object {
        private const val CHANNEL_ID_TIMER = "menu_timer"
        private const val CHANNEL_NAME_TIMER = "Timer App Timer"
        private const val TIMER_ID = 0

        lateinit var nBuilder: NotificationCompat.Builder
        lateinit var nManager: NotificationManager

        var timerIsRunning = false

//        enum class CurrentCycleName(val cycle: String) {
//            firstCycle("1st Study"),
//            firstBreak("1st Break"),
//            secondCycle("2nd Study"),
//            secondBreak("2nd Break"),
//            thirdCycle("3rd Study"),
//            thirdBreak("3rd Break"),
//            fourthCycle("Last Study"),
//            LongBreak("Long Break")
//        }

        fun showTimerExpired(context: Context) {
            val startIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            startIntent.action = AppConstants.ACTION_START
            val startPendingIntent = PendingIntent.getBroadcast(context,0,startIntent,PendingIntent.FLAG_UPDATE_CURRENT)
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, false)
            nBuilder.setContentTitle("Timer Expired!")
                .setContentText("Start Again?")
                .setContentIntent(getPendingIntentWithStack(context,MainActivity::class.java))
                .addAction(android.R.drawable.ic_media_play,"Start",startPendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)
            nManager.notify(TIMER_ID,nBuilder.build())
        }

        @TargetApi(24)
        fun showTimerRunning(context: Context, wakeUpTime: Long, secondsRemaining: Long) {
            // stop button in notibar
            val stopIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            stopIntent.action = AppConstants.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context,0,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            // pause button in notibar
            val pauseIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            pauseIntent.action = AppConstants.ACTION_PAUSE
            val pausePendingIntent = PendingIntent.getBroadcast(context,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            // date format fuer die anzeige zeit, wann der timer endet
            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)


            nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, false)
            // TODO: content title should show what cycle it is (number needs to get replaced)
            nBuilder.setContentTitle("Timer Running! ${PrefUtil.getCurrentCycle(context)}")
                .setContentText("End: ${df.format(Date(wakeUpTime))}")
                .setContentIntent(getPendingIntentWithStack(context,MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_stop,"Stop",stopPendingIntent)
                .addAction(android.R.drawable.ic_media_pause,"Pause",pausePendingIntent)
            nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,false)
            nManager.notify(TIMER_ID,nBuilder.build())
//            updateTimerRunning(secondsRemaining)
        }

//        fun updateTimerRunning(remainingTime: Long){
//            val timer = Timer("schedule", true)
//
//            timer.scheduleAtFixedRate(1000, (remainingTime*1000)) {
//                nBuilder.setContentText("End: $remainingTime")
//                nManager.notify(TIMER_ID, nBuilder.build())
//            }
//        }

        fun showTimerPaused(context: Context) {
            val resumeIntent = Intent(context, TimerNotificationActionReceiver::class.java)
            resumeIntent.action = AppConstants.ACTION_RESUME
            val resumePendingIntent = PendingIntent.getBroadcast(context,0,resumeIntent,PendingIntent.FLAG_UPDATE_CURRENT)
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, false)
            nBuilder.setContentTitle("Timer is paused!")
                .setContentText("Resume?")
                .setContentIntent(getPendingIntentWithStack(context,MainActivity::class.java))
                .addAction(android.R.drawable.ic_media_play,"Resume",resumePendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,false)
            nManager.notify(TIMER_ID,nBuilder.build())
        }

        fun hideTimerNotification(context: Context) {
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)
            timerIsRunning = false
        }

        private fun getBasicNotificationBuilder(context: Context, channelId: String, playSound: Boolean): NotificationCompat.Builder {
            val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(true)
                .setDefaults(0)
            if (playSound) nBuilder.setSound(notificationSound)
            return nBuilder
        }

        private fun <T> getPendingIntentWithStack(context: Context, javaClass: Class<T>): PendingIntent {
            val resultIntent = Intent(context, javaClass)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntent)

            return stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun NotificationManager.createNotificationChannel(channelID: String, channelName: String, playSound: Boolean) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
                val nChannel = NotificationChannel(channelID, channelName, channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor = Color.BLUE
                this.createNotificationChannel(nChannel)
            }
        }
    }
}