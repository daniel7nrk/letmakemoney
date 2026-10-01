import SwiftUI

extension Color {
    public static let royalBlue = Color(hex: "#2E6FF2")
    public static let royalBlueLight = Color(hex: "#5A8FF6")
    public static let royalBlueDark = Color(hex: "#1E4BB3")
    public static let promptSurface = Color(hex: "#F8FAFC")
    public static let promptCard = Color(hex: "#FFFFFF")
    public static let promptBorder = Color(hex: "#E2E8F0")
    public static let promptTextPrimary = Color(hex: "#0F172A")
    public static let promptTextSecondary = Color(hex: "#64748B")
    public static let promptSuccess = Color(hex: "#10B981")
    public static let promptWarning = Color(hex: "#F59E0B")
    public static let promptDanger = Color(hex: "#EF4444")
    public static let promptPurple = Color(hex: "#8B5CF6")

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
