package com.example.chemlab.features.auth.data.dto

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String
)