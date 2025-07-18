package com.example.dailyquotes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dailyquotes.ads.AdManager
import com.example.dailyquotes.databinding.ActivityQuoteOfDayBinding
import com.example.dailyquotes.util.NavigationHelper
import com.example.dailyquotes.viewmodel.QuoteOfDayViewModel

class QuoteOfDayActivity : AppCompatActivity(), AdManager.InterstitialAdListener {
    
    private lateinit var binding: ActivityQuoteOfDayBinding
    private lateinit var viewModel: QuoteOfDayViewModel
    private lateinit var adManager: AdManager
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuoteOfDayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[QuoteOfDayViewModel::class.java]
        
        // Initialize AdManager
        adManager = AdManager.getInstance(this)
        adManager.loadBannerAd(binding.adViewContainer)
        
        // Set up interstitial ad
        adManager.setInterstitialAdListener(this)
        adManager.loadInterstitialAd()
        
        // Setup UI
        setupBottomNavigation()
        setupButtons()
        observeViewModel()
        
        // Show interstitial ad after 3 seconds
        handler.postDelayed({
            if (adManager.isInterstitialAdReady()) {
                adManager.showInterstitialAd(this)
            } else {
                // If ad is not ready yet, try to load it again
                adManager.loadInterstitialAd()
            }
        }, 3000) // 3 seconds delay
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_daily
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    NavigationHelper.navigateTo(this, MainActivity::class.java, true, true)
                    true
                }
                R.id.navigation_daily -> {
                    // Already on daily
                    true
                }
                R.id.navigation_favorites -> {
                    NavigationHelper.navigateTo(this, FavoritesActivity::class.java, true)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupButtons() {
        binding.favoriteButton.setOnClickListener {
            viewModel.quoteOfTheDay.value?.let { quote ->
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
            }
        }
        
        binding.copyButton.setOnClickListener {
            copyQuoteToClipboard()
        }
    }
    
    private fun observeViewModel() {
        viewModel.quoteOfTheDay.observe(this) { quote ->
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
        
        viewModel.currentDate.observe(this) { date ->
            binding.dateText.text = date
        }
    }
    
    private fun updateFavoriteButton(isFavorite: Boolean) {
        // For MaterialButton, we need to change both the icon and tint
        if (isFavorite) {
            // Use filled heart icon with red tint
            binding.favoriteButton.setIconResource(R.drawable.ic_heart_filled)
            binding.favoriteButton.setIconTint(android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.accent_secondary, theme)
            ))
            binding.favoriteButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.primary_light, theme)
            )
        } else {
            // Use outline heart icon with white tint
            binding.favoriteButton.setIconResource(R.drawable.ic_heart)
            binding.favoriteButton.setIconTint(android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.white, theme)
            ))
            binding.favoriteButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.primary_light, theme)
            )
        }
    }
    
    private fun copyQuoteToClipboard() {
        viewModel.quoteOfTheDay.value?.let { quote ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val quoteText = "${quote.text}\n— ${quote.author}"
            val clip = ClipData.newPlainText("Quote", quoteText)
            clipboard.setPrimaryClip(clip)
            
            Toast.makeText(this, R.string.quote_copied, Toast.LENGTH_SHORT).show()
        }
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
        // Make sure the daily tab is selected when returning to this activity
        binding.bottomNavigation.selectedItemId = R.id.navigation_daily
    }
    
    // InterstitialAdListener implementation
    override fun onInterstitialAdLoaded() {
        // Ad loaded successfully
    }
    
    override fun onInterstitialAdFailedToLoad(errorMessage: String) {
        // Ad failed to load
    }
    
    override fun onInterstitialAdShown() {
        // Ad was shown
    }
    
    override fun onInterstitialAdDismissed() {
        // Ad was dismissed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Remove any pending callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
    }
}
