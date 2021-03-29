package com.hooni.pomodoro.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.Surface
import androidx.activity.compose.setContent

class MainActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                TimerScreen()
            }
        }
    }
}