package com.example.chemlab.features.auth.data.dto

data class UserDTO(
    val id: Long,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val role: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
