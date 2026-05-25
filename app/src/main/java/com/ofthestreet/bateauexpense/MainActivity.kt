package com.ofthestreet.bateauexpense

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
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

        refreshParticipantChips()
        updateBalance()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_balance -> { showBalanceDialog(); true }
            R.id.action_clear -> { confirmClear(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshParticipantChips() {
        binding.chipGroupParticipants.removeAllViews()

        participants.forEach { name ->
            val chip = Chip(this).apply {
                text = name
                isCloseIconVisible = true
                setChipBackgroundColorResource(android.R.color.white)
                setTextColor(resources.getColor(R.color.primary, theme))
                setCloseIconTintResource(R.color.primary)
                setOnCloseIconClickListener { confirmRemoveParticipant(name) }
            }
            binding.chipGroupParticipants.addView(chip)
        }

        val addChip = Chip(this).apply {
            text = "+ Ajouter"
            chipIcon = null
            setChipBackgroundColorResource(R.color.secondary)
            setTextColor(resources.getColor(android.R.color.white, theme))
            setOnClickListener { showAddParticipantDialog() }
        }
        binding.chipGroupParticipants.addView(addChip)
    }

    private fun showAddParticipantDialog() {
        val input = EditText(this).apply {
            hint = "Prénom ou surnom"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(this)
            .setTitle("Nouveau participant")
            .setView(input)
            .setPositiveButton("Ajouter") { _, _ ->
                val name = input.text.toString().trim()
                when {
                    name.isEmpty() -> Toast.makeText(this, "Entrez un prénom", Toast.LENGTH_SHORT).show()
                    participants.contains(name) -> Toast.makeText(this, "$name est déjà dans la liste", Toast.LENGTH_SHORT).show()
                    else -> {
                        participants.add(name)
                        repository.saveParticipants(participants)
                        refreshParticipantChips()
                        Toast.makeText(this, "$name ajouté ! 🎉", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()

        input.requestFocus()
    }

    private fun confirmRemoveParticipant(name: String) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer $name ?")
            .setMessage("$name sera retiré de la liste des participants.")
            .setPositiveButton("Supprimer") { _, _ ->
                participants.remove(name)
                repository.saveParticipants(participants)
                refreshParticipantChips()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showAddExpenseDialog() {
        if (participants.isEmpty()) {
            Toast.makeText(this, "Ajoutez d'abord des participants via le + en haut !", Toast.LENGTH_LONG).show()
            showAddParticipantDialog()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_description)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val spinnerPaidBy = dialogView.findViewById<Spinner>(R.id.spinner_paid_by)
        val llParticipants = dialogView.findViewById<LinearLayout>(R.id.ll_participants)

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            participants
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerPaidBy.adapter = spinnerAdapter

        val checkboxes = participants.map { name ->
            CheckBox(this).apply {
                text = name
                isChecked = true
                textSize = 15f
                setPadding(8, 8, 8, 8)
            }
        }
        checkboxes.forEach { llParticipants.addView(it) }

        AlertDialog.Builder(this)
            .setTitle("Nouvelle dépense ⛵")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val desc = etDescription.text.toString().trim()
                val amountStr = etAmount.text.toString().trim().replace(',', '.')
                val paidBy = spinnerPaidBy.selectedItem?.toString() ?: ""

                if (desc.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(this, "Description et montant requis", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selected = participants.filterIndexed { i, _ -> checkboxes[i].isChecked }
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
                val emoji = when {
                    balance > 0.01 -> "💰"
                    balance < -0.01 -> "💸"
                    else -> "✅"
                }
                sb.appendLine("$emoji $person: $sign${currencyFormat.format(balance)}")
            }
        }

        sb.appendLine("\n=== REMBOURSEMENTS ===")
        if (settlements.isEmpty()) {
            sb.appendLine("✅ Tout est équilibré !")
        } else {
            settlements.forEach { s ->
                sb.appendLine("${s.from} → ${s.to}: ${currencyFormat.format(s.amount)}")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Bilan ⚖️")
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
