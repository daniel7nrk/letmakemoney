import SwiftUI

/// One-time Supabase login screen.
/// Shown only once — after login, the JWT is stored in Keychain and
/// the user NEVER needs internet again just to open the app.
/// Internet is only used later for background Supabase sync.
public struct SupabaseLoginView: View {
    @StateObject private var auth = SupabaseAuthService.shared

    @State private var email: String    = ""
    @State private var password: String = ""
    @State private var isSignUp: Bool   = false
    @State private var showPassword: Bool = false

    var onAuthenticated: () -> Void

    public var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [Color("AccentGreen").opacity(0.15), Color(.systemBackground)],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 28) {
                    // Logo & heading
                    VStack(spacing: 10) {
                        Image(systemName: "bolt.circle.fill")
                            .font(.system(size: 64))
                            .foregroundColor(Color("AccentGreen"))
                        Text("PromptPay")
                            .font(.largeTitle.bold())
                        Text(isSignUp
                             ? "Create your account\nto enable cloud backup"
                             : "Sign in to enable\ncloud backup & sync")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.top, 40)

                    // Info card
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: "wifi.slash")
                            .foregroundColor(.orange)
                            .font(.title3)
                        VStack(alignment: .leading, spacing: 4) {
                            Text("One-time online login")
                                .font(.headline)
                            Text("After signing in once, the app works 100% offline. Internet is only used to sync your transactions to the cloud.")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding()
                    .background(Color.orange.opacity(0.1))
                    .cornerRadius(12)
                    .padding(.horizontal)

                    // Form
                    VStack(spacing: 16) {
                        // Email field
                        HStack {
                            Image(systemName: "envelope")
                                .foregroundColor(.secondary)
                                .frame(width: 24)
                            TextField("Email address", text: $email)
                                .keyboardType(.emailAddress)
                                .autocapitalization(.none)
                                .autocorrectionDisabled()
                        }
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(12)

                        // Password field
                        HStack {
                            Image(systemName: "lock")
                                .foregroundColor(.secondary)
                                .frame(width: 24)
                            if showPassword {
                                TextField("Password", text: $password)
                                    .autocapitalization(.none)
                                    .autocorrectionDisabled()
                            } else {
                                SecureField("Password", text: $password)
                            }
                            Button {
                                showPassword.toggle()
                            } label: {
                                Image(systemName: showPassword ? "eye.slash" : "eye")
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(12)

                        // Error message
                        if let err = auth.authError {
                            HStack {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(.red)
                                Text(err)
                                    .font(.caption)
                                    .foregroundColor(.red)
                            }
                            .padding(.horizontal)
                        }

                        // Primary action button
                        Button {
                            Task {
                                if isSignUp {
                                    await auth.signUp(email: email, password: password)
                                } else {
                                    await auth.signIn(email: email, password: password)
                                }
                                if auth.isAuthenticated {
                                    onAuthenticated()
                                }
                            }
                        } label: {
                            Group {
                                if auth.isLoading {
                                    ProgressView()
                                        .tint(.white)
                                } else {
                                    Text(isSignUp ? "Create Account & Continue" : "Sign In & Continue")
                                        .fontWeight(.semibold)
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(
                                (email.isEmpty || password.count < 6 || auth.isLoading)
                                ? Color.gray.opacity(0.4)
                                : Color("AccentGreen")
                            )
                            .foregroundColor(.white)
                            .cornerRadius(14)
                        }
                        .disabled(email.isEmpty || password.count < 6 || auth.isLoading)

                        // Toggle sign in / sign up
                        Button {
                            isSignUp.toggle()
                            auth.authError = nil
                        } label: {
                            Text(isSignUp
                                 ? "Already have an account? Sign In"
                                 : "No account yet? Create one")
                                .font(.subheadline)
                                .foregroundColor(Color("AccentGreen"))
                        }

                        // Skip option (offline-only, no cloud backup)
                        Button {
                            onAuthenticated()
                        } label: {
                            Text("Skip — use offline only")
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .underline()
                        }
                        .padding(.top, 4)
                    }
                    .padding(.horizontal)

                    Spacer(minLength: 40)
                }
            }
        }
        .navigationBarHidden(true)
    }
}
