package com.example.dailyquotes

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.dailyquotes.databinding.ActivityImportQuotesBinding
import com.example.dailyquotes.util.QuoteImporter

class ImportQuotesActivity : AppCompatActivity(), QuoteImporter.ImportCallback {

    private lateinit var binding: ActivityImportQuotesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportQuotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.import_quotes)
        
        // Set up import button
        binding.importButton.setOnClickListener {
            startImport()
        }
    }
    
    private fun startImport() {
        // Show progress UI
        binding.importButton.isEnabled = false
        binding.progressContainer.visibility = View.VISIBLE
        binding.statusText.text = getString(R.string.preparing_import)
        
        // Start import process
        QuoteImporter.importQuotesFromRawResource(
            context = this,
            resourceId = R.raw.all_quotes,
            callback = this
        )
    }
    
    // Import callback implementations
    override fun onStart(totalQuotes: Int) {
        binding.statusText.text = getString(R.string.importing_quotes, 0, totalQuotes)
        binding.progressBar.max = 100
        binding.progressBar.progress = 0
    }
    
    override fun onProgress(current: Int, total: Int, percentComplete: Int) {
        binding.statusText.text = getString(R.string.importing_quotes, current, total)
        binding.progressBar.progress = percentComplete
    }
    
    override fun onComplete(totalQuotes: Int) {
        binding.statusText.text = getString(R.string.import_complete, totalQuotes)
        binding.progressBar.progress = 100
        binding.doneButton.visibility = View.VISIBLE
        binding.doneButton.setOnClickListener {
            finish()
        }
    }
    
    override fun onError(message: String) {
        binding.statusText.text = getString(R.string.import_error, message)
        binding.importButton.isEnabled = true
        binding.retryButton.visibility = View.VISIBLE
        binding.retryButton.setOnClickListener {
            binding.retryButton.visibility = View.GONE
            startImport()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
