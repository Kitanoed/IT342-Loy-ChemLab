package com.example.chemlab.features.requests.data.dto

data class RequestDTO(
    val id: Long,
    val requesterUsername: String?,
    val requesterEmail: String?,
    val status: String?,
    val remarks: String?,
    val items: List<RequestItemDTO>?,
    val createdAt: String?,
    val updatedAt: String?
)

data class RequestItemDTO(
    val id: Long?,
    val inventoryItemId: Long?,
    val itemCode: String?,
    val itemName: String?,
    val quantity: Int?,
    val unitSnapshot: String?,
    val expirationSnapshot: String?
)

data class RequestPageResponse(
    val content: List<RequestDTO>,
    val number: Int?,
    val totalPages: Int?,
    val totalElements: Long?,
    val size: Int?
)

data class RequestActionBody(
    val remarks: String? = null
)
