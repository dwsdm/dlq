package com.dailywisdom.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dailywisdom.app.notification.NotificationScheduler
import com.dailywisdom.app.util.NavigationHelper
import java.util.Calendar

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DURATION = 1500L // 1.5 seconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the content view to our new splash screen layout
        setContentView(R.layout.activity_splash)
        
        // Ensure notifications are scheduled properly
        ensureNotificationsAreScheduled()
        
        // Delay the transition to MainActivity to show the splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Use NavigationHelper to navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close this activity so it's not in the back stack
            
            // Use custom fade animations for splash screen
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }, SPLASH_DURATION)
    }
    
    private fun ensureNotificationsAreScheduled() {
        // Only proceed if notifications are enabled
        if (!NotificationScheduler.isNotificationsEnabled(this)) {
            Log.d(TAG, "Notifications are disabled, not scheduling")
            return
        }
        
        // Check if a notification has been sent today
        if (!NotificationScheduler.hasNotificationBeenSentToday(this)) {
            // Check if we're past 9 AM and should show a notification now
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            
            if (currentHour >= 9) {
                Log.d(TAG, "It's past 9 AM and no notification has been sent today, sending now")
                // Send notification via broadcast to the NotificationReceiver
                val intent = Intent(this, com.dailywisdom.app.notification.NotificationReceiver::class.java)
                sendBroadcast(intent)
            } else {
                Log.d(TAG, "It's before 9 AM, notification will be sent at scheduled time")
            }
        } else {
            Log.d(TAG, "Notification already sent today, scheduling for tomorrow")
        }
        
        // Always ensure the next notification is scheduled
        NotificationScheduler.scheduleNextNotification(this)
    }
}
