package com.example.dailyquotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Quote::class], version = 1, exportSchema = false)
abstract class QuoteDatabase : RoomDatabase() {
    
    abstract fun quoteDao(): QuoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: QuoteDatabase? = null
        
        fun getDatabase(context: Context, scope: CoroutineScope): QuoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteDatabase::class.java,
                    "quote_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(QuoteDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class QuoteDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database creation is handled by DailyWisdomApp
            }
        }
    }
}
