package com.hooni.pomodoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hooni.pomodoro.R
import com.hooni.pomodoro.util.Util.TimerState

@Composable
fun TimerScreen(
    minutesRemaining: String,
    secondsRemaining: String,
    currentPomodoro: Int,
    progress: Float,
    onPausePlay: (TimerState) -> Unit,
    onAutostart: (Boolean) -> Unit,
    isRunning: TimerState,
    isAutostart: Boolean,
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
            isRunning = isRunning,
            isAutostart = isAutostart,
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
    onPausePlay: (TimerState) -> Unit,
    onAutostart: (Boolean) -> Unit,
    isRunning: TimerState,
    isAutostart: Boolean,
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
            isRunning = isRunning,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        FloatingActionButtons(
            onPausePlay = onPausePlay,
            onAutostart = onAutostart,
            isRunning = isRunning,
            isAutostart = isAutostart,
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
    isRunning: TimerState,
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
            isRunning = isRunning,
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
    isRunning: TimerState,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Time(minutesRemaining, secondsRemaining)
        Pomodoros(currentPomodoro, isRunning)
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
fun Pomodoros(currentPomodoro: Int, isRunning: TimerState) {

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
        if(isRunning == TimerState.Running) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(18.dp)
                    .height(18.dp)
                    .padding(4.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun FloatingActionButtons(
    onPausePlay: (TimerState) -> Unit,
    onAutostart: (Boolean) -> Unit,
    isRunning: TimerState,
    isAutostart: Boolean,
    modifier: Modifier
) {

    Row(modifier = modifier) {
        RestartFloatingActionButton(
            isRunning = isRunning,
            isAutostart = isAutostart,
            onAutostart = onAutostart,
            modifier = Modifier.padding(32.dp)
        )

        PausePlayFloatingActionButton(
            isRunning = isRunning,
            onPausePlay = onPausePlay,
            modifier = Modifier.padding(32.dp)
        )

    }
}

@Composable
fun PausePlayFloatingActionButton(
    isRunning: TimerState,
    onPausePlay: (TimerState) -> Unit,
    modifier: Modifier
) {
    val pausePlay: () -> Unit = {
        onPausePlay(isRunning)
    }

    FloatingActionButton(
        onClick = pausePlay,
        modifier = modifier
            .padding(16.dp)
    ) {
        Icon(
            if (isRunning == TimerState.Running) painterResource(id = R.drawable.ic_pause)
            else painterResource(id = R.drawable.ic_play),
            contentDescription = if (isRunning == TimerState.Running) "Pause" else "Play"
        )
    }
}

@Composable
fun RestartFloatingActionButton(
    isRunning: TimerState,
    isAutostart: Boolean,
    onAutostart: (Boolean) -> Unit,
    modifier: Modifier
) {
    val autostart: () -> Unit = {
        onAutostart(isAutostart)
    }

    FloatingActionButton(
        onClick = autostart,
        modifier = modifier
            .padding(16.dp)
    ) {
        Icon(
            if (isRunning != TimerState.Running) painterResource(id = R.drawable.ic_reset)
            else {
                if (isAutostart) painterResource(id = R.drawable.ic_break_on_pause)
                else painterResource(id = R.drawable.ic_continue)
            },
            contentDescription = if (isRunning == TimerState.Running) "Pause" else "Play"
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
            isRunning = TimerState.Running,
            isAutostart = false,
            openSettings = {},
            modifier = Modifier.padding(4.dp)
        )
    }
}
