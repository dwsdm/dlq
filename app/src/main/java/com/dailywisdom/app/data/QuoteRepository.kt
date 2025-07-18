package com.dailywisdom.app.data

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

class QuoteRepository(private val quoteDao: QuoteDao, private val context: Context) {
    
    val allQuotes: LiveData<List<Quote>> = quoteDao.getAllQuotes()
    val favorites: LiveData<List<Quote>> = quoteDao.getFavorites()
    
    suspend fun getRandomQuote(): Quote? {
        return quoteDao.getRandomQuote()
    }
    
    suspend fun getQuoteOfTheDay(): Quote? {
        // Get today's date in yyyy-MM-dd format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(Date())
        
        // Get preferences to track which quote to show next
        val preferences = QuotePreferences(context)
        val lastDate = preferences.getLastQuoteOfDayDate()
        
        // If we already showed a quote today, return the same quote
        if (lastDate == today) {
            val lastIndex = preferences.getLastQuoteOfDayIndex()
            if (lastIndex >= 0) {
                return quoteDao.getQuoteByIndex(lastIndex)
            }
        }
        
        // Get total quote count
        val quoteCount = quoteDao.getQuoteCount()
        if (quoteCount == 0) return null
        
        // Check if this is the first time opening the app (no quote index saved yet)
        var nextIndex = preferences.getLastQuoteOfDayIndex()
        
        // If this is the first time or we've reached the end, pick a random starting point
        if (nextIndex < 0 || nextIndex >= quoteCount) {
            // Generate a random starting index between 0 and quoteCount-1
            nextIndex = Random().nextInt(quoteCount)
        } else {
            // Otherwise, just get the next quote in sequence
            nextIndex = (nextIndex + 1) % quoteCount
        }
        
        // Get the quote at the calculated index
        val quote = quoteDao.getQuoteByIndex(nextIndex)
        
        // Save the index and date for next time
        preferences.saveLastQuoteOfDayIndex(nextIndex)
        preferences.saveLastQuoteOfDayDate(today)
        
        return quote
    }
    
    suspend fun toggleFavorite(quote: Quote) {
        val updatedQuote = quote.copy(isFavorite = !quote.isFavorite)
        quoteDao.updateQuote(updatedQuote)
    }
    
    suspend fun searchQuotes(query: String): List<Quote> {
        return quoteDao.searchQuotes(query)
    }
    
    suspend fun searchFavorites(query: String): List<Quote> {
        return quoteDao.searchFavorites(query)
    }
    
    suspend fun getAllCategories(): List<String> {
        return quoteDao.getAllCategories()
    }
    
    suspend fun getQuotesByCategory(category: String): List<Quote> {
        return quoteDao.getQuotesByCategory(category)
    }
    
    suspend fun insertQuote(quote: Quote): Long {
        return quoteDao.insertQuote(quote)
    }
    
    companion object {
        // Function to provide default quotes
        fun getDefaultQuotes(): List<Quote> {
            return listOf(
                Quote(text = "The greatest glory in living lies not in never falling, but in rising every time we fall.", author = "Nelson Mandela", category = "Inspiration"),
                Quote(text = "The way to get started is to quit talking and begin doing.", author = "Walt Disney", category = "Motivation"),
                Quote(text = "Your time is limited, so don't waste it living someone else's life.", author = "Steve Jobs", category = "Life"),
                Quote(text = "If life were predictable it would cease to be life, and be without flavor.", author = "Eleanor Roosevelt", category = "Life"),
                Quote(text = "If you look at what you have in life, you'll always have more. If you look at what you don't have in life, you'll never have enough.", author = "Oprah Winfrey", category = "Gratitude"),
                Quote(text = "If you set your goals ridiculously high and it's a failure, you will fail above everyone else's success.", author = "James Cameron", category = "Success"),
                Quote(text = "Life is what happens when you're busy making other plans.", author = "John Lennon", category = "Life"),
                Quote(text = "Spread love everywhere you go. Let no one ever come to you without leaving happier.", author = "Mother Teresa", category = "Love"),
                Quote(text = "When you reach the end of your rope, tie a knot in it and hang on.", author = "Franklin D. Roosevelt", category = "Perseverance"),
                Quote(text = "Always remember that you are absolutely unique. Just like everyone else.", author = "Margaret Mead", category = "Humor"),
                Quote(text = "Don't judge each day by the harvest you reap but by the seeds that you plant.", author = "Robert Louis Stevenson", category = "Life"),
                Quote(text = "The future belongs to those who believe in the beauty of their dreams.", author = "Eleanor Roosevelt", category = "Dreams"),
                Quote(text = "Tell me and I forget. Teach me and I remember. Involve me and I learn.", author = "Benjamin Franklin", category = "Learning"),
                Quote(text = "The best and most beautiful things in the world cannot be seen or even touched — they must be felt with the heart.", author = "Helen Keller", category = "Beauty"),
                Quote(text = "It is during our darkest moments that we must focus to see the light.", author = "Aristotle", category = "Hope"),
                Quote(text = "Whoever is happy will make others happy too.", author = "Anne Frank", category = "Happiness"),
                Quote(text = "Do not go where the path may lead, go instead where there is no path and leave a trail.", author = "Ralph Waldo Emerson", category = "Leadership"),
                Quote(text = "You will face many defeats in life, but never let yourself be defeated.", author = "Maya Angelou", category = "Perseverance"),
                Quote(text = "In the end, it's not the years in your life that count. It's the life in your years.", author = "Abraham Lincoln", category = "Life"),
                Quote(text = "Never let the fear of striking out keep you from playing the game.", author = "Babe Ruth", category = "Courage"),
                Quote(text = "Life is either a daring adventure or nothing at all.", author = "Helen Keller", category = "Adventure"),
                Quote(text = "Many of life's failures are people who did not realize how close they were to success when they gave up.", author = "Thomas A. Edison", category = "Perseverance"),
                Quote(text = "You have brains in your head. You have feet in your shoes. You can steer yourself any direction you choose.", author = "Dr. Seuss", category = "Choice"),
                Quote(text = "If you really look closely, most overnight successes took a long time.", author = "Steve Jobs", category = "Success"),
                Quote(text = "The purpose of our lives is to be happy.", author = "Dalai Lama", category = "Purpose"),
                Quote(text = "You only live once, but if you do it right, once is enough.", author = "Mae West", category = "Life"),
                Quote(text = "To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment.", author = "Ralph Waldo Emerson", category = "Authenticity"),
                Quote(text = "The only impossible journey is the one you never begin.", author = "Tony Robbins", category = "Journey"),
                Quote(text = "In this life we cannot do great things. We can only do small things with great love.", author = "Mother Teresa", category = "Love"),
                Quote(text = "Only a life lived for others is a life worthwhile.", author = "Albert Einstein", category = "Service")
            )
        }
    }
}
