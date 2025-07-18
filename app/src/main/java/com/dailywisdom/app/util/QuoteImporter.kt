package com.dailywisdom.app.util

import android.content.Context
import android.util.Log
import com.dailywisdom.app.data.Quote
import com.dailywisdom.app.data.QuoteDao
import com.dailywisdom.app.data.QuoteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility class for importing quotes from JSON files into the app database
 */
class QuoteImporter {
    companion object {
        private const val TAG = "QuoteImporter"
        
        /**
         * Import quotes from a raw resource JSON file
         * @param context Application context
         * @param resourceId Raw resource ID of the JSON file
         * @param callback Optional callback to report progress and completion
         */
        fun importQuotesFromRawResource(
            context: Context,
            resourceId: Int,
            callback: ImportCallback? = null
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Read JSON from raw resource
                    val inputStream = context.resources.openRawResource(resourceId)
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
                    
                    // Get database instance
                    val quoteDao = QuoteDatabase.getDatabase(
                        context, 
                        CoroutineScope(Dispatchers.IO)
                    ).quoteDao()
                    
                    // Insert quotes in batches to avoid OOM errors
                    insertQuotesInBatches(quoteDao, quotes, callback)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error importing quotes", e)
                    withContext(Dispatchers.Main) {
                        callback?.onError(e.message ?: "Unknown error")
                    }
                }
            }
        }
        
        /**
         * Insert quotes in batches to avoid memory issues with large datasets
         */
        private suspend fun insertQuotesInBatches(
            quoteDao: QuoteDao,
            quotes: List<Quote>,
            callback: ImportCallback?
        ) {
            val batchSize = 500
            val totalQuotes = quotes.size
            var importedCount = 0
            
            withContext(Dispatchers.Main) {
                callback?.onStart(totalQuotes)
            }
            
            for (i in quotes.indices step batchSize) {
                val endIndex = minOf(i + batchSize, quotes.size)
                val batch = quotes.subList(i, endIndex)
                
                quoteDao.insertAllQuotes(batch)
                importedCount += batch.size
                
                val progress = (importedCount.toFloat() / totalQuotes.toFloat() * 100).toInt()
                
                withContext(Dispatchers.Main) {
                    callback?.onProgress(importedCount, totalQuotes, progress)
                }
                
                Log.d(TAG, "Imported $importedCount/$totalQuotes quotes (${progress}%)")
            }
            
            withContext(Dispatchers.Main) {
                callback?.onComplete(totalQuotes)
            }
            
            Log.d(TAG, "Quote import completed. Total quotes: $totalQuotes")
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
    
    /**
     * Callback interface for import progress reporting
     */
    interface ImportCallback {
        fun onStart(totalQuotes: Int)
        fun onProgress(current: Int, total: Int, percentComplete: Int)
        fun onComplete(totalQuotes: Int)
        fun onError(message: String)
    }
}
