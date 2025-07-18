package com.example.dailyquotes.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dailyquotes.notification.NotificationReceiver
import com.example.dailyquotes.notification.NotificationScheduler
import java.util.Calendar

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    companion object {
        private const val TAG = "NotificationWorker"
    }
    
    override fun doWork(): Result {
        // Check if notifications are enabled
        if (!NotificationScheduler.isNotificationsEnabled(context)) {
            Log.d(TAG, "Notifications are disabled, skipping")
            return Result.success()
        }
        
        // Check if it's time to show a notification (around 9 AM)
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Only show notification if it's between 8 AM and 10 AM to avoid multiple notifications
        // This worker runs every hour, but we only want to show the notification once per day
        if (currentHour in 8..10) {
            Log.d(TAG, "It's notification time ($currentHour:00), sending notification")
            
            // Trigger notification
            val intent = Intent(context, NotificationReceiver::class.java)
            context.sendBroadcast(intent)
        } else {
            Log.d(TAG, "Not notification time yet (current hour: $currentHour), skipping")
            
            // Still make sure the AlarmManager notification is scheduled
            NotificationScheduler.scheduleNextNotification(context)
        }
        
        return Result.success()
    }
}
