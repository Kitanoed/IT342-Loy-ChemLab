package com.example.chemlab.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.chemlab.R
import com.example.chemlab.data.storage.TokenManager

class DashboardActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var tvName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMemberSince: TextView
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
        tvName = findViewById(R.id.tvName)
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupUI() {
        val user = tokenManager.getUser()
        if (user != null) {
            tvName.text = "Name: ${user.firstName} ${user.lastName}"
            tvUsername.text = "Username: ${user.username}"
            tvEmail.text = "Email: ${user.email}"
            tvMemberSince.text = "Member since: ${user.createdAt}"
        }
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        tokenManager.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
