package com.dailywisdom.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val author: String,
    val category: String? = null,
    val isFavorite: Boolean = false
)
