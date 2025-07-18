package com.example.dailyquotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dailyquotes.data.Quote
import com.example.dailyquotes.data.QuoteDatabase
import com.example.dailyquotes.data.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class QuoteOfDayViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: QuoteRepository
    
    private val _quoteOfTheDay = MutableLiveData<Quote>()
    val quoteOfTheDay: LiveData<Quote> = _quoteOfTheDay
    
    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate
    
    init {
        val quoteDao = QuoteDatabase.getDatabase(application, viewModelScope).quoteDao()
        repository = QuoteRepository(quoteDao, application.applicationContext)
        
        // Set current date
        updateCurrentDate()
        
        // Load quote of the day
        loadQuoteOfTheDay()
    }
    
    private fun updateCurrentDate() {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        _currentDate.value = dateFormat.format(Date())
    }
    
    private fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            val quote = withContext(Dispatchers.IO) {
                repository.getQuoteOfTheDay()
            }
            quote?.let {
                _quoteOfTheDay.value = it
            }
        }
    }
    
    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch {
            // Toggle the favorite status in the database
            repository.toggleFavorite(quote)
            
            // Update the UI immediately by toggling the favorite status in the current quote
            val updatedQuote = quote.copy(isFavorite = !quote.isFavorite)
            _quoteOfTheDay.value = updatedQuote
        }
    }
}
