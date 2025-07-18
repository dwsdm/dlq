package com.example.dailyquotes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyquotes.R
import com.example.dailyquotes.data.Quote

class QuoteAdapter(private val listener: QuoteClickListener) : 
    ListAdapter<Quote, QuoteAdapter.QuoteViewHolder>(QuoteDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = getItem(position)
        holder.bind(quote)
    }
    
    inner class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quoteText: TextView = itemView.findViewById(R.id.quoteText)
        private val quoteAuthor: TextView = itemView.findViewById(R.id.quoteAuthor)
        private val quoteCategory: TextView = itemView.findViewById(R.id.quoteCategory)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
        private val copyButton: ImageButton = itemView.findViewById(R.id.copyButton)
        
        fun bind(quote: Quote) {
            quoteText.text = quote.text
            quoteAuthor.text = "— ${quote.author}"
            
            if (quote.category.isNullOrEmpty()) {
                quoteCategory.visibility = View.GONE
            } else {
                quoteCategory.visibility = View.VISIBLE
                quoteCategory.text = quote.category
            }
            
            // Set favorite icon based on status
            favoriteButton.setImageResource(
                if (quote.isFavorite) R.drawable.ic_heart_filled
                else R.drawable.ic_heart
            )
            
            // Set click listeners
            favoriteButton.setOnClickListener {
                listener.onFavoriteClick(quote)
            }
            
            copyButton.setOnClickListener {
                listener.onQuoteClick(quote)
            }
        }
    }
    
    interface QuoteClickListener {
        fun onQuoteClick(quote: Quote)
        fun onFavoriteClick(quote: Quote)
    }
    
    class QuoteDiffCallback : DiffUtil.ItemCallback<Quote>() {
        override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem == newItem
        }
    }
}
