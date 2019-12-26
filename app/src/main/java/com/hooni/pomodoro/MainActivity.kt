package com.hooni.pomodoro

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import com.hooni.pomodoro.util.NotificationUtil
import com.hooni.pomodoro.util.PrefUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val STANDARD_POMODORO_TIME: Long = 1500000 // 25 Minutes

        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService((Context.ALARM_SERVICE)) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            // passing additional information which cycle is next
            // intent.putExtra(CURRENT_CYCLE,)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService((Context.ALARM_SERVICE)) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState {
        Stopped, Paused, Running, PauseOnNext
    }

    // the actual timer which is running
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
        if (timerState == TimerState.PauseOnNext || timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(this, wakeUpTime, secondsRemaining)
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
            if (timerState == TimerState.Running) {
                // pause on end of cycle
                timerState = TimerState.PauseOnNext
                updateButtons()
                showStatusOnToast(it)
            } else if (timerState == TimerState.Paused || timerState == TimerState.Stopped) {
                // restart
                updateButtons()
                setTimer(timerLengthSeconds)
                uncheckBoxes()
                showStatusOnToast(it)
                // TODO: currently a workaround, the timer should be completely initialized and not
                // onTimerFinished called
                onTimerFinished()
                PrefUtil.setCurrentCycle(this,0)
            } else if (timerState == TimerState.PauseOnNext) {
                // do not pause on next
                timerState = TimerState.Running
                showStatusOnToast(it)
                updateButtons()
                // toast should show that it won't stop on next end of the cycle
            }

        }

        startStop.setOnClickListener {
            if (timerState == TimerState.Paused || timerState == TimerState.Stopped) {
                startTimer()
                updateButtons()
                showStatusOnToast(it)
            } else {
                timer.cancel()
                timerState = TimerState.Paused
                updateButtons()
                showStatusOnToast(it)
            }
        }
    }

    private fun showStatusOnToast(view: View) {
        when (timerState) {
            TimerState.Running -> {
                if (view == restart) Toast.makeText(this, "Timer will continue after this cycle", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Timer started", Toast.LENGTH_SHORT).show()
                }
            TimerState.Paused -> {
                if (view == restart) Toast.makeText(this, "Timer Reset", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Timer Paused", Toast.LENGTH_SHORT).show()
            }
            TimerState.PauseOnNext -> Toast.makeText(this,"Timer will pause after this cycle",Toast.LENGTH_SHORT).show()
            TimerState.Stopped -> Toast.makeText(this, "Timer stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initTimer() {

        timerState = PrefUtil.getTimerState(this)
        when(timerState) {
            TimerState.Stopped -> {
                on_break_text.text = getString(R.string.press_play)
                setNewTimerLength()
            }
            TimerState.Paused -> {
                on_break_text.text = getString(R.string.on_break)
                setPreviousTimerLength()
            }
            else -> setPreviousTimerLength()
        }
        secondsRemaining =
            if (timerState != TimerState.Stopped)
                PrefUtil.getSecondsRemaining(this)
            else
                timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerState.Running || timerState == TimerState.PauseOnNext)
            startTimer()

        updateButtons()
        updateCountDownUI()
    }

    private fun startTimer() {
        when(PrefUtil.getCurrentCycle(this)) {
            0,2,4,6 -> {
                // study cycle
                on_break_text.text = getString(R.string.on_study)
            }
            1,3,5 -> {
                // short break
                on_break_text.text = getString(R.string.on_break)
            }
            7 -> {
                // long break
                on_break_text.text = getString(R.string.on_long_break)
            }
            else -> {
                // error
            }
        }

        if (timerState == TimerState.Paused || timerState == TimerState.Stopped) timerState =
            TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountDownUI()
            }
        }.start()
    }

    private fun setNewTimerLength() {
        val lengthInMinutes =
            when(PrefUtil.getCurrentCycle(this)) {
                0,2,4,6 -> {
                    // study cycle
                    PrefUtil.getTimerLength(this)
                }
                1,3,5 -> {
                    // short break
                    PrefUtil.getShortBreakLength(this)
                }
                7 -> {
                    // long break
                    PrefUtil.getLongBreakLength(this)
                }
                else -> {
                    // error
                    PrefUtil.getTimerLength(this)
                }
            }
        timerLengthSeconds = (lengthInMinutes * 60L)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setTimer(time: Long) {
        val lengthInMinutes = time / 60000L
        timerLengthSeconds = (lengthInMinutes * 60L)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun onTimerFinished() {
        playNotification()
        updatePomodoroCounter()
        setNewTimerLength()
        progress_countdown.progress = 0
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds
        updateButtons()
        updateCountDownUI()

        if (PrefUtil.getAutoStart(this) && timerState == TimerState.Running) {
            startTimer()
        } else {
            on_break_text.text = getString(R.string.press_play)
            timerState = TimerState.Paused
        }
    }

    private fun uncheckBoxes() {
        pom1.isChecked = false
        pom2.isChecked = false
        pom3.isChecked = false
        pom4.isChecked = false
    }

    private fun updateCountDownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        val twoDigitSeconds = if (secondsStr.length == 2) secondsStr else "0$secondsStr"
        timerDisplay.text = getString(R.string.timerDisplay, minutesUntilFinished, twoDigitSeconds)
        progress_countdown.progress =  (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons() {
        when (timerState) {
            TimerState.Running -> {
                startStop.setImageResource(android.R.drawable.ic_media_pause)
                restart.setImageResource(R.drawable.ic_break_on_pause)
            }
            TimerState.Paused -> {
                startStop.setImageResource(android.R.drawable.ic_media_play)
                restart.setImageResource(R.drawable.ic_reset)
            }
            TimerState.PauseOnNext -> {
                startStop.setImageResource(android.R.drawable.ic_media_pause)
                restart.setImageResource(R.drawable.ic_continue)
            }
            TimerState.Stopped -> {
                startStop.setImageResource(android.R.drawable.ic_media_play)
                restart.setImageResource(R.drawable.ic_reset)
            }
        }
    }

    private fun updatePomodoroCounter() {
        PrefUtil.setCurrentCycle(this,(PrefUtil.getCurrentCycle(this)+1))

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        Log.d("main", "onCreateOptionsMenu")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("main", "onOptionsItemSelected")
        return when (item.itemId) {
            R.id.menu_item_settings -> {
                Log.d("main", "onOptionsItemSelected // menu selected")
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun playNotification() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val player = MediaPlayer.create(this,notification)
        player.start()
    }
}
