package com.example.tasktracker.screen

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tasktracker.viewmodel.TaskViewModel
import com.example.tasktracker.composables.AddTaskScreen
import com.example.tasktracker.composables.AudioPlayer
import com.example.tasktracker.db.TaskEntity
import com.example.tasktracker.utils.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var showDialog by rememberSaveable { mutableStateOf(false) }

    // Show first-time user name dialog
    LaunchedEffect(Unit) {
        val isFirstTime = prefs.isFirstTimeFlow.first()
        showDialog = isFirstTime
    }

    var nameInput by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Add Your Name") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Your Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (nameInput.trim().isNotEmpty()) {
                        coroutineScope.launch {
                            prefs.saveUserName(nameInput.trim())
                            showDialog = false
                        }
                    }
                }) {
                    Text("Save")
                }
            }
        )
    }

    val userName by prefs.userNameFlow.collectAsState(initial = "")

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(70.dp).padding(top = 20.dp),
                title = {
                    Text(
                        text = "Task Manager",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                actions = {
                    if (userName.isNotBlank()) {
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { expanded = true },
                                contentColor = Color.White
                            ) {
                                // Use Box with contentAlignment to center the letter
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = userName.first().uppercase(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .width(200.dp)
                                ) {
                                    Text("Hello, $userName", style = MaterialTheme.typography.bodyMedium)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            expanded = false
                                            showDialog = true  // reopen dialog to edit name
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Edit Name")
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AddTaskScreen(viewModel = viewModel)
            TaskListScreen(viewModel = viewModel)
        }
    }
}




@Composable
fun TaskListScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    LazyColumn {
        items(tasks) { task ->
            TaskItem(task = task)
        }
    }
}

@Composable
fun TaskItem(
    task: TaskEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val backgroundColors = listOf(
        Color(0xFFE57373),
        Color(0xFFBA68C8),
        Color(0xFF64B5F6),
        Color(0xFF4DB6AC),
        Color(0xFFFFD54F),
        Color(0xFFA1887F),
        Color(0xFF90A4AE)
    )
    val color = backgroundColors[task.id % backgroundColors.size]

    val dateText = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(color, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(text = dateText, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            task.imageData?.let { imgData ->
                val bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.size).asImageBitmap()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = task.imageDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.imageDescription ?: "",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = task.textContent ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                task.audioData?.let { audio ->
                    AudioPlayer(
                        audioData = audio,
                        description = task.audioDescription,
                        modifier = Modifier.fillMaxWidth(),
                        iconTint = Color.White
                    )
                }
            }
        }
    }
}

