package com.hooni.pomodoro

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.BuildCompat
import com.hooni.pomodoro.util.NotificationUtil
import com.hooni.pomodoro.util.PrefUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {


        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService((Context.ALARM_SERVICE)) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
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

    // the actual timer which is running
    private lateinit var timer: CountDownTimer

    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (PrefUtil.getDarkMode(this)) {
            AppConstants.LIGHT_MODE -> setTheme(R.style.lightTheme)
            AppConstants.DARK_MODE -> setTheme(R.style.darkTheme)
        }
        setContentView(R.layout.activity_main)
        setTimer(PrefUtil.getSecondsRemaining(this) * 1000)
        initUI()



        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                val startHiding = Runnable {
                    hideNaviAndStatusBar()
                }
                Handler().postDelayed(startHiding, 2000)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setNightMode()
        initTimer()
        updateStatusIcons()
        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)
        dimScreen()
        hideNaviAndStatusBar()
    }

    private fun hideNaviAndStatusBar() {
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        window.attributes.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS

    }


    private fun dimScreen() {
        if (isDimOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            //window.attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            //window.attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
    }

    private val isDimOn: Boolean
        get() = PrefUtil.getScreenTimeOut(this) && timerState == TimerState.Running


    // TODO: if the screen does not turn off but sets brightness to minimum, user interaction should
    //  make the screen bright again

//    override fun onUserInteraction() {
//        super.onUserInteraction()
//        if (window.attributes.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF) {
//            window.attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
//        }
//    }


    override fun onPause() {
        super.onPause()
        if (timerState == TimerState.Running) {
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
        resetStatusIcons()
        initButtons()
    }

    private fun setNightMode() {
        val outValue = TypedValue()
        theme.resolveAttribute(R.attr.themeName, outValue, true)
        val currentTheme = outValue.string.toString()
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.darkTheme)
        } else {
            when (PrefUtil.getDarkMode(this)) {
                AppConstants.LIGHT_MODE -> {
                    setTheme(R.style.lightTheme)
                    PrefUtil.setDarkMode(this, AppConstants.LIGHT_MODE)

                }
                AppConstants.DARK_MODE -> {
                    setTheme(R.style.darkTheme)
                    PrefUtil.setDarkMode(this, AppConstants.DARK_MODE)
                }
                else -> {
                    if (BuildCompat.isAtLeastQ()) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                    }
                }
            }
        }
        theme.resolveAttribute(R.attr.themeName, outValue, true)
        val newTheme = outValue.string.toString()
        if (currentTheme != newTheme) recreate()
    }


    private fun initButtons() {
        restart.setOnClickListener {
            if (timerState == TimerState.Running) {
                if (PrefUtil.getAutoStart(this)) PrefUtil.setAutoStart(this, false)
                else PrefUtil.setAutoStart(this, true)
            } else {
                // restart
                timerState = TimerState.Stopped
                setTimer(timerLengthSeconds)
                resetStatusIcons()
                // TODO: currently a workaround, the timer should be completely initialized and not
                // onTimerFinished called
                PrefUtil.setCurrentCycle(this, 0)
                onTimerFinished()
            }
            updateButtons()
            showStatusOnToast(it)
        }

        startStop.setOnClickListener {
            if (timerState == TimerState.Paused || timerState == TimerState.Stopped) {
                startTimer()
            } else {
                timer.cancel()
                timerState = TimerState.Paused
                on_break_text.text = getString(R.string.on_break)
            }
            updateButtons()
            showStatusOnToast(it)
        }

        settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showStatusOnToast(view: View) {
        when (timerState) {
            TimerState.Running -> {
                if (!PrefUtil.getAutoStart(this)) {
                    Toast.makeText(this, "Timer will pause after this cycle", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (view == restart) Toast.makeText(
                        this,
                        "Timer will continue after this cycle",
                        Toast.LENGTH_SHORT
                    ).show()
                    else Toast.makeText(this, "Timer started", Toast.LENGTH_SHORT).show()
                }

            }
            TimerState.Paused -> {
                if (view == restart) Toast.makeText(this, "Timer Reset", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Timer Paused", Toast.LENGTH_SHORT).show()
            }
            TimerState.Stopped -> Toast.makeText(this, "Timer stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initTimer() {

        timerState = PrefUtil.getTimerState(this)
        when (timerState) {
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
        else if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountDownUI()
    }

    private fun startTimer() {
        when (PrefUtil.getCurrentCycle(this)) {
            0, 2, 4, 6 -> {
                // study cycle
                on_break_text.text = getString(R.string.on_study)
            }
            1, 3, 5 -> {
                // short break
                on_break_text.text = getString(R.string.on_study_break)
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
        updateStatusIcons()

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
            when (PrefUtil.getCurrentCycle(this)) {
                0, 2, 4, 6 -> {
                    // study cycle
                    PrefUtil.getTimerLength(this)
                }
                1, 3, 5 -> {
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
        if (timerState != TimerState.Stopped) {
            updatePomodoroCounter()
            playNotification(this)
            vibratePhone(this)
        }
        setNewTimerLength()
        progress_countdown.progress = 0
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds


        if (PrefUtil.getAutoStart(this) && timerState == TimerState.Running) {
            startTimer()
        } else {
            on_break_text.text = getString(R.string.press_play)
            timerState = TimerState.Paused
        }
        updateButtons()
        updateCountDownUI()
    }

    private fun resetStatusIcons() {
        hourglass0.setImageResource(R.drawable.ic_hourglass_empty_24px)
        hourglass1.setImageResource(R.drawable.ic_hourglass_empty_24px)
        hourglass2.setImageResource(R.drawable.ic_hourglass_empty_24px)
        hourglass3.setImageResource(R.drawable.ic_hourglass_empty_24px)
        hourglass0.visibility = View.INVISIBLE
        hourglass1.visibility = View.INVISIBLE
        hourglass2.visibility = View.INVISIBLE
        hourglass3.visibility = View.INVISIBLE
    }

    private fun updateStatusIcons() {
        when (PrefUtil.getCurrentCycle(this)) {
            0, 2, 4, 6 -> {
                //study cycle
                when (PrefUtil.getCurrentCycle(this)) {
                    0 -> {
                        resetStatusIcons()
                        hourglass0.visibility = View.VISIBLE

                    }
                    2 -> {
                        hourglass0.visibility = View.VISIBLE
                        hourglass1.visibility = View.VISIBLE
                        hourglass2.visibility = View.INVISIBLE
                        hourglass3.visibility = View.INVISIBLE
                        hourglass0.setImageResource(R.drawable.ic_hourglass_full_24px)
                    }
                    4 -> {
                        hourglass0.visibility = View.VISIBLE
                        hourglass1.visibility = View.VISIBLE
                        hourglass2.visibility = View.VISIBLE
                        hourglass3.visibility = View.INVISIBLE
                        hourglass0.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass1.setImageResource(R.drawable.ic_hourglass_full_24px)
                    }
                    6 -> {
                        hourglass0.visibility = View.VISIBLE
                        hourglass1.visibility = View.VISIBLE
                        hourglass2.visibility = View.VISIBLE
                        hourglass3.visibility = View.VISIBLE
                        hourglass0.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass1.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass2.setImageResource(R.drawable.ic_hourglass_full_24px)
                    }

                }
            }
            1, 3, 5 -> {
                // short break
                when (PrefUtil.getCurrentCycle(this)) {
                    1 -> {
                        hourglass0.visibility = View.VISIBLE
                        hourglass0.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass1.setImageResource(R.drawable.ic_hourglass_empty_24px)
                        hourglass2.setImageResource(R.drawable.ic_hourglass_empty_24px)
                        hourglass3.setImageResource(R.drawable.ic_hourglass_empty_24px)

                    }
                    3 -> {
                        hourglass0.visibility = View.VISIBLE
                        hourglass1.visibility = View.VISIBLE
                        hourglass0.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass1.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass2.setImageResource(R.drawable.ic_hourglass_empty_24px)
                        hourglass3.setImageResource(R.drawable.ic_hourglass_empty_24px)

                    }
                    5 -> {
                        hourglass0.visibility = View.VISIBLE
                        hourglass1.visibility = View.VISIBLE
                        hourglass2.visibility = View.VISIBLE
                        hourglass0.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass1.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass2.setImageResource(R.drawable.ic_hourglass_full_24px)
                        hourglass3.setImageResource(R.drawable.ic_hourglass_empty_24px)
                    }

                }
            }
            7 -> {
                // long break
                hourglass0.visibility = View.VISIBLE
                hourglass1.visibility = View.VISIBLE
                hourglass2.visibility = View.VISIBLE
                hourglass3.visibility = View.VISIBLE
                hourglass0.setImageResource(R.drawable.ic_hourglass_full_24px)
                hourglass1.setImageResource(R.drawable.ic_hourglass_full_24px)
                hourglass2.setImageResource(R.drawable.ic_hourglass_full_24px)
                hourglass3.setImageResource(R.drawable.ic_hourglass_full_24px)
            }
            else -> {
                // error

            }
        }
    }

    private fun updateCountDownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        val twoDigitSeconds = if (secondsStr.length == 2) secondsStr else "0$secondsStr"
        timerDisplay.text = getString(R.string.timerDisplay, minutesUntilFinished, twoDigitSeconds)
        progress_countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun updateButtons() {
        when (timerState) {
            TimerState.Running -> {
                startStop.setImageResource(android.R.drawable.ic_media_pause)
                if (PrefUtil.getAutoStart(this)) {
                    restart.setImageResource(R.drawable.ic_break_on_pause)
                } else {
                    restart.setImageResource(R.drawable.ic_continue)
                }
            }
            TimerState.Paused -> {
                startStop.setImageResource(android.R.drawable.ic_media_play)
                restart.setImageResource(R.drawable.ic_reset)
            }
            TimerState.Stopped -> {
                startStop.setImageResource(android.R.drawable.ic_media_play)
                restart.setImageResource(R.drawable.ic_reset)
            }
        }
    }

    private fun updatePomodoroCounter() {
        PrefUtil.setCurrentCycle(this, (PrefUtil.getCurrentCycle(this) + 1))
    }


    // TODO: set brightness
    val checkWritePermission: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.System.canWrite(this)
            } else {
                return true
            }
        }

    private fun allowWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent =
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }

    val brightness: Int
        get() {
            return Settings.System.getInt(
                this.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                0
            )
        }

    private fun setBrightness(value: Int) {
        Settings.System.putInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}
