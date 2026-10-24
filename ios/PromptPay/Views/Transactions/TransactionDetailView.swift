import SwiftUI

public struct TransactionDetailView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject var viewModel: MainViewModel
    @State var transaction: Transaction

    @State private var descriptionText: String = ""
    @State private var selectedCategory: String = ""
    @State private var notesText: String = ""
    @State private var isRecurring: Bool = false
    @State private var showDeleteAlert: Bool = false

    public init(viewModel: MainViewModel, transaction: Transaction) {
        self.viewModel = viewModel
        self._transaction = State(initialValue: transaction)
        self._descriptionText = State(initialValue: transaction.description)
        self._selectedCategory = State(initialValue: transaction.category)
        self._notesText = State(initialValue: transaction.notes)
        self._isRecurring = State(initialValue: transaction.isRecurring)
    }

    public var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Hero Amount Card (Card 26px)
                VStack(spacing: 8) {
                    Text(transaction.merchant.isEmpty ? transaction.description : transaction.merchant)
                        .font(.urbanist(17, weight: .semibold))
                        .foregroundColor(.white.opacity(0.85))

                    Text(FormatUtil.formatAmount(transaction.amount, currency: transaction.currency))
                        .font(.urbanist(36, weight: .bold))
                        .foregroundColor(.white)

                    if transaction.fee > 0 {
                        Text("Fee: \(FormatUtil.formatAmount(transaction.fee, currency: transaction.currency)) · \(transaction.feeCategory)")
                            .font(.urbanist(13, weight: .semibold))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 5)
                            .background(Color.white.opacity(0.2))
                            .foregroundColor(.white)
                            .clipShape(Capsule())
                    }

                    Text(FormatUtil.formatDate(transaction.dateEpochMillis))
                        .font(.urbanist(13, weight: .regular))
                        .foregroundColor(.white.opacity(0.75))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 28)
                .padding(.horizontal, 20)
                .background(Color.royalBlue)
                .cornerRadius(26)
                .shadow(color: Color.black.opacity(0.15), radius: 10, x: 0, y: 5)

                // Meta Information Card (Card 26px)
                VStack(spacing: 12) {
                    DetailRowView(title: "Payment Method", value: transaction.paymentMethod)
                    Divider()
                    // Strict Immutable Source
                    DetailRowView(
                        title: "Source",
                        value: transaction.source == "SMS" ? "SMS Alert (Fixed)" : "Manual Entry (Fixed)",
                        badgeColor: transaction.source == "SMS" ? .promptPurple : .rapportGood
                    )
                    Divider()
                    DetailRowView(title: "Transaction Fee", value: FormatUtil.formatAmount(transaction.fee, currency: transaction.currency))
                    Divider()
                    DetailRowView(title: "Fee Category", value: transaction.feeCategory)
                    Divider()
                    DetailRowView(title: "Status", value: transaction.status)
                }
                .padding(20)
                .background(Color.promptCard)
                .cornerRadius(26)
                .overlay(
                    RoundedRectangle(cornerRadius: 26)
                        .stroke(Color.promptBorder, lineWidth: 1)
                )

                // Description Field
                VStack(alignment: .leading, spacing: 8) {
                    Text("Transaction Description")
                        .font(.urbanist(14, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    TextField("Enter description...", text: $descriptionText)
                        .font(.urbanist(15, weight: .medium))
                        .padding(14)
                        .background(Color.promptCard)
                        .cornerRadius(14)
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(Color.promptBorder, lineWidth: 1)
                        )
                }

                // Category Selector
                VStack(alignment: .leading, spacing: 8) {
                    Text("Category")
                        .font(.urbanist(14, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(viewModel.categories, id: \.id) { cat in
                                Button(action: {
                                    selectedCategory = cat.name
                                }) {
                                    Text(cat.name)
                                        .font(.urbanist(13, weight: .semibold))
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 9)
                                        .background(selectedCategory == cat.name ? Color.royalBlue : Color.promptCard)
                                        .foregroundColor(selectedCategory == cat.name ? .white : .promptTextPrimary)
                                        .clipShape(Capsule())
                                        .overlay(
                                            Capsule()
                                                .stroke(selectedCategory == cat.name ? Color.clear : Color.promptBorder, lineWidth: 1)
                                        )
                                }
                            }
                        }
                    }
                }

                // Notes Field
                VStack(alignment: .leading, spacing: 8) {
                    Text("Personal Notes")
                        .font(.urbanist(14, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    TextField("Add note (optional)...", text: $notesText)
                        .font(.urbanist(15, weight: .medium))
                        .padding(14)
                        .background(Color.promptCard)
                        .cornerRadius(14)
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(Color.promptBorder, lineWidth: 1)
                        )
                }

                // Recurring Toggle Card
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Recurring Subscription / Bill")
                            .font(.urbanist(15, weight: .semibold))
                            .foregroundColor(.promptTextPrimary)
                        Text("Mark as monthly or repeating payment")
                            .font(.urbanist(12, weight: .regular))
                            .foregroundColor(.promptTextSecondary)
                    }
                    Spacer()
                    Toggle("", isOn: $isRecurring)
                        .labelsHidden()
                }
                .padding(16)
                .background(Color.promptCard)
                .cornerRadius(22)
                .overlay(
                    RoundedRectangle(cornerRadius: 22)
                        .stroke(Color.promptBorder, lineWidth: 1)
                )

                // Save Changes Button (Pill)
                Button(action: {
                    var updated = transaction
                    updated.description = descriptionText
                    updated.category = selectedCategory
                    updated.notes = notesText
                    updated.isRecurring = isRecurring
                    viewModel.updateTransaction(updated)
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Text("Save Changes")
                        .font(.urbanist(16, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.royalBlue)
                        .clipShape(Capsule())
                }
                .padding(.top, 10)
            }
            .padding(20)
        }
        .background(Color.promptSurface.edgesIgnoringSafeArea(.all))
        .navigationBarTitle("Transaction Detail", displayMode: .inline)
        .navigationBarItems(trailing: Button(action: {
            showDeleteAlert = true
        }) {
            Image(systemName: "trash")
                .foregroundColor(.promptDanger)
        })
        .alert(isPresented: $showDeleteAlert) {
            Alert(
                title: Text("Delete Transaction"),
                message: Text("Are you sure you want to permanently delete this transaction?"),
                primaryButton: .destructive(Text("Delete")) {
                    viewModel.deleteTransaction(transaction)
                    presentationMode.wrappedValue.dismiss()
                },
                secondaryButton: .cancel()
            )
        }
    }
}

public struct DetailRowView: View {
    public let title: String
    public let value: String
    public var badgeColor: Color? = nil

    public var body: some View {
        HStack {
            Text(title)
                .font(.urbanist(14, weight: .regular))
                .foregroundColor(.promptTextSecondary)
            Spacer()
            if let badgeColor = badgeColor {
                Text(value)
                    .font(.urbanist(12, weight: .bold))
                    .foregroundColor(badgeColor)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(badgeColor.opacity(0.12))
                    .clipShape(Capsule())
            } else {
                Text(value)
                    .font(.urbanist(14, weight: .semibold))
                    .foregroundColor(.promptTextPrimary)
            }
        }
    }
}
