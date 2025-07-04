package com.walter.mbogo.utility

data class ProcessedMessage(
    val code: String,
    val phone: String,
    val name: String,
    val amount: Double,
    val date: Long,
    val type: String
)