package com.example.tasktracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val textContent: String? = null,

    // For image-based tasks
    val imageData: ByteArray? = null,       // Store image as ByteArray (BLOB)
    val imageDescription: String? = null,

    // For audio-based tasks
    val audioData: ByteArray? = null,       // Store audio as ByteArray (BLOB)
    val audioDescription: String? = null,

    // Date field (store as epoch time in millis)
    val date: Long = System.currentTimeMillis(),

    // Timer field (nullable, default null)
    val timer: Long? = null
)


