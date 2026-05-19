package com.example.chemlab.features.auth.data.dto

data class AuthResponse(
    val success: Boolean,
    val data: AuthData?,
    val error: ErrorInfo?,
    val timestamp: String
)
