package com.dailywisdom.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dailywisdom.app.adapter.QuoteAdapter
import com.dailywisdom.app.ads.AdManager
import com.dailywisdom.app.data.Quote
import com.dailywisdom.app.databinding.ActivityFavoritesBinding
import com.dailywisdom.app.util.NavigationHelper
import com.dailywisdom.app.viewmodel.FavoritesViewModel

class FavoritesActivity : AppCompatActivity(), QuoteAdapter.QuoteClickListener {
    
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var viewModel: FavoritesViewModel
    private lateinit var quoteAdapter: QuoteAdapter
    private lateinit var adManager: AdManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[FavoritesViewModel::class.java]
        
        // Initialize AdManager
        adManager = AdManager.getInstance(this)
        adManager.loadBannerAd(binding.adViewContainer)
        
        // Setup UI
        setupRecyclerView()
        setupBottomNavigation()
        setupSearchView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(this)
        
        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = quoteAdapter
        }
    }
    
    override fun onQuoteClick(quote: Quote) {
        copyQuoteToClipboard(quote)
    }
    
    override fun onFavoriteClick(quote: Quote) {
        viewModel.toggleFavorite(quote)
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_favorites
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    NavigationHelper.navigateTo(this, MainActivity::class.java, true, true)
                    true
                }
                R.id.navigation_daily -> {
                    NavigationHelper.navigateTo(this, QuoteOfDayActivity::class.java, true)
                    true
                }
                R.id.navigation_favorites -> {
                    // Already on favorites
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.clearSearch()
                } else {
                    viewModel.searchFavorites(newText)
                }
                return true
            }
        })
        
        binding.searchView.setOnCloseListener {
            viewModel.clearSearch()
            false
        }
    }
    
    private fun observeViewModel() {
        viewModel.favorites.observe(this) { favorites ->
            if (!viewModel.isSearching.value!!) {
                updateQuotesList(favorites)
            }
        }
        
        viewModel.searchResults.observe(this) { results ->
            if (viewModel.isSearching.value!!) {
                updateQuotesList(results)
            }
        }
        
        viewModel.isSearching.observe(this) { isSearching ->
            if (!isSearching) {
                viewModel.favorites.value?.let { updateQuotesList(it) }
            }
        }
    }
    
    private fun updateQuotesList(quotes: List<Quote>) {
        quoteAdapter.submitList(quotes)
        
        if (quotes.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.favoritesRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.favoritesRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun copyQuoteToClipboard(quote: Quote) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val quoteText = "${quote.text}\n— ${quote.author}"
        val clip = ClipData.newPlainText("Quote", quoteText)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(this, R.string.quote_copied, Toast.LENGTH_SHORT).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        NavigationHelper.navigateBack(this)
        return true
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        NavigationHelper.navigateBack(this)
    }
    
    override fun onResume() {
        super.onResume()
        // Make sure the favorites tab is selected when returning to this activity
        binding.bottomNavigation.selectedItemId = R.id.navigation_favorites
    }
}
