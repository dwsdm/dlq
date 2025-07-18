package com.example.dailyquotes

import android.app.Application
import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dailyquotes.ads.AdManager
import com.example.dailyquotes.data.Quote
import com.example.dailyquotes.data.QuoteDatabase
import com.example.dailyquotes.notification.NotificationScheduler
import com.example.dailyquotes.worker.NotificationWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class DailyQuotesApp : MultiDexApplication() {
    
    companion object {
        private const val TAG = "DailyQuotesApp"
        private const val NOTIFICATION_WORK_NAME = "daily_quotes_notification_work"
    }
    
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Schedule notifications
        scheduleNotifications()
        
        // Preload quotes if needed
        preloadQuotesIfNeeded()
    }
    
    private fun scheduleNotifications() {
        // Schedule the notification worker to run daily
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.DAYS,  // Repeat every day
            23, TimeUnit.HOURS  // With 1-hour flex window
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NOTIFICATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    private fun preloadQuotesIfNeeded() {
        val database = QuoteDatabase.getDatabase(this, applicationScope)
        val quoteDao = database.quoteDao()
        
        applicationScope.launch(Dispatchers.IO) {
            val count = quoteDao.getAllQuotesList().size
            if (count == 0) {
                // If no quotes in database, import them
                importQuotesFromRawResource(quoteDao)
            }
        }
    }
    
    private suspend fun importQuotesFromRawResource(quoteDao: com.example.dailyquotes.data.QuoteDao) {
        withContext(Dispatchers.IO) {
            try {
                // Load quotes from JSON file in raw folder
                val inputStream = resources.openRawResource(com.example.dailyquotes.R.raw.all_quotes)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.use { it.readText() }
                
                // Parse JSON to list of quotes
                val quotesListType = object : TypeToken<List<Quote>>() {}.type
                val quotes: List<Quote> = Gson().fromJson(jsonString, quotesListType)
                
                // Insert quotes into database
                quoteDao.insertAllQuotes(quotes)
                
                Log.d(TAG, "Imported ${quotes.size} quotes into database")
            } catch (e: Exception) {
                Log.e(TAG, "Error importing quotes: ${e.message}", e)
            }
        }
    }
}
