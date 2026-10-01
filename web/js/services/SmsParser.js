// SMS Parser Service - ES Module Port of Android & iOS Regex Extraction Engine

const TRANSACTION_KEYWORDS = [
  "paid", "received", "transferred", "payment", "txn", "txid",
  "debit", "credit", "confirmed", "m-pesa", "momo", "airtel money",
  "bank", "bal:", "balance:", "spent"
];

const GENERIC_SMS_WORDS = new Set([
  "momo", "paid", "received", "payment", "bank", "bill", "transfer",
  "transferred", "spent", "debit", "credit", "confirmed", "airtime",
  "deposit", "money", "sent", "you"
]);

export class SmsParser {
  static parseSms(sender, body, categories = [], defaultCurrency = "RWF") {
    const lowerBody = body.toLowerCase();

    // 1. Check if SMS matches transaction-alert pattern
    const isTransaction = TRANSACTION_KEYWORDS.some(kw => lowerBody.includes(kw));
    if (!isTransaction) {
      return null;
    }

    // 2. Extract Amount & Currency
    const amountResult = this.extractAmountAndCurrency(body, defaultCurrency);
    if (!amountResult) {
      return null;
    }

    // 3. Extract Merchant / Counterparty
    const merchant = this.extractMerchant(body, sender);

    // 4. Keyword-based Category Auto-suggestion
    let matchedCategory = "Uncategorized";
    let status = "PENDING";

    for (const cat of categories) {
      if (!cat.keywords) continue;
      const keywordsList = cat.keywords
        .split(",")
        .map(k => k.trim().toLowerCase())
        .filter(k => k.length > 0 && !GENERIC_SMS_WORDS.has(k));

      const foundMatch = keywordsList.some(kw => 
        lowerBody.includes(kw) || merchant.toLowerCase().includes(kw)
      );

      if (foundMatch) {
        matchedCategory = cat.name;
        status = "CONFIRMED";
        break;
      }
    }

    if (matchedCategory === "Uncategorized") {
      status = "PENDING";
    }

    // Negative for expenses, positive for received income
    const isIncome = lowerBody.includes("received") || lowerBody.includes("deposit") || lowerBody.includes("credited");
    const finalAmount = isIncome ? Math.abs(amountResult.amount) : -Math.abs(amountResult.amount);

    return {
      amount: finalAmount,
      currency: amountResult.currency,
      merchant: merchant,
      description: `${merchant} (SMS)`,
      category: matchedCategory,
      status: status
    };
  }

  static extractAmountAndCurrency(body, defaultCurrency) {
    // Pattern 1: Amount followed by currency (e.g. "4,250 RWF", "4250.00 KES")
    const pattern1 = /(?<![A-Za-z0-9])([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)\s*(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)(?![A-Za-z0-9])/i;
    const match1 = body.match(pattern1);
    if (match1) {
      const numStr = match1[1].replace(/,/g, '');
      const rawCurr = match1[2].toUpperCase();
      const val = parseFloat(numStr);
      if (!isNaN(val) && val > 0) {
        let stdCurrency = rawCurr;
        if (rawCurr === "FRW" || rawCurr === "RF") stdCurrency = "RWF";
        if (rawCurr === "KSH") stdCurrency = "KES";
        return { amount: val, currency: stdCurrency };
      }
    }

    // Pattern 2: Currency followed by amount (e.g. "RWF 4,250", "KSH 4250")
    const pattern2 = /(RWF|FRW|RF|KES|KSH|UGX|TZS|USD|BIF|SSP)\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)/i;
    const match2 = body.match(pattern2);
    if (match2) {
      const rawCurr = match2[1].toUpperCase();
      const numStr = match2[2].replace(/,/g, '');
      const val = parseFloat(numStr);
      if (!isNaN(val) && val > 0) {
        let stdCurrency = rawCurr;
        if (rawCurr === "FRW" || rawCurr === "RF") stdCurrency = "RWF";
        if (rawCurr === "KSH") stdCurrency = "KES";
        return { amount: val, currency: stdCurrency };
      }
    }

    // Pattern 3: Transaction verb followed by number (e.g. "paid 4,250", "received 15000")
    const pattern3 = /(?:paid|received|transferred|spent|sent|debited|credited|sum of|amount of)\s+([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)/i;
    const match3 = body.match(pattern3);
    if (match3) {
      const numStr = match3[1].replace(/,/g, '');
      const val = parseFloat(numStr);
      if (!isNaN(val) && val > 0) {
        return { amount: val, currency: defaultCurrency };
      }
    }

    return null;
  }

  static extractMerchant(body, sender) {
    const patterns = [
      /(?:paid to|payment to|transferred to|sent to|to)\s+([A-Za-z0-9&\s'-]{2,25}?)(?=\s+(?:on|at|date|ref|txid|bal|balance|new|financial|your|is|was|for|\.|\,|$))/i,
      /(?:from|received from)\s+([A-Za-z0-9&\s'-]{2,25}?)(?=\s+(?:on|at|date|ref|txid|bal|balance|new|financial|your|is|was|for|\.|\,|$))/i
    ];

    for (const p of patterns) {
      const m = body.match(p);
      if (m && m[1]) {
        const candidate = m[1].trim();
        const cleaned = candidate.substring(0, 25).replace(/[.,\s]+$/, '');
        if (cleaned.length >= 2 && cleaned.toLowerCase() !== 'me') {
          return cleaned;
        }
      }
    }

    return sender && sender.trim() ? sender.trim() : "Mobile Money Merchant";
  }
}
