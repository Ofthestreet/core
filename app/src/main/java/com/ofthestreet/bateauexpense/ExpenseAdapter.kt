package com.ofthestreet.bateauexpense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ofthestreet.bateauexpense.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(
    private val expenses: MutableList<Expense>,
    private val onDelete: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    inner class ViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.tvDescription.text = expense.description
            binding.tvAmount.text = currencyFormat.format(expense.amount)
            binding.tvPaidBy.text = "Payé par ${expense.paidBy}"
            binding.tvParticipants.text = "Entre: ${expense.participants.joinToString(", ")}"
            binding.tvDate.text = dateFormat.format(Date(expense.date))
            binding.tvShare.text = "${currencyFormat.format(expense.sharePerPerson)}/pers."
            binding.btnDelete.setOnClickListener { onDelete(expense) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount() = expenses.size
}
