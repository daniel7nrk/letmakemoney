// PromptPay Web Application Controller & Router (Rapport Design System Edition)
import { storage } from './services/StorageService.js';
import { SmsParser } from './services/SmsParser.js';
import { ScamDetector } from './services/ScamDetector.js';
import { Charts } from './components/Charts.js';
import { ICONS, getCategoryIconSvg } from './components/Icons.js';
import { COUNTRIES, formatCurrency, getCountryByCurrency } from './models/Country.js';
import { getStrings } from './util/AppStrings.js';
import { DEFAULT_CATEGORIES, INITIAL_TRANSACTIONS, INITIAL_SCAM_ALERTS } from './models/DefaultData.js';

class App {
  constructor() {
    this.currentTab = 'dashboard';
    this.currentSubtab = 'data';
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
    this.initTheme();
    await storage.ready;
    await this.loadData();
    this.setupEventListeners();
    this.applyTranslations();
    this.render();
    this.registerServiceWorker();
  }

  initTheme() {
    const savedTheme = localStorage.getItem('promptpay_theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
  }

  toggleTheme() {
    const curr = document.documentElement.getAttribute('data-theme') || 'light';
    const next = curr === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('promptpay_theme', next);
    this.showToast(this.lang === 'fr' ? `Thème ${next === 'dark' ? 'sombre' : 'clair'} activé` : `Switched to ${next} mode`);
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
    const setTxt = (id, txt) => {
      const el = document.getElementById(id);
      if (el) el.innerText = txt;
    };

    // Navigation Labels
    setTxt('tab-nav-dashboard', s.navDashboard);
    setTxt('tab-nav-transactions', s.navTransactions);
    setTxt('tab-nav-pending', s.navPendingReview);
    setTxt('tab-nav-reports', s.navReports);
    setTxt('tab-nav-scams', s.navScamAlerts);
    setTxt('tab-nav-categories', s.navCategories);
    setTxt('tab-nav-settings', s.navSettings);

    // Header buttons
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
    setTxt('lbl-setting-language', s.languageSetting);
    setTxt('lbl-setting-lang-desc', s.languageDesc);
    setTxt('lbl-scam-filter-title', s.scamFilterHeading);
    setTxt('lbl-scam-filter-desc', s.scamFilterDesc);
    setTxt('lbl-security-title', s.securityHeading);
    setTxt('lbl-security-desc', s.securityDesc);
    setTxt('lbl-enter-pin', s.enterPinLabel);
    setTxt('btn-save-pin', s.btnSavePin);

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
    // Top Tabs navigation
    document.querySelectorAll('.tabs .tab').forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const route = item.dataset.route;
        if (route) this.switchTab(route);
      });
    });

    // Left Floating Dock navigation
    document.querySelectorAll('.dock .dock__btn[data-route]').forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const route = item.dataset.route;
        if (route) this.switchTab(route);
      });
    });

    // Dock Add Transaction Button
    document.querySelectorAll('[data-act="add-tx"]').forEach(btn => {
      btn.addEventListener('click', () => this.openModal('modal-add-transaction'));
    });

    // Theme Switcher Button
    const btnTheme = document.getElementById('btn-theme-toggle');
    if (btnTheme) btnTheme.addEventListener('click', () => this.toggleTheme());

    // Top Header Buttons
    const btnSimulate = document.getElementById('btn-simulate-sms');
    if (btnSimulate) btnSimulate.addEventListener('click', () => this.openModal('modal-simulate-sms'));

    const btnQuickSim = document.getElementById('btn-quick-sim');
    if (btnQuickSim) btnQuickSim.addEventListener('click', () => this.openModal('modal-simulate-sms'));

    const btnAddTx = document.getElementById('btn-add-transaction');
    if (btnAddTx) btnAddTx.addEventListener('click', () => this.openModal('modal-add-transaction'));

    const btnQuickScam = document.getElementById('btn-quick-scam');
    if (btnQuickScam) btnQuickScam.addEventListener('click', () => this.switchTab('scam-alerts'));

    const btnAvatar = document.getElementById('btn-user-avatar');
    if (btnAvatar) btnAvatar.addEventListener('click', () => this.switchTab('settings'));

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

    // Settings Sub-Tabs Navigation (.set-tab)
    document.querySelectorAll('.set-tab').forEach(tabBtn => {
      tabBtn.addEventListener('click', () => {
        document.querySelectorAll('.set-tab').forEach(b => {
          b.classList.remove('active');
          b.setAttribute('aria-selected', 'false');
        });
        tabBtn.classList.add('active');
        tabBtn.setAttribute('aria-selected', 'true');

        const subtab = tabBtn.dataset.subtab;
        this.currentSubtab = subtab;
        document.querySelectorAll('.set-panel').forEach(p => p.style.display = 'none');
        const activePanel = document.getElementById(`set-panel-${subtab}`);
        if (activePanel) activePanel.style.display = 'block';
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

    // MoMo Scam Filtering Switch (.switch)
    const switchScam = document.getElementById('switch-scam-toggle');
    if (switchScam) {
      switchScam.setAttribute('aria-checked', String(this.profile.scamFilteringEnabled !== false));
      switchScam.addEventListener('click', async () => {
        const current = switchScam.getAttribute('aria-checked') === 'true';
        const next = !current;
        switchScam.setAttribute('aria-checked', String(next));
        this.profile.scamFilteringEnabled = next;
        await storage.saveUserProfile(this.profile);
        this.showToast(`MoMo Scam Defense ${next ? 'Enabled' : 'Disabled'}`);
      });
    }

    // Security PIN Lock Switch (.switch)
    const switchPin = document.getElementById('switch-pin-toggle');
    const pinBox = document.getElementById('pin-setup-box');
    if (switchPin) {
      const isPinLocked = (this.profile && this.profile.lockMode === 'PIN');
      switchPin.setAttribute('aria-checked', String(isPinLocked));
      if (pinBox) pinBox.style.display = isPinLocked ? 'block' : 'none';

      switchPin.addEventListener('click', async () => {
        const current = switchPin.getAttribute('aria-checked') === 'true';
        const next = !current;
        switchPin.setAttribute('aria-checked', String(next));
        if (pinBox) pinBox.style.display = next ? 'block' : 'none';
        if (!next) {
          this.profile.lockMode = 'NONE';
          await storage.saveUserProfile(this.profile);
          this.showToast('PIN Lock Disabled');
        }
      });
    }

    // Save PIN Button
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
        this.profile.pinHash = btoa(val);
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

    // Save Profile Form in Settings
    const btnSaveProf = document.getElementById('btn-save-profile');
    if (btnSaveProf) {
      btnSaveProf.addEventListener('click', async () => {
        const nameVal = document.getElementById('input-prof-name').value.trim();
        const emailVal = document.getElementById('input-prof-email').value.trim();
        const budgetVal = parseFloat(document.getElementById('input-prof-budget').value) || 50000;
        const roleVal = document.getElementById('input-prof-role').value.trim() || 'Mobile Money User';

        if (nameVal) this.profile.name = nameVal;
        this.profile.email = emailVal;
        this.profile.monthlyBudget = budgetVal;
        this.profile.role = roleVal;
        await storage.saveUserProfile(this.profile);
        this.render();
        this.showToast(this.lang === 'fr' ? 'Profil mis à jour !' : 'Profile updated!');
      });
    }

    // Export CSV and JSON buttons
    const btnExportTxCsv = document.getElementById('btn-export-tx-csv');
    if (btnExportTxCsv) btnExportTxCsv.addEventListener('click', () => this.exportCsv('transactions'));

    const btnExportTxLogCsv = document.getElementById('btn-export-tx-log-csv');
    if (btnExportTxLogCsv) btnExportTxLogCsv.addEventListener('click', () => this.exportCsv('transactions'));

    const btnReportExportCsv = document.getElementById('btn-report-export-csv');
    if (btnReportExportCsv) btnReportExportCsv.addEventListener('click', () => this.exportCsv('transactions'));

    const btnReportExportJson = document.getElementById('btn-report-export-json');
    if (btnReportExportJson) btnReportExportJson.addEventListener('click', () => storage.exportBackup());

    const btnExportCatsCsv = document.getElementById('btn-export-cats-csv');
    if (btnExportCatsCsv) btnExportCatsCsv.addEventListener('click', () => this.exportCsv('categories'));

    const btnDownloadJson = document.getElementById('btn-download-json-backup');
    if (btnDownloadJson) btnDownloadJson.addEventListener('click', () => storage.exportBackup());

    const btnExportBackup = document.getElementById('btn-export-backup');
    if (btnExportBackup) btnExportBackup.addEventListener('click', () => storage.exportBackup());

    // Import JSON backup
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

    // Reset Demo Data (The red Danger Zone action from the screenshot)
    const btnResetData = document.getElementById('btn-reset-demo-data');
    if (btnResetData) {
      btnResetData.addEventListener('click', async () => {
        const confirmMsg = this.lang === 'fr'
          ? 'Réinitialiser toutes les données ? Toutes vos transactions et réglages reviendront à l\'état initial.'
          : 'Reset all demo data? Every transaction, category, and setting will return to the starting demo data.';
        if (!confirm(confirmMsg)) return;

        localStorage.clear();
        await storage.put('settings', {
          key: 'user_profile',
          name: 'Jane Doe',
          countryCode: 'RWANDA',
          currencyCode: 'RWF',
          scamFilteringEnabled: true,
          languageCode: this.lang,
          monthlyBudget: 50000,
          role: 'Mobile Money User'
        });

        for (const cat of DEFAULT_CATEGORIES) await storage.put('categories', cat);
        for (const tx of INITIAL_TRANSACTIONS) await storage.put('transactions', tx);
        for (const s of INITIAL_SCAM_ALERTS) await storage.put('scam_alerts', s);

        await this.loadData();
        this.render();
        this.showToast(this.lang === 'fr' ? 'Données réinitialisées !' : 'Demo data reset successfully!');
      });
    }

    // Add Custom Category Form
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

  // MARK: - Tab Switching
  switchTab(tabId) {
    this.currentTab = tabId;

    // Update Top Nav Tabs
    document.querySelectorAll('.tabs .tab').forEach(t => {
      t.classList.toggle('active', t.dataset.route === tabId);
    });

    // Update Dock Buttons
    document.querySelectorAll('.dock .dock__btn[data-route]').forEach(b => {
      b.classList.toggle('active', b.dataset.route === tabId);
      if (b.dataset.route === tabId) {
        b.setAttribute('aria-current', 'page');
      } else {
        b.removeAttribute('aria-current');
      }
    });

    // Update Active View
    document.querySelectorAll('.view').forEach(v => {
      v.classList.toggle('active', v.dataset.view === tabId);
    });

    // Update Head Lead Title
    const headTitle = document.getElementById('page-head-title');
    const s = this.s;
    if (headTitle) {
      const titles = {
        'dashboard': `Workspace<br><span style="font-weight:400;color:var(--muted);">${s.navDashboard}</span>`,
        'transactions': `All Transactions<br><span style="font-weight:400;color:var(--muted);">Log & Side Fees</span>`,
        'pending-review': `Pending Review<br><span style="font-weight:400;color:var(--muted);">${s.navPendingReview}</span>`,
        'reports': `Financial Reports<br><span style="font-weight:400;color:var(--muted);">${s.navReports}</span>`,
        'scam-alerts': `MoMo Fraud Defense<br><span style="font-weight:400;color:var(--muted);">Heuristic Shield</span>`,
        'categories': `Budget Categories<br><span style="font-weight:400;color:var(--muted);">${s.navCategories}</span>`,
        'settings': `Workspace<br><span style="font-weight:400;color:var(--muted);">${s.navSettings}</span>`
      };
      headTitle.innerHTML = titles[tabId] || titles.dashboard;
    }

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
    const toast = document.getElementById('toast-container');
    const msg = document.getElementById('toast-msg');
    if (!toast || !msg) return;
    msg.innerText = message;
    toast.style.display = 'flex';
    toast.style.visibility = 'visible';
    setTimeout(() => {
      toast.style.display = 'none';
      toast.style.visibility = 'hidden';
    }, 3500);
  }

  // MARK: - Simulate SMS
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
    this.showToast(`${this.lang === 'fr' ? 'Transaction enregistrée' : 'Transaction recorded'}: ${formatted}${feeNotice}`);
  }

  // MARK: - Add Manual Transaction
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

    // Fixed source: MANUAL. Fee is kept beside the amount, NOT added to principal.
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

  exportCsv(type) {
    let csv = '';
    let filename = '';
    if (type === 'transactions') {
      csv = 'Date,Merchant,Category,Source,Status,Amount,Currency,Fee,FeeCategory\n';
      this.transactions.forEach(t => {
        const d = new Date(t.dateEpochMillis).toISOString();
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

  // MARK: - Render All Views
  render() {
    const currency = this.selectedCountry.currencyCode;
    const s = this.s;

    // Header & User Profile
    const avatarInitials = (this.profile.name || 'Jane Doe').split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
    const avatarEls = [document.getElementById('user-avatar-text'), document.getElementById('profile-card-avatar')];
    avatarEls.forEach(el => { if (el) el.innerText = avatarInitials; });

    const profNameEl = document.getElementById('profile-card-name');
    if (profNameEl) profNameEl.innerText = this.profile.name || 'Jane Doe';

    const profEmailEl = document.getElementById('profile-card-email');
    if (profEmailEl) profEmailEl.innerText = this.profile.email || `${(this.profile.name || 'jane').toLowerCase().replace(/\s+/g, '.')}@promptpay.app`;

    const profRoleEl = document.getElementById('profile-card-role');
    if (profRoleEl) profRoleEl.innerText = `${this.selectedCountry.name} · ${this.profile.role || 'Mobile Money User'}`;

    // Selects in Settings
    const settingCountryEl = document.getElementById('setting-country');
    if (settingCountryEl && this.profile) settingCountryEl.value = this.profile.countryCode;

    const settingLanguageEl = document.getElementById('setting-language');
    if (settingLanguageEl && this.profile) settingLanguageEl.value = this.profile.languageCode || 'en';

    // Compute Metrics
    const confirmedTxs = this.transactions.filter(t => t.status === 'CONFIRMED');
    const incomeTotal = confirmedTxs.filter(t => t.amount > 0).reduce((acc, t) => acc + t.amount, 0);
    const expenseTotal = confirmedTxs.filter(t => t.amount < 0).reduce((acc, t) => acc + Math.abs(t.amount), 0);
    const netBalance = incomeTotal - expenseTotal;
    const pendingTxs = this.transactions.filter(t => t.status === 'PENDING');
    const activeScams = this.scamAlerts.filter(s => !s.isOverridden);

    // Head Stats
    const factCats = document.getElementById('stat-fact-categories');
    if (factCats) factCats.innerText = this.categories.length;

    const factAlerts = document.getElementById('stat-fact-alerts');
    if (factAlerts) factAlerts.innerText = (this.profile.scamFilteringEnabled !== false) ? 'Active' : 'Off';

    // Bell badge
    const bellBadge = document.getElementById('bell-badge');
    if (bellBadge) bellBadge.style.display = activeScams.length > 0 ? 'block' : 'none';

    // Settings Profile Card: Quota / Budget Gauge
    const budgetTotal = this.profile.monthlyBudget || 50000;
    const budgetPct = Math.min(100, Math.round((expenseTotal / budgetTotal) * 100));
    const budgetPctEl = document.getElementById('stat-budget-pct');
    if (budgetPctEl) budgetPctEl.innerText = `${budgetPct}%`;

    const budgetMeterFill = document.querySelector('.set-me__quota .meter__fill');
    if (budgetMeterFill) budgetMeterFill.style.width = `${budgetPct}%`;

    const spentAmtEl = document.getElementById('stat-spent-amt');
    if (spentAmtEl) spentAmtEl.innerText = `${formatCurrency(expenseTotal, currency)} spent of ${formatCurrency(budgetTotal, currency)}`;

    // Mini Facts on Profile Card
    const countTxEl = document.getElementById('stat-count-tx');
    if (countTxEl) countTxEl.innerText = this.transactions.length;

    const countCatsEl = document.getElementById('stat-count-cats');
    if (countCatsEl) countCatsEl.innerText = this.categories.length;

    const countPendingEl = document.getElementById('stat-count-pending');
    if (countPendingEl) countPendingEl.innerText = pendingTxs.length;

    // Latest Transaction Tile
    const latestTx = this.transactions[0];
    if (latestTx) {
      const nextTitle = document.getElementById('lbl-next-tx-desc');
      if (nextTitle) nextTitle.innerText = latestTx.merchant || latestTx.description;
      const nextAmt = document.getElementById('lbl-next-tx-amt');
      if (nextAmt) nextAmt.innerText = `${formatCurrency(latestTx.amount, currency)} · ${latestTx.source}`;
    }

    // Data Tab Counts
    const dataTxCount = document.getElementById('txt-tx-count');
    if (dataTxCount) dataTxCount.innerText = this.transactions.length;

    const dataCatsCount = document.getElementById('txt-cats-count');
    if (dataCatsCount) dataCatsCount.innerText = this.categories.length;

    // Populate Category Dropdown in Modals
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

    // Recent Activity Table (shows amount and side fee)
    const recentBody = document.getElementById('recent-activity-body');
    if (recentBody) {
      const recent = this.transactions.slice(0, 5);
      if (recent.length === 0) {
        recentBody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--muted);padding:24px;">${s.noTransactionsYet}</td></tr>`;
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

    const hasFee = (t.fee && t.fee > 0);
    const feeHtml = hasFee
      ? `<div style="margin-top:4px;"><span class="fee-badge has-fee">${s.sideFeeLabel} ${formatCurrency(t.fee, currency)}</span></div>`
      : '';

    return `
      <tr>
        <td>
          <div style="display:flex;align-items:center;gap:12px;">
            <div style="width:36px;height:36px;border-radius:12px;background:var(--well);display:grid;place-items:center;color:var(--text);flex-shrink:0;">
              ${iconSvg}
            </div>
            <div>
              <div style="font-weight:600;color:var(--text);">${t.merchant || t.description}</div>
              <div style="font-size:12px;color:var(--muted);">${t.paymentMethod || 'Mobile Money'}</div>
            </div>
          </div>
        </td>
        <td><span class="badge badge--plain">${t.category || 'Uncategorized'}</span></td>
        <td>
          <span class="${isSms ? 'badge--sms' : 'badge--manual'}" title="${s.sourceFixedNote}">
            ${isSms ? s.sourceSms : s.sourceManual}
          </span>
        </td>
        <td>
          <span class="status ${isPending ? 'status--due' : 'status--paid'}">${t.status}</span>
        </td>
        <td style="font-size:13px;color:var(--muted);">${dateStr}</td>
        <td style="font-weight:700;text-align:right;color:${isIncome ? 'var(--good)' : 'var(--text)'};">
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

    let filtered = this.transactions;
    if (this.transactionFilter === 'SMS') {
      filtered = this.transactions.filter(t => t.source === 'SMS');
    } else if (this.transactionFilter === 'MANUAL') {
      filtered = this.transactions.filter(t => t.source === 'MANUAL');
    }

    if (filtered.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:var(--muted);padding:32px;">${s.noTransactionsFound}</td></tr>`;
      return;
    }

    tableBody.innerHTML = filtered.map(t => {
      const isIncome = (t.amount > 0);
      const isPending = (t.status === 'PENDING');
      const isSms = (t.source === 'SMS');
      const dateStr = new Date(t.dateEpochMillis).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
      const formattedAmt = formatCurrency(t.amount, currency);
      const iconSvg = getCategoryIconSvg(t.category);

      const hasFee = (t.fee && t.fee > 0);
      const feeCell = hasFee
        ? `<div><span class="fee-badge has-fee">${formatCurrency(t.fee, currency)}</span><div style="font-size:11px;color:var(--muted);margin-top:2px;">${t.feeCategory || 'Transaction Fee'}</div></div>`
        : `<span class="fee-badge">${s.noFeeRecorded}</span>`;

      return `
        <tr>
          <td>
            <div style="display:flex;align-items:center;gap:12px;">
              <div style="width:36px;height:36px;border-radius:12px;background:var(--well);display:grid;place-items:center;color:var(--text);flex-shrink:0;">
                ${iconSvg}
              </div>
              <div>
                <div style="font-weight:600;">${t.merchant || t.description}</div>
                <div style="font-size:12px;color:var(--muted);">${t.paymentMethod}</div>
              </div>
            </div>
          </td>
          <td><span class="badge badge--plain">${t.category}</span></td>
          <td>
            <span class="${isSms ? 'badge--sms' : 'badge--manual'}" title="${s.sourceFixedNote}">
              ${isSms ? s.sourceSms : s.sourceManual}
            </span>
          </td>
          <td><span class="status ${isPending ? 'status--due' : 'status--paid'}">${t.status}</span></td>
          <td style="font-size:13px;color:var(--muted);">${dateStr}</td>
          <td style="font-weight:700;color:${isIncome ? 'var(--good)' : 'var(--text)'};">${formattedAmt}</td>
          <td>${feeCell}</td>
          <td style="text-align:right;">
            <button class="btn btn--ghost btn--sm" onclick="window.promptPayApp.deleteTransaction(${t.id})">
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
        <div class="card" style="text-align:center;padding:48px 24px;">
          <div style="display:inline-flex;color:var(--good);margin-bottom:12px;">${ICONS.checkCircle}</div>
          <h3 style="font-size:18px;font-weight:700;">${s.allCaughtUpTitle}</h3>
          <p class="card__sub" style="margin-top:4px;">${s.allCaughtUpDesc}</p>
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
              <div style="font-size:18px;font-weight:700;color:var(--text);">${t.merchant}</div>
              <div style="font-size:13px;color:var(--muted);margin-top:2px;">${dateStr} &bull; ${t.paymentMethod} &bull; <span class="badge--sms">${s.sourceSms}</span></div>
              <div style="font-size:14px;color:var(--text);margin-top:8px;">${t.description}</div>
            </div>
            <div style="text-align:right;">
              <div style="font-size:22px;font-weight:800;color:var(--text);">${amtStr}</div>
              ${feeHtml}
            </div>
          </div>

          <div style="margin-top:16px;padding-top:16px;border-top:1px solid var(--line);">
            <div style="font-size:12px;font-weight:600;color:var(--muted);margin-bottom:8px;">${s.selectCorrectCategory}</div>
            <div style="display:flex;gap:8px;flex-wrap:wrap;">
              ${this.categories.map(c => `
                <button class="btn btn--ghost btn--sm" onclick="window.promptPayApp.confirmCategorization(${t.id}, '${c.name}')">
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
        <div class="card" style="text-align:center;padding:48px 24px;">
          <div style="display:inline-flex;color:var(--pine);margin-bottom:12px;">${ICONS.shield}</div>
          <h3 style="font-size:18px;font-weight:700;">${s.noScamTitle}</h3>
          <p class="card__sub" style="margin-top:4px;">${s.noScamDesc}</p>
        </div>
      `;
      return;
    }

    container.innerHTML = activeScams.map(item => {
      const dateStr = new Date(item.dateEpochMillis).toLocaleString();
      return `
        <div class="card" style="border-left: 4px solid var(--tomato); margin-bottom: 16px;">
          <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:16px;">
            <div>
              <div style="display:flex;align-items:center;gap:8px;">
                <span class="badge badge--tomato">${ICONS.shieldAlert} ${s.suspiciousBlocked}</span>
                <span style="font-weight:700;font-size:15px;">${s.senderLabel}: ${item.sender}</span>
              </div>
              <div style="font-size:12px;color:var(--muted);margin-top:4px;">${s.detectedLabel}: ${dateStr}</div>
            </div>
            <div style="display:flex;gap:8px;">
              <button class="btn btn--ghost btn--sm" onclick="window.promptPayApp.overrideScamAlert(${item.id})">${s.markSafe}</button>
              <button class="btn btn--danger btn--sm" onclick="window.promptPayApp.deleteScamAlert(${item.id})">${s.deleteAlert}</button>
            </div>
          </div>

          <div style="margin-top:12px;background:var(--well);padding:12px;border-radius:12px;font-family:monospace;font-size:13px;color:var(--text);max-height:120px;overflow-y:auto;">
            ${item.body}
          </div>

          <div style="margin-top:12px;display:flex;align-items:center;gap:16px;font-size:13px;flex-wrap:wrap;">
            <div><strong>${s.reasonLabel}:</strong> <span style="color:var(--tomato);">${item.primaryReason}</span></div>
            <div><strong>${s.signalsLabel}:</strong> <span style="color:var(--muted);">${item.detectedSignals || 'None'}</span></div>
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
            <div style="width:40px;height:40px;border-radius:14px;background:${c.colorHex}22;color:${c.colorHex};display:grid;place-items:center;">
              ${iconSvg}
            </div>
            <div>
              <div style="font-weight:700;font-size:15px;color:var(--text);">${c.name}</div>
              <div style="font-size:12px;color:var(--muted);">${c.tag || 'Category'} &bull; ${c.keywords ? c.keywords.split(',').length + ' keywords' : 'No keywords'}</div>
            </div>
          </div>
          ${c.isCustom ? `
            <button class="btn btn--danger btn--sm" onclick="window.promptPayApp.deleteCategory(${c.id})">${ICONS.trash}</button>
          ` : `
            <span class="badge badge--plain">${s.builtIn}</span>
          `}
        </div>
      `;
    }).join('');
  }
}

// Instantiate and expose globally
window.promptPayApp = new App();
