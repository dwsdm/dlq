package com.dailywisdom.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dailywisdom.app.worker.NotificationWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
        private const val NOTIFICATION_WORK_NAME = "daily_wisdom_notification_work"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Device booted, scheduling notifications")
            
            // Schedule immediate notification
            NotificationScheduler.scheduleNextNotification(context)
            
            // Schedule periodic work for daily notifications
            val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                24, TimeUnit.HOURS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOTIFICATION_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationWorkRequest
            )
        }
    }
}
