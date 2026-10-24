import SwiftUI

public struct TransactionCardView: View {
    public let transaction: Transaction
    public var onTap: (() -> Void)? = nil

    public init(transaction: Transaction, onTap: (() -> Void)? = nil) {
        self.transaction = transaction
        self.onTap = onTap
    }

    public var body: some View {
        Button(action: { onTap?() }) {
            VStack(alignment: .leading, spacing: 10) {
                // Top row: Merchant name and Amount
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(transaction.merchant.isEmpty ? transaction.description : transaction.merchant)
                            .font(.urbanist(16, weight: .bold))
                            .foregroundColor(.promptTextPrimary)
                            .lineLimit(1)

                        if !transaction.description.isEmpty && transaction.description != transaction.merchant {
                            Text(transaction.description)
                                .font(.urbanist(13, weight: .regular))
                                .foregroundColor(.promptTextSecondary)
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 3) {
                        Text(FormatUtil.formatAmount(transaction.amount, currency: transaction.currency))
                            .font(.urbanist(16, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        if transaction.fee > 0 {
                            HStack(spacing: 4) {
                                Text("Fee: \(FormatUtil.formatAmount(transaction.fee, currency: transaction.currency))")
                                    .font(.urbanist(11, weight: .medium))
                                    .foregroundColor(.rapportTomato)

                                Text(transaction.feeCategory.isEmpty ? "Fee" : transaction.feeCategory)
                                    .font(.urbanist(9, weight: .bold))
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.rapportButter.opacity(0.2))
                                    .foregroundColor(.rapportTomato)
                                    .clipShape(Capsule())
                            }
                        }
                    }
                }

                // Bottom row: Category badge, Fixed Source badge, and Date
                HStack(spacing: 8) {
                    // Category Badge
                    Text(transaction.category)
                        .font(.urbanist(11, weight: .semibold))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(Color.royalBlue.opacity(0.08))
                        .foregroundColor(.royalBlue)
                        .clipShape(Capsule())

                    // Source Badge (Strictly locked: SMS vs Manual)
                    Text(transaction.source == "SMS" ? "SMS Alert (Fixed)" : "Manual Entry (Fixed)")
                        .font(.urbanist(10, weight: .semibold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(transaction.source == "SMS" ? Color.promptPurple.opacity(0.12) : Color.rapportGood.opacity(0.12))
                        .foregroundColor(transaction.source == "SMS" ? .promptPurple : .rapportGood)
                        .clipShape(Capsule())

                    Spacer()

                    Text(FormatUtil.formatDate(transaction.dateEpochMillis))
                        .font(.urbanist(11, weight: .regular))
                        .foregroundColor(.promptTextSecondary)
                }
            }
            .padding(16)
            .background(Color.promptCard)
            .cornerRadius(22)
            .overlay(
                RoundedRectangle(cornerRadius: 22)
                    .stroke(Color.promptBorder, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 3)
        }
        .buttonStyle(PlainButtonStyle())
    }
}
