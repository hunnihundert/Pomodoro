package com.hooni.pomodoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hooni.pomodoro.R

@Composable
fun TimerScreen(
    minutesRemaining: String,
    secondsRemaining: String,
    currentPomodoro: Int,
    progress: Float,
    onPausePlay: (Boolean) -> Unit,
    onAutostart: (Boolean) -> Unit,
    openSettings: () -> Unit
) {
    MaterialTheme {
        TimerScreenWhole(
            minutesRemaining = minutesRemaining,
            secondsRemaining = secondsRemaining,
            currentPomodoro = currentPomodoro,
            progress = progress,
            onPausePlay = onPausePlay,
            onAutostart = onAutostart,
            openSettings = openSettings,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TimerScreenWhole(
    minutesRemaining: String,
    secondsRemaining: String,
    currentPomodoro: Int,
    progress: Float,
    onPausePlay: (Boolean) -> Unit,
    onAutostart: (Boolean) -> Unit,
    openSettings: () -> Unit,
    modifier: Modifier
) {

    Column(modifier = modifier) {
        IconButton(
            onClick = openSettings,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_settings),
                contentDescription = "Settings"
            )
        }
        TimeAndPomodorosAndProgress(
            minutesRemaining = minutesRemaining,
            secondsRemaining = secondsRemaining,
            currentPomodoro = currentPomodoro,
            progress = progress,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        FloatingActionButtons(
            onPausePlay = onPausePlay,
            onAutostart = onAutostart,
            modifier = Modifier
                .padding(top = 48.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun TimeAndPomodorosAndProgress(
    minutesRemaining: String,
    secondsRemaining: String,
    currentPomodoro: Int,
    progress: Float,
    modifier: Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier
                .align(Alignment.Center)
                .width(dimensionResource(id = R.dimen.width_timer_progressBar))
                .height(dimensionResource(id = R.dimen.height_timer_progressBar)),
            strokeWidth = 6.dp
        )
        TimeAndPomodoros(
            minutesRemaining = minutesRemaining,
            secondsRemaining = secondsRemaining,
            currentPomodoro = currentPomodoro,
            Modifier
                .padding(dimensionResource(id = R.dimen.fontSize_timer_time))
                .align(Alignment.Center)
        )
    }
}

@Composable
fun TimeAndPomodoros(
    minutesRemaining: String,
    secondsRemaining: String,
    currentPomodoro: Int,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Time(minutesRemaining, secondsRemaining)
        Pomodoros(currentPomodoro)
    }

}

@Composable
fun Time(minutesRemaining: String, secondsRemaining: String) {
    Text(
        text = "$minutesRemaining:$secondsRemaining",
        fontSize = 32.sp
    )

}

@Composable
fun Pomodoros(currentPomodoro: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1 until currentPomodoro) {
            Icon(
                imageVector = Icons.Filled.Check,
                "Pomodoro Indicator",
                Modifier
                    .width(dimensionResource(id = R.dimen.width_timer_pomodoroIndicator))
                    .height(dimensionResource(id = R.dimen.height_timer_pomodoroIndicator))
            )
        }
        CircularProgressIndicator(
            modifier = Modifier
                .width(18.dp)
                .height(18.dp)
                .padding(4.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
fun FloatingActionButtons(
    onPausePlay: (Boolean) -> Unit,
    onAutostart: (Boolean) -> Unit,
    modifier: Modifier
) {

    val (isRunning, setRunning) = remember { mutableStateOf(true) }
    val pausePlay: () -> Unit = {
        setRunning(!isRunning)
        onPausePlay(isRunning)
    }

    Row(modifier = modifier) {
        RestartFloatingActionButton(
            isRunning = isRunning,
            onAutostart = onAutostart,
            modifier = Modifier.padding(32.dp)
        )

        PausePlayFloatingActionButton(
            onPausePlay = pausePlay,
            isRunning = isRunning,
            modifier = Modifier.padding(32.dp)
        )

    }
}

@Composable
fun PausePlayFloatingActionButton(
    onPausePlay: () -> Unit,
    isRunning: Boolean,
    modifier: Modifier
) {
    FloatingActionButton(
        onClick = onPausePlay,
        modifier = modifier
            .padding(16.dp)
    ) {
        Icon(
            if (isRunning) painterResource(id = R.drawable.ic_pause)
            else painterResource(id = R.drawable.ic_play),
            contentDescription = if (isRunning) "Pause" else "Play"
        )
    }
}

@Composable
fun RestartFloatingActionButton(
    isRunning: Boolean,
    onAutostart: (Boolean) -> Unit,
    modifier: Modifier
) {

    val (isAutoStart, setAutostart) = remember { mutableStateOf(true) }

    val autostart: () -> Unit = {
        setAutostart(!isAutoStart)
        onAutostart(isAutoStart)
    }

    FloatingActionButton(
        onClick = autostart,
        modifier = modifier
            .padding(16.dp)
    ) {
        Icon(
            if (!isRunning) painterResource(id = R.drawable.ic_reset)
            else {
                if (isAutoStart) painterResource(id = R.drawable.ic_break_on_pause)
                else painterResource(id = R.drawable.ic_continue)
            },
            contentDescription = if (isRunning) "Pause" else "Play"
        )
    }
}

@Preview
@Composable
fun TimerScreenWholePreview() {
    MaterialTheme {
        TimerScreenWhole(
            minutesRemaining = "24",
            secondsRemaining = "53",
            currentPomodoro = 3,
            progress = 0.7f,
            onPausePlay = {},
            onAutostart = {},
            openSettings = {},
            modifier = Modifier.padding(4.dp)
        )
    }
}
