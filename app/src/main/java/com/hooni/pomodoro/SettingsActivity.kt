package com.hooni.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hooni.pomodoro.util.PrefUtil

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (PrefUtil.getDarkMode(this)) {
            AppConstants.LIGHT_MODE -> setTheme(R.style.lightTheme)
            AppConstants.DARK_MODE -> setTheme(R.style.darkTheme)
        }

        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }
}
