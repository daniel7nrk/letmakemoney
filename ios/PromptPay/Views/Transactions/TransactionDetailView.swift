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
                // Hero Amount Card
                VStack(spacing: 8) {
                    Text(transaction.merchant.isEmpty ? transaction.description : transaction.merchant)
                        .font(.system(size: 17, weight: .medium))
                        .foregroundColor(.white.opacity(0.85))

                    Text(FormatUtil.formatAmount(transaction.amount, currency: transaction.currency))
                        .font(.system(size: 34, weight: .bold))
                        .foregroundColor(.white)

                    Text(FormatUtil.formatDate(transaction.dateEpochMillis))
                        .font(.system(size: 13))
                        .foregroundColor(.white.opacity(0.75))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 28)
                .padding(.horizontal, 20)
                .background(Color.royalBlue)
                .cornerRadius(24)
                .shadow(color: Color.royalBlue.opacity(0.25), radius: 10, x: 0, y: 5)

                // Meta Information Card
                VStack(spacing: 12) {
                    DetailRowView(title: "Payment Method", value: transaction.paymentMethod)
                    Divider()
                    // Strict Immutable Source
                    DetailRowView(
                        title: "Source",
                        value: transaction.source == "SMS" ? "SMS Alert (Fixed)" : "Manual Entry (Fixed)",
                        badgeColor: transaction.source == "SMS" ? .promptPurple : .promptSuccess
                    )
                    Divider()
                    DetailRowView(title: "Status", value: transaction.status)
                }
                .padding(18)
                .background(Color.promptCard)
                .cornerRadius(18)
                .overlay(
                    RoundedRectangle(cornerRadius: 18)
                        .stroke(Color.promptBorder, lineWidth: 1)
                )

                // Description Field
                VStack(alignment: .leading, spacing: 8) {
                    Text("Transaction Description")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    TextField("Enter description...", text: $descriptionText)
                        .padding(14)
                        .background(Color.promptSurface)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.promptBorder, lineWidth: 1)
                        )
                }

                // Category Selector
                VStack(alignment: .leading, spacing: 8) {
                    Text("Category")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(viewModel.categories, id: \.id) { cat in
                                Button(action: {
                                    selectedCategory = cat.name
                                }) {
                                    Text(cat.name)
                                        .font(.system(size: 13, weight: .medium))
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 8)
                                        .background(selectedCategory == cat.name ? Color.royalBlue : Color.promptSurface)
                                        .foregroundColor(selectedCategory == cat.name ? .white : .promptTextPrimary)
                                        .cornerRadius(20)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(selectedCategory == cat.name ? Color.clear : Color.promptBorder, lineWidth: 1)
                                        )
                                }
                            }
                        }
                    }
                }

                // Notes Field
                VStack(alignment: .leading, spacing: 8) {
                    Text("Notes")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    TextField("Add transaction note...", text: $notesText)
                        .padding(14)
                        .background(Color.promptSurface)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.promptBorder, lineWidth: 1)
                        )
                }

                // Recurring Toggle
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Flag as Recurring")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.promptTextPrimary)
                        Text("Monthly rent, subscriptions, or fixed bills")
                            .font(.system(size: 12))
                            .foregroundColor(.promptTextSecondary)
                    }
                    Spacer()
                    Toggle("", isOn: $isRecurring)
                        .labelsHidden()
                }
                .padding(16)
                .background(Color.promptCard)
                .cornerRadius(14)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color.promptBorder, lineWidth: 1)
                )

                // Save Button
                Button(action: {
                    var updated = transaction
                    updated.description = descriptionText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? transaction.merchant : descriptionText
                    updated.category = selectedCategory
                    updated.notes = notesText
                    updated.isRecurring = isRecurring
                    updated.status = "CONFIRMED"
                    // viewModel.updateTransaction automatically locks existing.source immutably
                    viewModel.updateTransaction(updated)
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Text("Save Changes")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.royalBlue)
                        .cornerRadius(14)
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
                .font(.system(size: 14))
                .foregroundColor(.promptTextSecondary)
            Spacer()
            if let badgeColor = badgeColor {
                Text(value)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(badgeColor)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(badgeColor.opacity(0.12))
                    .cornerRadius(6)
            } else {
                Text(value)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.promptTextPrimary)
            }
        }
    }
}
