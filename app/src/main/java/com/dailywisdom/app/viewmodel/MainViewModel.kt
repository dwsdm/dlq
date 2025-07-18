package com.dailywisdom.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dailywisdom.app.data.Quote
import com.dailywisdom.app.data.QuoteDatabase
import com.dailywisdom.app.data.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: QuoteRepository
    val allQuotes: LiveData<List<Quote>>
    val favorites: LiveData<List<Quote>>
    
    private val _currentQuote = MutableLiveData<Quote>()
    val currentQuote: LiveData<Quote> = _currentQuote
    
    private val _quoteChangeCount = MutableLiveData<Int>(0)
    val quoteChangeCount: LiveData<Int> = _quoteChangeCount
    
    init {
        val quoteDao = QuoteDatabase.getDatabase(application, viewModelScope).quoteDao()
        repository = QuoteRepository(quoteDao, application.applicationContext)
        allQuotes = repository.allQuotes
        favorites = repository.favorites
        
        // Load a random quote initially
        getRandomQuote()
    }
    
    fun getRandomQuote() {
        viewModelScope.launch {
            val quote = withContext(Dispatchers.IO) {
                repository.getRandomQuote()
            }
            quote?.let {
                _currentQuote.value = it
                incrementQuoteChangeCount()
            }
        }
    }
    
    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch {
            // Toggle the favorite status in the database
            repository.toggleFavorite(quote)
            
            // Update the UI immediately by toggling the favorite status in the current quote
            val updatedQuote = quote.copy(isFavorite = !quote.isFavorite)
            _currentQuote.value = updatedQuote
        }
    }
    
    fun searchQuotes(query: String, callback: (List<Quote>) -> Unit) {
        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) {
                repository.searchQuotes(query)
            }
            callback(results)
        }
    }
    
    private fun incrementQuoteChangeCount() {
        val current = _quoteChangeCount.value ?: 0
        _quoteChangeCount.value = current + 1
    }
    
    fun resetQuoteChangeCount() {
        _quoteChangeCount.value = 0
    }
}
