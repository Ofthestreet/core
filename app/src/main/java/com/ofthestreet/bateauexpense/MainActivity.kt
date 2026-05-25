package com.ofthestreet.bateauexpense

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ofthestreet.bateauexpense.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ExpenseRepository
    private lateinit var adapter: ExpenseAdapter

    private val expenses = mutableListOf<Expense>()
    private val participants = mutableListOf<String>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        repository = ExpenseRepository(this)
        expenses.addAll(repository.getExpenses())
        participants.addAll(repository.getParticipants())

        adapter = ExpenseAdapter(expenses) { expense ->
            expenses.remove(expense)
            repository.saveExpenses(expenses)
            adapter.notifyDataSetChanged()
            updateBalance()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener { showAddExpenseDialog() }

        updateBalance()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_participants -> {
                showParticipantsDialog()
                true
            }
            R.id.action_balance -> {
                showBalanceDialog()
                true
            }
            R.id.action_clear -> {
                confirmClear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddExpenseDialog() {
        if (participants.isEmpty()) {
            Toast.makeText(this, "Ajoutez d'abord des participants !", Toast.LENGTH_SHORT).show()
            showParticipantsDialog()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_description)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val etPaidBy = dialogView.findViewById<EditText>(R.id.et_paid_by)

        val checkedParticipants = BooleanArray(participants.size) { true }
        val participantsArray = participants.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Nouvelle dépense")
            .setView(dialogView)
            .setNeutralButton("Participants") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Qui participe ?")
                    .setMultiChoiceItems(participantsArray, checkedParticipants) { _, which, checked ->
                        checkedParticipants[which] = checked
                    }
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setPositiveButton("Ajouter") { _, _ ->
                val desc = etDescription.text.toString().trim()
                val amountStr = etAmount.text.toString().trim().replace(',', '.')
                val paidBy = etPaidBy.text.toString().trim()

                if (desc.isEmpty() || amountStr.isEmpty() || paidBy.isEmpty()) {
                    Toast.makeText(this, "Tous les champs sont requis", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selected = participants.filterIndexed { i, _ -> checkedParticipants[i] }
                if (selected.isEmpty()) {
                    Toast.makeText(this, "Sélectionnez au moins un participant", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val expense = Expense(
                    description = desc,
                    amount = amount,
                    paidBy = paidBy,
                    participants = selected
                )
                expenses.add(0, expense)
                repository.saveExpenses(expenses)
                adapter.notifyItemInserted(0)
                binding.recyclerView.scrollToPosition(0)
                updateBalance()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showParticipantsDialog() {
        val input = EditText(this).apply {
            hint = "Nom du participant"
        }

        val existingList = participants.joinToString("\n").ifEmpty { "Aucun participant" }
        val message = "Participants actuels:\n$existingList\n\nAjouter:"

        AlertDialog.Builder(this)
            .setTitle("Participants")
            .setMessage(message)
            .setView(input)
            .setPositiveButton("Ajouter") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty() && !participants.contains(name)) {
                    participants.add(name)
                    repository.saveParticipants(participants)
                    Toast.makeText(this, "$name ajouté !", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Supprimer dernier") { _, _ ->
                if (participants.isNotEmpty()) {
                    val removed = participants.removeLast()
                    repository.saveParticipants(participants)
                    Toast.makeText(this, "$removed supprimé", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Fermer", null)
            .show()
    }

    private fun showBalanceDialog() {
        val balances = BalanceCalculator.calculate(expenses, participants)
        val settlements = BalanceCalculator.settlements(expenses, participants)

        val sb = StringBuilder()

        sb.appendLine("=== SOLDES ===")
        if (balances.isEmpty()) {
            sb.appendLine("Aucun participant")
        } else {
            balances.entries.sortedByDescending { it.value }.forEach { (person, balance) ->
                val sign = if (balance >= 0) "+" else ""
                sb.appendLine("$person: $sign${currencyFormat.format(balance)}")
            }
        }

        sb.appendLine("\n=== REMBOURSEMENTS ===")
        if (settlements.isEmpty()) {
            sb.appendLine("Tout est équilibré !")
        } else {
            settlements.forEach { s ->
                sb.appendLine("${s.from} → ${s.to}: ${currencyFormat.format(s.amount)}")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Bilan des dépenses")
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmClear() {
        AlertDialog.Builder(this)
            .setTitle("Tout effacer ?")
            .setMessage("Voulez-vous supprimer toutes les dépenses ?")
            .setPositiveButton("Oui") { _, _ ->
                expenses.clear()
                repository.saveExpenses(expenses)
                adapter.notifyDataSetChanged()
                updateBalance()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun updateBalance() {
        val total = expenses.sumOf { it.amount }
        binding.tvTotal.text = "Total: ${currencyFormat.format(total)}"
        binding.tvExpenseCount.text = "${expenses.size} dépense(s)"
    }
}
