import SwiftUI

public struct LoginSignUpView: View {
    @ObservedObject var viewModel: MainViewModel
    public let onAuthSuccess: () -> Void

    @State private var isSignUp = false
    @State private var name = "Jane Doe"
    @State private var email = "jane.doe@promptpay.com"
    @State private var password = "password123"
    @State private var errorMessage: String? = nil

    public var body: some View {
        ZStack {
            Color.promptSurface.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 24) {
                    VStack(spacing: 8) {
                        Image(systemName: "person.badge.shield.checkmark.fill")
                            .font(.system(size: 54))
                            .foregroundColor(.royalBlue)
                            .padding(.top, 40)

                        Text(isSignUp ? "Create Your Account" : "Welcome Back")
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        Text(isSignUp ? "Set up your secure financial profile" : "Log in to view your verified transactions")
                            .font(.system(size: 15))
                            .foregroundColor(.promptTextSecondary)
                    }

                    VStack(spacing: 16) {
                        if isSignUp {
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Full Name")
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(.promptTextSecondary)

                                TextField("Jane Doe", text: $name)
                                    .padding(14)
                                    .background(Color.promptCard)
                                    .cornerRadius(12)
                                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.promptBorder, lineWidth: 1))
                            }
                        }

                        VStack(alignment: .leading, spacing: 6) {
                            Text("Email Address")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(.promptTextSecondary)

                            TextField("email@example.com", text: $email)
                                .keyboardType(.emailAddress)
                                .autocapitalization(.none)
                                .padding(14)
                                .background(Color.promptCard)
                                .cornerRadius(12)
                                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.promptBorder, lineWidth: 1))
                        }

                        VStack(alignment: .leading, spacing: 6) {
                            Text("Password")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(.promptTextSecondary)

                            SecureField("••••••••", text: $password)
                                .padding(14)
                                .background(Color.promptCard)
                                .cornerRadius(12)
                                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.promptBorder, lineWidth: 1))
                        }

                        if let error = errorMessage {
                            Text(error)
                                .font(.system(size: 13))
                                .foregroundColor(.promptDanger)
                        }

                        Button(action: handleAuth) {
                            Text(isSignUp ? "Sign Up" : "Log In")
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(Color.royalBlue)
                                .cornerRadius(14)
                        }
                        .padding(.top, 8)

                        // Sign In with Apple (Native iOS style)
                        Button(action: handleAppleSignIn) {
                            HStack {
                                Image(systemName: "applelogo")
                                    .font(.system(size: 18))
                                Text("Continue with Apple")
                                    .font(.system(size: 16, weight: .medium))
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 15)
                            .background(Color.black)
                            .cornerRadius(14)
                        }

                        Button(action: {
                            isSignUp.toggle()
                            errorMessage = nil
                        }) {
                            Text(isSignUp ? "Already have an account? Log In" : "Don't have an account? Sign Up")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.royalBlue)
                        }
                        .padding(.top, 4)
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
    }

    private func handleAuth() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Please enter email and password."
            return
        }
        var profile = viewModel.userProfile
        profile.email = email
        if isSignUp { profile.name = name }
        profile.hasCompletedOnboarding = true
        viewModel.userProfile = profile
        StorageService.shared.saveUserProfile(profile)
        onAuthSuccess()
    }

    private func handleAppleSignIn() {
        var profile = viewModel.userProfile
        profile.email = "apple.user@promptpay.com"
        profile.name = "Apple User"
        profile.hasCompletedOnboarding = true
        viewModel.userProfile = profile
        StorageService.shared.saveUserProfile(profile)
        onAuthSuccess()
    }
}
