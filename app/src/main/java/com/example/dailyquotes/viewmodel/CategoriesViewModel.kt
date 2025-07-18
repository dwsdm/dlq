package com.example.dailyquotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dailyquotes.data.QuoteDatabase
import com.example.dailyquotes.data.QuoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: QuoteRepository
    
    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories
    
    init {
        val quoteDao = QuoteDatabase.getDatabase(application, viewModelScope).quoteDao()
        repository = QuoteRepository(quoteDao, application.applicationContext)
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            val categoryList = withContext(Dispatchers.IO) {
                repository.getAllCategories()
            }
            _categories.value = categoryList
        }
    }
}
