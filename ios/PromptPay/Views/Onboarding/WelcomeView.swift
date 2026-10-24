import SwiftUI

public struct WelcomeView: View {
    public let onGetStarted: () -> Void

    public var body: some View {
        ZStack {
            Color.promptSurface.ignoresSafeArea()

            VStack(spacing: 32) {
                Spacer()

                // App Brand Icon
                ZStack {
                    Circle()
                        .fill(Color.royalBlue.opacity(0.12))
                        .frame(width: 120, height: 120)

                    Image(systemName: "shield.lefthalf.filled.badge.checkmark")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 64, height: 64)
                        .foregroundColor(.royalBlue)
                }

                VStack(spacing: 12) {
                    Text("PromptPay")
                        .font(.urbanist(32, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    Text("Automated mobile money & bank transaction tracking with real-time scam & fraud defense.")
                        .font(.urbanist(15, weight: .regular))
                        .foregroundColor(.promptTextSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }

                VStack(alignment: .leading, spacing: 16) {
                    featureRow(
                        icon: "bolt.badge.automatic.fill",
                        title: "Zero-Touch Automation",
                        desc: "Incoming SMS receipts are automatically ingested without manual typing."
                    )

                    featureRow(
                        icon: "exclamationmark.shield.fill",
                        title: "On-Device Fraud Detection",
                        desc: "Blocks phishing links, fake balance updates, and duplicate receipts."
                    )

                    featureRow(
                        icon: "lock.shield.fill",
                        title: "100% Private & Local",
                        desc: "Your financial logs and SMS data never leave your iPhone."
                    )
                }
                .padding(.horizontal, 28)

                Spacer()

                Button(action: onGetStarted) {
                    Text("Get Started")
                        .font(.urbanist(17, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.royalBlue)
                        .clipShape(Capsule())
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 16)
            }
        }
    }

    private func featureRow(icon: String, title: String, desc: String) -> some View {
        HStack(alignment: .top, spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(.royalBlue)
                .frame(width: 28, height: 28)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.urbanist(15, weight: .semibold))
                    .foregroundColor(.promptTextPrimary)

                Text(desc)
                    .font(.urbanist(13, weight: .regular))
                    .foregroundColor(.promptTextSecondary)
            }
        }
    }
}
