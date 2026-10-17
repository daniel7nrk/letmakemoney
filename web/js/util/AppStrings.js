// PromptPay Localization Dictionary (English & French) matching Android & iOS

export const TRANSLATIONS = {
  en: {
    // Navigation
    navDashboard: "Dashboard",
    navTransactions: "Transactions",
    navPendingReview: "Pending Review",
    navReports: "Reports & Analytics",
    navScamAlerts: "Fraud & Scam Alerts",
    navCategories: "Categories",
    navSettings: "Settings",

    // Header
    headerTitle: "Financial Overview",
    headerSubtitle: "Real-time mobile money and bank SMS expense intelligence",
    btnSimulateSms: "Simulate Incoming SMS",
    btnAddTransaction: "Add Transaction",

    // Dashboard Banners
    bannerPendingPrefix: "You have",
    bannerPendingSuffix: "uncategorized mobile money transaction(s) waiting for review.",
    btnReviewNow: "Review Now",
    bannerScamPrefix: "PromptPay flagged",
    bannerScamSuffix: "suspicious message(s) containing phishing links or spoofed balances.",
    btnInspectAlerts: "Inspect Alerts",

    // Stat Cards
    netMonthlyBalance: "Net Monthly Balance",
    totalMonthlyIncome: "Total Monthly Income",
    totalMonthlyExpenses: "Total Monthly Expenses",
    pendingReviews: "Pending Reviews",

    // Charts
    spendingFlowOverTime: "Spending Flow Over Time",
    categoryBreakdown: "Category Breakdown",
    dailyExpenses: "Daily Expenses",
    distribution: "Distribution",

    // Recent Activity & Transactions Table
    recentTransactions: "Recent Transactions",
    allTransactionsLog: "All Transactions Log",
    viewAll: "View All",
    colMerchant: "Merchant / Description",
    colCategory: "Category",
    colSource: "Type / Source",
    colStatus: "Status",
    colDate: "Date",
    colAmount: "Amount",
    colFee: "Fee (Side)",
    colAction: "Action",
    noTransactionsYet: "No transactions recorded yet",
    noTransactionsFound: "No transactions found matching this filter",
    delete: "Delete",
    btnExportBackup: "Export Backup (JSON)",
    filterAll: "All",
    filterSms: "Phone SMS Only",
    filterManual: "Manual Only",
    sourceSms: "Phone SMS",
    sourceManual: "Manual Entry",
    sourceFixedNote: "Fixed type: Cannot be altered",

    // Pending Review
    pendingTitle: "Uncategorized SMS Transactions",
    pendingSubtitle: "Confirm the merchant and assign the correct category to keep your budget accurate.",
    allCaughtUpTitle: "All caught up!",
    allCaughtUpDesc: "There are no uncategorized transactions waiting for review.",
    selectCorrectCategory: "SELECT CORRECT CATEGORY:",

    // Reports
    incomeCaptured: "Income Captured",
    expensesLogged: "Expenses Logged",
    netSavings: "Net Savings / Surplus",
    historicalVelocity: "Historical Spending Velocity",
    spendingByCategory: "Spending Distribution by Category",

    // Scam Alerts
    fraudTitle: "On-Device Scam & Fraud Defense Center",
    fraudSubtitle: "Review suspicious SMS messages intercepted by PromptPay's pattern recognition engine.",
    noScamTitle: "No Fraud Alerts Detected",
    noScamDesc: "PromptPay's on-device fraud engine is running and guarding your messages.",
    suspiciousBlocked: "SUSPICIOUS MESSAGE BLOCKED",
    senderLabel: "Sender",
    detectedLabel: "Detected",
    markSafe: "Mark as Safe",
    deleteAlert: "Delete",
    reasonLabel: "Reason",
    signalsLabel: "Signals",

    // Categories Tab
    createCustomCategory: "Create Custom Category",
    categoryName: "Category Name",
    tagLabel: "Tag",
    colorLabel: "Color",
    keywordsLabel: "SMS Keywords (comma separated)",
    btnAddCategory: "Add Category",
    activeCategories: "Active Expense Categories",
    builtIn: "Built-in",

    // Settings Tab
    settingsHeading: "Settings & Preferences",
    languageSetting: "Language / Langue",
    languageDesc: "Choose your preferred display language for PromptPay",
    langEnglish: "English (🇺🇸 English)",
    langFrench: "Français (🇫🇷 French)",
    countrySetting: "Country & Default Currency",
    scamFilterHeading: "Enable Real-time MoMo Scam Filtering",
    scamFilterDesc: "Automatically intercepts phishing links, fake balance alerts, and suspicious CTAs",
    securityHeading: "App Security & Masked PIN Lock",
    securityDesc: "Protect your financial data with an encrypted, masked PIN (hidden while typing)",
    enablePinLock: "Enable Masked PIN Lock",
    enterPinLabel: "Enter 4-Digit Security PIN",
    pinPlaceholder: "••••",
    btnSavePin: "Save PIN Security",
    offlineBackupHeading: "Offline Data Backup & Restore",
    offlineBackupDesc: "All transactions, categories, and settings are stored locally in your browser's IndexedDB. Export a backup or restore an existing one at any time.",
    importBackupBtn: "Import Backup (.json)",

    // Modals
    modalSimulateTitle: "Simulate Incoming Mobile Money SMS",
    modalSimulateDesc: "Test PromptPay's on-device pattern recognition, amount & fee extraction, and fraud detector in your browser:",
    samplePresets: "QUICK SAMPLE PRESETS:",
    smsSenderHeader: "SMS Sender Header",
    smsBodyLabel: "SMS Message Body",
    btnProcessSms: "Process & Ingest SMS",
    cancel: "Cancel",

    modalAddTitle: "Record Manual Transaction",
    txTypeLabel: "Transaction Type",
    expenseType: "Expense (-)",
    incomeType: "Income (+)",
    amountLabel: "Amount",
    amountPlaceholder: "e.g. 5000",
    categoryLabel: "Category",
    descMerchantLabel: "Description / Merchant",
    descPlaceholder: "e.g. Local Market Coffee",
    feeAmountLabel: "Transaction Fee (Displayed at side, not added)",
    feePlaceholder: "e.g. 50 (optional)",
    feeCategoryLabel: "Fee Category",
    paymentMethodLabel: "Payment Method",
    btnSaveTx: "Save Transaction",
    fixedManualNotice: "Note: This transaction will be permanently saved as a Manual Entry.",

    // Fee display
    sideFeeLabel: "Fee:",
    noFeeRecorded: "No fee"
  },

  fr: {
    // Navigation
    navDashboard: "Accueil",
    navTransactions: "Transactions",
    navPendingReview: "En attente",
    navReports: "Rapports",
    navScamAlerts: "Alertes Fraude",
    navCategories: "Catégories",
    navSettings: "Paramètres",

    // Header
    headerTitle: "Aperçu Financier",
    headerSubtitle: "Intelligence des dépenses Mobile Money et SMS bancaires en temps réel",
    btnSimulateSms: "Simuler un SMS",
    btnAddTransaction: "Ajouter une transaction",

    // Dashboard Banners
    bannerPendingPrefix: "Vous avez",
    bannerPendingSuffix: "transaction(s) mobile money non catégorisée(s) en attente d'examen.",
    btnReviewNow: "Examiner maintenant",
    bannerScamPrefix: "PromptPay a intercepté",
    bannerScamSuffix: "message(s) suspect(s) contenant des liens de phishing ou de faux soldes.",
    btnInspectAlerts: "Inspecter les alertes",

    // Stat Cards
    netMonthlyBalance: "Solde mensuel net",
    totalMonthlyIncome: "Revenu mensuel total",
    totalMonthlyExpenses: "Dépenses mensuelles totales",
    pendingReviews: "En attente d'examen",

    // Charts
    spendingFlowOverTime: "Évolution des dépenses au fil du temps",
    categoryBreakdown: "Répartition par catégorie",
    dailyExpenses: "Dépenses journalières",
    distribution: "Distribution",

    // Recent Activity & Transactions Table
    recentTransactions: "Transactions récentes",
    allTransactionsLog: "Journal de toutes les transactions",
    viewAll: "Voir tout",
    colMerchant: "Marchand / Description",
    colCategory: "Catégorie",
    colSource: "Type / Source",
    colStatus: "Statut",
    colDate: "Date",
    colAmount: "Montant",
    colFee: "Frais (à côté)",
    colAction: "Action",
    noTransactionsYet: "Aucune transaction enregistrée pour le moment",
    noTransactionsFound: "Aucune transaction trouvée correspondant à ce filtre",
    delete: "Supprimer",
    btnExportBackup: "Exporter la sauvegarde (JSON)",
    filterAll: "Toutes",
    filterSms: "SMS uniquement",
    filterManual: "Manuel uniquement",
    sourceSms: "SMS Téléphone",
    sourceManual: "Saisie Manuelle",
    sourceFixedNote: "Type fixe : Ne peut être modifié",

    // Pending Review
    pendingTitle: "Transactions SMS non catégorisées",
    pendingSubtitle: "Confirmez le marchand et attribuez la bonne catégorie pour garder votre budget exact.",
    allCaughtUpTitle: "Tout est à jour !",
    allCaughtUpDesc: "Il n'y a aucune transaction non catégorisée en attente d'examen.",
    selectCorrectCategory: "CHOISIR LA BONNE CATÉGORIE :",

    // Reports
    incomeCaptured: "Revenus enregistrés",
    expensesLogged: "Dépenses enregistrées",
    netSavings: "Épargne nette / Excédent",
    historicalVelocity: "Vitesse historique des dépenses",
    spendingByCategory: "Répartition des dépenses par catégorie",

    // Scam Alerts
    fraudTitle: "Centre de Défense Anti-Fraude Local",
    fraudSubtitle: "Passez en revue les messages SMS suspects interceptés par le moteur de détection de PromptPay.",
    noScamTitle: "Aucune alerte de fraude détectée",
    noScamDesc: "Le moteur anti-fraude de PromptPay fonctionne et protège vos messages.",
    suspiciousBlocked: "MESSAGE SUSPECT BLOQUÉ",
    senderLabel: "Expéditeur",
    detectedLabel: "Détecté",
    markSafe: "Marquer comme sûr",
    deleteAlert: "Supprimer",
    reasonLabel: "Raison",
    signalsLabel: "Signaux",

    // Categories Tab
    createCustomCategory: "Créer une catégorie personnalisée",
    categoryName: "Nom de la catégorie",
    tagLabel: "Étiquette",
    colorLabel: "Couleur",
    keywordsLabel: "Mots-clés SMS (séparés par des virgules)",
    btnAddCategory: "Ajouter la catégorie",
    activeCategories: "Catégories de dépenses actives",
    builtIn: "Intégré",

    // Settings Tab
    settingsHeading: "Paramètres et Préférences",
    languageSetting: "Langue / Language",
    languageDesc: "Choisissez votre langue d'affichage pour PromptPay",
    langEnglish: "English (🇺🇸 Anglais)",
    langFrench: "Français (🇫🇷 Français)",
    countrySetting: "Pays et Devise par défaut",
    scamFilterHeading: "Filtrage MoMo anti-fraude en temps réel",
    scamFilterDesc: "Intercepte automatiquement les liens de phishing, les faux soldes et les demandes suspectes",
    securityHeading: "Sécurité de l'application et Code PIN masqué",
    securityDesc: "Protégez vos données financières avec un code PIN masqué (caché pendant la saisie)",
    enablePinLock: "Activer le verrouillage par code PIN",
    enterPinLabel: "Entrez le code PIN à 4 chiffres",
    pinPlaceholder: "••••",
    btnSavePin: "Enregistrer le code PIN",
    offlineBackupHeading: "Sauvegarde et Restauration Hors Ligne",
    offlineBackupDesc: "Toutes les transactions, catégories et paramètres sont stockés localement dans votre navigateur via IndexedDB. Exportez ou restaurez à tout moment.",
    importBackupBtn: "Importer une sauvegarde (.json)",

    // Modals
    modalSimulateTitle: "Simuler un SMS Mobile Money",
    modalSimulateDesc: "Testez la reconnaissance de motifs, l'extraction de montants et frais, et le détecteur de fraude :",
    samplePresets: "EXEMPLES PRÉDÉFINIS :",
    smsSenderHeader: "En-tête de l'expéditeur SMS",
    smsBodyLabel: "Corps du message SMS",
    btnProcessSms: "Traiter et intégrer le SMS",
    cancel: "Annuler",

    modalAddTitle: "Enregistrer une transaction manuelle",
    txTypeLabel: "Type de transaction",
    expenseType: "Dépense (-)",
    incomeType: "Revenu (+)",
    amountLabel: "Montant",
    amountPlaceholder: "ex: 5000",
    categoryLabel: "Catégorie",
    descMerchantLabel: "Description / Marchand",
    descPlaceholder: "ex: Café du marché local",
    feeAmountLabel: "Frais de transaction (Affichés à côté, non ajoutés)",
    feePlaceholder: "ex: 50 (facultatif)",
    feeCategoryLabel: "Catégorie des frais",
    paymentMethodLabel: "Moyen de paiement",
    btnSaveTx: "Enregistrer la transaction",
    fixedManualNotice: "Remarque : Cette transaction sera définitivement marquée comme Saisie Manuelle.",

    // Fee display
    sideFeeLabel: "Frais :",
    noFeeRecorded: "Aucun frais"
  }
};

export function getStrings(lang = 'en') {
  return TRANSLATIONS[lang] || TRANSLATIONS.en;
}
