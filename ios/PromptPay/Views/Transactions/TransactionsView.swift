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
                    // Search bar
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.promptTextSecondary)
                        TextField("Search transactions...", text: $searchQuery)
                            .font(.system(size: 14))
                        if !searchQuery.isEmpty {
                            Button(action: { searchQuery = "" }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.promptTextSecondary)
                            }
                        }
                    }
                    .padding(12)
                    .background(Color.promptSurface)
                    .cornerRadius(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color.promptBorder, lineWidth: 1)
                    )

                    // Category Dropdown Menu
                    HStack {
                        Text("Category:")
                            .font(.system(size: 14, weight: .medium))
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
                            HStack {
                                Text(selectedCategory == "All" ? "All Categories" : selectedCategory)
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundColor(.promptTextPrimary)
                                Image(systemName: "chevron.down")
                                    .font(.system(size: 12))
                                    .foregroundColor(.promptTextSecondary)
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(Color.promptSurface)
                            .cornerRadius(10)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                        }

                        Spacer()

                        Text("\(filteredTransactions.count) items")
                            .font(.system(size: 12))
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
                    VStack(spacing: 8) {
                        Image(systemName: "tray")
                            .font(.system(size: 40))
                            .foregroundColor(.promptTextSecondary.opacity(0.6))
                        Text("No transactions found")
                            .font(.system(size: 16, weight: .medium))
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
