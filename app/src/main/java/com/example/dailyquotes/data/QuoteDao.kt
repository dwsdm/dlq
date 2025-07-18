package com.example.dailyquotes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes")
    fun getAllQuotes(): LiveData<List<Quote>>
    
    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotesList(): List<Quote>
    
    @Query("SELECT * FROM quotes WHERE isFavorite = 1")
    fun getFavorites(): LiveData<List<Quote>>
    
    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Int): Quote?
    
    @Query("SELECT * FROM quotes WHERE text LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    suspend fun searchQuotes(query: String): List<Quote>
    
    @Query("SELECT * FROM quotes WHERE isFavorite = 1 AND (text LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')")
    suspend fun searchFavorites(query: String): List<Quote>
    
    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): Quote?
    
    @Query("SELECT * FROM quotes LIMIT 1 OFFSET :index")
    suspend fun getQuoteByIndex(index: Int): Quote?
    
    @Query("SELECT DISTINCT category FROM quotes WHERE category IS NOT NULL ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT * FROM quotes WHERE category = :category ORDER BY id ASC")
    suspend fun getQuotesByCategory(category: String): List<Quote>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuotes(quotes: List<Quote>)
    
    @Update
    suspend fun updateQuote(quote: Quote)
    
    @Query("UPDATE quotes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)
    
    @Delete
    suspend fun deleteQuote(quote: Quote)
    
    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuoteCount(): Int
}
