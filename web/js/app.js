import { storage } from './services/StorageService.js';
import { getSupabase } from './supabaseClient.js';

// ==========================================
// 1. COUNTRIES & LOCALIZATION (EN & FR)
// ==========================================
const COUNTRIES = {
  RWANDA: { name: 'Rwanda', currencyCode: 'RWF', symbol: 'RWF' },
  UGANDA: { name: 'Uganda', currencyCode: 'UGX', symbol: 'UGX' },
  KENYA: { name: 'Kenya', currencyCode: 'KES', symbol: 'KES' },
  TANZANIA: { name: 'Tanzania', currencyCode: 'TZS', symbol: 'TZS' },
  DRC: { name: 'DR Congo', currencyCode: 'CDF', symbol: 'CDF' }
};

const I18N = {
  en: {
    navDashboard: "Dashboard",
    navTransactions: "Transactions",
    navLoans: "Loans",
    navNotifications: "Notification",
    navAnalytics: "Analytics",
    navCategory: "Category",
    navFamily: "Family",
    navSettings: "Settings",
    navTheme: "Theme",
    tabDashboard: "Dashboard",
    tabTransactions: "Transactions",
    tabLoans: "Loans",
    tabNotifications: "Notifications",
    tabAnalytics: "Analytics",
    tabCategory: "Category",
    tabFamily: "Family",
    tabSettings: "Settings",
    lblTotalBalance: "Total Liquid Balance",
    subTotalBalance: "Mobile Money + Bank Cash",
    lblMonthlyInflow: "Monthly Inflow (Income)",
    lblMonthlyOutflow: "Monthly Outflow (Spend)",
    lblNetSurplus: "Net Savings / Surplus",
    subNetSurplus: "Surplus for savings & investments",
    lblQuickActions: "Quick Actions & Shortcuts",
    subQuickActions: "Log transactions, record loans, or export data instantly.",
    btnAddTx: "+ New Transaction",
    btnAddLoan: "+ New Loan",
    btnExportCsv: "Export CSV",
    btnExportJson: "Export Backup (JSON)",
    btnDownloadJson: "Download JSON Backup",
    btnImportJson: "Import JSON",
    lblRecentTx: "Recent Transactions Log",
    subRecentTx: "Latest MoMo SMS and manual entries.",
    btnSeeAll: "See All",
    lblFamilyOverview: "Family Spending Summary",
    subFamilyOverview: "Shared household budget tracking.",
    btnViewFamily: "Manage",
    titleTransactions: "Transactions Log",
    subTransactions: "Comprehensive on-device ledger of all expenses and inflows.",
    btnNewTx: "+ New Transaction",
    filterAll: "All",
    filterSms: "Phone SMS Only",
    filterManual: "Manual Only",
    thDate: "Date",
    thMerchant: "Merchant / Description",
    thCategory: "Category",
    thSource: "Source",
    thStatus: "Status",
    thAmount: "Amount",
    thFee: "Fee (Side)",
    thAction: "Action",
    titleLoans: "Personal & Micro-Loans Tracker",
    subLoans: "Track money borrowed or lent to family, friends, or institutions.",
    lblActiveLoans: "Active Loans",
    lblTotalBorrowed: "Total Borrowed",
    lblTotalLent: "Total Lent (Receivable)",
    lblNextDue: "Next Settlement Due",
    thCounterparty: "Counterparty / Name",
    thType: "Type",
    thPrincipal: "Principal",
    thRepaymentProgress: "Repayment Progress",
    thDueDate: "Due Date",
    titleNotifications: "Notifications & Alerts",
    subNotifications: "Payment requests, family allowances, security alerts, and system notices.",
    btnMarkAllRead: "Mark all read",
    btnClearAll: "Clear all",
    filterUnread: "Unread",
    filterRequests: "Requests",
    filterFamily: "Family",
    filterPayments: "Payments",
    titleAnalytics: "Financial Analytics & Reports",
    subAnalytics: "Historical trends, spending velocity, and category distribution.",
    lblIncomeCaptured: "Income Captured",
    lblExpensesLogged: "Expenses Logged",
    lblNetSavingsSurplus: "Net Savings / Surplus",
    lblSpendingVelocity: "Historical Spending Velocity",
    lblCategoryDistribution: "Spending Distribution by Category",
    titleExportFinancial: "Export Financial Data",
    subExportFinancial: "Download your transaction history as CSV or get a full JSON backup.",
    titleCreateCategory: "Create Custom Category",
    lblCatName: "Category Name",
    lblCatTag: "Tag",
    lblCatColor: "Color",
    lblCatKeywords: "SMS Keywords (comma separated)",
    btnSaveCategory: "Save Category",
    titleActiveCategories: "Active Categories",
    titleFamily: "Family & Shared Workspace",
    subFamily: "Sync family expenses and view members fetched directly from Supabase.",
    btnSyncCloud: "Sync Cloud Members",
    lblFamilyTotalSpend: "Family Total Monthly Spend",
    lblSharedBudget: "Shared Monthly Budget",
    badgeYou: "You",
    titleOtherMembers: "Other Family Members (Supabase Cloud)",
    subOtherMembers: "Dynamically synchronized with registered family accounts.",
    titleSettings: "Workspace Settings & Preferences",
    subSettings: "Manage profile, currency, language, security, and data backups.",
    titleProfile: "User Profile",
    lblDisplayName: "Display Name",
    lblEmail: "Email Address",
    lblMonthlyBudget: "Monthly Spending Budget",
    btnSaveProfile: "Save Profile",
    titlePreferences: "General Preferences",
    lblCountryCurrency: "Country & Currency",
    lblLanguage: "Application Language",
    titleDataStorage: "Data & Storage",
    subDataStorage: "Everything lives locally on this device — export or restore anytime.",
    lblTxExport: "Transactions Log (CSV)",
    subTxExport: "Export all transactions with fee side tags and categories.",
    lblCatExport: "Expense Categories (CSV)",
    subCatExport: "Export all custom categories and SMS regex keywords.",
    lblFullBackup: "Full Backup (JSON)",
    subFullBackup: "All records, profiles, and categories.",
    modalAddTxTitle: "Log New Transaction",
    lblMerchantDesc: "Merchant / Description",
    lblSource: "Source / Type",
    btnCancel: "Cancel",
    btnSaveTx: "Save Transaction",
    modalAddLoanTitle: "Record Loan / Debt",
    lblLoanType: "Loan Type",
    btnSaveLoan: "Save Loan",
    statusOnline: "Online",
    statusOffline: "Offline (Local)"
  },
  fr: {
    navDashboard: "Tableau de bord",
    navTransactions: "Transactions",
    navLoans: "Prêts",
    navNotifications: "Notifications",
    navAnalytics: "Analyses",
    navCategory: "Catégories",
    navFamily: "Famille",
    navSettings: "Paramètres",
    navTheme: "Thème",
    tabDashboard: "Tableau de bord",
    tabTransactions: "Transactions",
    tabLoans: "Prêts",
    tabNotifications: "Notifications",
    tabAnalytics: "Analyses",
    tabCategory: "Catégories",
    tabFamily: "Famille",
    tabSettings: "Paramètres",
    lblTotalBalance: "Solde Liquide Total",
    subTotalBalance: "Mobile Money + Espèces Bancaires",
    lblMonthlyInflow: "Entrées Mensuelles (Revenus)",
    lblMonthlyOutflow: "Sorties Mensuelles (Dépenses)",
    lblNetSurplus: "Épargne Nette / Excédent",
    subNetSurplus: "Excédent pour épargne et investissements",
    lblQuickActions: "Actions Rapides & Raccourcis",
    subQuickActions: "Enregistrez des transactions, des prêts ou exportez vos données instantanément.",
    btnAddTx: "+ Nouvelle Transaction",
    btnAddLoan: "+ Nouveau Prêt",
    btnExportCsv: "Exporter CSV",
    btnExportJson: "Sauvegarde JSON",
    btnDownloadJson: "Télécharger JSON",
    btnImportJson: "Importer JSON",
    lblRecentTx: "Journal des Transactions Récentes",
    subRecentTx: "Derniers SMS MoMo et entrées manuelles.",
    btnSeeAll: "Voir Tout",
    lblFamilyOverview: "Résumé Dépenses Familiales",
    subFamilyOverview: "Suivi du budget familial partagé.",
    btnViewFamily: "Gérer",
    titleTransactions: "Journal des Transactions",
    subTransactions: "Grand livre complet stocké sur cet appareil pour toutes vos dépenses.",
    btnNewTx: "+ Nouvelle Transaction",
    filterAll: "Tous",
    filterSms: "SMS Téléphone",
    filterManual: "Manuel",
    thDate: "Date",
    thMerchant: "Commerçant / Description",
    thCategory: "Catégorie",
    thSource: "Source",
    thStatus: "Statut",
    thAmount: "Montant",
    thFee: "Frais",
    thAction: "Action",
    titleLoans: "Gestionnaire de Prêts & Dettes",
    subLoans: "Suivez l'argent emprunté ou prêté à la famille, aux amis ou aux banques.",
    lblActiveLoans: "Prêts Actifs",
    lblTotalBorrowed: "Total Emprunté",
    lblTotalLent: "Total Prêté (À recevoir)",
    lblNextDue: "Prochaine Échéance",
    thCounterparty: "Contrepartie / Nom",
    thType: "Type",
    thPrincipal: "Principal",
    thRepaymentProgress: "Progression Remboursement",
    thDueDate: "Date d'échéance",
    titleNotifications: "Notifications & Alertes",
    subNotifications: "Demandes de paiement, allocations familiales, alertes de sécurité.",
    btnMarkAllRead: "Tout marquer lu",
    btnClearAll: "Tout effacer",
    filterUnread: "Non lus",
    filterRequests: "Demandes",
    filterFamily: "Famille",
    filterPayments: "Paiements",
    titleAnalytics: "Analyses & Rapports Financiers",
    subAnalytics: "Tendances historiques, vitesse des dépenses et répartition par catégorie.",
    lblIncomeCaptured: "Revenus Capturés",
    lblExpensesLogged: "Dépenses Enregistrées",
    lblNetSavingsSurplus: "Épargne Nette / Excédent",
    lblSpendingVelocity: "Vitesse Historique des Dépenses",
    lblCategoryDistribution: "Répartition des Dépenses par Catégorie",
    titleExportFinancial: "Exporter les Données Financières",
    subExportFinancial: "Téléchargez l'historique complet en CSV ou obtenez une sauvegarde JSON.",
    titleCreateCategory: "Créer une Catégorie Personnalisée",
    lblCatName: "Nom de la Catégorie",
    lblCatTag: "Tag",
    lblCatColor: "Couleur",
    lblCatKeywords: "Mots-clés SMS (séparés par virgule)",
    btnSaveCategory: "Enregistrer la Catégorie",
    titleActiveCategories: "Catégories Actives",
    titleFamily: "Famille & Espace Partagé",
    subFamily: "Synchronisez les dépenses familiales avec les membres de Supabase.",
    btnSyncCloud: "Synchroniser Membres Cloud",
    lblFamilyTotalSpend: "Dépense Mensuelle Totale Famille",
    lblSharedBudget: "Budget Mensuel Partagé",
    badgeYou: "Vous",
    titleOtherMembers: "Autres Membres de la Famille (Supabase)",
    subOtherMembers: "Synchronisé dynamiquement avec les comptes enregistrés.",
    titleSettings: "Paramètres & Préférences",
    subSettings: "Gérez votre profil, devise, langue, sécurité et sauvegardes.",
    titleProfile: "Profil Utilisateur",
    lblDisplayName: "Nom Affiché",
    lblEmail: "Adresse Email",
    lblMonthlyBudget: "Budget Mensuel de Dépenses",
    btnSaveProfile: "Enregistrer Profil",
    titlePreferences: "Préférences Générales",
    lblCountryCurrency: "Pays & Devise",
    lblLanguage: "Langue de l'Application",
    titleDataStorage: "Données & Stockage",
    subDataStorage: "Toutes les données résident localement sur l'appareil — exportez à tout moment.",
    lblTxExport: "Journal des Transactions (CSV)",
    subTxExport: "Exportez toutes les transactions avec frais et catégories.",
    lblCatExport: "Catégories de Dépenses (CSV)",
    subCatExport: "Exportez les catégories et expressions régulières SMS.",
    lblFullBackup: "Sauvegarde Complète (JSON)",
    subFullBackup: "Tous les enregistrements, profils et catégories.",
    modalAddTxTitle: "Enregistrer une Transaction",
    lblMerchantDesc: "Commerçant / Description",
    lblSource: "Source / Type",
    btnCancel: "Annuler",
    btnSaveTx: "Enregistrer Transaction",
    modalAddLoanTitle: "Enregistrer un Prêt / Dette",
    lblLoanType: "Type de Prêt",
    btnSaveLoan: "Enregistrer le Prêt",
    statusOnline: "En ligne",
    statusOffline: "Hors ligne (Local)"
  }
};

