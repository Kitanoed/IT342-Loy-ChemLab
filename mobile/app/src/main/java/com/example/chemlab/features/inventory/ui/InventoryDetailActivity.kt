package com.example.chemlab.features.inventory.ui

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
import com.example.chemlab.features.auth.data.remote.RetrofitClient
import com.example.chemlab.features.auth.data.storage.TokenManager
import com.example.chemlab.features.auth.ui.DashboardActivity
import com.example.chemlab.features.auth.ui.LoginActivity
import com.example.chemlab.features.inventory.data.dto.InventoryItemDTO
import kotlinx.coroutines.launch

class InventoryDetailActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var detailCard: LinearLayout
    private lateinit var descriptionCard: LinearLayout
    private lateinit var tvItemName: TextView

    private var itemId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_detail)

        tokenManager = TokenManager(this)
        itemId = intent.getLongExtra("ITEM_ID", -1)

        if (!tokenManager.isLoggedIn() || itemId < 0) {
            navigateToLogin()
            return
        }

        initializeViews()
        setupListeners()
        fetchItem()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        detailCard = findViewById(R.id.blueprintFrame)
        descriptionCard = findViewById(R.id.safetyShield)
        tvItemName = findViewById(R.id.tvItemName)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
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
            finish()
        }
        findViewById<Button>(R.id.navRequests).setOnClickListener {
            try {
                val clazz = Class.forName("com.example.chemlab.features.requests.ui.RequestsListActivity")
                val intent = Intent(this, clazz)
                startActivity(intent)
            } catch (e: Exception) { /* Not yet available */ }
        }
    }

    private fun fetchItem() {
        val accessToken = tokenManager.getAccessToken() ?: run {
            navigateToLogin()
            return
        }

        showLoading(true)
        tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.getInventoryService(accessToken)
                val response = service.getItemById(itemId)

                if (response.isSuccessful && response.body() != null) {
                    displayItem(response.body()!!)
                } else {
                    showError("Failed to load item (${response.code()})")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message ?: "Unknown error"}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayItem(item: InventoryItemDTO) {
        detailCard.visibility = View.VISIBLE

        // Matches web InventoryItem.jsx detailRows
        setField(R.id.fieldCode, "Code", item.itemCode ?: "—")
        setField(R.id.fieldName, "Name", item.itemName ?: "—")
        setField(R.id.fieldType, "Type", item.itemType ?: "—")
        setField(R.id.fieldCategory, "Category", item.category ?: "—")
        setField(R.id.fieldQuantity, "Quantity", "${item.quantity?.toInt() ?: 0} ${item.unit ?: ""}")
        setField(R.id.fieldStatus, "Status", item.status ?: "—")
        setField(R.id.fieldLocation, "Location", item.storageLocation ?: "—")
        setField(R.id.fieldLot, "Lot Number", item.lotNumber ?: "—")
        setField(R.id.fieldExpiry, "Expiry Date", item.expiryDate ?: "—")
        setField(R.id.fieldLabId, "Lab ID", item.labId?.toString() ?: "—")
        setField(R.id.fieldVersion, "Version", item.version?.toString() ?: "—")
        setField(R.id.fieldUpdated, "Updated At", item.updatedAt ?: "—")

        // Description card
        val desc = item.description
        val safety = item.safetyNotes
        if (!desc.isNullOrBlank() || !safety.isNullOrBlank()) {
            descriptionCard.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvSafetyNotes).text = safety ?: "No safety notes provided."
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
