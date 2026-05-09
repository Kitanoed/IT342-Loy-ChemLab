package com.example.chemlab.features.auth.data.remote

import com.example.chemlab.features.auth.data.dto.AuthResponse
import com.example.chemlab.features.auth.data.dto.LoginRequest
import com.example.chemlab.features.auth.data.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<AuthResponse>
}