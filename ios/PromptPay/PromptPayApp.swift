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

    public var body: some View {
        if viewModel.userProfile.hasCompletedOnboarding {
            MainTabView(viewModel: viewModel)
        } else {
            WelcomeView {
                var updated = viewModel.userProfile
                updated.hasCompletedOnboarding = true
                viewModel.userProfile = updated
            }
        }
    }
}
