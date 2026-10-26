import SwiftUI

public struct ReportsView: View {
    @ObservedObject var viewModel: MainViewModel
    @State private var selectedTimeframe: String = "Monthly"
    @State private var showExportAlert: Bool = false

    private let timeframes = ["Weekly", "Monthly", "Yearly"]

    public init(viewModel: MainViewModel) {
        self.viewModel = viewModel
    }

    public var confirmedTransactions: [Transaction] {
        viewModel.transactions.filter { $0.status == "CONFIRMED" }
    }

    public var categoryTotals: [(category: String, amount: Double, percent: Double, color: Color)] {
        let total = viewModel.totalMonthlyExpenses
        guard total > 0 else { return [] }

        var grouped: [String: Double] = [:]
        for tx in confirmedTransactions {
            if tx.category != "Salary" && !tx.category.lowercased().contains("income") {
                grouped[tx.category, default: 0.0] += tx.amount
            }
        }

        let palette: [Color] = [.rapportPine, .royalBlue, .rapportButter, .promptPurple, .rapportTomato, .cyan, .orange]

        return grouped.enumerated().map { index, element in
            let pct = (element.value / total) * 100.0
            let color = palette[index % palette.count]
            return (category: element.key, amount: element.value, percent: pct, color: color)
        }.sorted { $0.amount > $1.amount }
    }

    public var totalFees: Double {
        confirmedTransactions.reduce(0.0) { $0 + $1.fee }
    }

    public var smsCount: Int {
        confirmedTransactions.filter { $0.source == "SMS" }.count
    }

    public var manualCount: Int {
        confirmedTransactions.filter { $0.source == "MANUAL" }.count
    }

