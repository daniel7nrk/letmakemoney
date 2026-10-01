// Scam Detector Service - On-Device Fraud Detection Engine

const CALL_TO_ACTION_KEYWORDS = [
  "click here", "click link", "verify your account", "verify account",
  "enter your pin", "enter pin", "share your pin", "call this number",
  "reply with", "reply code", "otp", "claim your", "claim now",
  "http://", "https://", "www.", ".com/", "bit.ly", "tinyurl",
  "verify here", "click to verify", "update your pin", "free bonus"
];

const TRANSACTION_CLAIM_KEYWORDS = [
  "paid", "received", "transferred", "payment", "txn", "txid",
  "debit", "credit", "confirmed", "m-pesa", "momo", "airtel money",
  "bank", "bal:", "balance:", "spent", "sent to", "you have paid",
  "you received", "received from", "deposit", "transfer", "rwf", "etb"
];

const BUILT_IN_TRUSTED_SENDERS = new Set([
  "m-money", "mtn momo", "mtnmomo", "mtnrwanda", "airtel",
  "mpesa", "mpesa info", "safaricom", "boa", "cbe", "urubutopay"
]);

export class ScamDetector {
  static isKnownSender(sender) {
    if (!sender) return false;
    const clean = sender.trim().toLowerCase();
    if (BUILT_IN_TRUSTED_SENDERS.has(clean)) return true;
    // Check if numeric shortcode (3 to 6 digits, e.g. 127, 131, 251994, 8994)
    if (/^\d{3,6}$/.test(clean)) return true;
    return false;
  }

  static detectScam(sender, body, recentTransactions = [], recentScams = [], filteringEnabled = true) {
    const extractedNewBalance = this.extractBalance(body);

    if (!filteringEnabled) {
      return {
        classification: "CONFIRMED_REAL",
        isScam: false,
        needsReview: false,
        primaryReason: "Scam filtering is currently turned off.",
        detectedSignals: [],
        extractedNewBalance
      };
    }

    const lowerSender = (sender || "").trim().toLowerCase();
    const lowerBody = (body || "").toLowerCase();

    const isTransactionClaim = TRANSACTION_CLAIM_KEYWORDS.some(kw => lowerBody.includes(kw));

    // Red Flag 1: Call to Action / Phishing / PIN Request
    const matchedCta = CALL_TO_ACTION_KEYWORDS.find(cta => lowerBody.includes(cta));
    if (matchedCta) {
      return {
        classification: "LIKELY_SCAM",
        isScam: true,
        needsReview: false,
        primaryReason: "Contains a suspicious link or pin request.",
        detectedSignals: [`Phishing / Suspicious CTA ('${matchedCta}')`],
        extractedNewBalance
      };
    }

    // Red Flag 2: Duplicate Message / Replay Attack (5-minute window)
    const now = Date.now();
    const fiveMinutesAgo = now - 5 * 60 * 1000;

    const isDuplicateTx = recentTransactions.some(tx => {
      if (tx.dateEpochMillis < fiveMinutesAgo) return false;
      const matchSender = (tx.paymentMethod && tx.paymentMethod.toLowerCase() === lowerSender) ||
                          (tx.description && tx.description.toLowerCase().includes(lowerSender));
      const amountStr = String(Math.abs(Math.round(tx.amount)));
      return matchSender && lowerBody.includes(amountStr);
    });

    const isDuplicateScam = recentScams.some(scam => 
      scam.dateEpochMillis >= fiveMinutesAgo &&
      scam.sender.toLowerCase() === lowerSender &&
      scam.body.toLowerCase() === lowerBody
    );

    if (isDuplicateTx || isDuplicateScam) {
      return {
        classification: "LIKELY_SCAM",
        isScam: true,
        needsReview: false,
        primaryReason: "Duplicate transaction message detected.",
        detectedSignals: ["Duplicate Replay Attack Pattern"],
        extractedNewBalance
      };
    }

    if (!isTransactionClaim) {
      return {
        classification: "CONFIRMED_REAL",
        isScam: false,
        needsReview: false,
        primaryReason: "",
        detectedSignals: [],
        extractedNewBalance
      };
    }

    // Structural Fingerprints
    const isMoMo = ["momo", "m-money", "mtn", "mtnmomo", "mtnrwanda", "rwf", "ugx", "kes"].some(kw => lowerSender.includes(kw) || lowerBody.includes(kw));
    const isBank = ["127", "131", "251994", "cbe", "boa", "bank", "telebirr", "etb", "urubutopay", "dear"].some(kw => lowerSender.includes(kw) || lowerBody.includes(kw));

    let structurePassed = true;
    let structureFailureReason = null;

    if (isMoMo) {
      if (!this.matchesMoMoUSSDWrapper(body)) {
        structurePassed = false;
        structureFailureReason = "Missing expected MoMo USSD wrapper format.";
      }
    } else if (isBank) {
      if (!this.matchesBankStructure(body)) {
        structurePassed = false;
        structureFailureReason = "Missing expected bank notification structure.";
      }
    } else {
      if (!this.matchesGenericStructure(body)) {
        structurePassed = false;
        structureFailureReason = "Missing expected transaction message structure.";
      }
    }

    // Sender Identity Verification
    const isKnown = this.isKnownSender(sender);

    const detectedSignals = [];
    if (!structurePassed && structureFailureReason) {
      detectedSignals.push("Invalid Provider Structure");
    }
    if (!isKnown) {
      detectedSignals.push(`Unrecognized Sender ('${sender}')`);
    }

    // Classification Rules
    if (!structurePassed) {
      return {
        classification: "LIKELY_SCAM",
        isScam: true,
        needsReview: false,
        primaryReason: structureFailureReason,
        detectedSignals,
        extractedNewBalance
      };
    }

    if (!isKnown) {
      return {
        classification: "NEEDS_REVIEW",
        isScam: false,
        needsReview: true,
        primaryReason: "Sender is not in trusted provider list.",
        detectedSignals,
        extractedNewBalance
      };
    }

    return {
      classification: "CONFIRMED_REAL",
      isScam: false,
      needsReview: false,
      primaryReason: "",
      detectedSignals: [],
      extractedNewBalance
    };
  }

