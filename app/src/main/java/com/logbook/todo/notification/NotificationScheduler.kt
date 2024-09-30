package com.logbook.todo.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.logbook.todo.database.entities.Task
import java.time.ZoneId

object NotificationScheduler {
    private const val TAG = "Notification Scheduler"

    fun scheduleTaskNotification(context: Context, task: Task) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Check if the app can schedule exact alarms (required for Android 12 and higher)
            if (alarmManager.canScheduleExactAlarms()) {
                // Schedule the notification if the permission is granted
                val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
                    putExtra("TASK_ID", task.id)
                    putExtra("TASK_TITLE", task.name)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerAtMillis = task.completionDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

                triggerAtMillis?.let {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        it,
                        pendingIntent
                    )
                }
            } else {
                // If the permission is not granted, request the user to enable it
                requestExactAlarmPermission(context)
            }
        } catch (e: Exception) {
            // Handle the error that might occur during scheduling
            Log.e(TAG, "Error in scheduling notification: ${e.message}", e)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun requestExactAlarmPermission(context: Context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                // For Android 12 and higher, send the user to the exact alarm permission settings
                val intent = Intent().apply {
                    action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } else {
                // For devices below Android 12, exact alarms permission is not required
                Log.w("TaskNotification", "Exact alarms permission is not required for this version of Android.")
            }
        } catch (e: Exception) {
            // Handle the error that might occur during scheduling
            Log.e(TAG, "Error in requesting permission for notification alarm: ${e.message}", e)
        }
    }


    fun cancelTaskNotification(context: Context, taskId: Int) {
        try {
            val intent = Intent(context, TaskAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            // Handle the error that might occur during cancelling notification
            Log.e(TAG, "Error in cancelling notification alarm: ${e.message}", e)
        }
    }
}
