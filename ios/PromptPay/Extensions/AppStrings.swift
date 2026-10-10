import Foundation

public struct AppStrings {
    // Bottom Navigation
    public let navHome: String
    public let navTransactions: String
    public let navReports: String
    public let navSettings: String

    // Settings Screen
    public let settingsTitle: String
    public let settingsAccountGroup: String
    public let settingsCountry: String
    public let settingsLanguage: String
    public let settingsLanguageSubtitle: String
    public let settingsSelectLanguage: String
    public let settingsSelectLanguagePrompt: String
    public let settingsCategoryManagement: String
    public let settingsCategorySubtitle: String
    public let settingsSecurityGroup: String
    public let settingsMomoScam: String
    public let settingsMomoScamSubtitle: String
    public let settingsLockMode: String
    public let settingsLockModeSubtitle: String
    public let settingsSignOut: String
    public let settingsConfirm: String
    public let settingsCancel: String

    public static let english = AppStrings(
        navHome: "Home",
        navTransactions: "Transactions",
        navReports: "Reports",
        navSettings: "Settings",
        settingsTitle: "Settings & Profile",
        settingsAccountGroup: "Account Settings",
        settingsCountry: "Country & Currency",
        settingsLanguage: "Language",
        settingsLanguageSubtitle: "Change app language",
        settingsSelectLanguage: "Select Language",
        settingsSelectLanguagePrompt: "Choose your preferred language for PromptPay.",
        settingsCategoryManagement: "Category Management",
        settingsCategorySubtitle: "Manage tags & keyword rules",
        settingsSecurityGroup: "Security & Privacy",
        settingsMomoScam: "MoMo Scam Detection",
        settingsMomoScamSubtitle: "Flag suspicious SMS & duplicate reversals",
        settingsLockMode: "App Lock Mode",
        settingsLockModeSubtitle: "PIN, Biometrics, or Direct Launch",
        settingsSignOut: "Sign Out",
        settingsConfirm: "Confirm",
        settingsCancel: "Cancel"
    )

    public static let french = AppStrings(
        navHome: "Accueil",
        navTransactions: "Transactions",
        navReports: "Rapports",
        navSettings: "Paramètres",
        settingsTitle: "Paramètres & Profil",
        settingsAccountGroup: "Paramètres du compte",
        settingsCountry: "Pays & Devise",
        settingsLanguage: "Langue",
        settingsLanguageSubtitle: "Changer la langue de l'application",
        settingsSelectLanguage: "Choisir la langue",
        settingsSelectLanguagePrompt: "Choisissez votre langue préférée pour PromptPay.",
        settingsCategoryManagement: "Gestion des catégories",
        settingsCategorySubtitle: "Gérer les balises et règles de mots-clés",
        settingsSecurityGroup: "Sécurité & Confidentialité",
        settingsMomoScam: "Détection d'arnaques MoMo",
        settingsMomoScamSubtitle: "Signaler les SMS suspects et fausses annulations",
        settingsLockMode: "Mode de verrouillage",
        settingsLockModeSubtitle: "Code PIN, biométrie ou accès direct",
        settingsSignOut: "Se déconnecter",
        settingsConfirm: "Confirmer",
        settingsCancel: "Annuler"
    )

    public static func get(_ language: AppLanguage) -> AppStrings {
        switch language {
        case .french: return french
        case .english: return english
        }
    }
}
