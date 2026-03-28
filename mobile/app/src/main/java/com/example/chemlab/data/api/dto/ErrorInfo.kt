package com.example.chemlab.data.api.dto

data class ErrorInfo(
    val code: String,
    val message: String,
    val details: Any? = null
)
