import Foundation
import Security

/// One-time online Supabase authentication.
///
/// - First launch: user signs in with email + password (requires internet).
/// - The JWT access token + refresh token are persisted in the iOS Keychain.
/// - On every subsequent launch the stored token is used — NO internet needed.
/// - When online and the access token has expired, it is silently refreshed.
///
/// SETUP: Set supabaseURL and supabaseKey to your project values.
public final class SupabaseAuthService: ObservableObject {
    public static let shared = SupabaseAuthService()

    // ─── SUPABASE PROJECT CREDENTIALS (anon/publishable key — safe in app) ───
    static let supabaseURL = "https://berurshibbxpunyselmd.supabase.co"
    static let supabaseKey = "sb_publishable_fRdux5ehpwczDqez9SLi_g_znW3Czty"
    // ─────────────────────────────────────────────────────────────────────────
    // NOTE: The secret/service_role key must NEVER go in an iOS app.
    //       Only the publishable (anon) key above is used here.

    private let session = URLSession(configuration: .default)

    // Keychain keys
    private let kAccessToken  = "supabase.access_token"
    private let kRefreshToken = "supabase.refresh_token"
    private let kUserId       = "supabase.user_id"
    private let kUserEmail    = "supabase.user_email"
    private let kExpiresAt    = "supabase.expires_at"

    @Published public private(set) var isAuthenticated: Bool = false
    @Published public private(set) var supabaseUserId: String? = nil
    @Published public private(set) var authError: String? = nil
    @Published public private(set) var isLoading: Bool = false

    private init() {
        // Restore session from keychain on startup (offline-safe)
        if let token = keychainRead(kAccessToken), !token.isEmpty {
            isAuthenticated = true
            supabaseUserId = keychainRead(kUserId)
        }
    }

    // MARK: - Current Access Token (for sync requests)
    public var accessToken: String? { keychainRead(kAccessToken) }
    public var userId: String?      { keychainRead(kUserId) }

