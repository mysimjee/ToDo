package com.logbook.todo.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.random.Random


@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(entity = Task::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SubTask(
    @PrimaryKey(autoGenerate = true) var id: Int = 0, // Auto-generate ID
    var taskId: Int = 0, // Default value for foreign key
    var name: String = "", // Default value for name
    var isCompleted: Boolean = false, // Default completion status
)