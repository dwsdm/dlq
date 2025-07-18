package com.example.dailyquotes.util

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dailyquotes.R
import com.example.dailyquotes.data.QuoteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Simple utility activity to display the total number of quotes in the database
 */
class QuoteCountActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quote_count)
        
        val textView = findViewById<TextView>(R.id.quoteCountText)
        
        CoroutineScope(Dispatchers.Main).launch {
            val count = withContext(Dispatchers.IO) {
                val quoteDao = QuoteDatabase.getDatabase(
                    this@QuoteCountActivity,
                    CoroutineScope(Dispatchers.Main)
                ).quoteDao()
                
                quoteDao.getQuoteCount()
            }
            
            textView.text = "Total quotes in database: $count"
        }
    }
}
