import SwiftUI

public struct ProfileSettingsView: View {
    @ObservedObject var viewModel: MainViewModel
    @State private var showLanguageSheet: Bool = false
    @State private var showCountrySheet: Bool = false
    @State private var pendingLanguage: AppLanguage = .english

    public init(viewModel: MainViewModel) {
        self.viewModel = viewModel
    }

    private var strings: AppStrings {
        AppStrings.get(viewModel.selectedLanguage)
    }

    public var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Profile Header Card
                    HStack(spacing: 16) {
                        Circle()
                            .fill(Color.royalBlue)
                            .frame(width: 60, height: 60)
                            .overlay(
                                Text(viewModel.userProfile?.name.prefix(1).uppercased() ?? "P")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(.white)
                            )

                        VStack(alignment: .leading, spacing: 4) {
                            Text(viewModel.userProfile?.name.isEmpty == false ? viewModel.userProfile!.name : "PromptPay User")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.promptTextPrimary)

                            Text(viewModel.userProfile?.email.isEmpty == false ? viewModel.userProfile!.email : "user@promptpay.com")
                                .font(.system(size: 14))
                                .foregroundColor(.promptTextSecondary)
                        }

                        Spacer()
                    }
                    .padding(18)
                    .background(Color.promptCard)
                    .cornerRadius(18)
                    .overlay(
                        RoundedRectangle(cornerRadius: 18)
                            .stroke(Color.promptBorder, lineWidth: 1)
                    )

                    // Account Settings Group
                    VStack(alignment: .leading, spacing: 12) {
                        Text(strings.settingsAccountGroup)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.promptTextSecondary)
                            .padding(.horizontal, 4)

                        VStack(spacing: 0) {
                            // Country & Currency Tile
                            Button(action: { showCountrySheet = true }) {
                                SettingsRow(
                                    icon: "globe",
                                    title: strings.settingsCountry,
                                    subtitle: "\(viewModel.selectedCountry.countryName) (\(viewModel.selectedCountry.currencyCode))"
                                )
                            }
                            .buttonStyle(PlainButtonStyle())

                            Divider().padding(.horizontal, 16)

                            // Language Tile
                            Button(action: {
                                pendingLanguage = viewModel.selectedLanguage
                                showLanguageSheet = true
                            }) {
                                SettingsRow(
                                    icon: "character.book.closed.fill",
                                    title: strings.settingsLanguage,
                                    subtitle: "\(viewModel.selectedLanguage.flag) \(viewModel.selectedLanguage.displayName) (\(viewModel.selectedLanguage.nativeName))"
                                )
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        .background(Color.promptCard)
                        .cornerRadius(18)
                        .overlay(
                            RoundedRectangle(cornerRadius: 18)
                                .stroke(Color.promptBorder, lineWidth: 1)
                        )
                    }

                    // Security & Privacy Group
                    VStack(alignment: .leading, spacing: 12) {
                        Text(strings.settingsSecurityGroup)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.promptTextSecondary)
                            .padding(.horizontal, 4)

                        VStack(spacing: 0) {
                            // MoMo Scam Detection
                            HStack {
                                Image(systemName: "shield.lefthalf.filled")
                                    .font(.system(size: 20))
                                    .foregroundColor(.royalBlue)
                                    .frame(width: 32)

                                VStack(alignment: .leading, spacing: 2) {
                                    Text(strings.settingsMomoScam)
                                        .font(.system(size: 15, weight: .semibold))
                                        .foregroundColor(.promptTextPrimary)

                                    Text(strings.settingsMomoScamSubtitle)
                                        .font(.system(size: 12))
                                        .foregroundColor(.promptTextSecondary)
                                }

                                Spacer()

                                Toggle("", isOn: Binding(
                                    get: { viewModel.isScamFilteringEnabled },
                                    set: { viewModel.isScamFilteringEnabled = $0 }
                                ))
                                .labelsHidden()
                            }
                            .padding(16)
                        }
                        .background(Color.promptCard)
                        .cornerRadius(18)
                        .overlay(
                            RoundedRectangle(cornerRadius: 18)
                                .stroke(Color.promptBorder, lineWidth: 1)
                        )
                    }

                    // Sign Out Button
                    Button(action: {
                        viewModel.signOut()
                    }) {
                        HStack {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                                .font(.system(size: 16, weight: .bold))
                            Text(strings.settingsSignOut)
                                .font(.system(size: 15, weight: .bold))
                        }
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.promptCard)
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color.red.opacity(0.3), lineWidth: 1)
                        )
                    }
                    .padding(.top, 8)
                }
                .padding(20)
            }
            .background(Color.promptSurface.ignoresSafeArea())
            .navigationBarTitle(strings.settingsTitle, displayMode: .inline)
        }
        // Language Picker Sheet / Dialog
        .actionSheet(isPresented: $showLanguageSheet) {
            ActionSheet(
                title: Text(strings.settingsSelectLanguage),
                message: Text(strings.settingsSelectLanguagePrompt),
                buttons: [
                    .default(Text("🇺🇸 English (English)")) {
                        viewModel.selectLanguage(.english)
                    },
                    .default(Text("🇫🇷 French (Français)")) {
                        viewModel.selectLanguage(.french)
                    },
                    .cancel(Text(strings.settingsCancel))
                ]
            )
        }
        .sheet(isPresented: $showCountrySheet) {
            CountrySelectionView(viewModel: viewModel) {
                showCountrySheet = false
            }
        }
    }
}

private struct SettingsRow: View {
    let icon: String
    let title: String
    let subtitle: String

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(.royalBlue)
                .frame(width: 32)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.promptTextPrimary)

                Text(subtitle)
                    .font(.system(size: 13))
                    .foregroundColor(.promptTextSecondary)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(.promptTextSecondary.opacity(0.6))
        }
        .padding(16)
    }
}
