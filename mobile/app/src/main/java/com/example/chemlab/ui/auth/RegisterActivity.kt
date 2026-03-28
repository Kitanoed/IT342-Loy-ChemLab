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
import com.example.chemlab.data.api.dto.RegisterRequest
import com.example.chemlab.data.storage.TokenManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    
    private lateinit var tokenManager: TokenManager
    private val authService = RetrofitClient.getAuthService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        tokenManager = TokenManager(this)
        
        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            registerUser()
        }
        
        tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun registerUser() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || 
            email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return
        }

        if (firstName.length < 2) {
            showError("First name must be at least 2 characters")
            return
        }

        if (lastName.length < 2) {
            showError("Last name must be at least 2 characters")
            return
        }

        if (username.length < 3) {
            showError("Username must be at least 3 characters")
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

        performRegistration(firstName, lastName, username, email, password)
    }

    private fun performRegistration(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String
    ) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val request = RegisterRequest(
                    email = email,
                    username = username,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )
                val response = authService.register(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    if (authResponse.success && authResponse.data != null) {
                        val data = authResponse.data
                        tokenManager.saveTokens(data.accessToken, data.refreshToken)
                        tokenManager.saveUser(data.user)
                        showError("") // Clear errors
                        navigateToDashboard()
                    } else {
                        val errorMsg = authResponse.error?.message ?: "Registration failed"
                        showError(errorMsg)
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> response.body()?.error?.message ?: "Invalid input"
                        409 -> "Email or username already exists"
                        else -> "Registration failed. Please try again"
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
            btnRegister.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnRegister.isEnabled = true
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

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
