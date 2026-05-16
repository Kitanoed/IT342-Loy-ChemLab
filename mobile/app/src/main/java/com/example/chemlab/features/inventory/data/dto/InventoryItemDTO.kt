package com.example.chemlab.features.inventory.data.dto

data class InventoryItemDTO(
    val id: Long,
    val itemCode: String?,
    val itemName: String?,
    val itemType: String?,
    val category: String?,
    val quantity: Double?,
    val unit: String?,
    val status: String?,
    val storageLocation: String?,
    val labId: Long?,
    val description: String?,
    val safetyNotes: String?,
    val pubchemCid: Int?,
    val molecularFormula: String?,
    val molecularWeight: String?,
    val iupacName: String?,
    val lotNumber: String?,
    val expiryDate: String?,
    val version: Int?,
    val updatedAt: String?,
    val createdAt: String?
)

data class InventoryPageResponse(
    val content: List<InventoryItemDTO>,
    val page: Int?,
    val totalPages: Int?,
    val totalElements: Long?,
    val size: Int?
)
