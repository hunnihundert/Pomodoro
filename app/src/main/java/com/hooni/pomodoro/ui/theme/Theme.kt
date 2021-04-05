package com.hooni.pomodoro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PomodoroTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = PomodoroTypography,
        shapes = PomodoroShapes,
        content = content
    )
}

private val LightColors = lightColors(
    primary = Color.White,
    primaryVariant = PomodoroAccent,
    onPrimary = Color.Black,
    secondary = Color.White,
    secondaryVariant = Red900,
    onSecondary = Color.Black,
    error = Red800,
    surface = PomodoroRed
)

private val DarkColors = darkColors(
    primary = Red300,
    primaryVariant = Red700,
    onPrimary = Color.Black,
    secondary = Red300,
    onSecondary = Color.Black,
    background = Color.Black,
    error = Red200,
    surface = Color.Black
)