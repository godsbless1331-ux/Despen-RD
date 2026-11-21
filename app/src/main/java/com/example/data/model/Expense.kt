package com.example.data.model

import java.util.UUID

data class HouseholdExpense(
    val id: String = UUID.randomUUID().toString(),
    val storeName: String,
    val amount: Double,
    val itemCount: Int,
    val profileId: String = "p_main",
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "MANUAL", // RECEIPT, MANUAL, STORE_CHECKOUT
    val notes: String = ""
)
