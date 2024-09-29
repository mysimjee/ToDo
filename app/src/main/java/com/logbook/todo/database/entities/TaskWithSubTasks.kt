package com.logbook.todo.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSubTasks(
    @Embedded var task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubTask>
)