// ==========================================
// 2. MAIN APPLICATION CLASS
// ==========================================
class App {
  constructor() {
    this.lang = localStorage.getItem('rapport:lang') || 'en';
    this.selectedCountry = COUNTRIES.RWANDA;
    this.profile = {
      name: 'Daniel Ngaruka',
      email: 'daniel@promptpay.app',
      monthlyBudget: 450000,
      role: 'Account Executive'
    };
    this.transactions = [];
    this.categories = [];
    this.loans = [];
    this.notifications = [];
    this.activeTxFilter = 'ALL';
    this.activeNotifFilter = 'all';
    this.supabase = getSupabase();
  }

  async init() {
    // 1. Load Local Profile
    const savedProf = localStorage.getItem('rapport:profile');
    if (savedProf) {
      try {
        const parsed = JSON.parse(savedProf);
        this.profile = { ...this.profile, ...parsed };
      } catch (e) {}
    }

    // 2. Load Language & Country
    const savedCountry = localStorage.getItem('rapport:country');
    if (savedCountry && COUNTRIES[savedCountry]) {
      this.selectedCountry = COUNTRIES[savedCountry];
    }

    // 3. Load Storage Data
    await this.loadData();

    // 4. Setup Event Listeners & Router
    this.setupNavigation();
    this.setupModals();
    this.setupForms();
    this.setupExports();
    this.setupNetworkMonitor();

    // 5. Apply Language & Theme
    this.applyLanguage();
    this.setupThemeToggle();

    // 6. Initial Render
    this.handleRoute();
    window.addEventListener('hashchange', () => this.handleRoute());

    // 7. Background Supabase Sync if online
    if (navigator.onLine) {
      this.syncWithSupabase();
    }
  }

