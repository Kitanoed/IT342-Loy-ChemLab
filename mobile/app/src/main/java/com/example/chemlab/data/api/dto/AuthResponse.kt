package com.example.chemlab.data.api.dto

data class AuthResponse(
    val success: Boolean,
    val data: AuthData?,
    val error: ErrorInfo?,
    val timestamp: String
)
