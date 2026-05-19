package com.example.chemlab.features.inventory.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chemlab.R
import com.example.chemlab.features.auth.data.remote.RetrofitClient
import com.example.chemlab.features.auth.data.storage.TokenManager
import com.example.chemlab.features.auth.ui.DashboardActivity
import com.example.chemlab.features.auth.ui.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class InventoryListActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var rvInventory: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var tvPageInfo: TextView
    private lateinit var tvSubtext: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnSearch: Button
    private lateinit var btnReset: Button
    private lateinit var btnLogout: Button
    private lateinit var etSearch: TextInputEditText

    private lateinit var adapter: InventoryAdapter
    private var currentPage = 0
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_list)

        tokenManager = TokenManager(this)

        if (!tokenManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        initializeViews()
        setupRecyclerView()
        setupListeners()
        setupSubtext()
        fetchInventory(0)
    }

    private fun initializeViews() {
        rvInventory = findViewById(R.id.rvInventory)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvPageInfo = findViewById(R.id.tvPageInfo)
        tvSubtext = findViewById(R.id.tvSubtext)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnSearch = findViewById(R.id.btnSearch)
        btnReset = findViewById(R.id.btnReset)
        btnLogout = findViewById(R.id.btnLogout)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun setupSubtext() {
        val role = tokenManager.getUser()?.role ?: "STUDENT"
        tvSubtext.text = "Read-only inventory list for $role role."
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter { item ->
            val intent = Intent(this, InventoryDetailActivity::class.java)
            intent.putExtra("ITEM_ID", item.id)
            startActivity(intent)
        }
        rvInventory.layoutManager = LinearLayoutManager(this)
        rvInventory.adapter = adapter
    }

    private fun setupListeners() {
        btnPrev.setOnClickListener { if (currentPage > 0) fetchInventory(currentPage - 1) }
        btnNext.setOnClickListener { if (currentPage + 1 < totalPages) fetchInventory(currentPage + 1) }
        btnSearch.setOnClickListener { fetchInventory(0) }
        btnReset.setOnClickListener {
            etSearch.setText("")
            fetchInventory(0)
        }
        btnLogout.setOnClickListener {
            tokenManager.logout()
            navigateToLogin()
        }

        // Topbar navigation
        findViewById<Button>(R.id.navDashboard).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.navInventory).setOnClickListener { /* Already here */ }
        findViewById<Button>(R.id.navRequests).setOnClickListener {
            try {
                val clazz = Class.forName("com.example.chemlab.features.requests.ui.RequestsListActivity")
                val intent = Intent(this, clazz)
                startActivity(intent)
            } catch (e: Exception) { /* Not yet available */ }
        }
    }

    private fun fetchInventory(page: Int) {
        val accessToken = tokenManager.getAccessToken() ?: run {
            navigateToLogin()
            return
        }

        showLoading(true)
        tvError.visibility = View.GONE
        tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getInventoryService(accessToken)
                val searchQuery = etSearch.text?.toString()?.trim()?.ifEmpty { null }

                val response = service.listInventory(
                    page = page,
                    size = 10,
                    search = searchQuery
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val items = body.content

                    currentPage = body.page ?: page
                    totalPages = body.totalPages ?: 0

                    adapter.updateItems(items)

                    if (items.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    }

                    tvPageInfo.text = "Page ${currentPage + 1} of ${maxOf(totalPages, 1)}"
                    btnPrev.isEnabled = currentPage > 0
                    btnNext.isEnabled = currentPage + 1 < totalPages
                } else {
                    showError("Failed to load inventory (${response.code()})")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message ?: "Unknown error"}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        rvInventory.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
