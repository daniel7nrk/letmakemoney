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
                    // Source locked badge info banner
                    HStack {
                        Image(systemName: "lock.shield.fill")
                            .foregroundColor(.royalBlue)
                        Text("This entry will be permanently marked as **Manual Entry (Fixed)** and cannot be altered to an SMS record.")
                            .font(.system(size: 13))
                            .foregroundColor(.promptTextPrimary)
                    }
                    .padding(14)
                    .background(Color.royalBlue.opacity(0.08))
                    .cornerRadius(12)

                    // Amount input
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Amount (\(viewModel.selectedCountry.currencyCode))")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        TextField("0.00", text: $amountText)
                            .keyboardType(.decimalPad)
                            .font(.system(size: 24, weight: .bold))
                            .padding(14)
                            .background(Color.promptSurface)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                    }

                    // Transaction Fee input (optional)
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Transaction Fee (\(viewModel.selectedCountry.currencyCode)) - Optional")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        TextField("0", text: $feeText)
                            .keyboardType(.decimalPad)
                            .font(.system(size: 18, weight: .semibold))
                            .padding(14)
                            .background(Color.promptSurface)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                    }

                    // Description / Merchant
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Description / Merchant")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        TextField("e.g. Groceries, Coffee, Taxi", text: $descriptionText)
                            .padding(14)
                            .background(Color.promptSurface)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                    }

                    // Category selection
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

                    // Submit button
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
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.royalBlue)
                            .cornerRadius(14)
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
