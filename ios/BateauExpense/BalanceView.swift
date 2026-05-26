import SwiftUI

struct BalanceView: View {
    @ObservedObject var store: ExpenseStore
    let currency: NumberFormatter
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section {
                    ForEach(store.balances, id: \.name) { entry in
                        HStack {
                            Text(entry.amount > 0.01 ? "💰" : entry.amount < -0.01 ? "💸" : "✅")
                            Text(entry.name)
                            Spacer()
                            Text((entry.amount >= 0 ? "+" : "") +
                                 (currency.string(from: NSNumber(value: entry.amount)) ?? ""))
                                .fontWeight(.semibold)
                                .foregroundStyle(entry.amount > 0.01 ? .green :
                                                 entry.amount < -0.01 ? .red : .secondary)
                        }
                    }
                } header: {
                    Text("Soldes")
                }

                Section {
                    if store.settlements.isEmpty {
                        Label("Tout est équilibré !", systemImage: "checkmark.seal.fill")
                            .foregroundStyle(.green)
                    } else {
                        ForEach(store.settlements) { s in
                            HStack {
                                Text(s.from)
                                    .foregroundStyle(.red)
                                Image(systemName: "arrow.right")
                                    .foregroundStyle(.secondary)
                                    .font(.caption)
                                Text(s.to)
                                    .foregroundStyle(.green)
                                Spacer()
                                Text(currency.string(from: NSNumber(value: s.amount)) ?? "")
                                    .fontWeight(.semibold)
                                    .foregroundStyle(.blue)
                            }
                        }
                    }
                } header: {
                    Text("Remboursements à faire")
                }
            }
            .navigationTitle("Bilan ⚖️")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Fermer") { dismiss() }
                }
            }
        }
    }
}
