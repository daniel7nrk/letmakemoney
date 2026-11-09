import Foundation

/// Offline-first Supabase sync service.
///
/// Flow:
///   1. All transactions are saved locally first (instant, works offline).
///   2. When NetworkMonitor detects connectivity → syncAllPendingData() is called.
///   3. If the user is authenticated (JWT stored in Keychain), unsynced records
///      are upserted to Supabase using the user's access token.
///   4. On success, records are marked isSynced = true locally.
///
/// The user JWT is silently refreshed in the background when online.
/// If offline, sync is skipped and will retry on next connectivity event.
public final class SupabaseSyncService {
    public static let shared = SupabaseSyncService()

    private let storage  = StorageService.shared
    private let auth     = SupabaseAuthService.shared
    private let session  = URLSession(configuration: .default)
    private var isSyncing = false

    private var supabaseURL: String { SupabaseAuthService.supabaseURL }
    private var supabaseKey: String { SupabaseAuthService.supabaseKey }

    private init() {}

    // MARK: - Public Entry Point
    /// Called automatically by MainViewModel when network comes online.
    public func syncAllPendingData() {
        guard !isSyncing else { return }
        guard supabaseURL != "https://YOUR_PROJECT_REF.supabase.co" else {
            print("[SupabaseSync] ⚠️  Supabase credentials not set — skipping sync")
            return
        }
        guard auth.isAuthenticated, auth.accessToken != nil else {
            print("[SupabaseSync] ⚠️  Not authenticated — skipping sync (user must log in once online)")
            return
        }
        isSyncing = true
        Task {
            defer { self.isSyncing = false }
            // Silently refresh JWT if close to expiry
            await auth.refreshIfNeeded()
            await syncTransactions()
        }
    }

    // MARK: - Transactions Upsert
    private func syncTransactions() async {
        let unsynced = storage.loadUnsyncedTransactions()
        guard !unsynced.isEmpty else {
            print("[SupabaseSync] ✅ All transactions already synced")
            return
        }

        guard let token = auth.accessToken,
              let userId = auth.userId, !userId.isEmpty else {
            print("[SupabaseSync] ⚠️  Missing auth token or user ID")
            return
        }

        print("[SupabaseSync] Uploading \(unsynced.count) unsynced transaction(s)…")

        let payloads: [[String: Any]] = unsynced.map { tx in
            [
                "id":                      tx.id,
                "user_id":                 userId,
                "amount":                  tx.amount,
                "currency":                tx.currency,
                "merchant":                tx.merchant,
                "description":             tx.description,
                "category":                tx.category,
                "fee":                     tx.fee,
                "fee_category":            tx.feeCategory,
                "source":                  tx.source,
                "status":                  tx.status,
                "is_recurring":            tx.isRecurring,
                "notes":                   tx.notes,
                "payment_method":          tx.paymentMethod,
                "date_epoch_millis":       tx.dateEpochMillis,
                "updated_at_epoch_millis": tx.updatedAtEpochMillis,
                "is_synced":               true
            ]
        }

        guard let body = try? JSONSerialization.data(withJSONObject: payloads) else { return }

        var request = URLRequest(url: URL(string: "\(supabaseURL)/rest/v1/transactions?on_conflict=id")!)
        request.httpMethod = "POST"
        request.setValue("application/json",        forHTTPHeaderField: "Content-Type")
        request.setValue("application/json",        forHTTPHeaderField: "Accept")
        request.setValue(supabaseKey,               forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(token)",         forHTTPHeaderField: "Authorization")
        request.setValue("resolution=merge-duplicates", forHTTPHeaderField: "Prefer")
        request.httpBody = body

        do {
            let (data, response) = try await session.data(for: request)
            if let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) {
                let ids = unsynced.map { $0.id }
                storage.markTransactionsAsSynced(ids)
                print("[SupabaseSync] ✅ Synced \(ids.count) transaction(s)")
            } else if let http = response as? HTTPURLResponse {
                let errMsg = String(data: data, encoding: .utf8) ?? "unknown"
                print("[SupabaseSync] ❌ Upsert failed \(http.statusCode): \(errMsg)")
            }
        } catch {
            print("[SupabaseSync] ❌ Network error: \(error.localizedDescription)")
        }
    }
}
