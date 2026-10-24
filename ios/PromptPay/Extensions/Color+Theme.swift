import SwiftUI
import UIKit

// =========================================================================
// Rapport Design System Tokens (1:1 with web/css/rapport.css & Android)
// =========================================================================

extension Color {
    // Light Theme Tokens
    public static let rapportBgLight = Color(hex: "#EBE8E1")
    public static let rapportSolidLight = Color(hex: "#F6F4EF")
    public static let rapportFloatLight = Color(hex: "#FDFCF9")
    public static let rapportTextLight = Color(hex: "#17191A")
    public static let rapportMutedLight = Color(hex: "#676A66")
    public static let rapportFaintLight = Color(hex: "#8E918C")
    public static let rapportLineLight = Color(hex: "#17191A").opacity(0.12)
    public static let rapportWellLight = Color(hex: "#17191A").opacity(0.06)
    public static let rapportCreamLight = Color(hex: "#FBF8F2")

    // Dark Theme Tokens
    public static let rapportBgDark = Color(hex: "#111412")
    public static let rapportSolidDark = Color(hex: "#1C201E")
    public static let rapportFloatDark = Color(hex: "#242927")
    public static let rapportTextDark = Color(hex: "#F1EEE8")
    public static let rapportMutedDark = Color(hex: "#A3A6A0")
    public static let rapportFaintDark = Color(hex: "#7D807B")
    public static let rapportLineDark = Color.white.opacity(0.08)
    public static let rapportWellDark = Color.white.opacity(0.06)

    // Brand Accents
    public static let rapportPine = Color(hex: "#2F5C50")
    public static let rapportPineDark = Color(hex: "#6CC59A")
    public static let rapportButter = Color(hex: "#F2C94C")
    public static let rapportTomato = Color(hex: "#C8431F")
    public static let rapportGood = Color(hex: "#2F7A52")
    public static let rapportBad = Color(hex: "#C8431F")

    // Dock & Pill Tokens
    public static let rapportDock = Color(hex: "#17191A")
    public static let rapportOnDock = Color(hex: "#FBF8F2")
    public static let rapportPill = Color(hex: "#17191A")
    public static let rapportOnPill = Color(hex: "#FBF8F2")

    // Dynamic Colors (Auto Light/Dark mode via UIColor)
    public static let promptSurface = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(Color.rapportBgDark) : UIColor(Color.rapportBgLight)
    })
    public static let promptCard = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(Color.rapportSolidDark) : UIColor(Color.rapportSolidLight)
    })
    public static let promptFloat = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(Color.rapportFloatDark) : UIColor(Color.rapportFloatLight)
    })
    public static let promptBorder = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(Color.rapportLineDark) : UIColor(Color.rapportLineLight)
    })
    public static let promptTextPrimary = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(Color.rapportTextDark) : UIColor(Color.rapportTextLight)
    })
    public static let promptTextSecondary = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(Color.rapportMutedDark) : UIColor(Color.rapportMutedLight)
    })
    public static let promptSuccess = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(Color.rapportPineDark) : UIColor(Color.rapportGood)
    })
    public static let promptWarning = Color.rapportButter
    public static let promptDanger = Color.rapportTomato
    public static let promptPurple = Color(hex: "#8B5CF6")

    // Primary Brand Ink (replacing old RoyalBlue to match web .btn--ink)
    public static let royalBlue = Color(UIColor { trait in
        trait.userInterfaceStyle == .dark ? UIColor(Color.rapportTextDark) : UIColor(Color.rapportTextLight)
    })
    public static let royalBlueLight = Color.rapportPine
    public static let royalBlueDark = Color(hex: "#17191A")

    public init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
