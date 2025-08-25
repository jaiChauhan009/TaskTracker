package com.example.tasktracker.composables

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tasktracker.viewmodel.TaskViewModel
import com.example.tasktracker.db.TaskEntity
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var imageData by remember { mutableStateOf<ByteArray?>(null) }
    var imageDescription by remember { mutableStateOf("") }

    var audioData by remember { mutableStateOf<ByteArray?>(null) }
    var audioDescription by remember { mutableStateOf("") }

    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFilePath by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(false) }
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            imageData = stream.toByteArray()
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionGranted = granted
            showPermissionDeniedMessage = !granted
        }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    fun startRecording() {
        if (!permissionGranted) {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            return
        }
        try {
            val file = File(context.cacheDir, "audio_record.3gp")
            audioFilePath = file.absolutePath

            val mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false

            audioFilePath?.let {
                val file = File(it)
                if (file.exists()) {
                    audioData = file.readBytes()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllInputs() {
        title = ""
        description = ""
        imageData = null
        imageDescription = ""
        audioData = null
        audioDescription = ""
        isRecording = false
        recorder?.release()
        recorder = null
        audioFilePath = null
        showPermissionDeniedMessage = false
    }

    Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true,
            maxLines = 1,
            isError = title.isBlank()
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            maxLines = 4
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { takePictureLauncher.launch(null) }
            ) {
                Text("Capture Photo")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (isRecording) stopRecording() else startRecording()
                }
            ) {
                Text(if (isRecording) "Stop Recording" else "Record Audio")
            }
        }

        if (showPermissionDeniedMessage) {
            Text(
                "Audio recording permission denied. Please enable it in settings.",
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            imageData?.let {
                Column(
                    modifier = Modifier
                        .weight(1f).padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap()
                    Image(
                        bitmap = bitmap,
                        contentDescription = imageDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 90.dp)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    TextField(
                        value = imageDescription,
                        onValueChange = { imageDescription = it },
                        label = { Text("Image Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            audioData?.let {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Audio recorded", modifier = Modifier.padding(bottom = 5.dp))
                    TextField(
                        value = audioDescription,
                        onValueChange = { audioDescription = it },
                        label = { Text("Audio Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (title.isBlank()) {
                    Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val task = TaskEntity(
                    title = title,
                    textContent = if (description.isNotBlank()) description else null,
                    imageData = imageData,
                    imageDescription = if (imageDescription.isNotBlank()) imageDescription else null,
                    audioData = audioData,
                    audioDescription = if (audioDescription.isNotBlank()) audioDescription else null
                )
                viewModel.insertTask(task)
                clearAllInputs()
                Toast.makeText(context, "Task saved", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("Save Task")
        }
    }
}


