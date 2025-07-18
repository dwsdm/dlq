package com.dailywisdom.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import java.util.*
import java.util.Date

class NotificationScheduler {
    
    companion object {
        private const val TAG = "NotificationScheduler"
        private const val PREFS_NAME = "daily_wisdom_prefs"
        private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
        private const val LAST_NOTIFICATION_TIME_KEY = "last_notification_time"
        private const val NOTIFICATION_REQUEST_CODE = 1234
        
        fun scheduleNextNotification(context: Context) {
            if (!isNotificationsEnabled(context)) {
                Log.d(TAG, "Notifications are disabled, not scheduling")
                return
            }
            
            // Check if we've already sent a notification today
            if (hasNotificationBeenSentToday(context)) {
                Log.d(TAG, "Already sent notification today, scheduling for tomorrow")
            }
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Set alarm for tomorrow at 9:00 AM
            val calendar = Calendar.getInstance()
            val currentTimeMillis = System.currentTimeMillis()
            calendar.timeInMillis = currentTimeMillis
            
            // If it's already past 9:00 AM, schedule for tomorrow
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 9) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            // Set time to 9:00 AM
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // If the calculated time is in the past (e.g., it's 8:30 AM and we're setting for 9:00 AM today),
            // move to tomorrow
            if (calendar.timeInMillis <= currentTimeMillis) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            // Log the scheduled time
            val date = Date(calendar.timeInMillis)
            Log.d(TAG, "Scheduling next notification for: $date")
            
            // Schedule the alarm with different methods depending on API level
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // For Android 12+, check if we can schedule exact alarms
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        Log.d(TAG, "Scheduled exact alarm on Android 12+")
                    } else {
                        // Fall back to inexact alarm if we don't have permission
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        Log.d(TAG, "Scheduled inexact alarm on Android 12+ (no permission for exact)")
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled exact alarm on Android 6-11")
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled exact alarm on Android 5 or below")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling alarm", e)
                // Try fallback method
                try {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Used fallback alarm scheduling method")
                } catch (e2: Exception) {
                    Log.e(TAG, "Fallback alarm scheduling also failed", e2)
                }
            }
        }
        
        fun cancelNotifications(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Cancel the alarm
            alarmManager.cancel(pendingIntent)
        }
        
        fun setNotificationsEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply()
            
            if (enabled) {
                scheduleNextNotification(context)
            } else {
                cancelNotifications(context)
            }
        }
        
        fun isNotificationsEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(NOTIFICATIONS_ENABLED_KEY, true) // Enabled by default
        }
        
        fun recordNotificationSent(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(LAST_NOTIFICATION_TIME_KEY, System.currentTimeMillis()).apply()
            Log.d(TAG, "Recorded notification sent at ${Date()}")
        }
        
        fun hasNotificationBeenSentToday(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastNotificationTime = prefs.getLong(LAST_NOTIFICATION_TIME_KEY, 0)
            
            if (lastNotificationTime == 0L) {
                return false
            }
            
            // Check if the last notification was sent today
            val lastNotificationCalendar = Calendar.getInstance()
            lastNotificationCalendar.timeInMillis = lastNotificationTime
            
            val todayCalendar = Calendar.getInstance()
            
            return lastNotificationCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                   lastNotificationCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
        }
    }
}
