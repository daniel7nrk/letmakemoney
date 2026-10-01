import Foundation

public struct UserProfile: Identifiable, Codable, Equatable {
    public var id: Int
    public var name: String
    public var email: String
    public var photoUri: String?
    public var countryCode: String
    public var currencyCode: String
    public var pushNotificationsEnabled: Bool
    public var biometricEnabled: Bool
    public var hasCompletedOnboarding: Bool
    public var pinHash: String?
    public var lockMode: String          // "PIN", "BIOMETRIC", or "NONE"

    public init(
        id: Int = 1,
        name: String = "Jane Doe",
        email: String = "jane.doe@promptpay.com",
        photoUri: String? = nil,
        countryCode: String = "RWANDA",
        currencyCode: String = "RWF",
        pushNotificationsEnabled: Bool = true,
        biometricEnabled: Bool = false,
        hasCompletedOnboarding: Bool = false,
        pinHash: String? = nil,
        lockMode: String = "PIN"
    ) {
        self.id = id
        self.name = name
        self.email = email
        self.photoUri = photoUri
        self.countryCode = countryCode
        self.currencyCode = currencyCode
        self.pushNotificationsEnabled = pushNotificationsEnabled
        self.biometricEnabled = biometricEnabled
        self.hasCompletedOnboarding = hasCompletedOnboarding
        self.pinHash = pinHash
        self.lockMode = lockMode
    }
}
