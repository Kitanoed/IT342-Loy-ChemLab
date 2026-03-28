package com.example.chemlab.data.api.dto

data class AuthData(
    val user: UserDTO,
    val accessToken: String,
    val refreshToken: String
)
