import SwiftUI

/// One-time Supabase login screen.
/// Shown only once during onboarding — after login, the JWT is stored in Keychain and
/// the user NEVER needs internet again just to open the app.
/// Internet is only used later for background Supabase sync.
public struct SupabaseLoginView: View {
    @StateObject private var auth = SupabaseAuthService.shared

    @State private var email: String = ""
    @State private var password: String = ""
    @State private var isSignUp: Bool = false
    @State private var showPassword: Bool = false

    var onAuthenticated: () -> Void

    private var isFormDisabled: Bool {
        email.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || password.count < 6 || auth.isLoading
    }

    private var actionButtonBackground: Color {
        isFormDisabled ? Color.gray.opacity(0.4) : Color.royalBlue
    }

    public var body: some View {
        ZStack {
            Color.promptSurface.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 28) {
                    headerSection
                    infoCardSection
                    formCardSection
                    Spacer(minLength: 40)
                }
            }
        }
        .navigationBarHidden(true)
    }

    // MARK: - Subviews
    private var headerSection: some View {
        VStack(spacing: 10) {
            Image(systemName: "bolt.circle.fill")
                .font(.system(size: 64))
                .foregroundColor(.rapportPine)
            Text("PromptPay")
                .font(.urbanist(32, weight: .bold))
                .foregroundColor(.promptTextPrimary)
            Text(isSignUp
                 ? "Create your account\nto enable cloud backup"
                 : "Sign in to enable\ncloud backup & sync")
                .font(.urbanist(15, weight: .medium))
                .foregroundColor(.promptTextSecondary)
                .multilineTextAlignment(.center)
        }
        .padding(.top, 40)
    }

    private var infoCardSection: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: "wifi.slash")
                .foregroundColor(.rapportButter)
                .font(.title3)
            VStack(alignment: .leading, spacing: 4) {
                Text("One-time online login")
                    .font(.urbanist(15, weight: .bold))
                    .foregroundColor(.promptTextPrimary)
                Text("After signing in once, the app works 100% offline. Internet is only used to sync your transactions to the cloud.")
                    .font(.urbanist(13, weight: .regular))
                    .foregroundColor(.promptTextSecondary)
            }
        }
        .padding(16)
        .background(Color.rapportButter.opacity(0.12))
        .cornerRadius(22)
        .overlay(
            RoundedRectangle(cornerRadius: 22)
                .stroke(Color.rapportButter.opacity(0.3), lineWidth: 1)
        )
        .padding(.horizontal)
    }

    private var formCardSection: some View {
        VStack(spacing: 16) {
            emailInputField
            passwordInputField

            if let err = auth.authError {
                HStack {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundColor(.promptDanger)
                    Text(err)
                        .font(.urbanist(13, weight: .medium))
                        .foregroundColor(.promptDanger)
                }
                .padding(.horizontal)
            }

            submitButton
            toggleModeButton
            skipButton
        }
        .padding(22)
        .background(Color.promptCard)
        .cornerRadius(26)
        .overlay(
            RoundedRectangle(cornerRadius: 26)
                .stroke(Color.promptBorder, lineWidth: 1)
        )
        .padding(.horizontal)
    }

    private var emailInputField: some View {
        HStack {
            Image(systemName: "envelope")
                .foregroundColor(.promptTextSecondary)
                .frame(width: 24)
            TextField("Email address", text: $email)
                .font(.urbanist(15, weight: .medium))
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
                .autocorrectionDisabled()
        }
        .padding(14)
        .background(Color.promptCard)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(Color.promptBorder, lineWidth: 1)
        )
    }

    private var passwordInputField: some View {
        HStack {
            Image(systemName: "lock")
                .foregroundColor(.promptTextSecondary)
                .frame(width: 24)
            if showPassword {
                TextField("Password", text: $password)
                    .font(.urbanist(15, weight: .medium))
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
            } else {
                SecureField("Password", text: $password)
                    .font(.urbanist(15, weight: .medium))
            }
            Button {
                showPassword.toggle()
            } label: {
                Image(systemName: showPassword ? "eye.slash" : "eye")
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
    }

    private var submitButton: some View {
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
                        .font(.urbanist(16, weight: .bold))
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(actionButtonBackground)
            .foregroundColor(.white)
            .clipShape(Capsule())
        }
        .disabled(isFormDisabled)
    }

    private var toggleModeButton: some View {
        Button {
            isSignUp.toggle()
            auth.clearError()
        } label: {
            Text(isSignUp
                 ? "Already have an account? Sign In"
                 : "No account yet? Create one")
                .font(.urbanist(14, weight: .semibold))
                .foregroundColor(.royalBlue)
        }
    }

    private var skipButton: some View {
        Button {
            onAuthenticated()
        } label: {
            Text("Skip — use offline only")
                .font(.urbanist(12, weight: .medium))
                .foregroundColor(.promptTextSecondary)
                .underline()
        }
        .padding(.top, 4)
    }
}
