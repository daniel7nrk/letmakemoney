// Country and Currency configurations matching Android & iOS models
export const COUNTRIES = {
  RWANDA: {
    id: "RWANDA",
    name: "Rwanda",
    currencyCode: "RWF",
    currencyName: "Rwandan Franc",
    flagUrl: "https://flagcdn.com/w40/rw.png",
    providers: ["MTN MoMo", "Airtel Money", "Bank of Kigali", "Equity Bank"]
  },
  KENYA: {
    id: "KENYA",
    name: "Kenya",
    currencyCode: "KES",
    currencyName: "Kenyan Shilling",
    flagUrl: "https://flagcdn.com/w40/ke.png",
    providers: ["M-Pesa (Safaricom)", "Airtel Money", "KCB", "Equity Bank"]
  },
  UGANDA: {
    id: "UGANDA",
    name: "Uganda",
    currencyCode: "UGX",
    currencyName: "Ugandan Shilling",
    flagUrl: "https://flagcdn.com/w40/ug.png",
    providers: ["MTN MoMo", "Airtel Money", "Stanbic", "Centenary Bank"]
  },
  TANZANIA: {
    id: "TANZANIA",
    name: "Tanzania",
    currencyCode: "TZS",
    currencyName: "Tanzanian Shilling",
    flagUrl: "https://flagcdn.com/w40/tz.png",
    providers: ["M-Pesa", "Airtel Money", "Tigo Pesa", "CRDB", "NMB"]
  },
  BURUNDI: {
    id: "BURUNDI",
    name: "Burundi",
    currencyCode: "BIF",
    currencyName: "Burundian Franc",
    flagUrl: "https://flagcdn.com/w40/bi.png",
    providers: ["Lumicash", "Ecocash", "Banque de Crédit de Bujumbura"]
  },
  SOUTH_SUDAN: {
    id: "SOUTH_SUDAN",
    name: "South Sudan",
    currencyCode: "SSP",
    currencyName: "South Sudanese Pound",
    flagUrl: "https://flagcdn.com/w40/ss.png",
    providers: ["m-Gurush", "Zain Money"]
  }
};

export function getCountryByCurrency(code) {
  if (!code) return COUNTRIES.RWANDA;
  const upper = code.toUpperCase();
  for (const c of Object.values(COUNTRIES)) {
    if (c.currencyCode === upper) return c;
  }
  return COUNTRIES.RWANDA;
}

export function formatCurrency(amount, currencyCode = "RWF") {
  const formatted = Math.abs(amount).toLocaleString(undefined, {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  });
  return `${amount < 0 ? '-' : ''}${formatted} ${currencyCode}`;
}
