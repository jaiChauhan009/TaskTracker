package com.example.tasktracker.screen

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tasktracker.viewmodel.TaskViewModel
import com.example.tasktracker.composables.AudioPlayer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun TaskScreen(navController: NavController, viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val context = LocalContext.current

    var expandedTaskId by remember { mutableStateOf<Int?>(null) } // Which task card is expanded
    var ratingMap by remember { mutableStateOf(mutableMapOf<Int, String>()) }

    // Ratings dropdown options
    val ratingOptions = listOf("Bad", "Average", "Good", "Excellent", "Outstanding")
    val defaultRating = "Good"

    // Group tasks by date or show current date at top (show current date for now)
    val dateText = remember {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Date header at top
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->

                // TaskItem compact with click to open detail card below it
                val currentRating = ratingMap[task.id] ?: defaultRating
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                TextField(
                                    value =
                                )
                                IconButton(onClick = {
                                    // For demo: Toast and update or call viewmodel method
                                    Toast.makeText(
                                        context,
                                        "Completed: ${task.title}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = Color(0xFF4CAF50))
                                }

                                // Delete button
                                IconButton(onClick = {
                                    viewModel.deleteTask(task)
                                    if (expandedTaskId == task.id) expandedTaskId = null
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336))
                                }

                                // Rating dropdown state
                                var dropdownExpanded by remember { mutableStateOf(false) }

                                Box {
                                    TextButton(
                                        onClick = { dropdownExpanded = true },
                                        modifier = Modifier.border(
                                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    ) {
                                        Text(currentRating)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = dropdownExpanded,
                                        onDismissRequest = { dropdownExpanded = false }
                                    ) {
                                        ratingOptions.forEach { rating ->
                                            DropdownMenuItem(
                                                text = { Text(rating) },
                                                onClick = {
                                                    ratingMap = ratingMap.toMutableMap().apply { put(task.id, rating) }
                                                    dropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Full description textContent if any
                            Text(
                                text = task.textContent ?: "-",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Show image if any
                            task.imageData?.let {
                                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap()
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = task.imageDescription,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = task.imageDescription ?: "",
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            task.audioData?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                AudioPlayer(audioData = it, description = task.audioDescription)
                            }
                        }
                    }
                }
            }
        }
    }
}
