package com.logbook.todo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.logbook.todo.AppLifecycleObserver
import com.logbook.todo.MainActivity
import com.logbook.todo.R


class TaskAlarmReceiver : BroadcastReceiver() {
    private val tag = "Task Alarm Broadcast Receiver"

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val taskId = intent.getIntExtra("TASK_ID", -1)
            val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task"

            // Create a notification channel if necessary
            createNotificationChannel(context)


            if (!AppLifecycleObserver.isInForeground) {
                val notificationIntent =
                    Intent(context, MainActivity::class.java) // Modify as needed
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    taskId,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, "TASK_REMINDER_CHANNEL")
                    .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                    .setContentTitle("Task Reminder")
                    .setContentText("Reminder for: $taskTitle")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                try {
                    // Attempt to show the notification
                    with(NotificationManagerCompat.from(context)) {
                        notify(taskId, builder.build())
                    }
                } catch (e: SecurityException) {
                    // Handle the SecurityException when notification permission is not granted
                    Log.e(tag, "Failed to send notification due to missing permission", e)
                }
            }  else {
                Log.d(tag, "App is in the foreground.")
            }

        } catch (e: Exception) {
            // Handle the error that might occur during scheduling
            Log.e(tag, "Error in onReceive method: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        try {
            val channelId = "TASK_REMINDER_CHANNEL"
            val name = "Task Reminder"
            val descriptionText = "Channel for Task Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Check if the channel already exists
            if (notificationManager.getNotificationChannel(channelId) == null) {
                // Create the notification channel if it doesn't exist
                val channel = NotificationChannel(channelId, name, importance).apply {
                    description = descriptionText
                }
                notificationManager.createNotificationChannel(channel)
                Log.i(tag, "Notification channel created: $channelId")
            } else {
                Log.i(tag, "Notification channel already exists: $channelId")
            }
        } catch (e: Exception) {
            // Handle any error that might occur during channel creation
            Log.e(tag, "Error in creating notification channel: ${e.message}", e)
        }
    }

}