  static matchesMoMoUSSDWrapper(body) {
    const trimmed = (body || "").trim();
    if (/^\*\d{3,4}\*.*(?:\*EN#|\.EN#|\*FR#|\*KIN#|EN#|#)$/i.test(trimmed)) return true;
    if (trimmed.includes("*S*") || trimmed.includes("*R*") || trimmed.includes("*TxId:") || trimmed.includes("TxId:")) {
      if (trimmed.endsWith("EN#") || trimmed.endsWith("FR#") || trimmed.endsWith("#")) return true;
    }
    return false;
  }

  static matchesBankStructure(body) {
    const lower = (body || "").toLowerCase();
    const hasDear = lower.includes("dear ");
    const hasTx = lower.includes("transaction number") || lower.includes("txn number") || lower.includes("txid") || lower.includes("ref") || lower.includes("by transaction");
    const hasBal = lower.includes("balance is") || lower.includes("current balance") || lower.includes("new balance") || lower.includes("account");
    return (hasDear && hasTx && hasBal) || (hasDear && hasTx) || (hasTx && hasBal);
  }

  static matchesGenericStructure(body) {
    const lower = (body || "").toLowerCase();
    const hasAction = lower.includes("paid") || lower.includes("received") || lower.includes("transferred") || lower.includes("debited") || lower.includes("credited");
    const hasBal = lower.includes("balance") || lower.includes("bal:") || lower.includes("bal");
    return hasAction || hasBal;
  }

  static extractBalance(body) {
    const pattern = /(?:new balance|current balance|bal|balance|balance is)[:\s]+(?:[A-Za-z]{3}\s*)?([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)/i;
    const m = (body || "").match(pattern);
    if (m && m[1]) {
      const numStr = m[1].replace(/,/g, '');
      const val = parseFloat(numStr);
      return isNaN(val) ? null : val;
    }
    return null;
  }
}
