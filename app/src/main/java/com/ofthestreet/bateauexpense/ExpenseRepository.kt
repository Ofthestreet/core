package com.ofthestreet.bateauexpense

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExpenseRepository(context: Context) {

    private val prefs = context.getSharedPreferences("bateau_expense", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getExpenses(): MutableList<Expense> {
        val json = prefs.getString(KEY_EXPENSES, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Expense>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveExpenses(expenses: List<Expense>) {
        prefs.edit().putString(KEY_EXPENSES, gson.toJson(expenses)).apply()
    }

    fun getParticipants(): MutableList<String> {
        val json = prefs.getString(KEY_PARTICIPANTS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveParticipants(participants: List<String>) {
        prefs.edit().putString(KEY_PARTICIPANTS, gson.toJson(participants)).apply()
    }

    companion object {
        private const val KEY_EXPENSES = "expenses"
        private const val KEY_PARTICIPANTS = "participants"
    }
}
