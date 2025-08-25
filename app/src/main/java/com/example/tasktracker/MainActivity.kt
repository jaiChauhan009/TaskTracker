package com.example.tasktracker

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tasktracker.db.AppDatabase
import com.example.tasktracker.screen.HomeScreen
import com.example.tasktracker.screen.ScheduleMessageScreen
import com.example.tasktracker.screen.SplashScreen
import com.example.tasktracker.screen.TaskScreen
import com.example.tasktracker.ui.theme.TaskTrackerTheme
import com.example.tasktracker.utils.MessageWorker
import com.example.tasktracker.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.work.*

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TaskViewModel

    private var permissionGranted by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val workManager = WorkManager.getInstance(applicationContext)
        val dao = AppDatabase.getInstance(applicationContext).taskDao()
        viewModel = TaskViewModel(dao)

        enableEdgeToEdge()

        // Initialize permissionGranted synchronously
        permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                "android.permission.POST_NOTIFICATIONS"
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        checkNotificationPermission()

        setContent {
            TaskTrackerTheme {
                ScheduleMessageScreen(workManager)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        }
    }

    @Composable
    fun ScheduleMessageScreen(workManager: WorkManager) {
        val context = LocalContext.current
        var inputMinutes by remember { mutableStateOf("") }
        var showMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Enter delay in minutes to schedule message")

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = inputMinutes,
                onValueChange = { value ->
                    inputMinutes = value.filter { it.isDigit() }
                },
                label = { Text("Minutes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val minutes = inputMinutes.toLongOrNull()
                    if (minutes != null && minutes > 0) {
                        if (permissionGranted) {
                            scheduleMessage(workManager, minutes, "Scheduled message after $minutes minutes")
                            showMessage = "Message scheduled after $minutes minutes"
                        } else {
                            showMessage = "Notification permission is required"
                        }
                    } else {
                        showMessage = "Enter a valid number of minutes"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Schedule Message")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = showMessage)
        }
    }

    private fun scheduleMessage(workManager: WorkManager, minutes: Long, message: String) {
        val delayMillis = minutes * 60 * 1000
        val inputData = Data.Builder()
            .putString("MESSAGE", message)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MessageWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            "scheduled_message_work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: TaskViewModel
) {
    val pagerPages = listOf("Home", "Task")
    val pagerState = rememberPagerState(pageCount = { pagerPages.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(110.dp)
                    .padding(horizontal = 8.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                pagerPages.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        icon = { Icon(if (index == 0) Icons.Default.Home else Icons.Default.DateRange, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> HomeScreen(navController,viewModel)
                1 -> TaskScreen(navController,viewModel)
            }
        }
    }
}




@Composable
fun AppNavHost(navController: NavHostController,viewModel: TaskViewModel) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Home.route) { MainScreen(navController,viewModel) }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
}

