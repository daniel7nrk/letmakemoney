import Foundation

public struct FormatUtil {
    private static let numberFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter
    }()

    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter
    }()

    public static func formatAmount(_ amount: Double, currency: String) -> String {
        let formattedNumber = numberFormatter.string(from: NSNumber(value: amount)) ?? String(format: "%.0f", amount)
        return "\(formattedNumber) \(currency)"
    }

    public static func formatDate(_ epochMillis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(epochMillis) / 1000.0)
        return dateFormatter.string(from: date)
    }

    public static func relativeTimeString(from epochMillis: Int64) -> String {
        let now = Date().timeIntervalSince1970
        let seconds = now - (Double(epochMillis) / 1000.0)

        if seconds < 60 {
            return "Just now"
        } else if seconds < 3600 {
            let mins = Int(seconds / 60)
            return "\(mins)m ago"
        } else if seconds < 86400 {
            let hrs = Int(seconds / 3600)
            return "\(hrs)h ago"
        } else {
            let days = Int(seconds / 86400)
            return "\(days)d ago"
        }
    }
}
