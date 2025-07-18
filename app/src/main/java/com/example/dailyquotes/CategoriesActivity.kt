package com.example.dailyquotes

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailyquotes.adapter.CategoryAdapter
import com.example.dailyquotes.databinding.ActivityCategoriesBinding
import com.example.dailyquotes.util.NavigationHelper
import com.example.dailyquotes.viewmodel.CategoriesViewModel

class CategoriesActivity : AppCompatActivity(), CategoryAdapter.CategoryClickListener {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.categories)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]
        
        // Setup RecyclerView
        adapter = CategoryAdapter(this)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.categoriesRecyclerView.adapter = adapter
        
        // Observe categories
        viewModel.categories.observe(this) { categories ->
            if (categories.isNotEmpty()) {
                binding.loadingProgressBar.visibility = View.GONE
                binding.categoriesRecyclerView.visibility = View.VISIBLE
                adapter.submitList(categories)
            } else {
                binding.loadingProgressBar.visibility = View.VISIBLE
                binding.categoriesRecyclerView.visibility = View.GONE
            }
        }
        
        // Load categories
        viewModel.loadCategories()
    }
    
    override fun onCategoryClick(category: String) {
        // Navigate to CategoryQuotesActivity when a category is clicked
        val intent = Intent(this, CategoryQuotesActivity::class.java).apply {
            putExtra(CategoryQuotesActivity.EXTRA_CATEGORY, category)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
}
