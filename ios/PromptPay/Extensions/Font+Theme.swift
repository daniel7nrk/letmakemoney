import SwiftUI

// =========================================================================
// Rapport Typography Tokens (Urbanist font with system fallback)
// =========================================================================

extension Font {
    public static func urbanist(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        let fontName: String
        switch weight {
        case .bold, .heavy, .black:
            fontName = "Urbanist-Bold"
        case .semibold:
            fontName = "Urbanist-SemiBold"
        case .medium:
            fontName = "Urbanist-Medium"
        default:
            fontName = "Urbanist-Regular"
        }
        return Font.custom(fontName, size: size)
    }

    // Semantic scales matching the Rapport design system
    public static let rapportDisplayLarge = Font.urbanist(34, weight: .bold)
    public static let rapportHeadline = Font.urbanist(26, weight: .bold)
    public static let rapportTitle = Font.urbanist(20, weight: .bold)
    public static let rapportSubheadline = Font.urbanist(16, weight: .semibold)
    public static let rapportBody = Font.urbanist(14, weight: .medium)
    public static let rapportCaption = Font.urbanist(12, weight: .medium)
    public static let rapportBadge = Font.urbanist(11, weight: .bold)
}
