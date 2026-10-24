import SwiftUI

public struct MainTabView: View {
    @ObservedObject var viewModel: MainViewModel
    @State private var selectedTab: Int = 0
    @State private var showAddTransactionSheet: Bool = false

    public init(viewModel: MainViewModel) {
        self.viewModel = viewModel
    }

    public var body: some View {
        let strings = AppStrings.get(viewModel.selectedLanguage)

        return ZStack(alignment: .bottom) {
            // Main content depending on selected tab
            Group {
                switch selectedTab {
                case 0:
                    TransactionsView(viewModel: viewModel)
                case 1:
                    TransactionsView(viewModel: viewModel)
                case 2:
                    TransactionsView(viewModel: viewModel)
                case 3:
                    ProfileSettingsView(viewModel: viewModel)
                default:
                    TransactionsView(viewModel: viewModel)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Rapport Floating Capsule Dock
            HStack(spacing: 0) {
                // Home
                TabBarItem(
                    icon: "house.fill",
                    title: strings.navHome,
                    isSelected: selectedTab == 0
                ) {
                    selectedTab = 0
                }

                // Transactions
                TabBarItem(
                    icon: "list.bullet.rectangle.fill",
                    title: strings.navTransactions,
                    isSelected: selectedTab == 1
                ) {
                    selectedTab = 1
                }

                // Centered Floating Add (+) Action Button (Rapport Pine Pill)
                Button(action: {
                    showAddTransactionSheet = true
                }) {
                    ZStack {
                        Capsule()
                            .fill(Color.rapportPine)
                            .frame(width: 46, height: 32)
                            .shadow(color: Color.rapportPine.opacity(0.4), radius: 6, x: 0, y: 2)

                        Image(systemName: "plus")
                            .font(.system(size: 17, weight: .bold))
                            .foregroundColor(Color.rapportOnDock)
                    }
                }
                .frame(maxWidth: .infinity)

                // Reports
                TabBarItem(
                    icon: "chart.pie.fill",
                    title: strings.navReports,
                    isSelected: selectedTab == 2
                ) {
                    selectedTab = 2
                }

                // Settings / More
                TabBarItem(
                    icon: "gearshape.fill",
                    title: strings.navSettings,
                    isSelected: selectedTab == 3
                ) {
                    selectedTab = 3
                }
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .background(
                Capsule()
                    .fill(Color.rapportDock)
                    .overlay(
                        Capsule()
                            .stroke(Color.white.opacity(0.14), lineWidth: 1)
                    )
                    .shadow(color: Color.black.opacity(0.28), radius: 14, x: 0, y: 6)
            )
            .padding(.horizontal, 16)
            .padding(.bottom, 12)
        }
        .edgesIgnoringSafeArea(.bottom)
        .sheet(isPresented: $showAddTransactionSheet) {
            AddTransactionView(viewModel: viewModel)
        }
    }
}

public struct TabBarItem: View {
    public let icon: String
    public let title: String
    public let isSelected: Bool
    public let action: () -> Void

    public var body: some View {
        Button(action: action) {
            VStack(spacing: 3) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(isSelected ? .rapportButter : .rapportOnDock.opacity(0.65))
                Text(title)
                    .font(.urbanist(11, weight: isSelected ? .bold : .medium))
                    .foregroundColor(isSelected ? .rapportButter : .rapportOnDock.opacity(0.65))
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 4)
        }
    }
}
