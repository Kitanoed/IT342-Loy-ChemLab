package com.example.chemlab.features.auth.data.dto

data class UserDTO(
    val id: Long,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val createdAt: String
)