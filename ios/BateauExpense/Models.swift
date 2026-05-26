import Foundation

struct Expense: Codable, Identifiable {
    var id = UUID()
    var description: String
    var amount: Double
    var paidBy: String
    var participants: [String]
    var date: Date = Date()

    var sharePerPerson: Double {
        participants.isEmpty ? amount : amount / Double(participants.count)
    }
}

struct Settlement: Identifiable {
    var id = UUID()
    let from: String
    let to: String
    let amount: Double
}
