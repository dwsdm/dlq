package com.example.dailyquotes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dailyquotes.ads.AdManager
import com.example.dailyquotes.databinding.ActivityMainBinding
import com.example.dailyquotes.notification.NotificationScheduler
import com.example.dailyquotes.util.NavigationHelper
import com.example.dailyquotes.util.QuoteCountActivity
import com.example.dailyquotes.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(), AdManager.RewardAdListener {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adManager: AdManager
    private var quoteChangeCount = 0
    private var refreshCount = 0  // Track refresh button clicks for showing ads
    private var favoriteCount = 0  // Track favorite actions for showing ads
    
    private lateinit var bottomNavigation: com.google.android.material.bottomnavigation.BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Initialize AdManager
        adManager = AdManager.getInstance(this)
        adManager.setRewardAdListener(this)
        
        // Banner ad will be shown in a different way since we removed adViewContainer
        // Consider adding a banner ad view back to the layout or handle it differently
        
        adManager.loadRewardedAd()
        
        // Setup UI
        setupBottomNavigation()
        setupButtons()
        observeViewModel()
        
        // Check if we should open quote of the day
        if (intent.getBooleanExtra("OPEN_QUOTE_OF_DAY", false)) {
            NavigationHelper.navigateTo(this, QuoteOfDayActivity::class.java)
        }
    }
    
    private fun setupBottomNavigation() {
        // Find the BottomNavigationView in the window decor view since it's not in our main layout anymore
        bottomNavigation = window.decorView.findViewById(R.id.bottom_navigation)
        
        // Explicitly set the selected item to home
        bottomNavigation.selectedItemId = R.id.navigation_home
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on home
                    true
                }
                R.id.navigation_daily -> {
                    NavigationHelper.navigateTo(this, QuoteOfDayActivity::class.java)
                    true
                }
                R.id.navigation_favorites -> {
                    NavigationHelper.navigateTo(this, FavoritesActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupButtons() {
        // Initialize buttons using view binding
        val refreshButton = binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.refreshButton)
        val favoriteButton = binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.favoriteButton)
        val copyButton = binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.copyButton)
        
        refreshButton.setOnClickListener {
            viewModel.getRandomQuote()
            
            // Increment refresh count and show rewarded ad every 4 refreshes
            refreshCount++
            if (refreshCount % 4 == 0) {
                // Show rewarded ad after every 4 refreshes
                showRewardedAd()
                // Reset counter after showing ad
                refreshCount = 0
            }
        }
        
        favoriteButton.setOnClickListener {
            viewModel.currentQuote.value?.let { quote ->
                // Toggle favorite status
                val newFavoriteStatus = !quote.isFavorite
                
                // Update the UI immediately
                updateFavoriteButton(newFavoriteStatus)
                
                // Show toast message
                Toast.makeText(
                    this,
                    if (newFavoriteStatus) R.string.add_to_favorites else R.string.remove_from_favorites,
                    Toast.LENGTH_SHORT
                ).show()
                
                // Update in database
                viewModel.toggleFavorite(quote)
                
                // Only count when adding to favorites, not removing
                if (newFavoriteStatus) {
                    // Increment favorite count when a quote is added to favorites
                    favoriteCount++
                    Log.d("MainActivity", "Favorite count: $favoriteCount")
                    
                    // Show rewarded ad after every 2 favorites added
                    if (favoriteCount % 2 == 0) {
                        // Display a rewarded ad after user has added 2 quotes to favorites
                        showRewardedAd()
                        // Reset counter after showing ad
                        favoriteCount = 0
                    }
                }
            }
        }
        
        copyButton.setOnClickListener {
            viewModel.currentQuote.value?.let { quote ->
                copyToClipboard(quote.text, quote.author)
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentQuote.observe(this) { quote ->
            binding.quoteText.text = quote.text
            binding.quoteAuthor.text = "— ${quote.author}"
            
            if (quote.category.isNullOrEmpty()) {
                binding.quoteCategory.text = ""
                binding.quoteCategory.visibility = android.view.View.GONE
            } else {
                binding.quoteCategory.text = quote.category
                binding.quoteCategory.visibility = android.view.View.VISIBLE
            }
            
            // Update favorite button
            updateFavoriteButton(quote.isFavorite)
        }
        
        viewModel.quoteChangeCount.observe(this) { count ->
            quoteChangeCount = count
            
            // Show rewarded ad every second quote change
            if (count > 0 && count % 2 == 0) {
                showRewardedAd()
            }
        }
    }
    
    private fun updateFavoriteButton(isFavorite: Boolean) {
        val favoriteButton = binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.favoriteButton)
        favoriteButton.setIconResource(
            if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart
        )
    }
    
    private fun copyToClipboard(text: String, author: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val quoteText = "$text\n— $author"
        val clip = ClipData.newPlainText("Quote", quoteText)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(this, R.string.quote_copied, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Shows a rewarded ad if one is ready, otherwise loads a new one.
     * This is called after every 4 refresh clicks or every 2 quotes added to favorites.
     */
    private fun showRewardedAd() {
        if (adManager.isRewardedAdReady()) {
            adManager.showRewardedAd(this)
        } else {
            // If ad is not ready, just log and continue
            Log.d("MainActivity", "Rewarded ad is not ready yet")
            adManager.loadRewardedAd() // Load the next ad
        }
    }
    
    // Handle back button to properly navigate bottom navigation
    override fun onBackPressed() {
        if (bottomNavigation.selectedItemId != R.id.navigation_home) {
            bottomNavigation.selectedItemId = R.id.navigation_home
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        // Update notification menu item based on current state
        menu.findItem(R.id.action_notifications).isChecked = 
            NotificationScheduler.isNotificationsEnabled(this)
            
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_browse_categories -> {
                // Launch the categories activity
                NavigationHelper.navigateTo(this, CategoriesActivity::class.java)
                true
            }
            R.id.action_notifications -> {
                // Toggle notifications
                val newState = !item.isChecked
                item.isChecked = newState
                NotificationScheduler.setNotificationsEnabled(this, newState)
                
                Toast.makeText(
                    this,
                    if (newState) R.string.notifications_enabled else R.string.notifications_disabled,
                    Toast.LENGTH_SHORT
                ).show()
                
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Make sure the home tab is selected when returning to this activity
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }
    
    // AdManager.RewardAdListener implementation
    override fun onRewardAdLoaded() {
        // Ad loaded successfully
    }
    
    override fun onRewardAdFailedToLoad(errorMessage: String) {
        // Ad failed to load
    }
    
    override fun onRewardAdShown() {
        // Ad shown
    }
    
    override fun onRewardAdDismissed() {
        // Ad dismissed
        adManager.loadRewardedAd()
    }
    
    override fun onRewardEarned(amount: Int, type: String) {
        // User earned a reward
        Toast.makeText(
            this,
            "You earned a reward: $amount $type",
            Toast.LENGTH_SHORT
        ).show()
    }
}
