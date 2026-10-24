import SwiftUI

public struct SmsExplainerView: View {
    public let onContinue: () -> Void

    public var body: some View {
        ZStack {
            Color.promptSurface.ignoresSafeArea()

            VStack(alignment: .leading, spacing: 24) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Automated SMS Protection")
                        .font(.urbanist(26, weight: .bold))
                        .foregroundColor(.promptTextPrimary)

                    Text("How PromptPay automatically captures your mobile money transactions on iPhone.")
                        .font(.urbanist(15, weight: .regular))
                        .foregroundColor(.promptTextSecondary)
                }
                .padding(.horizontal, 24)
                .padding(.top, 24)

                ScrollView {
                    VStack(spacing: 16) {
                        guideCard(
                            stepNumber: "1",
                            icon: "envelope.badge.shield.half.filled",
                            title: "IdentityLookup SMS Filter",
                            desc: "Apple's system extension automatically inspects incoming SMS from unknown senders & MoMo operators to filter scams and log legitimate transactions in the background."
                        )

                        guideCard(
                            stepNumber: "2",
                            icon: "repeat.circle.fill",
                            title: "Apple Shortcuts Automation",
                            desc: "Set up a zero-touch Personal Automation in the iOS Shortcuts app so every MoMo/Bank SMS is piped directly into PromptPay without you ever opening the app."
                        )

                        guideCard(
                            stepNumber: "3",
                            icon: "doc.on.clipboard.fill",
                            title: "Smart Clipboard Detection",
                            desc: "Whenever you copy an SMS notification, PromptPay instantly detects and parses it the moment you open the app."
                        )

                        guideCard(
                            stepNumber: "4",
                            icon: "lock.shield.fill",
                            title: "Strict On-Device Privacy",
                            desc: "No bank SMS or personal identity data is ever transmitted to an external server. Everything stays on your iPhone."
                        )
                    }
                    .padding(.horizontal, 24)
                }

                Spacer()

                Button(action: onContinue) {
                    Text("Enable & Continue")
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

    private func guideCard(stepNumber: String, icon: String, title: String, desc: String) -> some View {
        HStack(alignment: .top, spacing: 14) {
            ZStack {
                Circle()
                    .fill(Color.royalBlue.opacity(0.1))
                    .frame(width: 42, height: 42)

                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(.royalBlue)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.urbanist(16, weight: .semibold))
                    .foregroundColor(.promptTextPrimary)

                Text(desc)
                    .font(.urbanist(13, weight: .regular))
                    .foregroundColor(.promptTextSecondary)
                    .lineSpacing(2)
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.promptCard)
        .cornerRadius(22)
        .overlay(
            RoundedRectangle(cornerRadius: 22)
                .stroke(Color.promptBorder, lineWidth: 1)
        )
    }
}
