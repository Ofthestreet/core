import SwiftUI

struct ExpenseRow: View {
    let expense: Expense
    let currency: NumberFormatter

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            HStack(alignment: .firstTextBaseline) {
                Text(expense.description)
                    .font(.headline)
                Spacer()
                Text(currency.string(from: NSNumber(value: expense.amount)) ?? "")
                    .font(.headline)
                    .foregroundStyle(.blue)
            }
            HStack {
                Label(expense.paidBy, systemImage: "person.fill")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Spacer()
                Text("\(currency.string(from: NSNumber(value: expense.sharePerPerson)) ?? "")/pers.")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Text(expense.participants.joined(separator: " · "))
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(1)
        }
        .padding(.vertical, 4)
    }
}
