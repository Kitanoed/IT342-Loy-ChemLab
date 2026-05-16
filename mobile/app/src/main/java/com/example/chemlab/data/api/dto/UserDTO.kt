package com.example.chemlab.data.api.dto

data class UserDTO(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val role: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
