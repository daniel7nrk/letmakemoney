import Foundation

/// Offline-first Supabase sync service.
/// All data is written locally first. When the device comes online,
/// this service upserts unsynced records to Supabase in the background.
///
/// SETUP: Replace the two constants below with your project values from
///   https://supabase.com  →  Project Settings → API
public final class SupabaseSyncService {
    public static let shared = SupabaseSyncService()

    // ─── CONFIGURE THESE ─────────────────────────────────────────────────────
    private let supabaseURL  = "https://YOUR_PROJECT_REF.supabase.co"
    private let supabaseKey  = "YOUR_ANON_KEY"
    // ─────────────────────────────────────────────────────────────────────────

    private let storage = StorageService.shared
    private let session = URLSession(configuration: .default)
    private var isSyncing = false

    private init() {}

    // MARK: - Public Entry Point
    /// Call this whenever the device comes back online.
    public func syncAllPendingData() {
        guard !isSyncing else { return }
        guard supabaseURL != "https://YOUR_PROJECT_REF.supabase.co" else {
            print("[SupabaseSync] ⚠️  Supabase credentials not configured — skipping sync")
            return
        }
        isSyncing = true
        Task {
            defer { self.isSyncing = false }
            await syncTransactions()
        }
    }

    // MARK: - Transactions
    private func syncTransactions() async {
        let unsynced = storage.loadUnsyncedTransactions()
        guard !unsynced.isEmpty else { return }

        print("[SupabaseSync] Uploading \(unsynced.count) unsynced transaction(s)…")

        let payloads = unsynced.map { tx -> [String: Any] in
            var dict: [String: Any] = [
                "id":                    tx.id,
                "amount":                tx.amount,
                "currency":              tx.currency,
                "merchant":              tx.merchant,
                "description":           tx.description,
                "category":              tx.category,
                "fee":                   tx.fee,
                "fee_category":          tx.feeCategory,
                "source":                tx.source,
                "status":                tx.status,
                "date_epoch_millis":     tx.dateEpochMillis,
                "updated_at_epoch_millis": tx.updatedAtEpochMillis,
                "is_synced":             false        // Supabase column, set server-side or update after
            ]
            return dict
        }

        guard let body = try? JSONSerialization.data(withJSONObject: payloads) else { return }

        var request = URLRequest(url: URL(string: "\(supabaseURL)/rest/v1/transactions?on_conflict=id")!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue(supabaseKey, forHTTPHeaderField: "apikey")
        request.setValue("Bearer \(supabaseKey)", forHTTPHeaderField: "Authorization")
        request.setValue("resolution=merge-duplicates", forHTTPHeaderField: "Prefer")
        request.httpBody = body

        do {
            let (_, response) = try await session.data(for: request)
            if let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) {
                let ids = unsynced.map { $0.id }
                storage.markTransactionsAsSynced(ids)
                print("[SupabaseSync] ✅ Synced \(ids.count) transaction(s) successfully")
            } else if let http = response as? HTTPURLResponse {
                print("[SupabaseSync] ❌ Sync failed with status \(http.statusCode)")
            }
        } catch {
            print("[SupabaseSync] ❌ Network error: \(error.localizedDescription)")
        }
    }
}
