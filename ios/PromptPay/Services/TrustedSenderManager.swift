import Foundation

public final class TrustedSenderManager {
    public static let shared = TrustedSenderManager()

    private let appGroupName = "group.com.promptpay"
    private var defaults: UserDefaults {
        UserDefaults(suiteName: appGroupName) ?? UserDefaults.standard
    }

    private let keyTrustedSenders = "trusted_senders_set"
    private let keyBalancePrefix = "last_balance_"
    private let keyScamFilteringEnabled = "scam_filtering_enabled"

    private let builtInTrustedSenders: Set<String> = [
        "m-money", "mtn momo", "mtnmomo", "mtnrwanda", "airtel",
        "mpesa", "mpesa info", "safaricom", "boa", "cbe", "urubutopay"
    ]

    private init() {}

    public func isKnownOrTrustedSender(_ sender: String) -> Bool {
        let cleanSender = sender.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()

        // 1. Check built-in trusted list
        if builtInTrustedSenders.contains(cleanSender) {
            return true
        }

        // 2. Check if numeric shortcode (3 to 6 digits, e.g. 127, 131, 251994, 8994)
        let shortcodeRegex = try? NSRegularExpression(pattern: #"^\d{3,6}$"#)
        let range = NSRange(location: 0, length: cleanSender.utf16.count)
        if let match = shortcodeRegex?.firstMatch(in: cleanSender, options: [], range: range), match.range.location != NSNotFound {
            return true
        }

        // 3. Check user-persisted trusted senders
        let userSet = Set(defaults.stringArray(forKey: keyTrustedSenders) ?? [])
        return userSet.contains(cleanSender)
    }

    public func addTrustedSender(_ sender: String) {
        let cleanSender = sender.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !cleanSender.isEmpty else { return }

        var userSet = Set(defaults.stringArray(forKey: keyTrustedSenders) ?? [])
        userSet.insert(cleanSender)
        defaults.set(Array(userSet), forKey: keyTrustedSenders)
    }

    public func getLastBalance(for providerKey: String) -> Double? {
        let cleanKey = providerKey.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !cleanKey.isEmpty else { return nil }
        let key = keyBalancePrefix + cleanKey
        if defaults.object(forKey: key) != nil {
            return defaults.double(forKey: key)
        }
        return nil
    }

    public func setLastBalance(for providerKey: String, balance: Double) {
        let cleanKey = providerKey.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !cleanKey.isEmpty else { return }
        let key = keyBalancePrefix + cleanKey
        defaults.set(balance, forKey: key)
    }

    public func isScamFilteringEnabled() -> Bool {
        if defaults.object(forKey: keyScamFilteringEnabled) == nil {
            return true // Enabled by default
        }
        return defaults.bool(forKey: keyScamFilteringEnabled)
    }

    public func setScamFilteringEnabled(_ enabled: Bool) {
        defaults.set(enabled, forKey: keyScamFilteringEnabled)
    }
}
