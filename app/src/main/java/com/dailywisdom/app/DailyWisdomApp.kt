package com.dailywisdom.app

import android.app.Application
import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dailywisdom.app.ads.AdManager
import com.dailywisdom.app.data.Quote
import com.dailywisdom.app.data.QuoteDatabase
import com.dailywisdom.app.notification.NotificationScheduler
import com.dailywisdom.app.worker.NotificationWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class DailyWisdomApp : MultiDexApplication() {
    
    companion object {
        private const val TAG = "DailyWisdomApp"
        private const val NOTIFICATION_WORK_NAME = "daily_wisdom_notification_work"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AdManager
        AdManager.getInstance(this)
        
        // Initialize database and preload quotes if needed
        CoroutineScope(Dispatchers.IO).launch {
            preloadQuotesIfNeeded()
            
            // Schedule notifications after quotes are loaded
            withContext(Dispatchers.Main) {
                if (NotificationScheduler.isNotificationsEnabled(applicationContext)) {
                    scheduleNotifications()
                }
            }
        }
    }
    
    private fun scheduleNotifications() {
        // Schedule immediate notification if needed
        NotificationScheduler.scheduleNextNotification(this)
        
        // Schedule periodic work for daily notifications with a more frequent interval
        // This ensures the notification system is more robust
        val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.HOURS  // Check more frequently to ensure we don't miss a day
        )
        .setInitialDelay(15, TimeUnit.MINUTES) // Small initial delay to avoid immediate execution
        .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NOTIFICATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // Use REPLACE to ensure we always have the latest configuration
            notificationWorkRequest
        )
        
        Log.d(TAG, "Scheduled reliable daily notification work")
    }
    
    private suspend fun preloadQuotesIfNeeded() {
        Log.d(TAG, "Database initialization started")
        
        // Get the database instance
        val quoteDao = QuoteDatabase.getDatabase(
            this,
            CoroutineScope(Dispatchers.IO)
        ).quoteDao()
        
        // Check if database already has quotes
        val quoteCount = quoteDao.getQuoteCount()
        
        if (quoteCount == 0) {
            Log.d(TAG, "Database is empty, importing quotes from raw resource")
            importQuotesFromRawResource(quoteDao)
        } else {
            Log.d(TAG, "Database already has $quoteCount quotes")
        }
    }
    
    private suspend fun importQuotesFromRawResource(quoteDao: com.dailywisdom.app.data.QuoteDao) {
        try {
            // Read JSON from raw resource
            val inputStream = resources.openRawResource(R.raw.all_quotes)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            
            // Parse JSON
            val gson = Gson()
            val quoteListType = object : TypeToken<List<WebAppQuote>>() {}.type
            val webAppQuotes: List<WebAppQuote> = gson.fromJson(jsonString, quoteListType)
            
            Log.d(TAG, "Parsed ${webAppQuotes.size} quotes from JSON")
            
            // Convert to app Quote model
            val quotes = webAppQuotes.map { webQuote ->
                Quote(
                    text = webQuote.text,
                    author = webQuote.author,
                    category = webQuote.category,
                    isFavorite = false
                )
            }
            
            // Insert quotes in batches to avoid OOM errors
            val batchSize = 500
            var importedCount = 0
            
            for (i in quotes.indices step batchSize) {
                val endIndex = minOf(i + batchSize, quotes.size)
                val batch = quotes.subList(i, endIndex)
                
                quoteDao.insertAllQuotes(batch)
                importedCount += batch.size
                
                val progress = (importedCount.toFloat() / quotes.size.toFloat() * 100).toInt()
                Log.d(TAG, "Imported $importedCount/${quotes.size} quotes (${progress}%)")
            }
            
            Log.d(TAG, "Quote import completed. Total quotes: ${quotes.size}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error importing quotes", e)
        }
    }
    
    /**
     * Data class representing the quote format from the web app
     */
    data class WebAppQuote(
        val text: String,
        val author: String,
        val category: String? = null
    )
}
