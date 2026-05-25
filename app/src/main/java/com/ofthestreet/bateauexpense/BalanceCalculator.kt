package com.ofthestreet.bateauexpense

object BalanceCalculator {

    data class Settlement(val from: String, val to: String, val amount: Double)

    fun calculate(expenses: List<Expense>, participants: List<String>): Map<String, Double> {
        val balances = participants.associateWith { 0.0 }.toMutableMap()
        for (expense in expenses) {
            val share = expense.sharePerPerson
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount
            for (participant in expense.participants) {
                balances[participant] = (balances[participant] ?: 0.0) - share
            }
        }
        return balances
    }

    fun settlements(expenses: List<Expense>, participants: List<String>): List<Settlement> {
        val balances = calculate(expenses, participants)
        val debtors = balances.filter { it.value < -0.01 }
            .map { Pair(it.key, it.value) }.sortedBy { it.second }.toMutableList()
        val creditors = balances.filter { it.value > 0.01 }
            .map { Pair(it.key, it.value) }.sortedByDescending { it.second }.toMutableList()

        val result = mutableListOf<Settlement>()
        var di = 0
        var ci = 0
        val debtAmounts = debtors.map { it.second }.toDoubleArray()
        val creditAmounts = creditors.map { it.second }.toDoubleArray()

        while (di < debtors.size && ci < creditors.size) {
            val amount = minOf(-debtAmounts[di], creditAmounts[ci])
            result.add(Settlement(debtors[di].first, creditors[ci].first, amount))
            debtAmounts[di] += amount
            creditAmounts[ci] -= amount
            if (debtAmounts[di] >= -0.01) di++
            if (creditAmounts[ci] <= 0.01) ci++
        }

        return result
    }
}
