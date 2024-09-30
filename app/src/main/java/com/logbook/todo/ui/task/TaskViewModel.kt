package com.logbook.todo.ui.task

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logbook.todo.TaskRepository
import com.logbook.todo.database.entities.SubTask
import com.logbook.todo.database.entities.Task
import com.logbook.todo.notification.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class TaskViewModel : ViewModel() {

    private val taskRepository: TaskRepository = TaskRepository.get()

    // MutableLiveData to hold the current task
    private val _task = MutableLiveData<Task?>()
    val task: MutableLiveData<Task?> get() = _task

    // MutableLiveData to hold the current subtasks
    private val _subtasks = MutableLiveData<List<SubTask>?>() // Assuming Subtask is your data class for subtasks
    val subtasks: MutableLiveData<List<SubTask>?> get() = _subtasks

    init {
        // Initialize with a default Task
        resetTask()
    }

    // Function to load a task and its related subtasks
    fun loadTaskWithSubTasks(taskId: Int) {
        viewModelScope.launch {
            try {
                // Load the task from the repository
                val loadedTask = taskRepository.getTask(taskId)
                _task.postValue(loadedTask)

                // Load the subtasks related to the task
                val loadedSubTasks = taskRepository.getSubTasks(taskId)
                _subtasks.postValue(loadedSubTasks)

                Log.d("TaskViewModel", "Task and subtasks loaded successfully.")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error loading task and subtasks", e)
            }
        }
    }

    // Function to update the task name
    fun updateTaskName(name: String) {
        try {
            _task.value = _task.value?.copy(name = name)
        }catch (e: Exception) {
            Log.e("TaskViewModel", "Error loading task and subtasks", e)
        }
    }

    // Function to add a subtask to _subtasks
    fun addSubTask(subTask: SubTask) {
        try {
            // Get the current list of subtasks
            val updatedSubtasks = _subtasks.value?.toMutableList() ?: mutableListOf()

            // Add the new subtask
            updatedSubtasks.add(subTask)

            // Update the LiveData with the new list of subtasks
            _subtasks.value = updatedSubtasks
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error adding subtask: ${e.message}", e)
        }
    }

    // Function to remove a subtask from _subtasks
    fun removeSubTask(subTask: SubTask) {
        _subtasks.value?.let { currentSubtasks ->
            try {
                // Create a mutable copy of the current list of subtasks
                val updatedSubtasks = currentSubtasks.toMutableList()

                // Check if the subtask exists and remove it
                if (updatedSubtasks.remove(subTask)) {
                    // Update the LiveData with the new list of subtasks
                    _subtasks.value = updatedSubtasks

                    viewModelScope.launch(Dispatchers.IO) {
                        taskRepository.deleteSubTask(subTask)
                    }
                } else {
                    Log.e("TaskViewModel", "Subtask not found for removal.")
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error removing subtask: ${e.message}", e)
            }
        }
    }

    // Function to update the isCompleted status of a subtask
    fun updateSubTaskCompletion(subTask: SubTask, isCompleted: Boolean) {
        _subtasks.value?.let { currentSubtasks ->
            try {
                // Create a mutable copy of the current list of subtasks
                val updatedSubtasks = currentSubtasks.toMutableList()

                // Find the subtask and update its completion status
                val index = updatedSubtasks.indexOf(subTask)
                if (index != -1) {
                    val updatedSubTask = updatedSubtasks[index].copy(isCompleted = isCompleted)
                    updatedSubtasks[index] = updatedSubTask

                    // Update the LiveData with the new list of subtasks
                    _subtasks.value = updatedSubtasks
                } else {
                    Log.e("TaskViewModel", "Subtask not found for updating completion status.")
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error updating subtask completion: ${e.message}", e)
            }
        }
    }

    // Function to attach a photo
    fun attachPhoto(uri: Uri) {
        try {
            _task.value = _task.value?.copy(photoAttachment = uri)
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error attaching photo: ${e.message}", e)
        }
    }

    fun clearPhotoAttachment() {
        try {
            _task.value = _task.value?.copy(photoAttachment = null)
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error clearing photo attachment: ${e.message}", e)
        }
    }

    fun setCompletionDate(date: String) {
        try {
            // Define the formatter based on the expected input format
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            // Parse the input string to LocalDateTime
            val parsedDate = LocalDateTime.parse(date, formatter)

            _task.value = _task.value?.copy(completionDate = parsedDate)
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error setting completion date: ${e.message}", e)
        }
    }

    // Function to set priority
    fun setPriority(priority: Int) {
        try {
            _task.value = _task.value?.copy(priority = priority)
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error setting priority: ${e.message}", e)
        }
    }

    // Function to update task completion status
    fun updateTaskCompletion(isCompleted: Boolean) {
        try {
            _task.value = _task.value?.copy(isCompleted = isCompleted)
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error updating task completion status: ${e.message}", e)
        }
    }

    // Function to add tags
    fun addTags(newTags: List<String>) {
        try {
            val updatedTags = _task.value?.tags?.toMutableList() ?: mutableListOf()
            // Add only tags that are not already in the list
            newTags.forEach { tag ->
                if (!updatedTags.contains(tag)) {
                    updatedTags.add(tag)
                }
            }
            _task.value = _task.value?.copy(tags = updatedTags)
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error adding tags: ${e.message}", e)
        }
    }

    // Function to remove a tag
    fun removeTag(tag: String) {
        try {
            // Get the current list of tags and convert it to a mutable list
            val updatedTags = _task.value?.tags?.toMutableList() ?: return

            // Remove the specified tag if it exists
            updatedTags.remove(tag)

            // Update the LiveData with the new list of tags
            _task.value = _task.value?.copy(tags = updatedTags)
        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error removing tag: ${e.message}", e)
        }
    }

    // Function to save the current task
    fun saveTask(context: Context) {
        _task.value?.let { taskToSave ->
            viewModelScope.launch {
                try {
                    taskRepository.insertTask(taskToSave)
                    subtasks.value?.forEach { subTask ->
                        taskRepository.insertSubTask(subTask)
                    }

                    // Schedule a notification for the task if it is not completed
                    if (!taskToSave.isCompleted) {
                        NotificationScheduler.scheduleTaskNotification(context, taskToSave)
                    }

                    // reset the task after saving
                    resetTask()
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error saving task: ${e.message}", e)
                }
            }
        }
    }

    // Function to update the selected task
    fun updateTask(context: Context) {
        _task.value?.let { taskToUpdate ->
            viewModelScope.launch {
                try {
                    // Update the main task
                    taskRepository.updateTask(taskToUpdate)

                    // Fetch the existing subtasks from the database
                    val existingSubTasks = taskRepository.getSubTasks(taskToUpdate.id)?.toMutableList()

                    // Iterate over the current subtasks from the ViewModel
                    subtasks.value?.forEach { subTask ->
                        if (existingSubTasks != null) {
                            if (existingSubTasks.contains(subTask)) {
                                // If the subtask exists, update it
                                taskRepository.updateSubTask(subTask)
                                existingSubTasks.remove(subTask) // Remove from existing list, so we know which ones remain
                            } else {
                                // If it's a new subtask, insert it
                                taskRepository.insertSubTask(subTask)
                            }
                        }
                    }

                    // No deletion of subtasks, even if the task is completed

                    // Schedule or cancel a notification for the task
                    if (taskToUpdate.completionDate?.isBefore(LocalDateTime.now()) == true) {
                        // If the completion date is in the past, cancel the notification
                        NotificationScheduler.cancelTaskNotification(context, taskToUpdate.id)
                    } else if (!taskToUpdate.isCompleted) {
                        // If the task is not completed and the completion date is valid
                        NotificationScheduler.cancelTaskNotification(context, taskToUpdate.id)
                        NotificationScheduler.scheduleTaskNotification(context, taskToUpdate)
                    } else {
                        // If the task is completed, cancel the notification
                        NotificationScheduler.cancelTaskNotification(context, taskToUpdate.id)
                    }


                    Log.d("TaskViewModel", "Task and subtasks updated successfully.")
                } catch (e: Exception) {
                    Log.e("TaskViewModel", "Error updating task and subtasks", e)
                }
            }
        }
    }


    private fun resetTask() {
        try {
            _task.value = Task(
                id = Random.nextInt(),
                name = "",
                isCompleted = false,
                completionDate = LocalDateTime.now(),
                priority = 0,
                tags = listOf(),
                photoAttachment = null
            )
            _subtasks.value = emptyList()
        }catch (e: Exception) {
            Log.e("TaskViewModel", "Error resting task value", e)
        }
    }
}
