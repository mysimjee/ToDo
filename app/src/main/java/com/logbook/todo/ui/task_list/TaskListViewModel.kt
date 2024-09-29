package com.logbook.todo.ui.task_list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logbook.todo.TaskRepository
import com.logbook.todo.database.entities.Task
import com.logbook.todo.database.entities.TaskWithSubTasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class TaskListViewModel : ViewModel() {

    private val taskRepository: TaskRepository = TaskRepository.get()

    private val _tasksByDate = MutableLiveData<Map<String, List<TaskWithSubTasks>>>()
    val tasksByDate: LiveData<Map<String, List<TaskWithSubTasks>>> get() = _tasksByDate

    // LiveData to indicate whether completed tasks are selected
    private val _showCompletedTasks = MutableLiveData<Boolean>(false)

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Customize the format as needed

    init {
        // Load tasks based on the initial value of showCompletedTasks
        try {
            loadTasksByCompletionStatus(_showCompletedTasks.value ?: false)
        } catch (e: Exception) {
            Log.e("TaskListViewModel", "Error loading tasks by completion status", e)
        }
    }

    // New function to load tasks based on completion status
    private fun loadTasksByCompletionStatus(isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tasks = taskRepository.getTasksByCompletionStatus(isCompleted)
                _tasksByDate.postValue(tasks?.let { organizeTasksByDate(it) })
            } catch (e: Exception) {
                Log.e("TaskListViewModel", "Error loading tasks by completion status", e)
            }
        }
    }



    private fun organizeTasksByDate(tasks: List<TaskWithSubTasks>): Map<String, List<TaskWithSubTasks>> {
        return tasks.groupBy {
            it.task.completionDate?.format(dateFormatter) ?: "No Date"
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                taskRepository.deleteTask(task)
                _showCompletedTasks.value?.let { loadTasksByCompletionStatus(it) }
            } catch (e: Exception) {
                Log.e("TaskListViewModel", "Error deleting task", e)
            }
        }
    }

    fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                taskRepository.updateTaskCompletionStatus(taskId, isCompleted)
                _showCompletedTasks.value?.let { loadTasksByCompletionStatus(it) }
            } catch (e: Exception) {
                Log.e("TaskListViewModel", "Error updating task completion status", e)
            }
        }
    }

    fun updateSubTaskCompletionStatus(subTaskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                taskRepository.updateSubTaskCompletionStatus(subTaskId, isCompleted)
            } catch (e: Exception) {
                Log.e("TaskListViewModel", "Error updating sub-task completion status", e)
            }
        }
    }

    fun updateSubTasksCompletionStatusByTaskId(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                taskRepository.updateSubTasksCompletionStatusByTaskId(taskId, isCompleted)
            } catch (e: Exception) {
                Log.e("TaskListViewModel", "Error updating sub-tasks completion status", e)
            }
        }
    }

    // Function to change the value of _showCompletedTasks
    fun updateShowCompletedTasks(isCompleted: Boolean) {
      try {
          _showCompletedTasks.value = isCompleted
          loadTasksByCompletionStatus(isCompleted)
      } catch (e: Exception){
          Log.e("TaskListViewModel", "Error loading tasks by completion status", e)

      }
    }
}
