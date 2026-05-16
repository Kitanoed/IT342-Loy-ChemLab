package com.example.chemlab.features.inventory.data.remote

import com.example.chemlab.features.inventory.data.dto.InventoryItemDTO
import com.example.chemlab.features.inventory.data.dto.InventoryPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryService {
    @GET("/api/inventory")
    suspend fun listInventory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "updatedAt,desc",
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null
    ): Response<InventoryPageResponse>

    @GET("/api/inventory/{id}")
    suspend fun getItemById(
        @Path("id") id: Long
    ): Response<InventoryItemDTO>
}
