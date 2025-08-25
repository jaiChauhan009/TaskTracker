package com.example.tasktracker.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tasktracker.utils.MessageWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun ScheduleMessageScreen(workManager: WorkManager) {
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("Hello!") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Set time for message", style = MaterialTheme.typography.bodySmall)

        Row {
            // Simple input for hour and minute (for convenience, adjust UI as needed)
            TextField(
                value = hour.toString(),
                onValueChange = { hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour },
                label = { Text("Hour") },
                modifier = Modifier.width(100.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            TextField(
                value = minute.toString(),
                onValueChange = { minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute },
                label = { Text("Minute") },
                modifier = Modifier.width(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            scheduleMessage(workManager, hour, minute, message)
        }) {
            Text("Schedule Message")
        }
    }
}



fun scheduleMessage(workManager: WorkManager, hour: Int, minute: Int, message: String) {
    val now = Calendar.getInstance()
    val scheduledTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // If scheduled time is before now, add one day (schedule for next day)
        if (before(now)) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val delay = scheduledTime.timeInMillis - now.timeInMillis

    val inputData = Data.Builder()
        .putString("MESSAGE", message)
        .build()

    val workRequest = OneTimeWorkRequestBuilder<MessageWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(inputData)
        .build()

    workManager.enqueueUniqueWork(
        "scheduled_message_work",
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
}
