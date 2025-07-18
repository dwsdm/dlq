package com.example.dailyquotes.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.dailyquotes.MainActivity
import com.example.dailyquotes.R
import com.example.dailyquotes.data.Quote
import com.example.dailyquotes.data.QuoteDatabase
import com.example.dailyquotes.data.QuoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "daily_wisdom_channel"
        const val NOTIFICATION_ID = 1001
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        // Create notification channel for Android O and above
        createNotificationChannel(context)
        
        // Get quote of the day and show notification
        CoroutineScope(Dispatchers.Main).launch {
            val quote = getQuoteOfTheDay(context)
            showNotification(context, quote)
            
            // Record that we sent a notification today
            NotificationScheduler.recordNotificationSent(context)
            
            // Schedule next notification
            NotificationScheduler.scheduleNextNotification(context)
        }
    }
    
    private suspend fun getQuoteOfTheDay(context: Context): Quote? {
        val quoteDao = QuoteDatabase.getDatabase(
            context,
            CoroutineScope(Dispatchers.Main)
        ).quoteDao()
        val repository = QuoteRepository(quoteDao, context)
        
        return withContext(Dispatchers.IO) {
            repository.getQuoteOfTheDay()
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val description = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(context: Context, quote: Quote?) {
        if (quote == null) return
        
        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_QUOTE_OF_DAY", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create a direct intent to QuoteOfDayActivity
        val quoteOfDayIntent = Intent(context, com.example.dailyquotes.QuoteOfDayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val quoteOfDayPendingIntent = PendingIntent.getActivity(
            context, 1, quoteOfDayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create a short teaser from the quote text to build suspense
        val words = quote.text.split(" ")
        val teaserWordCount = minOf(5, words.size) // Show only first 5 words or less if quote is shorter
        val teaser = words.take(teaserWordCount).joinToString(" ") + "..."
        
        // Build notification with the teaser as the title
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_quote)
            .setContentTitle("Daily Wisdom") // Set app name as the title
            .setContentText(teaser) // Use teaser as the content text
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_quote,
                "View Quote of the Day",
                quoteOfDayPendingIntent
            )
        
        // Log notification being shown
        android.util.Log.d("NotificationReceiver", "Showing notification for quote: ${quote.text}")
        
        // Show notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
