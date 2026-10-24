import SwiftUI

public struct AddTransactionView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject var viewModel: MainViewModel

    @State private var amountText: String = ""
    @State private var feeText: String = "0"
    @State private var descriptionText: String = ""
    @State private var selectedCategory: String = "General"

    public init(viewModel: MainViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Source locked badge info banner (Tile)
                    HStack(spacing: 12) {
                        Image(systemName: "lock.shield.fill")
                            .font(.system(size: 20))
                            .foregroundColor(.rapportPine)
                        Text("This entry will be permanently marked as **Manual Entry (Fixed)** and cannot be altered to an SMS record.")
                            .font(.urbanist(13, weight: .medium))
                            .foregroundColor(.promptTextPrimary)
                    }
                    .padding(16)
                    .background(Color.rapportPine.opacity(0.1))
                    .cornerRadius(22)
                    .overlay(
                        RoundedRectangle(cornerRadius: 22)
                            .stroke(Color.rapportPine.opacity(0.2), lineWidth: 1)
                    )

                    // Amount input (Card)
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Amount (\(viewModel.selectedCountry.currencyCode))")
                            .font(.urbanist(14, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        TextField("0.00", text: $amountText)
                            .keyboardType(.decimalPad)
                            .font(.urbanist(28, weight: .bold))
                            .padding(14)
                            .background(Color.promptCard)
                            .cornerRadius(14)
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                    }

                    // Transaction Fee input (optional)
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Transaction Fee (\(viewModel.selectedCountry.currencyCode)) - Optional")
                            .font(.urbanist(14, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        TextField("0", text: $feeText)
                            .keyboardType(.decimalPad)
                            .font(.urbanist(18, weight: .semibold))
                            .padding(14)
                            .background(Color.promptCard)
                            .cornerRadius(14)
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                    }

                    // Description / Merchant
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Description / Merchant")
                            .font(.urbanist(14, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        TextField("e.g. Groceries, Coffee, Taxi", text: $descriptionText)
                            .font(.urbanist(15, weight: .medium))
                            .padding(14)
                            .background(Color.promptCard)
                            .cornerRadius(14)
                            .overlay(
                                RoundedRectangle(cornerRadius: 14)
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                    }

                    // Category selection
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

                    // Submit button (Pill)
                    Button(action: {
                        guard let amt = Double(amountText), amt > 0 else { return }
                        let feeAmt = Double(feeText) ?? 0.0
                        viewModel.addManualTransaction(
                            amount: amt,
                            category: selectedCategory.isEmpty ? "General" : selectedCategory,
                            description: descriptionText,
                            fee: feeAmt,
                            feeCategory: "Transaction Fee"
                        )
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("Save Transaction")
                            .font(.urbanist(16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.royalBlue)
                            .clipShape(Capsule())
                    }
                    .padding(.top, 12)
                }
                .padding(20)
            }
            .background(Color.promptSurface.edgesIgnoringSafeArea(.all))
            .navigationBarTitle("Add Transaction", displayMode: .inline)
            .navigationBarItems(trailing: Button("Cancel") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
}