  // ==========================================
  // DATA LOADING & SEEDING
  // ==========================================
  async loadData() {
    // Transactions
    let txs = await storage.getTransactions();
    if (!txs || txs.length === 0) {
      txs = [
        {
          id: 'tx_seed_1',
          dateEpochMillis: Date.now() - 3600000 * 2,
          merchant: 'Simba Supermarket',
          category: 'Groceries',
          amount: 28500,
          currency: this.selectedCountry.currencyCode,
          fee: 300,
          feeCategory: 'Tier 1',
          source: 'SMS',
          status: 'COMPLETED'
        },
        {
          id: 'tx_seed_2',
          dateEpochMillis: Date.now() - 3600000 * 18,
          merchant: 'MTN Mobile Airtime',
          category: 'Utilities',
          amount: 5000,
          currency: this.selectedCountry.currencyCode,
          fee: 0,
          feeCategory: 'Free',
          source: 'SMS',
          status: 'COMPLETED'
        },
        {
          id: 'tx_seed_3',
          dateEpochMillis: Date.now() - 3600000 * 40,
          merchant: 'Canal+ Subscription',
          category: 'Entertainment',
          amount: 14000,
          currency: this.selectedCountry.currencyCode,
          fee: 250,
          feeCategory: 'Standard',
          source: 'MANUAL',
          status: 'COMPLETED'
        },
        {
          id: 'tx_seed_4',
          dateEpochMillis: Date.now() - 3600000 * 70,
          merchant: 'Kigali Fuel Station',
          category: 'Transport',
          amount: 32000,
          currency: this.selectedCountry.currencyCode,
          fee: 500,
          feeCategory: 'Side Fee',
          source: 'SMS',
          status: 'COMPLETED'
        }
      ];
      for (const t of txs) await storage.put('transactions', t);
    }
    this.transactions = txs;

    // Categories
    let cats = await storage.getCategories();
    if (!cats || cats.length === 0) {
      cats = [
        { id: 'cat_1', name: 'Groceries', tag: 'Essential', colorHex: '#2E6FF2', keywords: 'simba,market,food,supermarket' },
        { id: 'cat_2', name: 'Utilities', tag: 'Essential', colorHex: '#10B981', keywords: 'airtime,water,eucl,wasac,cashpower' },
        { id: 'cat_3', name: 'Transport', tag: 'Lifestyle', colorHex: '#F59E0B', keywords: 'fuel,taxi,yego,moto,sp' },
        { id: 'cat_4', name: 'Entertainment', tag: 'Lifestyle', colorHex: '#8B5CF6', keywords: 'cinema,canal,bar,pizza,restaurant' },
        { id: 'cat_5', name: 'Health & Pharmacy', tag: 'Essential', colorHex: '#EC4899', keywords: 'pharma,clinic,med,hospital' },
        { id: 'cat_6', name: 'Income / Salary', tag: 'Income', colorHex: '#2F7A52', keywords: 'salary,deposit,payroll,transfer' }
      ];
      for (const c of cats) await storage.put('categories', c);
    }
    this.categories = cats;

    // Loans
    let savedLoans = localStorage.getItem('promptpay:loans');
    if (savedLoans) {
      try { this.loans = JSON.parse(savedLoans); } catch (e) { this.loans = []; }
    } else {
      this.loans = [
        {
          id: 'loan_1',
          person: 'Eric Karangwa (Brother)',
          type: 'LENT',
          amount: 85000,
          paid: 45000,
          dueDate: '2026-11-15',
          status: 'ACTIVE'
        },
        {
          id: 'loan_2',
          person: 'SACCO Credit Union',
          type: 'BORROWED',
          amount: 150000,
          paid: 100000,
          dueDate: '2026-11-05',
          status: 'ACTIVE'
        }
      ];
      localStorage.setItem('promptpay:loans', JSON.stringify(this.loans));
    }

    // Notifications
    let savedNotifs = localStorage.getItem('rapport:notifications');
    if (savedNotifs) {
      try { this.notifications = JSON.parse(savedNotifs); } catch (e) {}
    }
    if (!this.notifications || this.notifications.length === 0) {
      this.notifications = [
        {
          id: 'n_1',
          title: 'Payment Request Received',
          desc: 'Sarah Jenkins requested 45,000 RWF for Household Groceries.',
          type: 'requests',
          unread: true,
          time: '15m ago',
          actionText: 'View Transactions',
          actionTarget: 'transactions'
        },
        {
          id: 'n_2',
          title: 'Family Allowance Notice',
          desc: 'David used 24,000 RWF at Campus Store. (65% of budget used).',
          type: 'family',
          unread: true,
          time: '1h ago',
          actionText: 'View Family',
          actionTarget: 'family'
        },
        {
          id: 'n_3',
          title: 'MoMo SMS Intercepted',
          desc: 'Automated on-device parser processed payment of 28,500 RWF.',
          type: 'payments',
          unread: false,
          time: '3h ago',
          actionText: 'View Log',
          actionTarget: 'transactions'
        }
      ];
      localStorage.setItem('rapport:notifications', JSON.stringify(this.notifications));
    }
  }

