import SwiftUI

@main
public struct PromptPayApp: App {
    @StateObject private var viewModel = MainViewModel()

    public init() {}

    public var body: some Scene {
        WindowGroup {
            ContentView(viewModel: viewModel)
        }
    }
}

public struct ContentView: View {
    @ObservedObject var viewModel: MainViewModel
    @Environment(\.scenePhase) private var scenePhase
    @State private var onboardingStep: OnboardingStep = .welcome

    private enum OnboardingStep {
        case welcome
        case country
        case smsExplainer
        case login
        case pinSetup
    }

    public var body: some View {
        Group {
            if viewModel.userProfile.hasCompletedOnboarding {
                MainTabView(viewModel: viewModel)
            } else {
                switch onboardingStep {
                case .welcome:
                    WelcomeView {
                        onboardingStep = .country
                    }
                case .country:
                    CountrySelectionView(viewModel: viewModel) {
                        onboardingStep = .smsExplainer
                    }
                case .smsExplainer:
                    SmsExplainerView {
                        onboardingStep = .login
                    }
                case .login:
                    LoginSignUpView(viewModel: viewModel) {
                        onboardingStep = .pinSetup
                    }
                case .pinSetup:
                    PinSetupView(viewModel: viewModel) {
                        var updated = viewModel.userProfile
                        updated.hasCompletedOnboarding = true
                        viewModel.updateProfile(updated)
                    }
                }
            }
        }
        .onOpenURL { url in
            viewModel.handleDeepLink(url: url)
        }
        .onChange(of: scenePhase) { newPhase in
            if newPhase == .active {
                viewModel.checkClipboard()
            }
        }
        .onAppear {
            viewModel.checkClipboard()
        }
        .alert(isPresented: $viewModel.showClipboardImportPrompt) {
            Alert(
                title: Text("📋 Transaction SMS Detected"),
                message: Text("PromptPay detected a transaction SMS on your clipboard:\n\n\"\(viewModel.detectedClipboardText ?? "")\"\n\nWould you like to import it?"),
                primaryButton: .default(Text("Import Transaction")) {
                    viewModel.importFromClipboard()
                },
                secondaryButton: .cancel {
                    viewModel.showClipboardImportPrompt = false
                    viewModel.detectedClipboardText = nil
                }
            )
        }
    }
}
