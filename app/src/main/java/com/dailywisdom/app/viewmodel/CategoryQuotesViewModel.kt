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

class CategoryQuotesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: QuoteRepository
    
    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes
    
    init {
        val quoteDao = QuoteDatabase.getDatabase(application, viewModelScope).quoteDao()
        repository = QuoteRepository(quoteDao, application.applicationContext)
    }
    
    fun loadQuotesByCategory(category: String) {
        viewModelScope.launch {
            val quotesList = withContext(Dispatchers.IO) {
                repository.getQuotesByCategory(category)
            }
            _quotes.value = quotesList
        }
    }
    
    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(quote)
            
            // Refresh the quotes list to update the UI
            val updatedQuotes = repository.getQuotesByCategory(quote.category ?: "")
            withContext(Dispatchers.Main) {
                _quotes.value = updatedQuotes
            }
        }
    }
}
