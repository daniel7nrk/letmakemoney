import Foundation

public enum Country: String, CaseIterable, Identifiable, Codable {
    case rwanda = "RWANDA"
    case kenya = "KENYA"
    case uganda = "UGANDA"
    case tanzania = "TANZANIA"
    case burundi = "BURUNDI"
    case southSudan = "SOUTH_SUDAN"

    public var id: String { rawValue }

    public var countryName: String {
        switch self {
        case .rwanda: return "Rwanda"
        case .kenya: return "Kenya"
        case .uganda: return "Uganda"
        case .tanzania: return "Tanzania"
        case .burundi: return "Burundi"
        case .southSudan: return "South Sudan"
        }
    }

    public var currencyCode: String {
        switch self {
        case .rwanda: return "RWF"
        case .kenya: return "KES"
        case .uganda: return "UGX"
        case .tanzania: return "TZS"
        case .burundi: return "BIF"
        case .southSudan: return "SSP"
        }
    }

    public var currencyName: String {
        switch self {
        case .rwanda: return "Rwandan Franc"
        case .kenya: return "Kenyan Shilling"
        case .uganda: return "Ugandan Shilling"
        case .tanzania: return "Tanzanian Shilling"
        case .burundi: return "Burundian Franc"
        case .southSudan: return "South Sudanese Pound"
        }
    }

    public var flagEmoji: String {
        switch self {
        case .rwanda: return "🇷🇼"
        case .kenya: return "🇰🇪"
        case .uganda: return "🇺🇬"
        case .tanzania: return "🇹🇿"
        case .burundi: return "🇧🇮"
        case .southSudan: return "🇸🇸"
        }
    }

    public var flagUrl: String {
        switch self {
        case .rwanda: return "https://flagcdn.com/w320/rw.png"
        case .kenya: return "https://flagcdn.com/w320/ke.png"
        case .uganda: return "https://flagcdn.com/w320/ug.png"
        case .tanzania: return "https://flagcdn.com/w320/tz.png"
        case .burundi: return "https://flagcdn.com/w320/bi.png"
        case .southSudan: return "https://flagcdn.com/w320/ss.png"
        }
    }

    public static func fromCurrencyCode(_ code: String) -> Country {
        let upper = code.uppercased().trimmingCharacters(in: .whitespaces)
        return Country.allCases.first { $0.currencyCode == upper } ?? .rwanda
    }
}