    public var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Timeframe Switcher (Pill Segmented Control)
                    HStack(spacing: 6) {
                        ForEach(timeframes, id: \.self) { tf in
                            let isSelected = selectedTimeframe == tf
                            Button(action: { selectedTimeframe = tf }) {
                                Text(tf)
                                    .font(.urbanist(14, weight: isSelected ? .bold : .semibold))
                                    .foregroundColor(isSelected ? .white : .promptTextSecondary)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 10)
                                    .background(isSelected ? Color.royalBlue : Color.clear)
                                    .clipShape(Capsule())
                            }
                        }
                    }
                    .padding(4)
                    .background(Color.promptCard)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule()
                            .stroke(Color.promptBorder, lineWidth: 1)
                    )
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                    // Hero Spending Summary Card
                    VStack(alignment: .leading, spacing: 10) {
                        Text("\(selectedTimeframe) Total Spending")
                            .font(.urbanist(14, weight: .semibold))
                            .foregroundColor(.white.opacity(0.85))

                        Text(FormatUtil.formatAmount(viewModel.totalMonthlyExpenses, currency: viewModel.selectedCountry.currencyCode))
                            .font(.urbanist(34, weight: .bold))
                            .foregroundColor(.white)

                        HStack {
                            Text("\(confirmedTransactions.count) verified transactions")
                                .font(.urbanist(12, weight: .medium))
                                .foregroundColor(.white.opacity(0.85))

                            Spacer()

                            Text("Fees: \(FormatUtil.formatAmount(totalFees, currency: viewModel.selectedCountry.currencyCode))")
                                .font(.urbanist(12, weight: .semibold))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color.white.opacity(0.2))
                                .clipShape(Capsule())
                                .foregroundColor(.white)
                        }
                    }
                    .padding(22)
                    .background(Color.royalBlue)
                    .cornerRadius(26)
                    .shadow(color: Color.black.opacity(0.12), radius: 10, x: 0, y: 5)
                    .padding(.horizontal, 16)

                    // Category Breakdown Section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Category Breakdown")
                            .font(.urbanist(18, weight: .bold))
                            .foregroundColor(.promptTextPrimary)
                            .padding(.horizontal, 16)

                        if categoryTotals.isEmpty {
                            VStack(spacing: 8) {
                                Image(systemName: "chart.pie")
                                    .font(.system(size: 32))
                                    .foregroundColor(.promptTextSecondary.opacity(0.5))
                                Text("No categorized expense data yet")
                                    .font(.urbanist(14, weight: .medium))
                                    .foregroundColor(.promptTextSecondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 24)
                            .background(Color.promptCard)
                            .cornerRadius(22)
                            .padding(.horizontal, 16)
                        } else {
                            VStack(spacing: 14) {
                                ForEach(categoryTotals, id: \.category) { item in
                                    VStack(spacing: 8) {
                                        HStack {
                                            Circle()
                                                .fill(item.color)
                                                .frame(width: 10, height: 10)

                                            Text(item.category)
                                                .font(.urbanist(14, weight: .semibold))
                                                .foregroundColor(.promptTextPrimary)

                                            Spacer()

                                            Text(FormatUtil.formatAmount(item.amount, currency: viewModel.selectedCountry.currencyCode))
                                                .font(.urbanist(14, weight: .bold))
                                                .foregroundColor(.promptTextPrimary)

                                            Text(String(format: "(%.0f%%)", item.percent))
                                                .font(.urbanist(12, weight: .medium))
                                                .foregroundColor(.promptTextSecondary)
                                        }

                                        // Progress Bar
                                        GeometryReader { geo in
                                            ZStack(alignment: .leading) {
                                                Capsule()
                                                    .fill(Color.promptSurface)
                                                    .frame(height: 7)

                                                Capsule()
                                                    .fill(item.color)
                                                    .frame(width: max(8, geo.size.width * CGFloat(item.percent / 100.0)), height: 7)
                                            }
                                        }
                                        .frame(height: 7)
                                    }
                                }
                            }
                            .padding(18)
                            .background(Color.promptCard)
                            .cornerRadius(22)
                            .overlay(
                                RoundedRectangle(cornerRadius: 22)
                                    .stroke(Color.promptBorder, lineWidth: 1)
                            )
                            .padding(.horizontal, 16)
                        }
                    }

                    // Source Breakdown Grid
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Ingestion Breakdown")
                            .font(.urbanist(18, weight: .bold))
                            .foregroundColor(.promptTextPrimary)
                            .padding(.horizontal, 16)

                        HStack(spacing: 12) {
                            StatBox(
                                title: "SMS Automated",
                                value: "\(smsCount)",
                                subtitle: "Zero-touch receipts",
                                color: .promptPurple
                            )

                            StatBox(
                                title: "Manual Entries",
                                value: "\(manualCount)",
                                subtitle: "Manually entered",
                                color: .rapportGood
                            )
                        }
                        .padding(.horizontal, 16)
                    }

                    // Export Report Button
                    Button(action: { showExportAlert = true }) {
                        HStack(spacing: 8) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 16, weight: .semibold))
                            Text("Export Spending Report (CSV)")
                                .font(.urbanist(15, weight: .bold))
                        }
                        .foregroundColor(.royalBlue)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.promptCard)
                        .clipShape(Capsule())
                        .overlay(
                            Capsule()
                                .stroke(Color.royalBlue.opacity(0.3), lineWidth: 1)
                        )
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 4)
                    .padding(.bottom, 80) // Space for floating bottom dock
                }
            }
            .background(Color.promptSurface.ignoresSafeArea())
            .navigationBarTitle("Reports & Insights", displayMode: .inline)
            .alert(isPresented: $showExportAlert) {
                Alert(
                    title: Text("Report Exported"),
                    message: Text("Your financial summary with \(confirmedTransactions.count) transactions has been compiled for export."),
                    dismissButton: .default(Text("OK"))
                )
            }
        }
    }
}

private struct StatBox: View {
    let title: String
    let value: String
    let subtitle: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.urbanist(12, weight: .semibold))
                .foregroundColor(.promptTextSecondary)

            Text(value)
                .font(.urbanist(26, weight: .bold))
                .foregroundColor(color)

            Text(subtitle)
                .font(.urbanist(11, weight: .regular))
                .foregroundColor(.promptTextSecondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color.promptCard)
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color.promptBorder, lineWidth: 1)
        )
    }
}
