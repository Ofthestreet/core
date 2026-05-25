package com.ofthestreet.bateauexpense

import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Double,
    val paidBy: String,
    val participants: List<String>,
    val date: Long = System.currentTimeMillis()
) {
    val sharePerPerson: Double
        get() = if (participants.isEmpty()) amount else amount / participants.size
}
