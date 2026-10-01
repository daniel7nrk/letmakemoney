// Default categories and demo seed data (No Emojis)
export const DEFAULT_CATEGORIES = [
  { id: 1, name: "Food & Dining", iconName: "dining", tag: "Essential", colorHex: "#FF6B6B", keywords: "starbucks,coffee,food,dining,restaurant,supermarket,simba,grocery,kfc,pizza" },
  { id: 2, name: "Rent & Utilities", iconName: "home", tag: "Fixed", colorHex: "#4D96FF", keywords: "apartment,rent,utilities,housing,electricity,water,skyline,wasac,reg" },
  { id: 3, name: "Salary", iconName: "salary", tag: "Income", colorHex: "#6BCB77", keywords: "salary,income,payroll,deposit,allowance" },
  { id: 4, name: "Transport", iconName: "transport", tag: "Commute", colorHex: "#FFD93D", keywords: "uber,yego,moto,cab,metro,fuel,shell,bus,taxi,petrol" },
  { id: 5, name: "Entertainment", iconName: "entertainment", tag: "Lifestyle", colorHex: "#9B51E0", keywords: "netflix,cinema,movie,entertainment,spotify,youtube,gaming" },
  { id: 6, name: "Shopping", iconName: "shopping", tag: "Lifestyle", colorHex: "#FF8E53", keywords: "amazon,mall,shopping,store,jumia,market" },
  { id: 7, name: "Health", iconName: "health", tag: "Essential", colorHex: "#20C997", keywords: "pharmacy,hospital,doctor,clinic,meds,dental" },
  { id: 8, name: "Bills", iconName: "bills", tag: "Fixed", colorHex: "#6C757D", keywords: "bill,tax,fee,rra,kra,airtime,internet,bundle" }
];

export const INITIAL_TRANSACTIONS = [
  {
    id: 1727720000001,
    amount: -4250,
    currency: "RWF",
    dateEpochMillis: Date.now() - 3600000 * 2,
    description: "Simba Supermarket (SMS)",
    merchant: "Simba Supermarket",
    category: "Food & Dining",
    source: "SMS",
    status: "CONFIRMED",
    paymentMethod: "MTN MoMo"
  },
  {
    id: 1727720000002,
    amount: -12000,
    currency: "RWF",
    dateEpochMillis: Date.now() - 3600000 * 18,
    description: "Shell Fuel Station (SMS)",
    merchant: "Shell Fuel Station",
    category: "Transport",
    source: "SMS",
    status: "CONFIRMED",
    paymentMethod: "MTN MoMo"
  },
  {
    id: 1727720000003,
    amount: 450000,
    currency: "RWF",
    dateEpochMillis: Date.now() - 3600000 * 48,
    description: "Monthly Payroll Deposit",
    merchant: "Employer Ltd",
    category: "Salary",
    source: "SMS",
    status: "CONFIRMED",
    paymentMethod: "Bank of Kigali"
  },
  {
    id: 1727720000004,
    amount: -18500,
    currency: "RWF",
    dateEpochMillis: Date.now() - 3600000 * 6,
    description: "Unknown Merchant (SMS)",
    merchant: "Unknown Merchant",
    category: "Uncategorized",
    source: "SMS",
    status: "PENDING",
    paymentMethod: "MTN MoMo"
  }
];

export const INITIAL_SCAM_ALERTS = [
  {
    id: 1727720000010,
    sender: "+250788999111",
    body: "You received 65,000 RWF bonus! Click http://bit.ly/momo-bonus to verify your PIN immediately.",
    dateEpochMillis: Date.now() - 3600000 * 4,
    primaryReason: "Contains a suspicious link or pin request.",
    detectedSignals: "Phishing / Suspicious CTA ('click link'), Unrecognized Sender",
    isOverridden: false
  }
];
