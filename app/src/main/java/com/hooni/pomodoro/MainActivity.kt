package com.hooni.pomodoro

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.hooni.pomodoro.util.NotificationUtil
import com.hooni.pomodoro.util.PrefUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        val STANDARD_POMODORO_TIME: Long = 1500000 // 25 Minuts
        val STANDARD_POMODORO_BREAK: Long = 300000 // 5 Minutes
        val STANDARD_POMODORO_LONG_BREAK: Long = 1200000 // 20 Minutes

        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService((Context.ALARM_SERVICE)) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            val alarmManager = context.getSystemService((Context.ALARM_SERVICE)) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)
        }
        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState {
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolBar)
        initUI()
    }

    override fun onResume() {
        super.onResume()
        initTimer()

        removeAlarm(this)

        NotificationUtil.hideTimerNotification(this)
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(this, wakeUpTime )
        } else if (timerState == TimerState.Paused) {
            NotificationUtil.showTimerPaused(this)
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun initUI() {
        setTimer(STANDARD_POMODORO_TIME)
        uncheckBoxes()
        initButtons()
    }

    private fun initButtons() {
        restart.setOnClickListener {
            setTimer(STANDARD_POMODORO_TIME)
            onTimerFinished()
        }

        startStop.setOnClickListener {
            if (timerState == TimerState.Running) {
                timer.cancel()
                timerState = TimerState.Paused
                updateButtons()
            } else {
                startTimer()
                updateButtons()
            }
        }
    }

    private fun initTimer() {
        timerState = PrefUtil.getTimerState(this)

        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if(alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountDownUI()
    }

    private fun startTimer() {
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountDownUI()
            }
        }.start()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
    }

    private fun setTimer(time: Long) {
        val lengthInMinutes = time / 60000L
        timerLengthSeconds = (lengthInMinutes * 60L)
        // progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        uncheckBoxes()
        setNewTimerLength()
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountDownUI()
    }

    private fun uncheckBoxes() {
        pom1.isSelected = false
        pom2.isSelected = false
        pom3.isSelected = false
        pom4.isSelected = false
        breakPomodoro.isSelected = false
    }

    private fun updateCountDownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        timerDisplay.text = "$minutesUntilFinished:${
        if (secondsStr.length == 2) secondsStr
        else "0$secondsStr"}"
        // progress_countdown.progress =  (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons() {
        when (timerState) {
            TimerState.Running -> {
                startStop.setImageResource(android.R.drawable.ic_media_pause)
                restart.isEnabled = false
            }
            TimerState.Paused -> {
                startStop.setImageResource(android.R.drawable.ic_media_play)
                restart.isEnabled = true
            }
            TimerState.Stopped -> {
                startStop.setImageResource(android.R.drawable.ic_media_play)
                restart.isEnabled = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        Log.d("main","onCreateOptionsMenu")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("main","onOptionsItemSelected")
        return when(item.itemId) {
            R.id.menu_item_settings -> {
                Log.d("main","onOptionsItemSelected // menu selected")
                val intent = Intent(this,SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
