package com.example.chemlab.features.requests.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chemlab.R
import com.example.chemlab.data.api.RetrofitClient
import com.example.chemlab.data.storage.TokenManager
import com.example.chemlab.features.inventory.ui.InventoryListActivity
import com.example.chemlab.features.requests.data.dto.RequestActionBody
import com.example.chemlab.features.requests.data.dto.RequestDTO
import com.example.chemlab.features.requests.data.dto.RequestItemDTO
import com.example.chemlab.ui.auth.DashboardActivity
import com.example.chemlab.ui.auth.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RequestDetailActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvSuccess: TextView
    private lateinit var tvTitle: TextView
    private lateinit var infoCard: LinearLayout
    private lateinit var itemsCard: LinearLayout
    private lateinit var reviewCard: LinearLayout
    private lateinit var itemsContainer: LinearLayout
    private lateinit var tvItemsTitle: TextView
    private lateinit var tvNoItems: TextView

    private var requestId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_detail)

        tokenManager = TokenManager(this)
        requestId = intent.getLongExtra("REQUEST_ID", -1)

        if (!tokenManager.isLoggedIn() || requestId < 0) {
            navigateToLogin()
            return
        }

        initializeViews()
        setupListeners()
        fetchRequest()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvSuccess = findViewById(R.id.tvSuccess)
        tvTitle = findViewById(R.id.tvTitle)
        infoCard = findViewById(R.id.infoCard)
        itemsCard = findViewById(R.id.itemsCard)
        reviewCard = findViewById(R.id.reviewCard)
        itemsContainer = findViewById(R.id.itemsContainer)
        tvItemsTitle = findViewById(R.id.tvItemsTitle)
        tvNoItems = findViewById(R.id.tvNoItems)
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
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
        findViewById<Button>(R.id.navRequests).setOnClickListener {
            finish()
        }

        // Approve/Reject
        findViewById<Button>(R.id.btnApprove).setOnClickListener { performAction("approve") }
        findViewById<Button>(R.id.btnReject).setOnClickListener { performAction("reject") }
    }

    private fun fetchRequest() {
        val accessToken = tokenManager.getAccessToken() ?: run {
            navigateToLogin()
            return
        }

        showLoading(true)
        tvError.visibility = View.GONE
        tvSuccess.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getRequestsService(accessToken)
                val response = service.getRequestById(requestId)

                if (response.isSuccessful && response.body() != null) {
                    displayRequest(response.body()!!)
                } else {
                    showError("Failed to load request (${response.code()})")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message ?: "Unknown error"}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayRequest(req: RequestDTO) {
        tvTitle.text = "Request #${req.id}"
        // Update topbar active link text
        findViewById<Button>(R.id.navCurrent).text = "Request #${req.id}"

        // Info card
        infoCard.visibility = View.VISIBLE
        setField(R.id.fieldReqId, "Request ID", "#${req.id}")
        setField(R.id.fieldStatus, "Status", req.status ?: "—")
        setField(R.id.fieldRequester, "Requester", req.requesterUsername ?: "—")
        setField(R.id.fieldCreated, "Created At", req.createdAt?.substringBefore('T') ?: "—")
        setField(R.id.fieldUpdated, "Updated At", req.updatedAt?.substringBefore('T') ?: "—")

        // Remarks
        val remarks = req.remarks
        val remarksSection = findViewById<LinearLayout>(R.id.remarksSection)
        if (!remarks.isNullOrBlank()) {
            remarksSection.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvRemarks).text = remarks
        }

        // Items
        val items = req.items ?: emptyList()
        itemsCard.visibility = View.VISIBLE
        tvItemsTitle.text = "Requested Items (${items.size})"

        if (items.isEmpty()) {
            tvNoItems.visibility = View.VISIBLE
        } else {
            tvNoItems.visibility = View.GONE
            itemsContainer.removeAllViews()
            items.forEach { item -> addItemRow(item) }
        }

        // Review card (show for TECHNICIAN/ADMIN when status is PENDING)
        val userRole = tokenManager.getUser()?.role?.uppercase() ?: ""
        val canReview = (userRole == "TECHNICIAN" || userRole == "ADMIN") && req.status?.uppercase() == "PENDING"
        reviewCard.visibility = if (canReview) View.VISIBLE else View.GONE
    }

    private fun addItemRow(item: RequestItemDTO) {
        val row = layoutInflater.inflate(R.layout.detail_field, itemsContainer, false)
        val label = row.findViewById<TextView>(R.id.tvFieldLabel)
        val value = row.findViewById<TextView>(R.id.tvFieldValue)

        label.text = item.itemName ?: item.itemCode ?: "Item #${item.inventoryItemId}"
        val qty = item.quantity ?: 0
        val unit = item.unitSnapshot ?: ""
        value.text = "$qty $unit".trim()

        itemsContainer.addView(row)
    }

    private fun performAction(action: String) {
        val accessToken = tokenManager.getAccessToken() ?: run {
            navigateToLogin()
            return
        }

        val remarks = findViewById<TextInputEditText>(R.id.etActionRemarks)
            .text?.toString()?.trim()
        val body = RequestActionBody(remarks = remarks?.ifEmpty { null })

        showLoading(true)
        tvError.visibility = View.GONE
        tvSuccess.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getRequestsService(accessToken)
                val response = if (action == "approve") {
                    service.approveRequest(requestId, body)
                } else {
                    service.rejectRequest(requestId, body)
                }

                if (response.isSuccessful && response.body() != null) {
                    tvSuccess.text = "Request ${action}d successfully!"
                    tvSuccess.visibility = View.VISIBLE
                    displayRequest(response.body()!!)
                } else {
                    showError("Failed to $action request (${response.code()})")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message ?: "Unknown error"}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun setField(containerId: Int, label: String, value: String) {
        val container = findViewById<View>(containerId)
        container.findViewById<TextView>(R.id.tvFieldLabel).text = label
        container.findViewById<TextView>(R.id.tvFieldValue).text = value
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
