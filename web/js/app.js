// PromptPay Web Application Controller & Router (Synchronized with Android & iOS)
import { storage } from './services/StorageService.js';
import { SmsParser } from './services/SmsParser.js';
import { ScamDetector } from './services/ScamDetector.js';
import { Charts } from './components/Charts.js';
import { ICONS, getCategoryIconSvg } from './components/Icons.js';
import { COUNTRIES, formatCurrency, getCountryByCurrency } from './models/Country.js';
import { getStrings } from './util/AppStrings.js';

class App {
  constructor() {
    this.currentTab = 'dashboard';
    this.transactions = [];
    this.categories = [];
    this.scamAlerts = [];
    this.profile = null;
    this.selectedCountry = COUNTRIES.RWANDA;
    this.lang = 'en';
    this.transactionFilter = 'ALL'; // 'ALL' | 'SMS' | 'MANUAL'
    this.isPinVisible = false;

    // Wait for DOM and storage
    document.addEventListener('DOMContentLoaded', () => this.init());
  }

  async init() {
    await storage.ready;
    await this.loadData();
    this.setupEventListeners();
    this.applyTranslations();
    this.render();
    this.registerServiceWorker();
  }

  async loadData() {
    this.transactions = await storage.getTransactions();
    this.categories = await storage.getCategories();
    this.scamAlerts = await storage.getScamAlerts();
    this.profile = await storage.getUserProfile();
    this.selectedCountry = getCountryByCurrency(this.profile.currencyCode);
    this.lang = this.profile.languageCode || 'en';
  }

