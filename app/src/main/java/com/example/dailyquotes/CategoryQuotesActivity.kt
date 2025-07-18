package com.example.dailyquotes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailyquotes.adapter.QuoteAdapter
import com.example.dailyquotes.data.Quote
import com.example.dailyquotes.databinding.ActivityCategoryQuotesBinding
import com.example.dailyquotes.util.NavigationHelper
import com.example.dailyquotes.viewmodel.CategoryQuotesViewModel

class CategoryQuotesActivity : AppCompatActivity(), QuoteAdapter.QuoteClickListener {

    private lateinit var binding: ActivityCategoryQuotesBinding
    private lateinit var viewModel: CategoryQuotesViewModel
    private lateinit var adapter: QuoteAdapter
    private var category: String = ""

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryQuotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get category from intent
        category = intent.getStringExtra(EXTRA_CATEGORY) ?: ""
        if (category.isEmpty()) {
            NavigationHelper.navigateBack(this)
            return
        }
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = category.capitalize()
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CategoryQuotesViewModel::class.java]
        
        // Setup RecyclerView
        adapter = QuoteAdapter(this)
        binding.quotesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.quotesRecyclerView.adapter = adapter
        
        // Observe quotes
        viewModel.quotes.observe(this) { quotes ->
            if (quotes.isNotEmpty()) {
                binding.loadingProgressBar.visibility = View.GONE
                binding.quotesRecyclerView.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                adapter.submitList(quotes)
            } else {
                binding.loadingProgressBar.visibility = View.GONE
                binding.quotesRecyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        }
        
        // Load quotes for the category
        viewModel.loadQuotesByCategory(category)
    }
    
    override fun onQuoteClick(quote: Quote) {
        // Copy quote to clipboard
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val quoteText = "${quote.text}\n— ${quote.author}"
        val clip = ClipData.newPlainText("Quote", quoteText)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(this, R.string.quote_copied, Toast.LENGTH_SHORT).show()
    }
    
    override fun onFavoriteClick(quote: Quote) {
        viewModel.toggleFavorite(quote)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavigationHelper.navigateBack(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        NavigationHelper.navigateBack(this)
    }
    
    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return this.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
}
