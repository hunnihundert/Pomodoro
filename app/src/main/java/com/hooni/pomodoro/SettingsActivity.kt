package com.hooni.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hooni.pomodoro.util.PrefUtil
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (PrefUtil.getDarkMode(this)) {
            AppConstants.LIGHT_MODE -> setTheme(R.style.lightTheme)
            AppConstants.DARK_MODE -> setTheme(R.style.darkTheme)
        }
        window.decorView.apply {

        }

        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolBarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
    }
}