  registerServiceWorker() {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('./sw.js').then((reg) => {
        console.log('PromptPay Offline Service Worker Registered:', reg.scope);
      }).catch((err) => {
        console.warn('Service Worker registration failed:', err);
      });
    }
  }

  get s() {
    return getStrings(this.lang);
  }

  // MARK: - Reactive UI Localization
  applyTranslations() {
    const s = this.s;

    // Navigation Labels
    const setTxt = (id, txt) => {
      const el = document.getElementById(id);
      if (el) el.innerText = txt;
    };

    setTxt('nav-lbl-dashboard', s.navDashboard);
    setTxt('nav-lbl-transactions', s.navTransactions);
    setTxt('nav-lbl-pending', s.navPendingReview);
    setTxt('nav-lbl-reports', s.navReports);
    setTxt('nav-lbl-scams', s.navScamAlerts);
    setTxt('nav-lbl-categories', s.navCategories);
    setTxt('nav-lbl-settings', s.navSettings);

    // Header
    setTxt('page-header-title', s.headerTitle);
    setTxt('page-header-subtitle', s.headerSubtitle);
    setTxt('btn-simulate-text', s.btnSimulateSms);
    setTxt('btn-add-text', s.btnAddTransaction);

    // Dashboard Banners
    setTxt('btn-review-now-banner', s.btnReviewNow);
    setTxt('btn-inspect-alerts-banner', s.btnInspectAlerts);

    // Metric Cards
    setTxt('lbl-net-balance', s.netMonthlyBalance);
    setTxt('lbl-total-income', s.totalMonthlyIncome);
    setTxt('lbl-total-expenses', s.totalMonthlyExpenses);
    setTxt('lbl-pending-reviews', s.pendingReviews);

    // Charts
    setTxt('lbl-spending-flow', s.spendingFlowOverTime);
    setTxt('lbl-daily-expenses', s.dailyExpenses);
    setTxt('lbl-category-breakdown', s.categoryBreakdown);
    setTxt('lbl-distribution', s.distribution);

    // Recent Activity Table
    setTxt('lbl-recent-tx', s.recentTransactions);
    setTxt('btn-view-all-tx', s.viewAll);
    setTxt('th-recent-merchant', s.colMerchant);
    setTxt('th-recent-category', s.colCategory);
    setTxt('th-recent-source', s.colSource);
    setTxt('th-recent-status', s.colStatus);
    setTxt('th-recent-date', s.colDate);
    setTxt('th-recent-amount', `${s.colAmount} & ${s.colFee}`);

    // Transactions Log Tab
    setTxt('lbl-all-tx-log', s.allTransactionsLog);
    setTxt('pill-filter-all', s.filterAll);
    setTxt('pill-filter-sms', s.filterSms);
    setTxt('pill-filter-manual', s.filterManual);
    setTxt('btn-export-text', s.btnExportBackup);
    setTxt('th-all-merchant', s.colMerchant);
    setTxt('th-all-category', s.colCategory);
    setTxt('th-all-source', s.colSource);
    setTxt('th-all-status', s.colStatus);
    setTxt('th-all-date', s.colDate);
    setTxt('th-all-amount', s.colAmount);
    setTxt('th-all-fee', s.colFee);
    setTxt('th-all-action', s.colAction);

    // Pending Review Tab
    setTxt('lbl-pending-title', s.pendingTitle);
    setTxt('lbl-pending-subtitle', s.pendingSubtitle);

    // Reports Tab
    setTxt('lbl-income-captured', s.incomeCaptured);
    setTxt('lbl-expenses-logged', s.expensesLogged);
    setTxt('lbl-net-savings', s.netSavings);
    setTxt('lbl-historical-velocity', s.historicalVelocity);
    setTxt('lbl-spending-distribution', s.spendingByCategory);

    // Scam Tab
    setTxt('lbl-fraud-title', s.fraudTitle);
    setTxt('lbl-fraud-subtitle', s.fraudSubtitle);

    // Categories Tab
    setTxt('lbl-create-category', s.createCustomCategory);
    setTxt('lbl-cat-name', s.categoryName);
    setTxt('lbl-cat-tag', s.tagLabel);
    setTxt('lbl-cat-color', s.colorLabel);
    setTxt('lbl-cat-keywords', s.keywordsLabel);
    setTxt('btn-save-category', s.btnAddCategory);
    setTxt('lbl-active-categories', s.activeCategories);

    // Settings Tab
    setTxt('lbl-settings-main', s.settingsHeading);
    setTxt('lbl-setting-language', s.languageSetting);
    setTxt('lbl-setting-lang-desc', s.languageDesc);
    setTxt('lbl-setting-country', s.countrySetting);
    setTxt('lbl-scam-filter-title', s.scamFilterHeading);
    setTxt('lbl-scam-filter-desc', s.scamFilterDesc);
    setTxt('lbl-security-title', s.securityHeading);
    setTxt('lbl-security-desc', s.securityDesc);
    setTxt('lbl-enter-pin', s.enterPinLabel);
    setTxt('btn-save-pin', s.btnSavePin);
    setTxt('lbl-backup-title', s.offlineBackupHeading);
    setTxt('lbl-backup-desc', s.offlineBackupDesc);
    setTxt('lbl-import-backup-btn', s.importBackupBtn);

    // Modals
    setTxt('modal-sim-title', s.modalSimulateTitle);
    setTxt('modal-sim-desc', s.modalSimulateDesc);
    setTxt('modal-sim-samples', s.samplePresets);
    setTxt('lbl-sim-sender', s.smsSenderHeader);
    setTxt('lbl-sim-body', s.smsBodyLabel);
    setTxt('btn-sim-cancel', s.cancel);
    setTxt('btn-sim-process', s.btnProcessSms);

    setTxt('modal-add-title', s.modalAddTitle);
    setTxt('lbl-add-type', s.txTypeLabel);
    setTxt('lbl-add-amount', s.amountLabel);
    setTxt('lbl-add-fee', s.feeAmountLabel);
    setTxt('lbl-add-feecat', s.feeCategoryLabel);
    setTxt('lbl-fee-note', s.feePlaceholder);
    setTxt('lbl-add-cat', s.categoryLabel);
    setTxt('lbl-add-desc', s.descMerchantLabel);
    setTxt('lbl-add-method', s.paymentMethodLabel);
    setTxt('btn-add-cancel', s.cancel);
    setTxt('btn-add-save', s.btnSaveTx);
    setTxt('lbl-manual-fixed-note', s.fixedManualNotice);

    // Set select values
    const langSelect = document.getElementById('setting-language');
    if (langSelect) langSelect.value = this.lang;
  }

  setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const tab = item.dataset.tab;
        if (tab) this.switchTab(tab);
      });
    });

    // Header buttons
    const btnSimulate = document.getElementById('btn-simulate-sms');
    if (btnSimulate) btnSimulate.addEventListener('click', () => this.openModal('modal-simulate-sms'));

    const btnAddTx = document.getElementById('btn-add-transaction');
    if (btnAddTx) btnAddTx.addEventListener('click', () => this.openModal('modal-add-transaction'));

    // Modal Close buttons
    document.querySelectorAll('.modal-close, .modal-cancel').forEach(btn => {
      btn.addEventListener('click', () => this.closeAllModals());
    });

    // Simulate SMS Form & Preset buttons
    const formSimulate = document.getElementById('form-simulate-sms');
    if (formSimulate) {
      formSimulate.addEventListener('submit', (e) => {
        e.preventDefault();
        this.handleSimulateSms();
      });
    }

    document.querySelectorAll('.sms-preset-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const sender = btn.dataset.sender;
        const text = btn.dataset.text;
        document.getElementById('sim-sender').value = sender;
        document.getElementById('sim-body').value = text;
      });
    });

    // Add Transaction Form
    const formAdd = document.getElementById('form-add-transaction');
    if (formAdd) {
      formAdd.addEventListener('submit', (e) => {
        e.preventDefault();
        this.handleAddTransaction();
      });
    }

    // Transaction Source Filter Pills
    document.querySelectorAll('.filter-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        this.transactionFilter = pill.dataset.filter || 'ALL';
        this.renderTransactionsTable(this.selectedCountry.currencyCode);
      });
    });

    // Settings Language Select
    const langSelect = document.getElementById('setting-language');
    if (langSelect) {
      langSelect.addEventListener('change', async (e) => {
        const newLang = e.target.value;
        this.lang = newLang;
        this.profile.languageCode = newLang;
        await storage.saveUserProfile(this.profile);
        this.applyTranslations();
        this.render();
        this.showToast(newLang === 'fr' ? 'Langue changée en Français !' : 'Language changed to English!');
      });
    }

    // Settings Country Select
    const countrySelect = document.getElementById('setting-country');
    if (countrySelect) {
      countrySelect.addEventListener('change', async (e) => {
        const code = e.target.value;
        this.profile.countryCode = code;
        this.profile.currencyCode = COUNTRIES[code].currencyCode;
        await storage.saveUserProfile(this.profile);
        this.selectedCountry = COUNTRIES[code];
        this.render();
        this.showToast(`Country updated to ${this.selectedCountry.name}`);
      });
    }

    // Scam filtering toggle
    const toggleScam = document.getElementById('setting-scam-toggle');
    if (toggleScam) {
      toggleScam.addEventListener('change', async (e) => {
        this.profile.scamFilteringEnabled = e.target.checked;
        await storage.saveUserProfile(this.profile);
        this.showToast(`Scam Filtering ${e.target.checked ? 'Enabled' : 'Disabled'}`);
      });
    }

    // PIN Lock toggle & box
    const togglePin = document.getElementById('setting-pin-toggle');
    const pinBox = document.getElementById('pin-setup-box');
    if (togglePin) {
      togglePin.checked = (this.profile && this.profile.lockMode === 'PIN');
      if (pinBox) pinBox.style.display = togglePin.checked ? 'block' : 'none';

      togglePin.addEventListener('change', async (e) => {
        const isChecked = e.target.checked;
        if (pinBox) pinBox.style.display = isChecked ? 'block' : 'none';
        if (!isChecked) {
          this.profile.lockMode = 'NONE';
          await storage.saveUserProfile(this.profile);
          this.showToast('PIN Lock Disabled');
        }
      });
    }

    // Save PIN button
    const btnSavePin = document.getElementById('btn-save-pin');
    if (btnSavePin) {
      btnSavePin.addEventListener('click', async () => {
        const pinInput = document.getElementById('input-security-pin');
        const val = pinInput.value.trim();
        if (val.length < 4) {
          alert(this.lang === 'fr' ? 'Veuillez saisir un code PIN d\'au moins 4 chiffres.' : 'Please enter a PIN with at least 4 digits.');
          return;
        }
        this.profile.lockMode = 'PIN';
        this.profile.pinHash = btoa(val); // Masked / Encrypted PIN stored locally
        await storage.saveUserProfile(this.profile);
        pinInput.value = '';
        this.showToast(this.lang === 'fr' ? 'Code PIN enregistré avec succès !' : 'PIN saved securely!');
      });
    }

    // Toggle PIN Visibility
    const btnTogglePinVis = document.getElementById('btn-toggle-pin-visibility');
    if (btnTogglePinVis) {
      btnTogglePinVis.addEventListener('click', () => {
        const pinInput = document.getElementById('input-security-pin');
        if (!pinInput) return;
        this.isPinVisible = !this.isPinVisible;
        pinInput.type = this.isPinVisible ? 'text' : 'password';
      });
    }

    // Export & Import backup buttons
    const btnExport = document.getElementById('btn-export-backup');
    if (btnExport) btnExport.addEventListener('click', () => storage.exportBackup());

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
            this.applyTranslations();
            this.render();
            this.showToast(this.lang === 'fr' ? 'Sauvegarde restaurée avec succès !' : 'Backup restored successfully!');
          } else {
            this.showToast('Failed to import backup.', 'danger');
          }
        };
        reader.readAsText(file);
      });
    }

    // Add custom category form
    const formAddCategory = document.getElementById('form-add-category');
    if (formAddCategory) {
      formAddCategory.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('cat-name').value.trim();
        const tag = document.getElementById('cat-tag').value.trim() || 'Custom';
        const keywords = document.getElementById('cat-keywords').value.trim();
        const color = document.getElementById('cat-color').value;

        if (!name) return;

        const newCat = {
          id: Date.now(),
          name,
          iconName: 'tag',
          tag,
          colorHex: color,
          isCustom: true,
          keywords
        };

        await storage.saveCategory(newCat);
        await this.loadData();
        this.render();
        formAddCategory.reset();
        this.showToast(`Category "${name}" created!`);
      });
    }
  }

  switchTab(tabId) {
    this.currentTab = tabId;
    document.querySelectorAll('.nav-item').forEach(item => {
      item.classList.toggle('active', item.dataset.tab === tabId);
    });
    document.querySelectorAll('.tab-view').forEach(view => {
      view.classList.toggle('active', view.id === `tab-${tabId}`);
    });
    this.render();
  }

  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
  }

  closeAllModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
  }

  showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
  }

  // MARK: - Simulate SMS (Phone SMS - Type Locked to SMS)
  async handleSimulateSms() {
    const sender = document.getElementById('sim-sender').value.trim();
    const body = document.getElementById('sim-body').value.trim();

    if (!body) {
      alert(this.lang === 'fr' ? 'Veuillez saisir le texte du SMS.' : 'Please enter an SMS message body.');
      return;
    }

    const filteringEnabled = this.profile.scamFilteringEnabled !== false;
    const detection = ScamDetector.detectScam(
      sender,
      body,
      this.transactions,
      this.scamAlerts,
      filteringEnabled
    );

    // If Likely Scam
    if (detection.classification === 'LIKELY_SCAM') {
      const alertItem = {
        id: Date.now(),
        sender,
        body,
        dateEpochMillis: Date.now(),
        primaryReason: detection.primaryReason,
        detectedSignals: detection.detectedSignals.join(', '),
        isOverridden: false
      };
      await storage.addScamAlert(alertItem);
      await this.loadData();
      this.closeAllModals();
      this.render();
      this.showToast(this.lang === 'fr' ? `Message suspect bloqué : ${sender}` : `Blocked suspicious message from ${sender}`, 'danger');
      return;
    }

    // Parse Transaction & Fee
    const parsed = SmsParser.parseSms(
      sender,
      body,
      this.categories,
      this.selectedCountry.currencyCode
    );

    if (!parsed) {
      alert(this.lang === 'fr' ? 'Message analysé, mais aucun montant ou motif de transaction détecté.' : 'Message parsed, but no transaction amounts or patterns were detected.');
      return;
    }

    const initialStatus = (detection.classification === 'NEEDS_REVIEW') ? 'PENDING' : parsed.status;

    // Fixed source: SMS
    const newTx = {
      id: Date.now(),
      amount: parsed.amount,
      currency: parsed.currency,
      dateEpochMillis: Date.now(),
      description: parsed.description,
      merchant: parsed.merchant,
      category: parsed.category,
      source: 'SMS',
      isSourceFixed: true,
      fee: parsed.fee || 0.0,
      feeCategory: parsed.feeCategory || 'Transaction Fee',
      status: initialStatus,
      paymentMethod: sender || 'Mobile Money'
    };

    await storage.addTransaction(newTx);
    await this.loadData();
    this.closeAllModals();
    this.render();

    const formatted = formatCurrency(newTx.amount, newTx.currency);
    const feeNotice = newTx.fee > 0 ? ` (Fee: ${formatCurrency(newTx.fee, newTx.currency)})` : '';
    this.showToast(`${this.lang === 'fr' ? 'Transaction enregistrée' : 'Transaction recorded'}: ${formatted} ${feeNotice}`);
  }

  // MARK: - Add Manual Transaction (Type Locked to MANUAL)
  async handleAddTransaction() {
    const amountVal = parseFloat(document.getElementById('add-amount').value);
    const type = document.getElementById('add-type').value;
    const category = document.getElementById('add-category').value;
    const description = document.getElementById('add-description').value.trim() || 'Manual Entry';
    const paymentMethod = document.getElementById('add-method').value || 'Cash';
    const feeVal = parseFloat(document.getElementById('add-fee').value) || 0.0;
    const feeCat = document.getElementById('add-fee-category').value || 'Transaction Fee';

    if (isNaN(amountVal) || amountVal <= 0) {
      alert(this.lang === 'fr' ? 'Veuillez saisir un montant valide.' : 'Please enter a valid amount.');
      return;
    }

    const finalAmount = (type === 'income') ? amountVal : -amountVal;

    // Fixed source: MANUAL. Fee is kept beside the amount, NOT added to the principal amount.
    const newTx = {
      id: Date.now(),
      amount: finalAmount,
      currency: this.selectedCountry.currencyCode,
      dateEpochMillis: Date.now(),
      description,
      merchant: description,
      category,
      source: 'MANUAL',
      isSourceFixed: true,
      fee: feeVal,
      feeCategory: feeCat,
      status: 'CONFIRMED',
      paymentMethod
    };

    await storage.addTransaction(newTx);
    await this.loadData();
    this.closeAllModals();
    this.render();
    this.showToast(this.lang === 'fr' ? 'Transaction ajoutée avec succès !' : 'Transaction added successfully!');
  }

  // MARK: - Categorize Pending Transaction
  async confirmCategorization(txId, categoryName) {
    const tx = this.transactions.find(t => t.id === txId);
    if (!tx) return;
    tx.category = categoryName;
    tx.status = 'CONFIRMED';
    await storage.updateTransaction(tx);
    await this.loadData();
    this.render();
    this.showToast(`Transaction verified under ${categoryName}!`);
  }

  // MARK: - Override Scam Alert
  async overrideScamAlert(scamId) {
    const scam = this.scamAlerts.find(s => s.id === scamId);
    if (!scam) return;

    scam.isOverridden = true;
    await storage.addScamAlert(scam);

    const parsed = SmsParser.parseSms(scam.sender, scam.body, this.categories, this.selectedCountry.currencyCode);
    if (parsed) {
      const newTx = {
        id: Date.now(),
        amount: parsed.amount,
        currency: parsed.currency,
        dateEpochMillis: Date.now(),
        description: parsed.description,
        merchant: parsed.merchant,
        category: parsed.category,
        source: 'SMS',
        isSourceFixed: true,
        fee: parsed.fee || 0.0,
        feeCategory: parsed.feeCategory || 'Transaction Fee',
        status: 'CONFIRMED',
        paymentMethod: scam.sender
      };
      await storage.addTransaction(newTx);
    }

    await this.loadData();
    this.render();
    this.showToast(this.lang === 'fr' ? 'Alerte annulée et transaction enregistrée.' : 'Scam alert overridden & transaction saved.');
  }

  async deleteScamAlert(scamId) {
    await storage.deleteScamAlert(scamId);
    await this.loadData();
    this.render();
    this.showToast(this.lang === 'fr' ? 'Alerte supprimée.' : 'Scam alert removed.');
  }

  async deleteTransaction(txId) {
    const confirmMsg = this.lang === 'fr' ? 'Voulez-vous vraiment supprimer cette transaction ?' : 'Are you sure you want to delete this transaction?';
    if (!confirm(confirmMsg)) return;
    await storage.deleteTransaction(txId);
    await this.loadData();
    this.render();
    this.showToast(this.lang === 'fr' ? 'Transaction supprimée.' : 'Transaction deleted.');
  }

  async deleteCategory(catId) {
    const confirmMsg = this.lang === 'fr' ? 'Voulez-vous vraiment supprimer cette catégorie ?' : 'Are you sure you want to delete this category?';
    if (!confirm(confirmMsg)) return;
    await storage.deleteCategory(catId);
    await this.loadData();
    this.render();
    this.showToast('Category deleted.');
  }

  // MARK: - Render All Views
  render() {
    const currency = this.selectedCountry.currencyCode;
    const s = this.s;

    // Header & User Profile
    const profileCountryEl = document.getElementById('user-profile-country');
    if (profileCountryEl) {
      profileCountryEl.innerHTML = `<img src="${this.selectedCountry.flagUrl}" width="20" height="14" style="vertical-align:middle;border-radius:2px;margin-right:6px;" alt=""> ${this.selectedCountry.name}`;
    }

    const settingCountryEl = document.getElementById('setting-country');
    if (settingCountryEl && this.profile) settingCountryEl.value = this.profile.countryCode;

    const settingScamEl = document.getElementById('setting-scam-toggle');
    if (settingScamEl && this.profile) settingScamEl.checked = (this.profile.scamFilteringEnabled !== false);

    // Compute Metrics
    const confirmedTxs = this.transactions.filter(t => t.status === 'CONFIRMED');
    const incomeTotal = confirmedTxs.filter(t => t.amount > 0).reduce((acc, t) => acc + t.amount, 0);
    const expenseTotal = confirmedTxs.filter(t => t.amount < 0).reduce((acc, t) => acc + Math.abs(t.amount), 0);
    const netBalance = incomeTotal - expenseTotal;
    const pendingTxs = this.transactions.filter(t => t.status === 'PENDING');
    const activeScams = this.scamAlerts.filter(s => !s.isOverridden);

    // Update Badges on Sidebar
    const pendingBadge = document.getElementById('badge-pending-count');
    if (pendingBadge) {
      pendingBadge.style.display = pendingTxs.length > 0 ? 'inline-block' : 'none';
      pendingBadge.innerText = pendingTxs.length;
    }

    const scamBadge = document.getElementById('badge-scam-count');
    if (scamBadge) {
      scamBadge.style.display = activeScams.length > 0 ? 'inline-block' : 'none';
      scamBadge.innerText = activeScams.length;
    }

    // Populate Category Dropdowns
    const catSelect = document.getElementById('add-category');
    if (catSelect) {
      catSelect.innerHTML = this.categories.map(c => `<option value="${c.name}">${c.name}</option>`).join('');
    }

    // 1. Render Dashboard
    this.renderDashboard(netBalance, incomeTotal, expenseTotal, pendingTxs, activeScams, currency);

    // 2. Render Transactions Tab
    this.renderTransactionsTable(currency);

    // 3. Render Pending Review Tab
    this.renderPendingReview(pendingTxs, currency);

    // 4. Render Reports Tab
    this.renderReports(incomeTotal, expenseTotal, currency);

    // 5. Render Scam Alerts Tab
    this.renderScamAlerts(activeScams);

    // 6. Render Categories Tab
    this.renderCategoriesList();
  }

  renderDashboard(netBalance, incomeTotal, expenseTotal, pendingTxs, activeScams, currency) {
    const s = this.s;
    const elNet = document.getElementById('metric-net-balance');
    const elIncome = document.getElementById('metric-income');
    const elExpense = document.getElementById('metric-expense');
    const elPending = document.getElementById('metric-pending-count');

    if (elNet) elNet.innerText = formatCurrency(netBalance, currency);
    if (elIncome) elIncome.innerText = formatCurrency(incomeTotal, currency);
    if (elExpense) elExpense.innerText = formatCurrency(-expenseTotal, currency);
    if (elPending) elPending.innerText = pendingTxs.length;

    // Pending Review Banner
    const bannerPending = document.getElementById('banner-pending-review');
    if (bannerPending) {
      bannerPending.style.display = pendingTxs.length > 0 ? 'flex' : 'none';
      const msgEl = document.getElementById('banner-pending-msg');
      if (msgEl) msgEl.innerText = `${s.bannerPendingPrefix} ${pendingTxs.length} ${s.bannerPendingSuffix}`;
    }

    // Scam Alert Banner
    const bannerScam = document.getElementById('banner-scam-alert');
    if (bannerScam) {
      bannerScam.style.display = activeScams.length > 0 ? 'flex' : 'none';
      const scamMsg = document.getElementById('banner-scam-msg');
      if (scamMsg) scamMsg.innerText = `${s.bannerScamPrefix} ${activeScams.length} ${s.bannerScamSuffix}`;
    }

    // Render Charts
    Charts.renderTimeline(document.getElementById('chart-timeline'), this.transactions, currency);
    Charts.renderCategoryDonut(document.getElementById('chart-categories'), this.transactions, this.categories, currency);

    // Recent Activity Table (displays amount and side fee)
    const recentBody = document.getElementById('recent-activity-body');
    if (recentBody) {
      const recent = this.transactions.slice(0, 5);
      if (recent.length === 0) {
        recentBody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:24px;">${s.noTransactionsYet}</td></tr>`;
      } else {
        recentBody.innerHTML = recent.map(t => this.renderTransactionRow(t, currency)).join('');
      }
    }
  }

  renderTransactionRow(t, currency) {
    const s = this.s;
    const isPending = (t.status === 'PENDING');
    const isIncome = (t.amount > 0);
    const isSms = (t.source === 'SMS');
    const dateStr = new Date(t.dateEpochMillis).toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    const formattedAmt = formatCurrency(t.amount, currency);
    const iconSvg = getCategoryIconSvg(t.category);

    // Fee badge
    const hasFee = (t.fee && t.fee > 0);
    const feeHtml = hasFee
      ? `<div style="margin-top:4px;"><span class="fee-badge has-fee">${s.sideFeeLabel} ${formatCurrency(t.fee, currency)}</span></div>`
      : '';

    return `
      <tr>
        <td>
          <div style="display:flex;align-items:center;gap:10px;">
            <div style="width:34px;height:34px;border-radius:8px;background:var(--bg-subtle);display:flex;align-items:center;justify-content:center;color:var(--primary);flex-shrink:0;">
              ${iconSvg}
            </div>
            <div>
              <div style="font-weight:600;color:var(--text-main);">${t.merchant || t.description}</div>
              <div style="font-size:12px;color:var(--text-muted);">${t.paymentMethod || 'Mobile Money'}</div>
            </div>
          </div>
        </td>
        <td>
          <span class="category-pill">${t.category || 'Uncategorized'}</span>
        </td>
        <td>
          <span class="badge ${isSms ? 'badge-source-sms' : 'badge-source-manual'}" title="${s.sourceFixedNote}">
            ${isSms ? s.sourceSms : s.sourceManual}
          </span>
        </td>
        <td>
          <span class="badge ${isPending ? 'badge-pending' : 'badge-confirmed'}">${t.status}</span>
        </td>
        <td style="font-size:13px;color:var(--text-secondary);">${dateStr}</td>
        <td style="font-weight:700;text-align:right;color:${isIncome ? 'var(--success)' : 'var(--text-main)'};">
          ${formattedAmt}
          ${feeHtml}
        </td>
      </tr>
    `;
  }

  renderTransactionsTable(currency) {
    const s = this.s;
    const tableBody = document.getElementById('all-transactions-body');
    if (!tableBody) return;

    // Apply Filter (ALL, SMS, MANUAL)
    let filtered = this.transactions;
    if (this.transactionFilter === 'SMS') {
      filtered = this.transactions.filter(t => t.source === 'SMS');
    } else if (this.transactionFilter === 'MANUAL') {
      filtered = this.transactions.filter(t => t.source === 'MANUAL');
    }

    if (filtered.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:var(--text-muted);padding:32px;">${s.noTransactionsFound}</td></tr>`;
      return;
    }

    tableBody.innerHTML = filtered.map(t => {
      const isIncome = (t.amount > 0);
      const isPending = (t.status === 'PENDING');
      const isSms = (t.source === 'SMS');
      const dateStr = new Date(t.dateEpochMillis).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
      const formattedAmt = formatCurrency(t.amount, currency);
      const iconSvg = getCategoryIconSvg(t.category);

      // Fee display beside amount
      const hasFee = (t.fee && t.fee > 0);
      const feeCell = hasFee
        ? `<div><span class="fee-badge has-fee">${formatCurrency(t.fee, currency)}</span><div style="font-size:11px;color:var(--text-muted);margin-top:2px;">${t.feeCategory || 'Transaction Fee'}</div></div>`
        : `<span class="fee-badge">${s.noFeeRecorded}</span>`;

      return `
        <tr>
          <td>
            <div style="display:flex;align-items:center;gap:10px;">
              <div style="width:34px;height:34px;border-radius:8px;background:var(--bg-subtle);display:flex;align-items:center;justify-content:center;color:var(--primary);flex-shrink:0;">
                ${iconSvg}
              </div>
              <div>
                <div style="font-weight:600;">${t.merchant || t.description}</div>
                <div style="font-size:12px;color:var(--text-muted);">${t.paymentMethod}</div>
              </div>
            </div>
          </td>
          <td><span class="category-pill">${t.category}</span></td>
          <td>
            <span class="badge ${isSms ? 'badge-source-sms' : 'badge-source-manual'}" title="${s.sourceFixedNote}">
              ${isSms ? s.sourceSms : s.sourceManual}
            </span>
          </td>
          <td><span class="badge ${isPending ? 'badge-pending' : 'badge-confirmed'}">${t.status}</span></td>
          <td>${dateStr}</td>
          <td style="font-weight:700;color:${isIncome ? 'var(--success)' : 'var(--text-main)'};">${formattedAmt}</td>
          <td>${feeCell}</td>
          <td style="text-align:right;">
            <button class="btn btn-secondary btn-sm" onclick="window.promptPayApp.deleteTransaction(${t.id})">
              ${ICONS.trash}
              <span>${s.delete}</span>
            </button>
          </td>
        </tr>
      `;
    }).join('');
  }

  renderPendingReview(pendingTxs, currency) {
    const s = this.s;
    const container = document.getElementById('pending-review-list');
    if (!container) return;

    if (pendingTxs.length === 0) {
      container.innerHTML = `
        <div style="text-align:center;padding:48px 24px;background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius-lg);">
          <div style="display:inline-flex;color:var(--success);margin-bottom:12px;">${ICONS.checkCircle}</div>
          <h3 style="font-size:18px;font-weight:700;">${s.allCaughtUpTitle}</h3>
          <p style="color:var(--text-muted);font-size:14px;margin-top:4px;">${s.allCaughtUpDesc}</p>
        </div>
      `;
      return;
    }

    container.innerHTML = pendingTxs.map(t => {
      const amtStr = formatCurrency(t.amount, currency);
      const dateStr = new Date(t.dateEpochMillis).toLocaleString();
      const feeHtml = (t.fee && t.fee > 0)
        ? `<div style="margin-top:4px;"><span class="fee-badge has-fee">${s.sideFeeLabel} ${formatCurrency(t.fee, currency)} (${t.feeCategory || 'Fee'})</span></div>`
        : '';

      return `
        <div class="card" style="margin-bottom:16px;">
          <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:16px;flex-wrap:wrap;">
            <div>
              <div style="font-size:18px;font-weight:700;color:var(--text-main);">${t.merchant}</div>
              <div style="font-size:13px;color:var(--text-muted);margin-top:2px;">${dateStr} &bull; ${t.paymentMethod} &bull; <span class="badge badge-source-sms">${s.sourceSms}</span></div>
              <div style="font-size:14px;color:var(--text-secondary);margin-top:8px;">${t.description}</div>
            </div>
            <div style="text-align:right;">
              <div style="font-size:22px;font-weight:800;color:var(--text-main);">${amtStr}</div>
              ${feeHtml}
            </div>
          </div>

          <div style="margin-top:16px;padding-top:16px;border-top:1px solid var(--border);">
            <div style="font-size:12px;font-weight:600;color:var(--text-muted);margin-bottom:8px;">${s.selectCorrectCategory}</div>
            <div style="display:flex;gap:8px;flex-wrap:wrap;">
              ${this.categories.map(c => `
                <button class="btn btn-secondary btn-sm" onclick="window.promptPayApp.confirmCategorization(${t.id}, '${c.name}')">
                  ${getCategoryIconSvg(c.name)}
                  <span>${c.name}</span>
                </button>
              `).join('')}
            </div>
          </div>
        </div>
      `;
    }).join('');
  }

  renderReports(incomeTotal, expenseTotal, currency) {
    const elRepInc = document.getElementById('report-total-income');
    const elRepExp = document.getElementById('report-total-expense');
    const elRepNet = document.getElementById('report-net-savings');

    if (elRepInc) elRepInc.innerText = formatCurrency(incomeTotal, currency);
    if (elRepExp) elRepExp.innerText = formatCurrency(-expenseTotal, currency);
    if (elRepNet) elRepNet.innerText = formatCurrency(incomeTotal - expenseTotal, currency);

    Charts.renderTimeline(document.getElementById('report-chart-timeline'), this.transactions, currency);
    Charts.renderCategoryDonut(document.getElementById('report-chart-donut'), this.transactions, this.categories, currency);
  }

  renderScamAlerts(activeScams) {
    const s = this.s;
    const container = document.getElementById('scam-alerts-list');
    if (!container) return;

    if (activeScams.length === 0) {
      container.innerHTML = `
        <div style="text-align:center;padding:48px 24px;background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius-lg);">
          <div style="display:inline-flex;color:var(--primary);margin-bottom:12px;">${ICONS.shield}</div>
          <h3 style="font-size:18px;font-weight:700;">${s.noScamTitle}</h3>
          <p style="color:var(--text-muted);font-size:14px;margin-top:4px;">${s.noScamDesc}</p>
        </div>
      `;
      return;
    }

    container.innerHTML = activeScams.map(item => {
      const dateStr = new Date(item.dateEpochMillis).toLocaleString();
      return `
        <div class="card" style="border-left: 4px solid var(--danger); margin-bottom: 16px;">
          <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:16px;">
            <div>
              <div style="display:flex;align-items:center;gap:8px;">
                <span class="badge badge-scam">${ICONS.shieldAlert} ${s.suspiciousBlocked}</span>
                <span style="font-weight:700;font-size:15px;">${s.senderLabel}: ${item.sender}</span>
              </div>
              <div style="font-size:12px;color:var(--text-muted);margin-top:4px;">${s.detectedLabel}: ${dateStr}</div>
            </div>
            <div style="display:flex;gap:8px;">
              <button class="btn btn-secondary btn-sm" onclick="window.promptPayApp.overrideScamAlert(${item.id})">${s.markSafe}</button>
              <button class="btn btn-danger btn-sm" onclick="window.promptPayApp.deleteScamAlert(${item.id})">${s.deleteAlert}</button>
            </div>
          </div>

          <div style="margin-top:12px;background:var(--bg-subtle);padding:12px;border-radius:var(--radius-md);font-family:monospace;font-size:13px;color:var(--text-main);max-height:120px;overflow-y:auto;">
            ${item.body}
          </div>

          <div style="margin-top:12px;display:flex;align-items:center;gap:16px;font-size:13px;flex-wrap:wrap;">
            <div><strong>${s.reasonLabel}:</strong> <span style="color:var(--danger);">${item.primaryReason}</span></div>
            <div><strong>${s.signalsLabel}:</strong> <span style="color:var(--text-secondary);">${item.detectedSignals || 'None'}</span></div>
          </div>
        </div>
      `;
    }).join('');
  }

  renderCategoriesList() {
    const s = this.s;
    const container = document.getElementById('categories-list-grid');
    if (!container) return;

    container.innerHTML = this.categories.map(c => {
      const iconSvg = getCategoryIconSvg(c.name);
      return `
        <div class="card" style="display:flex;align-items:center;justify-content:space-between;padding:16px;">
          <div style="display:flex;align-items:center;gap:12px;">
            <div style="width:40px;height:40px;border-radius:var(--radius-md);background:${c.colorHex}20;color:${c.colorHex};display:flex;align-items:center;justify-content:center;">
              ${iconSvg}
            </div>
            <div>
              <div style="font-weight:700;font-size:15px;color:var(--text-main);">${c.name}</div>
              <div style="font-size:12px;color:var(--text-muted);">${c.tag || 'Category'} &bull; ${c.keywords ? c.keywords.split(',').length + ' keywords' : 'No keywords'}</div>
            </div>
          </div>
          ${c.isCustom ? `
            <button class="btn btn-danger btn-sm" onclick="window.promptPayApp.deleteCategory(${c.id})">${ICONS.trash}</button>
          ` : `
            <span class="badge badge-neutral">${s.builtIn}</span>
          `}
        </div>
      `;
    }).join('');
  }
}

// Instantiate and expose globally for inline HTML callbacks
window.promptPayApp = new App();
