package com.example.chemlab.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chemlab.R
import com.example.chemlab.data.api.RetrofitClient
import com.example.chemlab.data.api.dto.LoginRequest
import com.example.chemlab.data.storage.TokenManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    
    private lateinit var tokenManager: TokenManager
    private val authService = RetrofitClient.getAuthService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        tokenManager = TokenManager(this)
        
        // Check if already logged in
        if (tokenManager.isLoggedIn()) {
            navigateToDashboard()
            return
        }
        
        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            loginUser()
        }
        
        tvRegister.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address")
            return
        }

        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }

        performLogin(email, password)
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = authService.login(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    if (authResponse.success && authResponse.data != null) {
                        val data = authResponse.data
                        tokenManager.saveTokens(data.accessToken, data.refreshToken)
                        tokenManager.saveUser(data.user)
                        showError("") // Clear errors
                        navigateToDashboard()
                    } else {
                        val errorMsg = authResponse.error?.message ?: "Login failed"
                        showError(errorMsg)
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> response.body()?.error?.message ?: "Invalid email or password"
                        401 -> "Invalid credentials"
                        else -> "Login failed. Please try again"
                    }
                    showError(errorMsg)
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message ?: "Unknown error"}")
                e.printStackTrace()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnLogin.isEnabled = true
        }
    }

    private fun showError(message: String) {
        if (message.isEmpty()) {
            tvError.visibility = View.GONE
        } else {
            tvError.text = message
            tvError.visibility = View.VISIBLE
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
