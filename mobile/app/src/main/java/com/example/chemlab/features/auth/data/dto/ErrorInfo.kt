package com.example.chemlab.features.auth.data.dto

data class ErrorInfo(
    val code: String,
    val message: String,
    val details: Any?
)