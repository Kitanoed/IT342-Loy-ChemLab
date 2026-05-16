package com.example.chemlab.features.auth.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.chemlab.R
import com.example.chemlab.features.auth.data.storage.TokenManager
import com.example.chemlab.features.inventory.ui.InventoryListActivity
import com.example.chemlab.features.requests.ui.RequestsListActivity
import com.example.chemlab.features.auth.ui.LoginActivity
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var tvGreeting: TextView
    private lateinit var tvName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUserId: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvLastUpdated: TextView
    private lateinit var tvRoleBadge: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tokenManager = TokenManager(this)

        // Check if logged in
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        initializeViews()
        setupUI()
        setupListeners()
    }

    private fun initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        tvName = findViewById(R.id.tvName)
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        tvUserId = findViewById(R.id.tvUserId)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        tvLastUpdated = findViewById(R.id.tvLastUpdated)
        tvRoleBadge = findViewById(R.id.tvRoleBadge)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupUI() {
        val user = tokenManager.getUser()
        if (user != null) {
            // Greeting (matches web: "Welcome, {username}!")
            tvGreeting.text = "Welcome, ${user.username}!"

            // Profile grid values (matches web Dashboard.jsx)
            val fullName = listOfNotNull(user.firstName, user.lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifEmpty { user.username }
            tvName.text = fullName
            tvUsername.text = user.username
            tvEmail.text = user.email
            tvUserId.text = "#${user.id}"
            tvMemberSince.text = formatDate(user.createdAt)
            tvLastUpdated.text = formatDate(user.updatedAt)

            // Role badge
            val role = user.role ?: "STUDENT"
            tvRoleBadge.text = role

            // Set badge color based on role (matches web badge-student/technician/admin)
            val badgeColor = when (role.uppercase()) {
                "STUDENT" -> 0xFF3B82F6.toInt()
                "TECHNICIAN" -> 0xFF10B981.toInt()
                "ADMIN" -> 0xFFA78BFA.toInt()
                else -> 0xFF3B82F6.toInt()
            }
            tvRoleBadge.setTextColor(badgeColor)
        }
    }

    private fun formatDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
            val date = inputFormat.parse(iso.substringBefore('.').substringBefore('Z'))
            if (date != null) outputFormat.format(date) else "—"
        } catch (e: Exception) {
            iso.substringBefore('T')
        }
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener { logout() }
        // Topbar navigation
        findViewById<Button>(R.id.navDashboard).setOnClickListener { /* Already here */ }
        findViewById<Button>(R.id.navInventory).setOnClickListener { navigateToInventory() }
        findViewById<Button>(R.id.navRequests).setOnClickListener { navigateToRequests() }
    }

    private fun logout() {
        tokenManager.logout()
        navigateToLogin()
    }

    private fun navigateToInventory() {
        val intent = Intent(this, InventoryListActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToRequests() {
        val intent = Intent(this, RequestsListActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
