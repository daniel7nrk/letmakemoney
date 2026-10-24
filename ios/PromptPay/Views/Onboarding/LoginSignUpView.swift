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
                            .font(.urbanist(26, weight: .bold))
                            .foregroundColor(.promptTextPrimary)

                        Text(isSignUp ? "Set up your secure financial profile" : "Log in to view your verified transactions")
                            .font(.urbanist(15, weight: .regular))
                            .foregroundColor(.promptTextSecondary)
                    }

                    VStack(spacing: 16) {
                        if isSignUp {
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Full Name")
                                    .font(.urbanist(13, weight: .semibold))
                                    .foregroundColor(.promptTextSecondary)

                                TextField("Jane Doe", text: $name)
                                    .font(.urbanist(15, weight: .medium))
                                    .padding(14)
                                    .background(Color.promptCard)
                                    .cornerRadius(14)
                                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.promptBorder, lineWidth: 1))
                            }
                        }

                        VStack(alignment: .leading, spacing: 6) {
                            Text("Email Address")
                                .font(.urbanist(13, weight: .semibold))
                                .foregroundColor(.promptTextSecondary)

                            TextField("email@example.com", text: $email)
                                .font(.urbanist(15, weight: .medium))
                                .keyboardType(.emailAddress)
                                .autocapitalization(.none)
                                .padding(14)
                                .background(Color.promptCard)
                                .cornerRadius(14)
                                .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.promptBorder, lineWidth: 1))
                        }

                        VStack(alignment: .leading, spacing: 6) {
                            Text("Password")
                                .font(.urbanist(13, weight: .semibold))
                                .foregroundColor(.promptTextSecondary)

                            SecureField("••••••••", text: $password)
                                .font(.urbanist(15, weight: .medium))
                                .padding(14)
                                .background(Color.promptCard)
                                .cornerRadius(14)
                                .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.promptBorder, lineWidth: 1))
                        }

                        if let error = errorMessage {
                            Text(error)
                                .font(.urbanist(13, weight: .medium))
                                .foregroundColor(.promptDanger)
                        }

                        // Submit Button (Pill)
                        Button(action: handleAuth) {
                            Text(isSignUp ? "Sign Up" : "Log In")
                                .font(.urbanist(17, weight: .semibold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                                .background(Color.royalBlue)
                                .clipShape(Capsule())
                        }
                        .padding(.top, 8)

                        // Sign In with Apple (Pill)
                        Button(action: handleAppleSignIn) {
                            HStack {
                                Image(systemName: "applelogo")
                                    .font(.system(size: 18))
                                Text("Continue with Apple")
                                    .font(.urbanist(16, weight: .semibold))
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.black)
                            .clipShape(Capsule())
                        }

                        Button(action: {
                            isSignUp.toggle()
                            errorMessage = nil
                        }) {
                            Text(isSignUp ? "Already have an account? Log In" : "Don't have an account? Sign Up")
                                .font(.urbanist(14, weight: .semibold))
                                .foregroundColor(.royalBlue)
                        }
                        .padding(.top, 4)
                    }
                    .padding(24)
                    .background(Color.promptCard)
                    .cornerRadius(26)
                    .overlay(RoundedRectangle(cornerRadius: 26).stroke(Color.promptBorder, lineWidth: 1))
                    .padding(.horizontal, 20)
                }
            }
        }
    }

    private func handleAuth() {
        if isSignUp && name.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = "Please enter your name"
            return
        }
        if email.trimmingCharacters(in: .whitespaces).isEmpty || !email.contains("@") {
            errorMessage = "Please enter a valid email address"
            return
        }
        if password.count < 6 {
            errorMessage = "Password must be at least 6 characters"
            return
        }

        var profile = viewModel.userProfile ?? UserProfile()
        profile.name = isSignUp ? name : (profile.name.isEmpty ? "Jane Doe" : profile.name)
        profile.email = email
        viewModel.updateProfile(profile)
        onAuthSuccess()
    }

    private func handleAppleSignIn() {
        var profile = viewModel.userProfile ?? UserProfile()
        if profile.name.isEmpty { profile.name = "Apple User" }
        if profile.email.isEmpty { profile.email = "apple.id@icloud.com" }
        viewModel.updateProfile(profile)
        onAuthSuccess()
    }
}
