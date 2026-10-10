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

            // Custom Bottom Navigation Bar with Centered Plus Button
            VStack(spacing: 0) {
                Divider()
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

                    // Centered Floating Add (+) Action Button
                    Button(action: {
                        showAddTransactionSheet = true
                    }) {
                        ZStack {
                            Circle()
                                .fill(Color.royalBlue)
                                .frame(width: 52, height: 52)
                                .shadow(color: Color.royalBlue.opacity(0.35), radius: 6, x: 0, y: 3)

                            Image(systemName: "plus")
                                .font(.system(size: 22, weight: .bold))
                                .foregroundColor(.white)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .offset(y: -8)

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
                .padding(.horizontal, 8)
                .padding(.top, 8)
                .padding(.bottom, 24)
                .background(Color.promptCard.edgesIgnoringSafeArea(.bottom))
            }
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
            VStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(isSelected ? .royalBlue : .promptTextSecondary)
                Text(title)
                    .font(.system(size: 11, weight: isSelected ? .bold : .normal))
                    .foregroundColor(isSelected ? .royalBlue : .promptTextSecondary)
            }
            .frame(maxWidth: .infinity)
        }
    }
}
