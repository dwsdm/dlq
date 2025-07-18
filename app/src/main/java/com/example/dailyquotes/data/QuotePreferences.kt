package com.example.dailyquotes.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class to manage quote preferences and state
 */
class QuotePreferences(context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * Get the index of the last shown quote of the day
     * @return The index of the last shown quote, or -1 if no quote has been shown yet
     */
    fun getLastQuoteOfDayIndex(): Int {
        return preferences.getInt(KEY_LAST_QUOTE_INDEX, -1)
    }
    
    /**
     * Save the index of the last shown quote of the day
     * @param index The index of the quote that was shown
     */
    fun saveLastQuoteOfDayIndex(index: Int) {
        preferences.edit().putInt(KEY_LAST_QUOTE_INDEX, index).apply()
    }
    
    /**
     * Get the date when the last quote of the day was shown
     * @return The date string in format "yyyy-MM-dd", or empty string if no quote has been shown
     */
    fun getLastQuoteOfDayDate(): String {
        return preferences.getString(KEY_LAST_QUOTE_DATE, "") ?: ""
    }
    
    /**
     * Save the date when a quote of the day was shown
     * @param dateString The date string in format "yyyy-MM-dd"
     */
    fun saveLastQuoteOfDayDate(dateString: String) {
        preferences.edit().putString(KEY_LAST_QUOTE_DATE, dateString).apply()
    }
    
    companion object {
        private const val PREFERENCES_NAME = "quote_preferences"
        private const val KEY_LAST_QUOTE_INDEX = "last_quote_index"
        private const val KEY_LAST_QUOTE_DATE = "last_quote_date"
    }
}
