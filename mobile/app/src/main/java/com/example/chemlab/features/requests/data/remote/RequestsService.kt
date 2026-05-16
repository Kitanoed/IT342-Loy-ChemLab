package com.example.chemlab.features.requests.data.remote

import com.example.chemlab.features.requests.data.dto.RequestActionBody
import com.example.chemlab.features.requests.data.dto.RequestDTO
import com.example.chemlab.features.requests.data.dto.RequestPageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RequestsService {
    @GET("/api/requests")
    suspend fun listRequests(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 15,
        @Query("status") status: String? = null
    ): Response<RequestPageResponse>

    @GET("/api/requests/{id}")
    suspend fun getRequestById(
        @Path("id") id: Long
    ): Response<RequestDTO>

    @PUT("/api/requests/{id}/approve")
    suspend fun approveRequest(
        @Path("id") id: Long,
        @Body body: RequestActionBody
    ): Response<RequestDTO>

    @PUT("/api/requests/{id}/reject")
    suspend fun rejectRequest(
        @Path("id") id: Long,
        @Body body: RequestActionBody
    ): Response<RequestDTO>
}
