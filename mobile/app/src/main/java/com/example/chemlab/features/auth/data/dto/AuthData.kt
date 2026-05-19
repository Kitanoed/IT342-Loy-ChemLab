package com.example.chemlab.features.auth.data.dto

data class AuthData(
    val user: UserDTO,
    val accessToken: String,
    val refreshToken: String
)
