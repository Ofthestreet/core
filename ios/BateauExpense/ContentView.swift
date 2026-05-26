import SwiftUI

struct ContentView: View {
    @StateObject private var store = ExpenseStore()
    @State private var showAddExpense = false
    @State private var showBalance = false
    @State private var showAddParticipant = false
    @State private var showClearConfirm = false
    @State private var newParticipantName = ""
    @State private var participantToRemove: String? = nil

    private let currency: NumberFormatter = {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = Locale(identifier: "fr_FR")
        return f
    }()

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                statsBar
                participantsBar
                expenseList
            }
            .navigationTitle("⛵ Bateau Expense")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbarContent }
        }
        .sheet(isPresented: $showAddExpense) {
            AddExpenseView(store: store, currency: currency)
        }
        .sheet(isPresented: $showBalance) {
            BalanceView(store: store, currency: currency)
        }
        .alert("Nouveau participant", isPresented: $showAddParticipant) {
            TextField("Prénom ou surnom", text: $newParticipantName)
                .textInputAutocapitalization(.words)
            Button("Ajouter") {
                store.addParticipant(newParticipantName.trimmingCharacters(in: .whitespaces))
                newParticipantName = ""
            }
            Button("Annuler", role: .cancel) { newParticipantName = "" }
        }
        .confirmationDialog(
            "Supprimer \(participantToRemove ?? "") ?",
            isPresented: Binding(
                get: { participantToRemove != nil },
                set: { if !$0 { participantToRemove = nil } }
            ),
            titleVisibility: .visible
        ) {
            Button("Supprimer", role: .destructive) {
                if let name = participantToRemove { store.removeParticipant(name) }
                participantToRemove = nil
            }
            Button("Annuler", role: .cancel) { participantToRemove = nil }
        }
        .confirmationDialog("Supprimer toutes les dépenses ?", isPresented: $showClearConfirm, titleVisibility: .visible) {
            Button("Tout effacer", role: .destructive) { store.clearExpenses() }
            Button("Annuler", role: .cancel) {}
        }
    }

    private var statsBar: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text("Total dépenses")
                    .font(.caption)
                    .foregroundStyle(.white.opacity(0.8))
                Text(currency.string(from: NSNumber(value: store.totalAmount)) ?? "0 €")
                    .font(.title3).bold()
                    .foregroundStyle(.white)
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 2) {
                Text("Dépenses")
                    .font(.caption)
                    .foregroundStyle(.white.opacity(0.8))
                Text("\(store.expenses.count)")
                    .font(.title3).bold()
                    .foregroundStyle(.white)
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
        .background(Color.blue)
    }

    private var participantsBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(store.participants, id: \.self) { name in
                    HStack(spacing: 4) {
                        Text(name)
                            .font(.subheadline)
                        Button {
                            participantToRemove = name
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .font(.subheadline)
                                .foregroundStyle(.blue.opacity(0.6))
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 7)
                    .background(.white)
                    .foregroundStyle(.blue)
                    .clipShape(Capsule())
                    .shadow(color: .black.opacity(0.08), radius: 2, y: 1)
                }

                Button {
                    showAddParticipant = true
                } label: {
                    Label("Ajouter", systemImage: "plus")
                        .font(.subheadline)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 7)
                        .background(Color.white.opacity(0.25))
                        .foregroundStyle(.white)
                        .clipShape(Capsule())
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
        .background(Color(red: 0.06, green: 0.39, blue: 0.75))
    }

    @ViewBuilder
    private var expenseList: some View {
        if store.expenses.isEmpty {
            Spacer()
            VStack(spacing: 16) {
                Text("⛵").font(.system(size: 64))
                Text("Aucune dépense").font(.title2).foregroundStyle(.secondary)
                Text("Appuyez sur + pour commencer").font(.subheadline).foregroundStyle(.secondary)
            }
            Spacer()
        } else {
            List {
                ForEach(store.expenses) { expense in
                    ExpenseRow(expense: expense, currency: currency)
                }
                .onDelete { store.removeExpense(at: $0) }
            }
            .listStyle(.plain)
        }
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .topBarTrailing) {
            HStack(spacing: 16) {
                Button { showBalance = true } label: {
                    Image(systemName: "chart.pie.fill")
                }
                Button { showClearConfirm = true } label: {
                    Image(systemName: "trash")
                }
                    .tint(.red)
                Button { showAddExpense = true } label: {
                    Image(systemName: "plus.circle.fill")
                        .font(.title3)
                }
            }
        }
    }
}
