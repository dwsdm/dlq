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

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: QuoteRepository
    val favorites: LiveData<List<Quote>>
    
    private val _searchResults = MutableLiveData<List<Quote>>()
    val searchResults: LiveData<List<Quote>> = _searchResults
    
    private val _isSearching = MutableLiveData<Boolean>(false)
    val isSearching: LiveData<Boolean> = _isSearching
    
    init {
        val quoteDao = QuoteDatabase.getDatabase(application, viewModelScope).quoteDao()
        repository = QuoteRepository(quoteDao, application.applicationContext)
        favorites = repository.favorites
    }
    
    fun searchFavorites(query: String) {
        if (query.isEmpty()) {
            _isSearching.value = false
            return
        }
        
        _isSearching.value = true
        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) {
                repository.searchFavorites(query)
            }
            _searchResults.value = results
        }
    }
    
    fun clearSearch() {
        _isSearching.value = false
    }
    
    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch {
            repository.toggleFavorite(quote)
        }
    }
}
