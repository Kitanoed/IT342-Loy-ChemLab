package com.example.chemlab.features.requests.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chemlab.R
import com.example.chemlab.features.auth.data.remote.RetrofitClient
import com.example.chemlab.features.auth.data.storage.TokenManager
import com.example.chemlab.features.inventory.ui.InventoryListActivity
import com.example.chemlab.features.auth.ui.DashboardActivity
import com.example.chemlab.features.auth.ui.LoginActivity
import kotlinx.coroutines.launch

class RequestsListActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var rvRequests: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var tvSubtext: TextView
    private lateinit var tvPageInfo: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnApply: Button
    private lateinit var btnReset: Button
    private lateinit var btnLogout: Button
    private lateinit var spinnerStatus: Spinner

    private lateinit var adapter: RequestsAdapter
    private var currentPage = 0
    private var totalPages = 0
    private var selectedStatus: String? = null

    private val statusOptions = arrayOf("All", "PENDING", "APPROVED", "REJECTED", "RELEASED", "COMPLETED")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests_list)

        tokenManager = TokenManager(this)
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        initializeViews()
        setupRecyclerView()
        setupSpinner()
        setupListeners()
        setupSubtext()
        fetchRequests(0)
    }

    private fun initializeViews() {
        rvRequests = findViewById(R.id.rvRequests)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvSubtext = findViewById(R.id.tvSubtext)
        tvPageInfo = findViewById(R.id.tvPageInfo)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnApply = findViewById(R.id.btnApply)
        btnReset = findViewById(R.id.btnReset)
        btnLogout = findViewById(R.id.btnLogout)
        spinnerStatus = findViewById(R.id.spinnerStatus)
    }

    private fun setupSubtext() {
        val role = tokenManager.getUser()?.role ?: "STUDENT"
        tvSubtext.text = if (role == "STUDENT") {
            "Submit and track your item requests."
        } else {
            "Manage incoming item requests."
        }
    }

    private fun setupRecyclerView() {
        adapter = RequestsAdapter { req ->
            val intent = Intent(this, RequestDetailActivity::class.java)
            intent.putExtra("REQUEST_ID", req.id)
            startActivity(intent)
        }
        rvRequests.layoutManager = LinearLayoutManager(this)
        rvRequests.adapter = adapter
    }

    private fun setupSpinner() {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = spinnerAdapter
        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStatus = if (position == 0) null else statusOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        btnPrev.setOnClickListener { if (currentPage > 0) fetchRequests(currentPage - 1) }
        btnNext.setOnClickListener { if (currentPage + 1 < totalPages) fetchRequests(currentPage + 1) }
        btnApply.setOnClickListener { fetchRequests(0) }
        btnReset.setOnClickListener {
            spinnerStatus.setSelection(0)
            selectedStatus = null
            fetchRequests(0)
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
        findViewById<Button>(R.id.navInventory).setOnClickListener {
            val intent = Intent(this, InventoryListActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.navRequests).setOnClickListener { /* Already here */ }
    }

    private fun fetchRequests(page: Int) {
        val accessToken = tokenManager.getAccessToken() ?: run {
            navigateToLogin()
            return
        }

        showLoading(true)
        tvError.visibility = View.GONE
        tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getRequestsService(accessToken)
                val response = service.listRequests(
                    page = page,
                    size = 15,
                    status = selectedStatus
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val requests = body.content

                    currentPage = body.number ?: page
                    totalPages = body.totalPages ?: 0

                    adapter.updateItems(requests)

                    if (requests.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    }

                    tvPageInfo.text = "Page ${currentPage + 1} of ${maxOf(totalPages, 1)}"
                    btnPrev.isEnabled = currentPage > 0
                    btnNext.isEnabled = currentPage + 1 < totalPages
                } else {
                    showError("Failed to load requests (${response.code()})")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message ?: "Unknown error"}")
            } finally {
                showLoading(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchRequests(currentPage)
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        rvRequests.visibility = if (isLoading) View.GONE else View.VISIBLE
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