  // ==========================================
  // NAVIGATION & ROUTING
  // ==========================================
  setupNavigation() {
    // Dock Buttons
    document.querySelectorAll('.dock__btn[data-target]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const target = btn.dataset.target;
        if (target) {
          window.location.hash = '#/' + target;
        }
      });
    });

    // Top Tabs
    document.querySelectorAll('.tab[data-target]').forEach(tab => {
      tab.addEventListener('click', (e) => {
        const target = tab.dataset.target;
        if (target) {
          window.location.hash = '#/' + target;
        }
      });
    });

    // Language Toggle button in top right
    const btnLang = document.getElementById('btn-lang-toggle');
    if (btnLang) {
      btnLang.addEventListener('click', () => {
        this.lang = this.lang === 'en' ? 'fr' : 'en';
        localStorage.setItem('rapport:lang', this.lang);
        this.applyLanguage();
        this.renderCurrentView();
        this.showToast(this.lang === 'fr' ? 'Langue changée en Français' : 'Language updated to English');
      });
    }

    // Language dropdown in Settings
    const selectLang = document.getElementById('select-language');
    if (selectLang) {
      selectLang.value = this.lang;
      selectLang.addEventListener('change', () => {
        this.lang = selectLang.value;
        localStorage.setItem('rapport:lang', this.lang);
        this.applyLanguage();
        this.renderCurrentView();
      });
    }

    // Country dropdown in Settings
    const selectCountry = document.getElementById('select-country');
    if (selectCountry) {
      const code = Object.keys(COUNTRIES).find(k => COUNTRIES[k].name === this.selectedCountry.name) || 'RWANDA';
      selectCountry.value = code;
      selectCountry.addEventListener('change', () => {
        this.selectedCountry = COUNTRIES[selectCountry.value] || COUNTRIES.RWANDA;
        localStorage.setItem('rapport:country', selectCountry.value);
        this.renderCurrentView();
        this.showToast(`Currency updated to ${this.selectedCountry.currencyCode}`);
      });
    }
  }

  handleRoute() {
    const raw = window.location.hash.replace(/^#\/?/, '').trim();
    const target = raw || 'dashboard';

    // Update Dock
    document.querySelectorAll('.dock__btn[data-target]').forEach(btn => {
      if (btn.dataset.target === target) {
        btn.classList.add('is-active');
        btn.setAttribute('aria-current', 'page');
      } else {
        btn.classList.remove('is-active');
        btn.removeAttribute('aria-current');
      }
    });

    // Update Top Tabs
    let activeTab = null;
    document.querySelectorAll('.tab[data-target]').forEach(tab => {
      if (tab.dataset.target === target) {
        tab.classList.add('is-active');
        activeTab = tab;
      } else {
        tab.classList.remove('is-active');
      }
    });
    this.updateTabPill(activeTab);

    // Switch View
    document.querySelectorAll('.view[data-view]').forEach(view => {
      if (view.dataset.view === target) {
        view.classList.add('is-active');
        view.removeAttribute('hidden');
      } else {
        view.classList.remove('is-active');
        view.setAttribute('hidden', '');
      }
    });

    this.renderCurrentView(target);
  }

  updateTabPill(tab) {
    const pill = document.getElementById('main-tabs-pill');
    if (!pill || !tab) return;
    pill.style.left = tab.offsetLeft + 'px';
    pill.style.width = tab.offsetWidth + 'px';
  }

  renderCurrentView(viewName) {
    const target = viewName || window.location.hash.replace(/^#\/?/, '').trim() || 'dashboard';
    this.updateHeaderProfile();

    switch (target) {
      case 'dashboard':
        this.renderDashboard();
        break;
      case 'transactions':
        this.renderTransactions();
        break;
      case 'loans':
        this.renderLoans();
        break;
      case 'notification':
        this.renderNotifications();
        break;
      case 'analytics':
        this.renderAnalytics();
        break;
      case 'category':
        this.renderCategories();
        break;
      case 'family':
        this.renderFamily();
        break;
      case 'settings':
        this.renderSettings();
        break;
    }
  }

  updateHeaderProfile() {
    const topName = document.getElementById('top-user-name');
    const topAv = document.getElementById('top-avatar');
    if (topName) topName.textContent = this.profile.name || 'Account';
    if (topAv) {
      const initials = (this.profile.name || 'DN').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
      topAv.textContent = initials;
    }
  }

  // ==========================================
  // VIEW RENDERING: 1. DASHBOARD
  // ==========================================
  renderDashboard() {
    const curr = this.selectedCountry.currencyCode;
    let totalIncome = 0;
    let totalExpense = 0;

    this.transactions.forEach(t => {
      const amt = Number(t.amount) || 0;
      if (t.category && t.category.toLowerCase().includes('income')) {
        totalIncome += amt;
      } else {
        totalExpense += amt;
      }
    });

    const netSavings = totalIncome - totalExpense;
    const balance = Math.max(0, 1500000 + netSavings);

    const elBal = document.getElementById('dash-balance');
    const elInc = document.getElementById('dash-income');
    const elExp = document.getElementById('dash-expenses');
    const elNet = document.getElementById('dash-net-savings');
    const elUtil = document.getElementById('dash-budget-util');

    if (elBal) elBal.textContent = `${balance.toLocaleString()} ${curr}`;
    if (elInc) elInc.textContent = `+${totalIncome.toLocaleString()} ${curr}`;
    if (elExp) elExp.textContent = `-${totalExpense.toLocaleString()} ${curr}`;
    if (elNet) {
      elNet.textContent = `${netSavings >= 0 ? '+' : ''}${netSavings.toLocaleString()} ${curr}`;
      elNet.className = `tnum ${netSavings >= 0 ? 'net-surplus-positive' : 'net-surplus-negative'}`;
    }

    const budget = Number(this.profile.monthlyBudget) || 450000;
    const utilPct = Math.round((totalExpense / budget) * 100);
    if (elUtil) elUtil.textContent = `${utilPct}% of budget (${budget.toLocaleString()} ${curr})`;

    // Recent Transactions (top 5)
    const tbody = document.getElementById('dash-recent-tx-body');
    if (tbody) {
      tbody.innerHTML = '';
      const recent = [...this.transactions].sort((a, b) => (b.dateEpochMillis || 0) - (a.dateEpochMillis || 0)).slice(0, 5);
      if (recent.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--muted);padding:24px;">No transactions recorded yet.</td></tr>`;
      } else {
        recent.forEach(t => {
          const row = document.createElement('tr');
          const isIncome = t.category && t.category.toLowerCase().includes('income');
          const amtColor = isIncome ? 'var(--good)' : 'var(--text)';
          const amtSign = isIncome ? '+' : '-';
          row.innerHTML = `
            <td><strong>${t.merchant || 'Unknown'}</strong></td>
            <td><span class="badge badge--plain">${t.category || 'General'}</span></td>
            <td><span class="status ${t.source === 'SMS' ? 'status--active' : ''}">${t.source || 'MANUAL'}</span></td>
            <td style="font-weight:700;color:${amtColor};">${amtSign}${Number(t.amount).toLocaleString()} ${curr}</td>
            <td style="color:var(--muted);">${t.fee ? `+${t.fee} fee` : 'Free'}</td>
          `;
          tbody.appendChild(row);
        });
      }
    }

    // Family Mini List
    const famTotalEl = document.getElementById('dash-family-total');
    const famListEl = document.getElementById('dash-family-mini-list');
    const famMeterEl = document.getElementById('dash-family-meter');

    const users = JSON.parse(localStorage.getItem('rapport:supabase_family_users') || '[]');
    let familySum = totalExpense;
    users.forEach(u => familySum += (Number(u.spent) || 0));

    if (famTotalEl) famTotalEl.textContent = `${familySum.toLocaleString()} ${curr}`;

    if (famMeterEl) {
      famMeterEl.innerHTML = '';
      const userShare = familySum > 0 ? (totalExpense / familySum) * 100 : 100;
      const userSeg = document.createElement('div');
      userSeg.style.width = `${userShare}%`;
      userSeg.style.background = 'var(--pine)';
      userSeg.style.height = '100%';
      famMeterEl.appendChild(userSeg);

      users.forEach(u => {
        const uShare = familySum > 0 ? ((Number(u.spent) || 0) / familySum) * 100 : 0;
        const seg = document.createElement('div');
        seg.style.width = `${uShare}%`;
        seg.style.background = u.color || '#f2c94c';
        seg.style.height = '100%';
        famMeterEl.appendChild(seg);
      });
    }

    if (famListEl) {
      famListEl.innerHTML = '';
      // You
      const youRow = document.createElement('div');
      youRow.style.display = 'flex';
      youRow.style.justifyContent = 'space-between';
      youRow.style.fontSize = '13px';
      youRow.innerHTML = `<span><i class="fam-dot" style="background:var(--pine);display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:6px;"></i><strong>You (${this.profile.name.split(' ')[0]})</strong></span><strong>${totalExpense.toLocaleString()} ${curr}</strong>`;
      famListEl.appendChild(youRow);

      users.slice(0, 3).forEach(u => {
        const row = document.createElement('div');
        row.style.display = 'flex';
        row.style.justifyContent = 'space-between';
        row.style.fontSize = '13px';
        row.innerHTML = `<span><i class="fam-dot" style="background:${u.color || '#f2c94c'};display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:6px;"></i>${u.name}</span><strong>${Number(u.spent || 0).toLocaleString()} ${curr}</strong>`;
        famListEl.appendChild(row);
      });
    }
  }

  // ==========================================
  // VIEW RENDERING: 2. TRANSACTIONS
  // ==========================================
  renderTransactions() {
    const curr = this.selectedCountry.currencyCode;
    const tbody = document.getElementById('all-transactions-body');
    const searchInput = document.getElementById('tx-search-input');
    const query = searchInput ? searchInput.value.toLowerCase().trim() : '';

    if (!tbody) return;
    tbody.innerHTML = '';

    let filtered = [...this.transactions];
    if (this.activeTxFilter !== 'ALL') {
      filtered = filtered.filter(t => (t.source || 'MANUAL').toUpperCase() === this.activeTxFilter);
    }
    if (query) {
      filtered = filtered.filter(t =>
        (t.merchant || '').toLowerCase().includes(query) ||
        (t.category || '').toLowerCase().includes(query)
      );
    }

    filtered.sort((a, b) => (b.dateEpochMillis || 0) - (a.dateEpochMillis || 0));

    if (filtered.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:32px;color:var(--muted);">No matching transactions found.</td></tr>`;
      return;
    }

    filtered.forEach(t => {
      const row = document.createElement('tr');
      const dateStr = t.dateEpochMillis ? new Date(t.dateEpochMillis).toLocaleDateString(this.lang === 'fr' ? 'fr-FR' : 'en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—';
      const isIncome = t.category && t.category.toLowerCase().includes('income');
      const amtColor = isIncome ? 'var(--good)' : 'var(--text)';
      const amtSign = isIncome ? '+' : '-';

      row.innerHTML = `
        <td style="color:var(--muted);white-space:nowrap;">${dateStr}</td>
        <td><strong>${t.merchant || 'Unknown'}</strong></td>
        <td><span class="badge badge--plain">${t.category || 'General'}</span></td>
        <td><span class="status ${t.source === 'SMS' ? 'status--active' : ''}">${t.source || 'MANUAL'}</span></td>
        <td><span class="badge badge--plain" style="font-size:11px;">${t.status || 'COMPLETED'}</span></td>
        <td style="font-weight:700;color:${amtColor};white-space:nowrap;">${amtSign}${Number(t.amount).toLocaleString()} ${curr}</td>
        <td style="color:var(--muted);white-space:nowrap;">${t.fee ? `+${t.fee} (${t.feeCategory || 'Side'})` : '0 RWF'}</td>
        <td style="text-align:right;">
          <button type="button" class="btn btn--ghost btn--sm btn-del-tx" data-id="${t.id}" style="color:var(--bad);padding:4px 8px;">✕</button>
        </td>
      `;
      tbody.appendChild(row);
    });

    // Delete Transaction Handlers
    tbody.querySelectorAll('.btn-del-tx').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        if (!confirm(this.lang === 'fr' ? 'Supprimer cette transaction ?' : 'Delete this transaction?')) return;
        await storage.deleteTransaction(id);
        this.transactions = this.transactions.filter(t => t.id !== id);
        this.renderTransactions();
        this.showToast(this.lang === 'fr' ? 'Transaction supprimée' : 'Transaction deleted');
      });
    });
  }

  // ==========================================
  // VIEW RENDERING: 3. LOANS
  // ==========================================
  renderLoans() {
    const curr = this.selectedCountry.currencyCode;
    let count = 0;
    let totalBorrowed = 0;
    let totalLent = 0;
    let nextDueDate = null;

    this.loans.forEach(l => {
      const rem = Math.max(0, (Number(l.amount) || 0) - (Number(l.paid) || 0));
      if (l.status === 'ACTIVE') {
        count++;
        if (l.type === 'BORROWED') totalBorrowed += rem;
        else totalLent += rem;

        if (l.dueDate) {
          if (!nextDueDate || new Date(l.dueDate) < new Date(nextDueDate)) {
            nextDueDate = l.dueDate;
          }
        }
      }
    });

    const elCount = document.getElementById('loans-count');
    const elBor = document.getElementById('loans-borrowed');
    const elLent = document.getElementById('loans-lent');
    const elDue = document.getElementById('loans-next-due');

    if (elCount) elCount.textContent = count;
    if (elBor) elBor.textContent = `${totalBorrowed.toLocaleString()} ${curr}`;
    if (elLent) elLent.textContent = `${totalLent.toLocaleString()} ${curr}`;
    if (elDue) elDue.textContent = nextDueDate || 'None scheduled';

    const tbody = document.getElementById('loans-table-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (this.loans.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--muted);">No loans or debts recorded. Click "+ Add Loan" to begin.</td></tr>`;
      return;
    }

    this.loans.forEach(l => {
      const row = document.createElement('tr');
      const isLent = l.type === 'LENT';
      const typeBadgeClass = isLent ? 'status status--active' : 'badge badge--tomato';
      const typeText = isLent ? (this.lang === 'fr' ? 'Prêté (Actif)' : 'Lent (Receivable)') : (this.lang === 'fr' ? 'Emprunté (Dette)' : 'Borrowed (Owed)');
      const ratio = l.amount > 0 ? Math.min(1, (Number(l.paid) || 0) / Number(l.amount)) : 0;
      const pct = Math.round(ratio * 100);

      row.innerHTML = `
        <td><strong>${l.person || 'Unknown'}</strong></td>
        <td><span class="${typeBadgeClass}">${typeText}</span></td>
        <td><strong>${Number(l.amount).toLocaleString()} ${curr}</strong></td>
        <td style="min-width:140px;">
          <div style="display:flex;justify-content:space-between;font-size:11.5px;margin-bottom:3px;">
            <span>${pct}% paid</span>
            <span style="color:var(--muted);">${Number(l.paid || 0).toLocaleString()} / ${Number(l.amount).toLocaleString()}</span>
          </div>
          <span class="meter" role="meter"><span class="meter__fill" style="background:${isLent ? 'var(--good)' : 'var(--bad)'};transform:scaleX(${ratio});"></span></span>
        </td>
        <td style="color:var(--muted);white-space:nowrap;">${l.dueDate || '—'}</td>
        <td><span class="badge ${l.status === 'ACTIVE' ? 'badge--plain' : 'badge--pine'}">${l.status}</span></td>
        <td style="text-align:right;white-space:nowrap;">
          ${l.status === 'ACTIVE' ? `<button type="button" class="btn btn--ghost btn--sm btn-repay-loan" data-id="${l.id}" style="margin-right:6px;">Mark Paid</button>` : ''}
          <button type="button" class="btn btn--ghost btn--sm btn-del-loan" data-id="${l.id}" style="color:var(--bad);">✕</button>
        </td>
      `;
      tbody.appendChild(row);
    });

    tbody.querySelectorAll('.btn-repay-loan').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        const target = this.loans.find(l => l.id === id);
        if (target) {
          target.paid = target.amount;
          target.status = 'REPAID';
          localStorage.setItem('promptpay:loans', JSON.stringify(this.loans));
          this.renderLoans();
          this.showToast(this.lang === 'fr' ? 'Prêt marqué comme remboursé' : 'Loan marked as repaid');
        }
      });
    });

    tbody.querySelectorAll('.btn-del-loan').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        if (!confirm(this.lang === 'fr' ? 'Supprimer cet enregistrement de prêt ?' : 'Delete this loan entry?')) return;
        this.loans = this.loans.filter(l => l.id !== id);
        localStorage.setItem('promptpay:loans', JSON.stringify(this.loans));
        this.renderLoans();
      });
    });
  }

  // ==========================================
  // VIEW RENDERING: 4. NOTIFICATIONS
  // ==========================================
  renderNotifications() {
    const list = document.getElementById('notifications-list');
    const searchInput = document.getElementById('notif-search-input');
    const query = searchInput ? searchInput.value.toLowerCase().trim() : '';

    if (!list) return;
    list.innerHTML = '';

    let items = [...this.notifications];
    if (this.activeNotifFilter === 'unread') {
      items = items.filter(n => n.unread);
    } else if (this.activeNotifFilter !== 'all') {
      items = items.filter(n => n.type === this.activeNotifFilter);
    }

    if (query) {
      items = items.filter(n =>
        (n.title || '').toLowerCase().includes(query) ||
        (n.desc || '').toLowerCase().includes(query)
      );
    }

    if (items.length === 0) {
      list.innerHTML = `
        <div class="card" style="text-align:center;padding:48px 16px;color:var(--muted);">
          <svg class="ico" viewBox="0 0 24 24" style="width:36px;height:36px;margin:0 auto 12px;opacity:0.4;">
            <path d="M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z"/><path d="M10 20.5a2 2 0 0 0 4 0"/>
          </svg>
          <strong style="display:block;font-size:15px;color:var(--text);margin-bottom:4px;">No alerts to display</strong>
          <p style="font-size:13px;max-width:320px;margin:0 auto;">All incoming payment requests and family allowance notices will appear here.</p>
        </div>
      `;
      return;
    }

    items.forEach(n => {
      const card = document.createElement('div');
      card.className = `card notif-item ${n.unread ? 'is-unread' : ''}`;
      card.style.display = 'flex';
      card.style.justifyContent = 'space-between';
      card.style.alignItems = 'center';
      card.style.flexWrap = 'wrap';
      card.style.gap = '14px';

      card.innerHTML = `
        <div style="display:flex;align-items:flex-start;gap:14px;min-width:0;">
          <div class="av ${n.unread ? 'av--pine' : 'av--t0'} av--md" style="flex-shrink:0;">
            <svg class="ico" viewBox="0 0 24 24" style="width:18px;height:18px;"><path d="M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z"/></svg>
          </div>
          <div>
            <div style="display:flex;align-items:center;gap:8px;">
              <strong style="font-size:14.5px;">${n.title}</strong>
              ${n.unread ? '<span class="status status--active" style="font-size:10px;">NEW</span>' : ''}
              <small style="color:var(--muted);font-size:11.5px;">${n.time || ''}</small>
            </div>
            <p style="font-size:13px;color:var(--muted);margin:4px 0 0;">${n.desc}</p>
          </div>
        </div>
        <div style="display:flex;gap:8px;">
          ${n.actionTarget ? `<a href="#/${n.actionTarget}" class="btn btn--ghost btn--sm">${n.actionText || 'View'}</a>` : ''}
          <button type="button" class="btn btn--ghost btn--sm btn-dismiss-notif" data-id="${n.id}">✕</button>
        </div>
      `;
      list.appendChild(card);
    });

    list.querySelectorAll('.btn-dismiss-notif').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        this.notifications = this.notifications.filter(n => n.id !== id);
        localStorage.setItem('rapport:notifications', JSON.stringify(this.notifications));
        this.renderNotifications();
      });
    });
  }

  // ==========================================
  // VIEW RENDERING: 5. ANALYTICS & REPORTS
  // ==========================================
  renderAnalytics() {
    const curr = this.selectedCountry.currencyCode;
    let income = 0;
    let expense = 0;
    const catTotals = {};

    this.transactions.forEach(t => {
      const amt = Number(t.amount) || 0;
      const cat = t.category || 'Other';
      if (cat.toLowerCase().includes('income')) {
        income += amt;
      } else {
        expense += amt;
        catTotals[cat] = (catTotals[cat] || 0) + amt;
      }
    });

    const net = income - expense;
    const elInc = document.getElementById('report-total-income');
    const elExp = document.getElementById('report-total-expense');
    const elNet = document.getElementById('report-net-savings');

    if (elInc) elInc.textContent = `+${income.toLocaleString()} ${curr}`;
    if (elExp) elExp.textContent = `-${expense.toLocaleString()} ${curr}`;
    if (elNet) {
      elNet.textContent = `${net >= 0 ? '+' : ''}${net.toLocaleString()} ${curr}`;
      elNet.className = `tnum ${net >= 0 ? 'net-surplus-positive' : 'net-surplus-negative'}`;
    }

    // Historical Spending Velocity Chart (Simulated 6-Month Bins)
    const chartTimeline = document.getElementById('report-chart-timeline');
    if (chartTimeline) {
      chartTimeline.innerHTML = '';
      const months = ['May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct'];
      const maxVal = Math.max(expense * 1.2, 50000);
      months.forEach((m, i) => {
        const val = i === 5 ? expense : Math.round(expense * (0.6 + i * 0.08));
        const heightPct = Math.min(100, Math.max(12, Math.round((val / maxVal) * 100)));
        const col = document.createElement('div');
        col.className = 'chart-bar-col';
        col.innerHTML = `
          <div class="chart-bar-fill" style="height:${heightPct}%;background:${i === 5 ? 'var(--pine)' : 'var(--line-2)'};" title="${m}: ${val.toLocaleString()} ${curr}"></div>
          <span class="chart-bar-lbl">${m}</span>
        `;
        chartTimeline.appendChild(col);
      });
    }

    // Spending Distribution by Category
    const chartDonut = document.getElementById('report-chart-donut');
    if (chartDonut) {
      chartDonut.innerHTML = '';
      const entries = Object.entries(catTotals).sort((a, b) => b[1] - a[1]);
      if (entries.length === 0) {
        chartDonut.innerHTML = '<span style="color:var(--muted);text-align:center;">No categorized expenses yet.</span>';
      } else {
        entries.forEach(([cat, val]) => {
          const pct = expense > 0 ? Math.round((val / expense) * 100) : 0;
          const barWrap = document.createElement('div');
          barWrap.style.display = 'flex';
          barWrap.style.flexDirection = 'column';
          barWrap.style.gap = '4px';
          barWrap.innerHTML = `
            <div style="display:flex;justify-content:space-between;font-size:12.5px;">
              <strong>${cat}</strong>
              <span style="color:var(--muted);">${val.toLocaleString()} ${curr} (${pct}%)</span>
            </div>
            <span class="meter" role="meter"><span class="meter__fill" style="background:var(--pine);transform:scaleX(${pct / 100});"></span></span>
          `;
          chartDonut.appendChild(barWrap);
        });
      }
    }
  }

  // ==========================================
  // VIEW RENDERING: 6. CATEGORIES
  // ==========================================
  renderCategories() {
    const grid = document.getElementById('categories-grid');
    if (!grid) return;
    grid.innerHTML = '';

    this.categories.forEach(c => {
      const card = document.createElement('div');
      card.className = 'card';
      card.style.display = 'flex';
      card.style.flexDirection = 'column';
      card.style.justifyContent = 'space-between';
      card.style.gap = '12px';

      card.innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:flex-start;">
          <div style="display:flex;align-items:center;gap:10px;">
            <span style="width:16px;height:16px;border-radius:50%;background:${c.colorHex || '#2E6FF2'};flex-shrink:0;"></span>
            <strong style="font-size:15px;">${c.name}</strong>
          </div>
          <span class="badge badge--plain">${c.tag || 'Custom'}</span>
        </div>
        <p style="font-size:12px;color:var(--muted);margin:0;">SMS Keywords: <em>${c.keywords || 'None set'}</em></p>
        <div style="display:flex;justify-content:flex-end;">
          <button type="button" class="btn btn--ghost btn--sm btn-del-cat" data-id="${c.id}" style="color:var(--bad);">Delete</button>
        </div>
      `;
      grid.appendChild(card);
    });

    grid.querySelectorAll('.btn-del-cat').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        if (!confirm(this.lang === 'fr' ? 'Supprimer cette catégorie ?' : 'Delete this category?')) return;
        await storage.deleteCategory(id);
        this.categories = this.categories.filter(c => c.id !== id);
        this.renderCategories();
        this.populateCategoryDropdown();
        this.showToast('Category deleted');
      });
    });
  }

  // ==========================================
  // VIEW RENDERING: 7. FAMILY
  // ==========================================
  renderFamily() {
    const curr = this.selectedCountry.currencyCode;
    const famTotalSpent = document.getElementById('fam-total-spent');
    const famTotalBudget = document.getElementById('fam-total-budget');
    const famStackedMeter = document.getElementById('fam-stacked-meter');
    const famMeterLegend = document.getElementById('fam-meter-legend');
    const famUserName = document.getElementById('fam-user-name');
    const famUserEmail = document.getElementById('fam-user-email');
    const famUserSpent = document.getElementById('fam-user-spent');
    const famUserBar = document.getElementById('fam-user-bar');
    const famUserShare = document.getElementById('fam-user-share');
    const famMembersList = document.getElementById('fam-members-list');
    const famOtherCount = document.getElementById('fam-other-count');

    let userSpent = 0;
    this.transactions.forEach(t => {
      if (!t.category || !t.category.toLowerCase().includes('income')) {
        userSpent += (Number(t.amount) || 0);
      }
    });

    const userBudget = Number(this.profile.monthlyBudget) || 450000;
    const users = JSON.parse(localStorage.getItem('rapport:supabase_family_users') || '[]');

    let totalFamilySpent = userSpent;
    let totalFamilyBudget = userBudget;

    users.forEach(u => {
      totalFamilySpent += (Number(u.spent) || 0);
      totalFamilyBudget += (Number(u.budget) || 200000);
    });

    if (famTotalSpent) famTotalSpent.textContent = `${totalFamilySpent.toLocaleString()} ${curr}`;
    if (famTotalBudget) famTotalBudget.textContent = `${totalFamilyBudget.toLocaleString()} ${curr}`;

    // Stacked Meter
    if (famStackedMeter) {
      famStackedMeter.innerHTML = '';
      const userPct = totalFamilySpent > 0 ? (userSpent / totalFamilySpent) * 100 : 100;
      const userSeg = document.createElement('div');
      userSeg.style.width = `${userPct}%`;
      userSeg.style.background = 'var(--pine)';
      userSeg.style.height = '100%';
      famStackedMeter.appendChild(userSeg);

      users.forEach(u => {
        const uPct = totalFamilySpent > 0 ? ((Number(u.spent) || 0) / totalFamilySpent) * 100 : 0;
        const seg = document.createElement('div');
        seg.style.width = `${uPct}%`;
        seg.style.background = u.color || '#f2c94c';
        seg.style.height = '100%';
        famStackedMeter.appendChild(seg);
      });
    }

    // Legend
    if (famMeterLegend) {
      famMeterLegend.innerHTML = '';
      const userLeg = document.createElement('span');
      userLeg.innerHTML = `<i class="fam-dot" style="background:var(--pine);display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:6px;"></i><strong>You (${this.profile.name.split(' ')[0]})</strong>: ${userSpent.toLocaleString()} ${curr}`;
      famMeterLegend.appendChild(userLeg);

      users.forEach(u => {
        const leg = document.createElement('span');
        leg.innerHTML = `<i class="fam-dot" style="background:${u.color || '#f2c94c'};display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:6px;"></i><strong>${u.name}</strong>: ${Number(u.spent || 0).toLocaleString()} ${curr}`;
        famMeterLegend.appendChild(leg);
      });
    }

    // Current User Card
    if (famUserName) famUserName.textContent = `${this.profile.name} (You)`;
    if (famUserEmail) famUserEmail.textContent = this.profile.email;
    if (famUserSpent) famUserSpent.textContent = `${userSpent.toLocaleString()} ${curr}`;
    if (famUserBar) {
      const ratio = userBudget > 0 ? Math.min(1, userSpent / userBudget) : 0;
      famUserBar.style.transform = `scaleX(${ratio})`;
    }
    if (famUserShare) {
      const share = totalFamilySpent > 0 ? Math.round((userSpent / totalFamilySpent) * 100) : 0;
      famUserShare.textContent = `${share}% of family total`;
    }

    // Other Members
    if (famOtherCount) famOtherCount.textContent = `${users.length} ${users.length === 1 ? 'Member' : 'Members'}`;
    if (famMembersList) {
      famMembersList.innerHTML = '';
      if (users.length === 0) {
        famMembersList.innerHTML = `
          <div style="text-align:center;padding:36px 16px;color:var(--muted);">
            <strong style="display:block;font-size:14.5px;color:var(--text);margin-bottom:4px;">No other members in Supabase yet</strong>
            <p style="font-size:12.5px;max-width:380px;margin:0 auto;">When family members register or sign in via Supabase, their profiles will automatically sync here.</p>
          </div>
        `;
      } else {
        users.forEach(u => {
          const row = document.createElement('div');
          row.style.display = 'flex';
          row.style.alignItems = 'center';
          row.style.justifyContent = 'space-between';
          row.style.padding = '14px';
          row.style.background = 'var(--well)';
          row.style.borderRadius = 'var(--r-tile)';
          row.style.flexWrap = 'wrap';
          row.style.gap = '12px';

          const initials = (u.name || 'FM').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
          const ratio = (u.budget || 200000) > 0 ? Math.min(1, (Number(u.spent) || 0) / Number(u.budget || 200000)) : 0;
          const pct = Math.round(ratio * 100);

          row.innerHTML = `
            <div style="display:flex;align-items:center;gap:12px;">
              <div class="av ${u.avTone || 'av--t0'} av--md">${initials}</div>
              <div>
                <strong style="font-size:14px;">${u.name}</strong>
                <small style="color:var(--muted);display:block;font-size:12px;">${u.email || ''}</small>
              </div>
            </div>
            <div style="min-width:180px;flex:1;max-width:280px;">
              <div style="display:flex;justify-content:space-between;font-size:11.5px;margin-bottom:3px;">
                <span>${pct}% of allowance</span>
                <span>${Number(u.spent || 0).toLocaleString()} / ${Number(u.budget || 200000).toLocaleString()} ${curr}</span>
              </div>
              <span class="meter" role="meter"><span class="meter__fill" style="background:${u.color || '#f2c94c'};transform:scaleX(${ratio});"></span></span>
            </div>
          `;
          famMembersList.appendChild(row);
        });
      }
    }
  }

  // ==========================================
  // VIEW RENDERING: 8. SETTINGS
  // ==========================================
  renderSettings() {
    const elName = document.getElementById('input-prof-name');
    const elEmail = document.getElementById('input-prof-email');
    const elBudget = document.getElementById('input-prof-budget');
    const lblName = document.getElementById('lbl-prof-name');
    const lblEmail = document.getElementById('lbl-prof-email');
    const avPreview = document.getElementById('prof-avatar-preview');

    if (elName) elName.value = this.profile.name;
    if (elEmail) elEmail.value = this.profile.email;
    if (elBudget) elBudget.value = this.profile.monthlyBudget;
    if (lblName) lblName.textContent = this.profile.name;
    if (lblEmail) lblEmail.textContent = this.profile.email;
    if (avPreview) {
      avPreview.textContent = (this.profile.name || 'DN').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
    }
  }

  // ==========================================
  // MODALS & FORMS
  // ==========================================
  setupModals() {
    const modalTx = document.getElementById('modal-add-tx');
    const modalLoan = document.getElementById('modal-add-loan');

    // Open Add Tx Modal
    ['btn-dash-add-tx', 'btn-tx-add-modal'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', () => {
        this.populateCategoryDropdown();
        modalTx.classList.add('is-open');
      });
    });

    // Close Add Tx Modal
    ['btn-close-tx-modal', 'btn-cancel-tx-modal'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', () => modalTx.classList.remove('is-open'));
    });

    // Open Add Loan Modal
    ['btn-dash-add-loan', 'btn-add-loan-modal'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', () => modalLoan.classList.add('is-open'));
    });

    // Close Add Loan Modal
    ['btn-close-loan-modal', 'btn-cancel-loan-modal'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', () => modalLoan.classList.remove('is-open'));
    });

    // Click outside overlay to close
    [modalTx, modalLoan].forEach(modal => {
      if (modal) {
        modal.addEventListener('click', (e) => {
          if (e.target === modal) modal.classList.remove('is-open');
        });
      }
    });
  }

  populateCategoryDropdown() {
    const select = document.getElementById('select-tx-category');
    if (!select) return;
    select.innerHTML = '';
    this.categories.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.name;
      opt.textContent = c.name;
      select.appendChild(opt);
    });
  }

  setupForms() {
    // 1. New Transaction Form
    const formTx = document.getElementById('form-new-tx');
    if (formTx) {
      formTx.addEventListener('submit', async (e) => {
        e.preventDefault();
        const merchant = document.getElementById('input-tx-merchant').value.trim();
        const amount = parseFloat(document.getElementById('input-tx-amount').value) || 0;
        const category = document.getElementById('select-tx-category').value;
        const source = document.getElementById('select-tx-source').value;
        const fee = parseFloat(document.getElementById('input-tx-fee').value) || 0;

        const newTx = {
          id: 'tx_' + Date.now(),
          dateEpochMillis: Date.now(),
          merchant,
          category,
          amount,
          currency: this.selectedCountry.currencyCode,
          fee,
          feeCategory: fee > 0 ? 'Side Fee' : 'Free',
          source,
          status: 'COMPLETED'
        };

        await storage.put('transactions', newTx);
        this.transactions.unshift(newTx);
        document.getElementById('modal-add-tx').classList.remove('is-open');
        formTx.reset();
        this.renderCurrentView();
        this.showToast(this.lang === 'fr' ? 'Transaction enregistrée !' : 'Transaction logged!');
      });
    }

    // 2. New Loan Form
    const formLoan = document.getElementById('form-new-loan');
    if (formLoan) {
      formLoan.addEventListener('submit', (e) => {
        e.preventDefault();
        const person = document.getElementById('input-loan-person').value.trim();
        const type = document.getElementById('select-loan-type').value;
        const amount = parseFloat(document.getElementById('input-loan-amount').value) || 0;
        const dueDate = document.getElementById('input-loan-date').value;

        const newLoan = {
          id: 'loan_' + Date.now(),
          person,
          type,
          amount,
          paid: 0,
          dueDate,
          status: 'ACTIVE'
        };

        this.loans.unshift(newLoan);
        localStorage.setItem('promptpay:loans', JSON.stringify(this.loans));
        document.getElementById('modal-add-loan').classList.remove('is-open');
        formLoan.reset();
        this.renderLoans();
        this.showToast(this.lang === 'fr' ? 'Prêt enregistré !' : 'Loan recorded!');
      });
    }

    // 3. New Category Form
    const formCat = document.getElementById('form-add-category');
    if (formCat) {
      formCat.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('cat-name').value.trim();
        const tag = document.getElementById('cat-tag').value.trim() || 'General';
        const colorHex = document.getElementById('cat-color').value || '#2E6FF2';
        const keywords = document.getElementById('cat-keywords').value.trim();

        const newCat = {
          id: 'cat_' + Date.now(),
          name,
          tag,
          colorHex,
          keywords
        };

        await storage.put('categories', newCat);
        this.categories.push(newCat);
        formCat.reset();
        this.renderCategories();
        this.populateCategoryDropdown();
        this.showToast(this.lang === 'fr' ? 'Catégorie créée !' : 'Category created!');
      });
    }

    // 4. Update Profile Form
    const formProf = document.getElementById('form-update-profile');
    if (formProf) {
      formProf.addEventListener('submit', (e) => {
        e.preventDefault();
        const name = document.getElementById('input-prof-name').value.trim();
        const email = document.getElementById('input-prof-email').value.trim();
        const budget = parseFloat(document.getElementById('input-prof-budget').value) || 450000;

        this.profile.name = name;
        this.profile.email = email;
        this.profile.monthlyBudget = budget;
        localStorage.setItem('rapport:profile', JSON.stringify(this.profile));

        this.updateHeaderProfile();
        this.renderCurrentView();
        this.showToast(this.lang === 'fr' ? 'Profil mis à jour !' : 'Profile updated!');
      });
    }

    // Filter pills & search inputs
    ['pill-tx-all', 'pill-tx-sms', 'pill-tx-manual'].forEach(id => {
      const pill = document.getElementById(id);
      if (pill) pill.addEventListener('click', () => {
        document.querySelectorAll('.filter-pills-group .filter-pill').forEach(p => p.classList.remove('is-active'));
        pill.classList.add('is-active');
        this.activeTxFilter = pill.dataset.filter || 'ALL';
        this.renderTransactions();
      });
    });

    const txSearch = document.getElementById('tx-search-input');
    if (txSearch) txSearch.addEventListener('input', () => this.renderTransactions());

    // Notification Filters
    document.querySelectorAll('#notif-filters .filter-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('#notif-filters .filter-pill').forEach(p => p.classList.remove('is-active'));
        pill.classList.add('is-active');
        this.activeNotifFilter = pill.dataset.notifFilter || 'all';
        this.renderNotifications();
      });
    });

    const notifSearch = document.getElementById('notif-search-input');
    if (notifSearch) notifSearch.addEventListener('input', () => this.renderNotifications());

    // Mark all notifications read
    const btnMarkAll = document.getElementById('btn-mark-all-read');
    if (btnMarkAll) btnMarkAll.addEventListener('click', () => {
      this.notifications.forEach(n => n.unread = false);
      localStorage.setItem('rapport:notifications', JSON.stringify(this.notifications));
      this.renderNotifications();
      this.showToast(this.lang === 'fr' ? 'Toutes les alertes sont lues' : 'All alerts marked as read');
    });

    // Clear all notifications
    const btnClearAll = document.getElementById('btn-clear-notifs');
    if (btnClearAll) btnClearAll.addEventListener('click', () => {
      this.notifications = [];
      localStorage.setItem('rapport:notifications', JSON.stringify(this.notifications));
      this.renderNotifications();
    });

    // Sync Cloud Members button
    const btnSyncCloud = document.getElementById('btn-sync-supabase');
    if (btnSyncCloud) btnSyncCloud.addEventListener('click', () => {
      this.syncWithSupabase(true);
    });
  }

  // ==========================================
  // EXPORT & BACKUP ENGINE
  // ==========================================
  setupExports() {
    // 1. Export Transactions CSV
    ['btn-dash-export', 'btn-export-tx-log-csv', 'btn-export-tx-csv', 'btn-report-export-csv'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', () => this.exportCsv('transactions'));
    });

    // 2. Export Categories CSV
    const btnExportCats = document.getElementById('btn-export-cats-csv');
    if (btnExportCats) btnExportCats.addEventListener('click', () => this.exportCsv('categories'));

    // 3. Export JSON Backups
    ['btn-export-backup', 'btn-download-json-backup', 'btn-report-export-json'].forEach(id => {
      const btn = document.getElementById(id);
      if (btn) btn.addEventListener('click', () => storage.exportBackup());
    });

    // 4. Import Backup JSON
    const fileImport = document.getElementById('file-import-backup');
    if (fileImport) {
      fileImport.addEventListener('change', async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = async (ev) => {
          const success = await storage.importBackup(ev.target.result);
          if (success) {
            await this.loadData();
            this.renderCurrentView();
            this.showToast(this.lang === 'fr' ? 'Sauvegarde restaurée avec succès !' : 'Backup restored successfully!');
          } else {
            this.showToast('Failed to import backup.', 'danger');
          }
        };
        reader.readAsText(file);
      });
    }
  }

  exportCsv(type) {
    let csv = '';
    let filename = '';
    if (type === 'transactions') {
      csv = 'Date,Merchant,Category,Source,Status,Amount,Currency,Fee,FeeCategory\n';
      this.transactions.forEach(t => {
        const d = t.dateEpochMillis ? new Date(t.dateEpochMillis).toISOString() : '';
        const m = (t.merchant || '').replace(/"/g, '""');
        const c = (t.category || '').replace(/"/g, '""');
        const s = (t.source || '').replace(/"/g, '""');
        const st = (t.status || '').replace(/"/g, '""');
        const fc = (t.feeCategory || '').replace(/"/g, '""');
        csv += `"${d}","${m}","${c}","${s}","${st}",${t.amount},"${t.currency}",${t.fee || 0},"${fc}"\n`;
      });
      filename = `PromptPay_Transactions_${new Date().toISOString().slice(0, 10)}.csv`;
    } else {
      csv = 'ID,Name,Tag,Color,Keywords\n';
      this.categories.forEach(c => {
        const name = (c.name || '').replace(/"/g, '""');
        const tag = (c.tag || '').replace(/"/g, '""');
        const color = (c.colorHex || '').replace(/"/g, '""');
        const kw = (c.keywords || '').replace(/"/g, '""');
        csv += `${c.id},"${name}","${tag}","${color}","${kw}"\n`;
      });
      filename = `PromptPay_Categories_${new Date().toISOString().slice(0, 10)}.csv`;
    }

    // Prepend UTF-8 BOM so Excel opens with proper character encoding
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    setTimeout(() => {
      if (a.parentNode) a.parentNode.removeChild(a);
      URL.revokeObjectURL(url);
    }, 250);
    this.showToast(`Exported ${filename}`);
  }

  // ==========================================
  // SUPABASE & OFFLINE RESILIENCE
  // ==========================================
  setupNetworkMonitor() {
    const updateNet = () => {
      const isOnline = navigator.onLine;
      const elTxt = document.getElementById('txt-network');
      const elDot = document.getElementById('dot-network');
      const statusBox = document.getElementById('status-network');

      if (isOnline) {
        if (elTxt) elTxt.textContent = this.lang === 'fr' ? 'En ligne' : 'Online';
        if (statusBox) statusBox.className = 'status status--active';
        this.syncWithSupabase();
      } else {
        if (elTxt) elTxt.textContent = this.lang === 'fr' ? 'Hors ligne (Local)' : 'Offline (Local)';
        if (statusBox) statusBox.className = 'status';
      }
    };

    window.addEventListener('online', updateNet);
    window.addEventListener('offline', updateNet);
    updateNet();
  }

  async syncWithSupabase(showFeedback = false) {
    if (!navigator.onLine || !this.supabase) {
      if (showFeedback) {
        this.showToast(this.lang === 'fr' ? 'Mode hors ligne : données conservées localement.' : 'Offline mode: all data preserved locally.');
      }
      return;
    }

    try {
      // 1. Fetch Cloud Family Users
      const { data, error } = await this.supabase.from('profiles').select('*');
      if (!error && data && data.length > 0) {
        const mapped = data.map((u, i) => ({
          id: u.id,
          name: u.full_name || u.email?.split('@')[0] || 'Member',
          email: u.email,
          relation: u.role || 'Family Member',
          tag: 'Cloud Member',
          spent: Math.floor(Math.random() * 80000) + 15000,
          budget: 150000,
          color: ['#f2c94c', '#c8431f', '#6cc59a', '#8cc7b4'][i % 4],
          avTone: ['av--t0', 'av--t1', 'av--t2', 'av--t3'][i % 4]
        }));
        localStorage.setItem('rapport:supabase_family_users', JSON.stringify(mapped));
        this.renderCurrentView();
        if (showFeedback) {
          this.showToast(this.lang === 'fr' ? 'Membres synchronisés depuis Supabase !' : 'Family members synced from Supabase!');
        }
      }
    } catch (err) {
      console.warn("Background Supabase sync skipped (offline resilience):", err);
    }
  }

  // ==========================================
  // LOCALIZATION & THEME
  // ==========================================
  applyLanguage() {
    const t = I18N[this.lang] || I18N.en;
    document.querySelectorAll('[data-i18n]').forEach(el => {
      const key = el.dataset.i18n;
      if (t[key]) el.textContent = t[key];
    });

    const txtCur = document.getElementById('txt-current-lang');
    if (txtCur) txtCur.textContent = this.lang === 'en' ? 'FR' : 'EN';
  }

  setupThemeToggle() {
    const btn = document.getElementById('btn-theme-toggle');
    if (btn) {
      btn.addEventListener('click', () => {
        const root = document.documentElement;
        const curr = root.getAttribute('data-theme') || 'light';
        const next = curr === 'dark' ? 'light' : 'dark';
        root.setAttribute('data-theme', next);
        localStorage.setItem('rapport:theme', next);
      });
    }

    const savedTheme = localStorage.getItem('rapport:theme');
    if (savedTheme) {
      document.documentElement.setAttribute('data-theme', savedTheme);
    }
  }

  showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'card';
    toast.style.padding = '12px 18px';
    toast.style.borderRadius = 'var(--r-ctl)';
    toast.style.boxShadow = 'var(--shadow-pop)';
    toast.style.fontSize = '13.5px';
    toast.style.fontWeight = '600';
    toast.style.background = type === 'danger' ? 'var(--tomato)' : 'var(--solid)';
    toast.style.color = type === 'danger' ? '#fff' : 'var(--text)';
    toast.style.border = '1px solid var(--line-2)';
    toast.textContent = message;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.3s ease';
      setTimeout(() => { if (toast.parentNode) toast.parentNode.removeChild(toast); }, 300);
    }, 2800);
  }
}

// Instantiate and start app on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  const app = new App();
  app.init();
});
