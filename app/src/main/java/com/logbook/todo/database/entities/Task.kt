package com.logbook.todo.database.entities

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) var id: Int = 0, // Auto-generate ID
    var name: String = "", // Default value for name
    var photoAttachment: Uri? = null, // Default value for nullable property
    var completionDate: LocalDateTime? = null, // Default value for nullable property
    var priority: Int = 1, // Default priority
    var isCompleted: Boolean = false, // Default completion status
    var tags: List<String> = emptyList() // Default value for empty list
)