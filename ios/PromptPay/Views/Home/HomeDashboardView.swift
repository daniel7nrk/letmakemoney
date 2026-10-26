import SwiftUI

public struct HomeDashboardView: View {
    @ObservedObject var viewModel: MainViewModel
    public let onSelectTab: (Int) -> Void

    @State private var showNotificationsSheet: Bool = false
    @State private var showAddTransactionSheet: Bool = false
    @State private var showSimulateSmsSheet: Bool = false
    @State private var selectedTransaction: Transaction? = nil

    public init(viewModel: MainViewModel, onSelectTab: @escaping (Int) -> Void) {
        self.viewModel = viewModel
        self.onSelectTab = onSelectTab
    }

    public var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Top Header Bar
                    HStack(spacing: 12) {
                        // User Avatar
                        Button(action: { onSelectTab(3) }) {
                            HStack(spacing: 12) {
                                Circle()
                                    .fill(Color.royalBlue)
                                    .frame(width: 48, height: 48)
                                    .overlay(
                                        Text(viewModel.userProfile.name.isEmpty ? "P" : String(viewModel.userProfile.name.prefix(1)).uppercased())
                                            .font(.urbanist(20, weight: .bold))
                                            .foregroundColor(.white)
                                    )

                                VStack(alignment: .leading, spacing: 2) {
                                    Text("Hello, \(viewModel.userProfile.name.isEmpty ? "User" : viewModel.userProfile.name)")
                                        .font(.urbanist(17, weight: .bold))
                                        .foregroundColor(.promptTextPrimary)

                                    Text("\(viewModel.selectedCountry.countryName) • \(viewModel.selectedCountry.currencyCode)")
                                        .font(.urbanist(13, weight: .regular))
                                        .foregroundColor(.promptTextSecondary)
                                }
                            }
                        }
                        .buttonStyle(PlainButtonStyle())

                        Spacer()

                        // Bell Notification Button with Red Badge
                        Button(action: { showNotificationsSheet = true }) {
                            ZStack(alignment: .topTrailing) {
                                Circle()
                                    .fill(Color.promptCard)
                                    .frame(width: 44, height: 44)
                                    .overlay(
                                        Image(systemName: "bell.fill")
                                            .font(.system(size: 18))
                                            .foregroundColor(.promptTextPrimary)
                                    )
                                    .overlay(
                                        Circle()
                                            .stroke(Color.promptBorder, lineWidth: 1)
                                    )

                                let count = viewModel.unreadNotificationsCount + viewModel.pendingTransactions.count
                                if count > 0 {
                                    Text("\(min(count, 99))")
                                        .font(.urbanist(10, weight: .bold))
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 5)
                                        .padding(.vertical, 2)
                                        .background(Color.promptDanger)
                                        .clipShape(Capsule())
                                        .offset(x: 4, y: -4)
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                    // Total Monthly Spending Hero Card
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Total Monthly Spending")
                            .font(.urbanist(15, weight: .semibold))
                            .foregroundColor(.white.opacity(0.85))

                        Text(FormatUtil.formatAmount(viewModel.totalMonthlyExpenses, currency: viewModel.selectedCountry.currencyCode))
                            .font(.urbanist(34, weight: .bold))
                            .foregroundColor(.white)

                        HStack(spacing: 6) {
                            Image(systemName: "lock.shield.fill")
                                .font(.system(size: 13))
                            Text("100% On-Device · Encrypted Storage")
                                .font(.urbanist(12, weight: .medium))
                        }
                        .foregroundColor(.white.opacity(0.85))
                        .padding(.top, 4)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(22)
                    .background(Color.royalBlue)
                    .cornerRadius(26)
                    .shadow(color: Color.black.opacity(0.12), radius: 10, x: 0, y: 5)
                    .padding(.horizontal, 16)

                    // Quick Actions Row (Add Manual, Reports, Test SMS)
                    HStack(spacing: 12) {
                        QuickActionButton(
                            icon: "plus",
                            title: "Add Manual",
                            color: .rapportPine
                        ) {
                            showAddTransactionSheet = true
                        }

                        QuickActionButton(
                            icon: "chart.pie.fill",
                            title: "Reports",
                            color: .royalBlue
                        ) {
                            onSelectTab(2)
                        }

                        QuickActionButton(
                            icon: "bubble.left.and.bubble.right.fill",
                            title: "Test SMS",
                            color: .rapportButter
                        ) {
                            showSimulateSmsSheet = true
                        }
                    }
                    .padding(.horizontal, 16)

                    // Pending Review Alert Banner (if any)
                    if !viewModel.pendingTransactions.isEmpty {
                        Button(action: { onSelectTab(1) }) {
                            HStack(spacing: 14) {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(.rapportTomato)

                                VStack(alignment: .leading, spacing: 2) {
                                    Text("\(viewModel.pendingTransactions.count) Pending Review")
                                        .font(.urbanist(15, weight: .bold))
                                        .foregroundColor(.promptTextPrimary)
                                    Text("Uncategorized transactions waiting for your approval")
                                        .font(.urbanist(12, weight: .regular))
                                        .foregroundColor(.promptTextSecondary)
                                }

                                Spacer()

                                Image(systemName: "chevron.right")
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(.promptTextSecondary)
                            }
                            .padding(16)
                            .background(Color.rapportButter.opacity(0.18))
                            .cornerRadius(20)
                            .overlay(
                                RoundedRectangle(cornerRadius: 20)
                                    .stroke(Color.rapportButter.opacity(0.5), lineWidth: 1)
                            )
                        }
                        .buttonStyle(PlainButtonStyle())
                        .padding(.horizontal, 16)
                    }

                    // Active Scam Alert Banner (if any)
                    if !viewModel.activeScamAlerts.isEmpty {
                        Button(action: { showNotificationsSheet = true }) {
                            HStack(spacing: 14) {
                                Image(systemName: "shield.slash.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(.promptDanger)

                                VStack(alignment: .leading, spacing: 2) {
                                    Text("Suspicious SMS Blocked")
                                        .font(.urbanist(15, weight: .bold))
                                        .foregroundColor(.promptTextPrimary)
                                    Text("\(viewModel.activeScamAlerts.first?.primaryReason ?? "Fraud attempt intercepted")")
                                        .font(.urbanist(12, weight: .regular))
                                        .foregroundColor(.promptTextSecondary)
                                        .lineLimit(1)
                                }

                                Spacer()

                                Image(systemName: "chevron.right")
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(.promptTextSecondary)
                            }
                            .padding(16)
                            .background(Color.promptDanger.opacity(0.12))
                            .cornerRadius(20)
                            .overlay(
                                RoundedRectangle(cornerRadius: 20)
                                    .stroke(Color.promptDanger.opacity(0.3), lineWidth: 1)
                            )
                        }
                        .buttonStyle(PlainButtonStyle())
                        .padding(.horizontal, 16)
                    }

                    // Recent Transactions Section
                    VStack(spacing: 12) {
                        HStack {
                            Text("Recent Transactions")
                                .font(.urbanist(18, weight: .bold))
                                .foregroundColor(.promptTextPrimary)

                            Spacer()

                            Button(action: { onSelectTab(1) }) {
                                HStack(spacing: 4) {
                                    Text("See All")
                                        .font(.urbanist(14, weight: .semibold))
                                    Image(systemName: "chevron.right")
                                        .font(.system(size: 11, weight: .bold))
                                }
                                .foregroundColor(.royalBlue)
                            }
                        }
                        .padding(.horizontal, 16)

                        if viewModel.transactions.isEmpty {
                            VStack(spacing: 8) {
                                Image(systemName: "tray")
                                    .font(.system(size: 32))
                                    .foregroundColor(.promptTextSecondary.opacity(0.5))
                                Text("No transactions recorded yet")
                                    .font(.urbanist(14, weight: .medium))
                                    .foregroundColor(.promptTextSecondary)
                            }
                            .padding(.vertical, 24)
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(viewModel.transactions.prefix(5)) { tx in
                                    TransactionCardView(transaction: tx) {
                                        selectedTransaction = tx
                                    }
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                    }
                    .padding(.top, 4)
                    .padding(.bottom, 80) // Space for floating bottom dock
                }
            }
            .background(Color.promptSurface.ignoresSafeArea())
            .navigationBarHidden(true)
            .sheet(isPresented: $showNotificationsSheet) {
                NotificationsView(viewModel: viewModel)
            }
            .sheet(isPresented: $showAddTransactionSheet) {
                AddTransactionView(viewModel: viewModel)
            }
            .sheet(isPresented: $showSimulateSmsSheet) {
                SimulateSmsSheet(viewModel: viewModel)
            }
            .sheet(item: $selectedTransaction) { tx in
                NavigationView {
                    TransactionDetailView(viewModel: viewModel, transaction: tx)
                }
            }
        }
    }
}

private struct QuickActionButton: View {
    let icon: String
    let title: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                ZStack {
                    Circle()
                        .fill(color.opacity(0.12))
                        .frame(width: 44, height: 44)
                    Image(systemName: icon)
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(color)
                }

                Text(title)
                    .font(.urbanist(12, weight: .semibold))
                    .foregroundColor(.promptTextPrimary)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(Color.promptCard)
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color.promptBorder, lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}
