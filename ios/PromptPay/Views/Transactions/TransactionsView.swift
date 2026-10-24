import SwiftUI

public struct TransactionsView: View {
    @ObservedObject var viewModel: MainViewModel
    @State private var selectedCategory: String = "All"
    @State private var searchQuery: String = ""
    @State private var selectedTransaction: Transaction? = nil

    public init(viewModel: MainViewModel) {
        self.viewModel = viewModel
    }

    public var filteredTransactions: [Transaction] {
        viewModel.transactions.filter { tx in
            let matchesCategory = (selectedCategory == "All") || (tx.category.lowercased() == selectedCategory.lowercased())
            let matchesSearch = searchQuery.isEmpty ||
                tx.merchant.localizedCaseInsensitiveContains(searchQuery) ||
                tx.description.localizedCaseInsensitiveContains(searchQuery) ||
                tx.category.localizedCaseInsensitiveContains(searchQuery)
            return matchesCategory && matchesSearch
        }
    }

    public var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Search & Filter Header
                VStack(spacing: 12) {
                    // Search bar (Pill)
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.promptTextSecondary)
                        TextField("Search transactions...", text: $searchQuery)
                            .font(.urbanist(14, weight: .medium))
                        if !searchQuery.isEmpty {
                            Button(action: { searchQuery = "" }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.promptTextSecondary)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(Color.promptSurface)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule()
                            .stroke(Color.promptBorder, lineWidth: 1)
                    )

                    // Category Dropdown Menu (Pill)
                    HStack {
                        Text("Category:")
                            .font(.urbanist(13, weight: .medium))
                            .foregroundColor(.promptTextSecondary)

                        Menu {
                            Button("All Categories") {
                                selectedCategory = "All"
                            }
                            ForEach(viewModel.categories, id: \.id) { cat in
                                Button(cat.name) {
                                    selectedCategory = cat.name
                                }
                            }
                        } label: {
                            HStack(spacing: 6) {
                                Text(selectedCategory == "All" ? "All Categories" : selectedCategory)
                                    .font(.urbanist(13, weight: .semibold))
                                    .foregroundColor(.promptTextPrimary)
                                Image(systemName: "chevron.down")
                                    .font(.system(size: 11))
                                    .foregroundColor(.promptTextSecondary)
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 6)
                            .background(Color.promptSurface)
                            .clipShape(Capsule())
                            .overlay(
                                Capsule()
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                        }

                        Spacer()

                        Text("\(filteredTransactions.count) items")
                            .font(.urbanist(12, weight: .regular))
                            .foregroundColor(.promptTextSecondary)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(Color.promptCard)
                .overlay(
                    Rectangle()
                        .frame(height: 1)
                        .foregroundColor(Color.promptBorder),
                    alignment: .bottom
                )

                // Transaction list
                if filteredTransactions.isEmpty {
                    Spacer()
                    VStack(spacing: 10) {
                        Image(systemName: "tray")
                            .font(.system(size: 40))
                            .foregroundColor(.promptTextSecondary.opacity(0.6))
                        Text("No transactions found")
                            .font(.urbanist(16, weight: .semibold))
                            .foregroundColor(.promptTextSecondary)
                    }
                    Spacer()
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(filteredTransactions) { tx in
                                TransactionCardView(transaction: tx) {
                                    selectedTransaction = tx
                                }
                            }
                        }
                        .padding(16)
                        .padding(.bottom, 80) // Space for floating dock
                    }
                }
            }
            .background(Color.promptSurface.edgesIgnoringSafeArea(.all))
            .navigationBarTitle("Transactions", displayMode: .inline)
            .sheet(item: $selectedTransaction) { tx in
                NavigationView {
                    TransactionDetailView(viewModel: viewModel, transaction: tx)
                }
            }
        }
    }
}
