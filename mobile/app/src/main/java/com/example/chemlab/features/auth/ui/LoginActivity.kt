package com.example.chemlab.features.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chemlab.R
import com.example.chemlab.features.auth.data.dto.LoginRequest
import com.example.chemlab.features.auth.data.remote.RetrofitClient
import com.example.chemlab.features.auth.data.storage.TokenManager
import com.example.chemlab.features.auth.data.dto.GoogleLoginRequest
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.util.Log

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private lateinit var tokenManager: TokenManager
    private val authService = RetrofitClient.getAuthService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tokenManager = TokenManager(this)

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
        btnGoogle = findViewById(R.id.btnGoogle)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener { loginUser() }
        btnGoogle.setOnClickListener { performGoogleLogin() }
        tvRegister.setOnClickListener { navigateToRegister() }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

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
                        showError("")
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
            btnGoogle.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnLogin.isEnabled = true
            btnGoogle.isEnabled = true
        }
    }

    private fun performGoogleLogin() {
        showLoading(true)
        val credentialManager = CredentialManager.create(this)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity,
                )
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val apiRequest = GoogleLoginRequest(idToken)
                    val response = authService.googleLogin(apiRequest)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()!!
                        if (authResponse.success && authResponse.data != null) {
                            val data = authResponse.data
                            tokenManager.saveTokens(data.accessToken, data.refreshToken)
                            tokenManager.saveUser(data.user)
                            showError("")
                            navigateToDashboard()
                        } else {
                            showError(authResponse.error?.message ?: "Google login failed")
                        }
                    } else {
                        showError("Google login failed")
                    }
                } else {
                    showError("Unexpected credential type")
                }
            } catch (e: GetCredentialException) {
                Log.e("LoginActivity", "Google Sign In Failed", e)
                showError("Google Sign In Cancelled or Failed")
            } catch (e: Exception) {
                Log.e("LoginActivity", "Google Login Exception", e)
                showError("Network error: ${e.message ?: "Unknown error"}")
            } finally {
                showLoading(false)
            }
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
