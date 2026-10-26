import SwiftUI

public struct SimulateSmsSheet: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject var viewModel: MainViewModel

    @State private var sender: String = "MTN MoMo"
    @State private var messageBody: String = "*165*S*Payment of 4,500 RWF to SIMBA SUPERMARKET with token was completed at 2026-10-26 13:20:00. Fee: 100 RWF. New balance: 85,400 RWF.EN#"
    @State private var resultMessage: String? = nil

    private struct Template {
        let title: String
        let sender: String
        let body: String
    }

    private let templates: [Template] = [
        Template(
            title: "🛒 Supermarket Grocery (MoMo)",
            sender: "MTN MoMo",
            body: "*165*S*Payment of 5,500 RWF to SIMBA SUPERMARKET with token was completed at 2026-10-26 13:20:00. Fee: 100 RWF. New balance: 79,900 RWF.EN#"
        ),
        Template(
            title: "🚕 Taxi Moto Ride (Airtel)",
            sender: "Airtel",
            body: "Txn ID: 981248. You transferred 2,000 RWF to YEGO MOTO at 2026-10-26 11:15:00. Fee: 50 RWF. New balance: 14,200 RWF."
        ),
        Template(
            title: "⚡ Electricity Cash Power",
            sender: "MTN MoMo",
            body: "*182*S*Payment of 10,000 RWF to EUCL Cashpower. Token: 9821-4821-4821. Fee: 150 RWF. New balance: 69,750 RWF.EN#"
        ),
        Template(
            title: "⚠️ Phishing Scam Link (Blocked)",
            sender: "Unknown",
            body: "MTN MoMo: Urgent! Your account is suspended. Click http://momo-secure-auth.com/verify to enter your PIN immediately."
        ),
        Template(
            title: "❓ Uncategorized Merchant (Pending Review)",
            sender: "0788998877",
            body: "Payment of 7,800 RWF to Kigali Tech Supplies was completed. Fee: 100 RWF. New balance: 61,850 RWF."
        )
    ]

    public var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("Select a realistic SMS receipt to test on-device ingestion, automated parsing, and real-time fraud defense:")
                        .font(.urbanist(14, weight: .regular))
                        .foregroundColor(.promptTextSecondary)

                    // Template Buttons
                    VStack(spacing: 10) {
                        ForEach(templates, id: \.title) { tmpl in
                            Button(action: {
                                sender = tmpl.sender
                                messageBody = tmpl.body
                            }) {
                                HStack {
                                    Text(tmpl.title)
                                        .font(.urbanist(14, weight: .semibold))
                                        .foregroundColor(.promptTextPrimary)
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .font(.system(size: 12))
                                        .foregroundColor(.promptTextSecondary)
                                }
                                .padding(14)
                                .background(Color.promptCard)
                                .cornerRadius(16)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 16)
                                        .stroke(Color.promptBorder, lineWidth: 1)
                                )
                            }
                        }
                    }

                    // Sender Input
                    VStack(alignment: .leading, spacing: 6) {
                        Text("SMS Sender / Originator")
                            .font(.urbanist(13, weight: .semibold))
                            .foregroundColor(.promptTextSecondary)

                        TextField("Sender (e.g. MTN MoMo)", text: $sender)
                            .font(.urbanist(14, weight: .medium))
                            .padding(12)
                            .background(Color.promptCard)
                            .cornerRadius(12)
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.promptBorder, lineWidth: 1))
                    }

                    // Body Input
                    VStack(alignment: .leading, spacing: 6) {
                        Text("SMS Message Body")
                            .font(.urbanist(13, weight: .semibold))
                            .foregroundColor(.promptTextSecondary)

                        TextEditor(text: $messageBody)
                            .font(.urbanist(13, weight: .regular))
                            .frame(minHeight: 90)
                            .padding(8)
                            .background(Color.promptCard)
                            .cornerRadius(12)
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.promptBorder, lineWidth: 1))
                    }

                    if let result = resultMessage {
                        Text(result)
                            .font(.urbanist(13, weight: .semibold))
                            .foregroundColor(.rapportPine)
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.rapportPine.opacity(0.1))
                            .cornerRadius(12)
                    }

                    // Ingest Button
                    Button(action: ingestSms) {
                        Text("Simulate Ingest SMS")
                            .font(.urbanist(16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.royalBlue)
                            .clipShape(Capsule())
                    }
                    .padding(.top, 10)
                }
                .padding(20)
            }
            .background(Color.promptSurface.ignoresSafeArea())
            .navigationBarTitle("Simulate SMS Ingestion", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }

    private func ingestSms() {
        let (tx, scam) = viewModel.processIncomingSms(sender: sender, body: messageBody)
        if let s = scam {
            resultMessage = "🚨 Fraud Detected! Scam alert created: \(s.primaryReason)"
        } else if let t = tx {
            resultMessage = "✅ Transaction recorded: \(FormatUtil.formatAmount(t.amount, currency: t.currency)) at \(t.merchant) (\(t.status))"
        } else {
            resultMessage = "ℹ️ Message processed (non-expense or already tracked)."
        }
    }
}
