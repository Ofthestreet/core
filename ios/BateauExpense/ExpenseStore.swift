import Foundation

class ExpenseStore: ObservableObject {
    @Published var expenses: [Expense] = []
    @Published var participants: [String] = []

    init() { load() }

    var totalAmount: Double { expenses.reduce(0) { $0 + $1.amount } }

    var balances: [(name: String, amount: Double)] {
        var b = Dictionary(uniqueKeysWithValues: participants.map { ($0, 0.0) })
        for expense in expenses {
            b[expense.paidBy, default: 0] += expense.amount
            let share = expense.sharePerPerson
            for p in expense.participants { b[p, default: 0] -= share }
        }
        return b.map { (name: $0.key, amount: $0.value) }
            .sorted { $0.amount > $1.amount }
    }

    var settlements: [Settlement] {
        var debts = balances.filter { $0.amount < -0.01 }.sorted { $0.amount < $1.amount }
        var credits = balances.filter { $0.amount > 0.01 }.sorted { $0.amount > $1.amount }
        var da = debts.map { $0.amount }
        var ca = credits.map { $0.amount }
        var result: [Settlement] = []
        var di = 0, ci = 0

        while di < debts.count && ci < credits.count {
            let amount = min(-da[di], ca[ci])
            result.append(Settlement(from: debts[di].name, to: credits[ci].name, amount: amount))
            da[di] += amount
            ca[ci] -= amount
            if da[di] >= -0.01 { di += 1 }
            if ca[ci] <= 0.01 { ci += 1 }
        }
        return result
    }

    func addExpense(_ expense: Expense) {
        expenses.insert(expense, at: 0)
        save()
    }

    func removeExpense(at offsets: IndexSet) {
        expenses.remove(atOffsets: offsets)
        save()
    }

    func addParticipant(_ name: String) {
        guard !name.isEmpty, !participants.contains(name) else { return }
        participants.append(name)
        save()
    }

    func removeParticipant(_ name: String) {
        participants.removeAll { $0 == name }
        save()
    }

    func clearExpenses() {
        expenses.removeAll()
        save()
    }

    private func save() {
        if let data = try? JSONEncoder().encode(expenses) {
            UserDefaults.standard.set(data, forKey: "expenses")
        }
        UserDefaults.standard.set(participants, forKey: "participants")
    }

    private func load() {
        if let data = UserDefaults.standard.data(forKey: "expenses"),
           let saved = try? JSONDecoder().decode([Expense].self, from: data) {
            expenses = saved
        }
        participants = UserDefaults.standard.stringArray(forKey: "participants") ?? []
    }
}