    // MARK: - One-Time Sign In (requires internet, done once)
    public func signIn(email: String, password: String) async {
        guard Self.supabaseURL != "https://YOUR_PROJECT_REF.supabase.co" else {
            await setError("Supabase URL not configured yet.")
            return
        }
        await setLoading(true)
        defer { Task { await self.setLoading(false) } }

        let url = URL(string: "\(Self.supabaseURL)/auth/v1/token?grant_type=password")!
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.setValue(Self.supabaseKey, forHTTPHeaderField: "apikey")
        let body = ["email": email, "password": password]
        req.httpBody = try? JSONEncoder().encode(body)

        do {
            let (data, response) = try await session.data(for: req)
            guard let http = response as? HTTPURLResponse else { return }

            if http.statusCode == 200 {
                if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                    let accessToken  = json["access_token"]  as? String ?? ""
                    let refreshToken = json["refresh_token"] as? String ?? ""
                    let expiresIn    = json["expires_in"]    as? Double ?? 3600
                    let expiresAt    = Date().timeIntervalSince1970 + expiresIn

                    var userId    = ""
                    var userEmail = email
                    if let user = json["user"] as? [String: Any] {
                        userId    = user["id"]    as? String ?? ""
                        userEmail = user["email"] as? String ?? email
                    }

                    keychainWrite(kAccessToken,  value: accessToken)
                    keychainWrite(kRefreshToken, value: refreshToken)
                    keychainWrite(kUserId,       value: userId)
                    keychainWrite(kUserEmail,    value: userEmail)
                    keychainWrite(kExpiresAt,    value: String(expiresAt))

                    await MainActor.run {
                        self.isAuthenticated = true
                        self.supabaseUserId  = userId
                        self.authError       = nil
                    }
                }
            } else {
                let msg = (try? JSONSerialization.jsonObject(with: data) as? [String: Any])
                    .flatMap { $0["error_description"] as? String ?? $0["msg"] as? String }
                    ?? "Login failed (HTTP \(http.statusCode))"
                await setError(msg)
            }
        } catch {
            await setError("Network error: \(error.localizedDescription)")
        }
    }

    // MARK: - Sign Up (first-time account creation, requires internet)
    public func signUp(email: String, password: String) async {
        guard Self.supabaseURL != "https://YOUR_PROJECT_REF.supabase.co" else {
            await setError("Supabase URL not configured yet.")
            return
        }
        await setLoading(true)
        defer { Task { await self.setLoading(false) } }

        let url = URL(string: "\(Self.supabaseURL)/auth/v1/signup")!
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.setValue(Self.supabaseKey, forHTTPHeaderField: "apikey")
        let body = ["email": email, "password": password]
        req.httpBody = try? JSONEncoder().encode(body)

        do {
            let (data, response) = try await session.data(for: req)
            guard let http = response as? HTTPURLResponse else { return }
            if (200...299).contains(http.statusCode) {
                // Sign up succeeded — now sign in to get tokens
                await signIn(email: email, password: password)
            } else {
                let msg = (try? JSONSerialization.jsonObject(with: data) as? [String: Any])
                    .flatMap { $0["error_description"] as? String ?? $0["msg"] as? String }
                    ?? "Sign-up failed (HTTP \(http.statusCode))"
                await setError(msg)
            }
        } catch {
            await setError("Network error: \(error.localizedDescription)")
        }
    }

    // MARK: - Silent Token Refresh (background, when online)
    public func refreshIfNeeded() async {
        guard let expiresAtStr = keychainRead(kExpiresAt),
              let expiresAt = Double(expiresAtStr) else { return }
        let shouldRefresh = Date().timeIntervalSince1970 > (expiresAt - 300) // refresh 5 min early
        guard shouldRefresh,
              let refreshToken = keychainRead(kRefreshToken) else { return }

        print("[SupabaseAuth] 🔄 Refreshing access token…")
        let url = URL(string: "\(Self.supabaseURL)/auth/v1/token?grant_type=refresh_token")!
        var req = URLRequest(url: url)
        req.httpMethod = "POST"
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.setValue(Self.supabaseKey, forHTTPHeaderField: "apikey")
        let body = ["refresh_token": refreshToken]
        req.httpBody = try? JSONEncoder().encode(body)

        do {
            let (data, _) = try await session.data(for: req)
            if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let newAccess  = json["access_token"]  as? String,
               let newRefresh = json["refresh_token"] as? String,
               let expiresIn  = json["expires_in"]    as? Double {
                keychainWrite(kAccessToken,  value: newAccess)
                keychainWrite(kRefreshToken, value: newRefresh)
                keychainWrite(kExpiresAt,    value: String(Date().timeIntervalSince1970 + expiresIn))
                print("[SupabaseAuth] ✅ Token refreshed")
            }
        } catch {
            print("[SupabaseAuth] ⚠️  Token refresh failed (offline?) — will retry when online")
        }
    }

    // MARK: - Sign Out (clears local session — user needs internet to log in again)
    public func signOut() {
        keychainDelete(kAccessToken)
        keychainDelete(kRefreshToken)
        keychainDelete(kUserId)
        keychainDelete(kUserEmail)
        keychainDelete(kExpiresAt)
        DispatchQueue.main.async {
            self.isAuthenticated = false
            self.supabaseUserId  = nil
        }
    }

    // MARK: - Helpers
    private func setError(_ msg: String) async {
        await MainActor.run { self.authError = msg }
    }
    private func setLoading(_ val: Bool) async {
        await MainActor.run { self.isLoading = val }
    }

    // MARK: - Keychain Wrappers
    private func keychainWrite(_ key: String, value: String) {
        let data = Data(value.utf8)
        let query: [String: Any] = [
            kSecClass as String:       kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String:   data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
        ]
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }

    private func keychainRead(_ key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String:       kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String:  true,
            kSecMatchLimit as String:  kSecMatchLimitOne
        ]
        var result: AnyObject?
        SecItemCopyMatching(query as CFDictionary, &result)
        guard let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    private func keychainDelete(_ key: String) {
        let query: [String: Any] = [
            kSecClass as String:       kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}
