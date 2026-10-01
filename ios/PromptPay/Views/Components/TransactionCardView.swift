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
            VStack(alignment: .leading, spacing: 8) {
                // Top row: Merchant name and Amount
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(transaction.merchant.isEmpty ? transaction.description : transaction.merchant)
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.promptTextPrimary)
                            .lineLimit(1)

                        if !transaction.description.isEmpty && transaction.description != transaction.merchant {
                            Text(transaction.description)
                                .font(.system(size: 13))
                                .foregroundColor(.promptTextSecondary)
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    Text(FormatUtil.formatAmount(transaction.amount, currency: transaction.currency))
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.promptTextPrimary)
                }

                // Bottom row: Category badge, Fixed Source badge, and Date
                HStack(spacing: 8) {
                    // Category Badge
                    Text(transaction.category)
                        .font(.system(size: 11, weight: .medium))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color.royalBlue.opacity(0.12))
                        .foregroundColor(.royalBlue)
                        .cornerRadius(6)

                    // Source Badge (Strictly locked: SMS vs Manual)
                    Text(transaction.source == "SMS" ? "SMS Alert (Fixed)" : "Manual Entry (Fixed)")
                        .font(.system(size: 10, weight: .semibold))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(transaction.source == "SMS" ? Color.promptPurple.opacity(0.12) : Color.promptSuccess.opacity(0.12))
                        .foregroundColor(transaction.source == "SMS" ? .promptPurple : .promptSuccess)
                        .cornerRadius(4)

                    Spacer()

                    Text(FormatUtil.formatDate(transaction.dateEpochMillis))
                        .font(.system(size: 11))
                        .foregroundColor(.promptTextSecondary)
                }
            }
            .padding(14)
            .background(Color.promptCard)
            .cornerRadius(14)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(Color.promptBorder, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.02), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(PlainButtonStyle())
    }
}
