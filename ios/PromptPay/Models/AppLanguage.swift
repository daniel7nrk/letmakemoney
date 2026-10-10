import Foundation

public enum AppLanguage: String, CaseIterable, Identifiable, Codable {
    case english = "en"
    case french = "fr"

    public var id: String { rawValue }

    public var displayName: String {
        switch self {
        case .english: return "English"
        case .french: return "French"
        }
    }

    public var nativeName: String {
        switch self {
        case .english: return "English"
        case .french: return "Français"
        }
    }

    public var flag: String {
        switch self {
        case .english: return "🇺🇸"
        case .french: return "🇫🇷"
        }
    }

    public static func fromCode(_ code: String?) -> AppLanguage {
        switch code?.lowercased() {
        case "fr", "fra", "french":
            return .french
        default:
            return .english
        }
    }
}
