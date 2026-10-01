import Foundation

public struct Category: Identifiable, Codable, Equatable {
    public var id: Int64
    public var name: String
    public var iconName: String
    public var tag: String             // "Essential", "Fixed", "Income", "Commute", "Lifestyle"
    public var colorHex: String
    public var isCustom: Bool
    public var keywords: String        // Comma separated keywords

    public init(
        id: Int64,
        name: String,
        iconName: String,
        tag: String,
        colorHex: String,
        isCustom: Bool = false,
        keywords: String = ""
    ) {
        self.id = id
        self.name = name
        self.iconName = iconName
        self.tag = tag
        self.colorHex = colorHex
        self.isCustom = isCustom
        self.keywords = keywords
    }

    public static let defaults: [Category] = [
        Category(id: 1, name: "Food & Dining", iconName: "fork.knife", tag: "Essential", colorHex: "#FFDAD6", keywords: "starbucks,coffee,food,dining,restaurant,supermarket,simba,grocery"),
        Category(id: 2, name: "Rent & Utilities", iconName: "house.fill", tag: "Fixed", colorHex: "#D7E2FF", keywords: "apartment,rent,utilities,housing,electricity,water,skyline"),
        Category(id: 3, name: "Salary", iconName: "banknote.fill", tag: "Income", colorHex: "#FFDDB4", keywords: "salary,income,payroll,deposit"),
        Category(id: 4, name: "Transport", iconName: "car.fill", tag: "Commute", colorHex: "#DAE2FF", keywords: "uber,yego,moto,cab,metro,fuel,shell,bus"),
        Category(id: 5, name: "Entertainment", iconName: "film.fill", tag: "Lifestyle", colorHex: "#FFB955", keywords: "netflix,cinema,movie,entertainment,spotify"),
        Category(id: 6, name: "Shopping", iconName: "bag.fill", tag: "Lifestyle", colorHex: "#E1E3E4", keywords: "amazon,mall,shopping,store"),
        Category(id: 7, name: "Health", iconName: "cross.fill", tag: "Essential", colorHex: "#FFDAD6", keywords: "pharmacy,hospital,doctor,clinic"),
        Category(id: 8, name: "Bills", iconName: "doc.text.fill", tag: "Fixed", colorHex: "#EDEEEF", keywords: "bill,tax,fee")
    ]
}
