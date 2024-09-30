package com.logbook.todo.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.logbook.todo.database.entities.SubTask
import com.logbook.todo.database.entities.Task
import com.logbook.todo.database.entities.TaskWithSubTasks


@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask)

    @Update
    suspend fun updateSubTask(subTask: SubTask)

    @Delete
    suspend fun deleteSubTask(subTask: SubTask)



    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: Long): Task

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY addedDate ASC")
    suspend fun getSubTasksForTask(taskId: Long): List<SubTask>




    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY completionDate IS NULL, completionDate ASC")
    suspend fun getTasksByCompletionStatus(isCompleted: Boolean): List<TaskWithSubTasks>


    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // New function to update the isCompleted status of a task
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean)


    // New function to update the isCompleted status of a SubTask
    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :subTaskId")
    suspend fun updateSubTaskCompletionStatus(subTaskId: Long, isCompleted: Boolean)

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE taskId = :taskId")
    suspend fun updateSubTasksCompletionStatusByTaskId(taskId: Long, isCompleted: Boolean)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubTasksByTaskId(taskId: Long)


}
