import SwiftUI

struct AddExpenseView: View {
    @ObservedObject var store: ExpenseStore
    let currency: NumberFormatter
    @Environment(\.dismiss) private var dismiss

    @State private var description = ""
    @State private var amountText = ""
    @State private var paidBy = ""
    @State private var selectedParticipants: Set<String> = []
    @State private var showError = false
    @State private var errorMessage = ""

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("Description (ex: Carburant, Repas…)", text: $description)
                    HStack {
                        TextField("0,00", text: $amountText)
                            .keyboardType(.decimalPad)
                        Text("€")
                            .foregroundStyle(.secondary)
                    }
                } header: {
                    Text("Dépense")
                }

                Section {
                    Picker("Payeur", selection: $paidBy) {
                        ForEach(store.participants, id: \.self) { name in
                            Text(name).tag(name)
                        }
                    }
                    .pickerStyle(.menu)
                } header: {
                    Text("Qui a payé ?")
                }

                Section {
                    ForEach(store.participants, id: \.self) { name in
                        Button {
                            if selectedParticipants.contains(name) {
                                selectedParticipants.remove(name)
                            } else {
                                selectedParticipants.insert(name)
                            }
                        } label: {
                            HStack {
                                Text(name).foregroundStyle(.primary)
                                Spacer()
                                Image(systemName: selectedParticipants.contains(name)
                                    ? "checkmark.circle.fill" : "circle")
                                    .foregroundStyle(selectedParticipants.contains(name) ? .blue : .secondary)
                                    .font(.title3)
                            }
                        }
                    }

                    if !store.participants.isEmpty {
                        let shareText = {
                            let n = selectedParticipants.count
                            guard n > 0, let amount = Double(amountText.replacingOccurrences(of: ",", with: ".")) else {
                                return ""
                            }
                            let share = amount / Double(n)
                            return "→ \(currency.string(from: NSNumber(value: share)) ?? "") par personne"
                        }()
                        if !shareText.isEmpty {
                            Text(shareText)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                } header: {
                    Text("Qui partage cette dépense ?")
                }
            }
            .navigationTitle("Nouvelle dépense ⛵")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Annuler") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Ajouter") { submit() }
                        .fontWeight(.semibold)
                }
            }
            .alert("Champ manquant", isPresented: $showError) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(errorMessage)
            }
            .onAppear {
                selectedParticipants = Set(store.participants)
                paidBy = store.participants.first ?? ""
            }
        }
    }

    private func submit() {
        let desc = description.trimmingCharacters(in: .whitespaces)
        let amountStr = amountText.replacingOccurrences(of: ",", with: ".")

        guard !desc.isEmpty else {
            errorMessage = "Entrez une description."
            showError = true; return
        }
        guard let amount = Double(amountStr), amount > 0 else {
            errorMessage = "Entrez un montant valide."
            showError = true; return
        }
        guard !paidBy.isEmpty else {
            errorMessage = "Choisissez qui a payé."
            showError = true; return
        }
        guard !selectedParticipants.isEmpty else {
            errorMessage = "Sélectionnez au moins un participant."
            showError = true; return
        }

        store.addExpense(Expense(
            description: desc,
            amount: amount,
            paidBy: paidBy,
            participants: Array(selectedParticipants)
        ))
        dismiss()
    }
}
