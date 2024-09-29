package com.logbook.todo

import android.content.Context
import android.util.Log

import com.logbook.todo.database.entities.TaskWithSubTasks
import com.logbook.todo.database.AppDatabase
import com.logbook.todo.database.entities.SubTask
import com.logbook.todo.database.entities.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository private constructor(context: Context) {
    private val db: AppDatabase = AppDatabase.getDatabase(context)
    private val tag = "TaskRepository" // Tag for logging

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = TaskRepository(context)
            }
        }

        fun get(): TaskRepository {
            return INSTANCE
                ?: throw IllegalStateException("TaskRepository must be initialized")
        }
    }

    // Retrieve tasks
    suspend fun getTask(id: Long): Task? {
        return try {
            withContext(Dispatchers.IO) {
                db.taskDao().getTask(id)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error retrieving task with id: $id", e)
            null
        }
    }

    // Retrieve subtasks
    suspend fun getSubTasks(taskId: Long): List<SubTask>? {
        return try {
            withContext(Dispatchers.IO) {
                db.taskDao().getSubTasksForTask(taskId)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error retrieving subtasks for taskId: $taskId", e)
            null
        }
    }

    // Update a subtask
    suspend fun updateSubTask(subTask: SubTask) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().updateSubTask(subTask)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating subtask: $subTask", e)
        }
    }

    // Delete a subtask
    suspend fun deleteSubTask(subTask: SubTask) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().deleteSubTask(subTask)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error deleting subtask: $subTask", e)
        }
    }

    // Retrieve tasks with their subtasks
    suspend fun getTasksWithSubTasks(): List<TaskWithSubTasks>? {
        return try {
            withContext(Dispatchers.IO) {
                db.taskDao().getTasksWithSubTasks()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error retrieving tasks with subtasks", e)
            null
        }
    }


    // Add the method to the repository
    suspend fun getTasksByCompletionStatus(isCompleted: Boolean): List<TaskWithSubTasks>? {
        return try {
            withContext(Dispatchers.IO) {
                db.taskDao().getTasksByCompletionStatus(isCompleted)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error retrieving tasks with subtasks based on isCompleted field", e)
            null
        }
    }



    // Insert a new task
    suspend fun insertTask(task: Task) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().insertTask(task)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error inserting task: $task", e)
        }
    }

    // Insert a new subtask
    suspend fun insertSubTask(subTask: SubTask) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().insertSubTask(subTask)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error inserting subtask: $subTask", e)
        }
    }

    // Update an existing task
    suspend fun updateTask(task: Task) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().updateTask(task)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating task: $task", e)
        }
    }

    // Delete a task
    suspend fun deleteTask(task: Task) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().deleteTask(task)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error deleting task: $task", e)
        }
    }

    // Update the completion status of a task
    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().updateTaskCompletionStatus(taskId, isCompleted)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating task completion status for taskId: $taskId", e)
        }
    }

    // Update the isCompleted status of a single SubTask by its ID
    suspend fun updateSubTaskCompletionStatus(subTaskId: Long, isCompleted: Boolean) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().updateSubTaskCompletionStatus(subTaskId, isCompleted)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating subtask completion status for subTaskId: $subTaskId", e)
        }
    }

    // Update the isCompleted status of all SubTasks associated with a specific Task ID
    suspend fun updateSubTasksCompletionStatusByTaskId(taskId: Long, isCompleted: Boolean) {
        try {
            withContext(Dispatchers.IO) {
                db.taskDao().updateSubTasksCompletionStatusByTaskId(taskId, isCompleted)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating completion status of all subtasks for taskId: $taskId", e)
        }
    }
}
