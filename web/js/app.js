// PromptPay Web Application Controller & Router
import { storage } from './services/StorageService.js';
import { SmsParser } from './services/SmsParser.js';
import { ScamDetector } from './services/ScamDetector.js';
import { Charts } from './components/Charts.js';
import { ICONS, getCategoryIconSvg } from './components/Icons.js';
import { COUNTRIES, formatCurrency, getCountryByCurrency } from './models/Country.js';

class App {
  constructor() {
    this.currentTab = 'dashboard';
    this.transactions = [];
    this.categories = [];
    this.scamAlerts = [];
    this.profile = null;
    this.selectedCountry = COUNTRIES.RWANDA;

    // Wait for DOM and storage
    document.addEventListener('DOMContentLoaded', () => this.init());
  }

  async init() {
    await storage.ready;
    await this.loadData();
    this.setupEventListeners();
    this.render();
    this.registerServiceWorker();
  }

  async loadData() {
    this.transactions = await storage.getTransactions();
    this.categories = await storage.getCategories();
    this.scamAlerts = await storage.getScamAlerts();
    this.profile = await storage.getUserProfile();
    this.selectedCountry = getCountryByCurrency(this.profile.currencyCode);
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
            this.render();
            this.showToast('Backup restored successfully!');
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

  // MARK: - Business Logic: Simulate SMS
  async handleSimulateSms() {
    const sender = document.getElementById('sim-sender').value.trim();
    const body = document.getElementById('sim-body').value.trim();

    if (!body) {
      alert('Please enter an SMS message body.');
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
      this.showToast(`Blocked suspicious message from ${sender}`, 'danger');
      return;
    }

    // Parse Transaction
    const parsed = SmsParser.parseSms(
      sender,
      body,
      this.categories,
      this.selectedCountry.currencyCode
    );

    if (!parsed) {
      alert('Message parsed, but no transaction amounts or patterns were detected.');
      return;
    }

    const initialStatus = (detection.classification === 'NEEDS_REVIEW') ? 'PENDING' : parsed.status;

    const newTx = {
      id: Date.now(),
      amount: parsed.amount,
      currency: parsed.currency,
      dateEpochMillis: Date.now(),
      description: parsed.description,
      merchant: parsed.merchant,
      category: parsed.category,
      source: 'SMS',
      status: initialStatus,
      paymentMethod: sender || 'Mobile Money'
    };

    await storage.addTransaction(newTx);
    await this.loadData();
    this.closeAllModals();
    this.render();

    const formatted = formatCurrency(newTx.amount, newTx.currency);
    this.showToast(`Transaction recorded: ${formatted} (${newTx.category})`);
  }

  // MARK: - Add Manual Transaction
  async handleAddTransaction() {
    const amountVal = parseFloat(document.getElementById('add-amount').value);
    const type = document.getElementById('add-type').value;
    const category = document.getElementById('add-category').value;
    const description = document.getElementById('add-description').value.trim() || 'Manual Entry';
    const paymentMethod = document.getElementById('add-method').value || 'Cash';

    if (isNaN(amountVal) || amountVal <= 0) {
      alert('Please enter a valid amount.');
      return;
    }

    const finalAmount = (type === 'income') ? amountVal : -amountVal;

    const newTx = {
      id: Date.now(),
      amount: finalAmount,
      currency: this.selectedCountry.currencyCode,
      dateEpochMillis: Date.now(),
      description,
      merchant: description,
      category,
      source: 'MANUAL',
      status: 'CONFIRMED',
      paymentMethod
    };

    await storage.addTransaction(newTx);
    await this.loadData();
    this.closeAllModals();
    this.render();
    this.showToast('Transaction added successfully!');
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
        status: 'CONFIRMED',
        paymentMethod: scam.sender
      };
      await storage.addTransaction(newTx);
    }

    await this.loadData();
    this.render();
    this.showToast('Scam alert overridden & transaction saved.');
  }

  async deleteScamAlert(scamId) {
    await storage.deleteScamAlert(scamId);
    await this.loadData();
    this.render();
    this.showToast('Scam alert removed.');
  }

  async deleteTransaction(txId) {
    if (!confirm('Are you sure you want to delete this transaction?')) return;
    await storage.deleteTransaction(txId);
    await this.loadData();
    this.render();
    this.showToast('Transaction deleted.');
  }

  async deleteCategory(catId) {
    if (!confirm('Are you sure you want to delete this category?')) return;
    await storage.deleteCategory(catId);
    await this.loadData();
    this.render();
    this.showToast('Category deleted.');
  }

  // MARK: - Render All Views
  render() {
    const currency = this.selectedCountry.currencyCode;

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
      if (msgEl) msgEl.innerText = `You have ${pendingTxs.length} uncategorized mobile money transaction(s) waiting for review.`;
    }

    // Scam Alert Banner
    const bannerScam = document.getElementById('banner-scam-alert');
    if (bannerScam) {
      bannerScam.style.display = activeScams.length > 0 ? 'flex' : 'none';
      const scamMsg = document.getElementById('banner-scam-msg');
      if (scamMsg) scamMsg.innerText = `PromptPay flagged ${activeScams.length} suspicious message(s) containing phishing links or spoofed balances.`;
    }

    // Render Charts
    Charts.renderTimeline(document.getElementById('chart-timeline'), this.transactions, currency);
    Charts.renderCategoryDonut(document.getElementById('chart-categories'), this.transactions, this.categories, currency);

    // Recent Activity Table
    const recentBody = document.getElementById('recent-activity-body');
    if (recentBody) {
      const recent = this.transactions.slice(0, 5);
      if (recent.length === 0) {
        recentBody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--text-muted);padding:24px;">No transactions recorded yet</td></tr>`;
      } else {
        recentBody.innerHTML = recent.map(t => this.renderTransactionRow(t, currency)).join('');
      }
    }
  }

  renderTransactionRow(t, currency) {
    const isPending = (t.status === 'PENDING');
    const isIncome = (t.amount > 0);
    const dateStr = new Date(t.dateEpochMillis).toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    const formattedAmt = formatCurrency(t.amount, currency);
    const iconSvg = getCategoryIconSvg(t.category);

    return `
      <tr>
        <td>
          <div style="display:flex;align-items:center;gap:10px;">
            <div style="width:34px;height:34px;border-radius:8px;background:var(--bg-subtle);display:flex;align-items:center;justify-content:center;color:var(--primary);flex-shrink:0;">
              ${iconSvg}
            </div>
            <div>
              <div style="font-weight:600;color:var(--text-main);">${t.merchant || t.description}</div>
              <div style="font-size:12px;color:var(--text-muted);">${t.paymentMethod || 'Mobile Money'} &bull; ${t.source}</div>
            </div>
          </div>
        </td>
        <td>
          <span class="category-pill">${t.category || 'Uncategorized'}</span>
        </td>
        <td>
          <span class="badge ${isPending ? 'badge-pending' : 'badge-confirmed'}">${t.status}</span>
        </td>
        <td style="font-size:13px;color:var(--text-secondary);">${dateStr}</td>
        <td style="font-weight:700;text-align:right;color:${isIncome ? 'var(--success)' : 'var(--text-main)'};">
          ${formattedAmt}
        </td>
      </tr>
    `;
  }

  renderTransactionsTable(currency) {
    const tableBody = document.getElementById('all-transactions-body');
    if (!tableBody) return;

    if (this.transactions.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:32px;">No transactions found</td></tr>`;
      return;
    }

    tableBody.innerHTML = this.transactions.map(t => {
      const isIncome = (t.amount > 0);
      const isPending = (t.status === 'PENDING');
      const dateStr = new Date(t.dateEpochMillis).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
      const formattedAmt = formatCurrency(t.amount, currency);
      const iconSvg = getCategoryIconSvg(t.category);

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
          <td><span class="badge ${isPending ? 'badge-pending' : 'badge-confirmed'}">${t.status}</span></td>
          <td>${dateStr}</td>
          <td style="font-weight:700;color:${isIncome ? 'var(--success)' : 'var(--text-main)'};">${formattedAmt}</td>
          <td style="text-align:right;">
            <button class="btn btn-secondary btn-sm" onclick="window.promptPayApp.deleteTransaction(${t.id})">
              ${ICONS.trash}
              <span>Delete</span>
            </button>
          </td>
        </tr>
      `;
    }).join('');
  }

  renderPendingReview(pendingTxs, currency) {
    const container = document.getElementById('pending-review-list');
    if (!container) return;

    if (pendingTxs.length === 0) {
      container.innerHTML = `
        <div style="text-align:center;padding:48px 24px;background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius-lg);">
          <div style="display:inline-flex;color:var(--success);margin-bottom:12px;">${ICONS.checkCircle}</div>
          <h3 style="font-size:18px;font-weight:700;">All caught up!</h3>
          <p style="color:var(--text-muted);font-size:14px;margin-top:4px;">There are no uncategorized transactions waiting for review.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = pendingTxs.map(t => {
      const amtStr = formatCurrency(t.amount, currency);
      const dateStr = new Date(t.dateEpochMillis).toLocaleString();

      return `
        <div class="card" style="margin-bottom:16px;">
          <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:16px;flex-wrap:wrap;">
            <div>
              <div style="font-size:18px;font-weight:700;color:var(--text-main);">${t.merchant}</div>
              <div style="font-size:13px;color:var(--text-muted);margin-top:2px;">${dateStr} &bull; ${t.paymentMethod}</div>
              <div style="font-size:14px;color:var(--text-secondary);margin-top:8px;">${t.description}</div>
            </div>
            <div style="font-size:22px;font-weight:800;color:var(--text-main);">${amtStr}</div>
          </div>

          <div style="margin-top:16px;padding-top:16px;border-top:1px solid var(--border);">
            <div style="font-size:12px;font-weight:600;color:var(--text-muted);margin-bottom:8px;">SELECT CORRECT CATEGORY:</div>
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
    const container = document.getElementById('scam-alerts-list');
    if (!container) return;

    if (activeScams.length === 0) {
      container.innerHTML = `
        <div style="text-align:center;padding:48px 24px;background:var(--bg-card);border:1px solid var(--border);border-radius:var(--radius-lg);">
          <div style="display:inline-flex;color:var(--primary);margin-bottom:12px;">${ICONS.shield}</div>
          <h3 style="font-size:18px;font-weight:700;">No Fraud Alerts Detected</h3>
          <p style="color:var(--text-muted);font-size:14px;margin-top:4px;">PromptPay's on-device fraud engine is running and guarding your messages.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = activeScams.map(s => {
      const dateStr = new Date(s.dateEpochMillis).toLocaleString();
      return `
        <div class="card" style="border-left: 4px solid var(--danger); margin-bottom: 16px;">
          <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:16px;">
            <div>
              <div style="display:flex;align-items:center;gap:8px;">
                <span class="badge badge-scam">${ICONS.shieldAlert} SUSPICIOUS MESSAGE BLOCKED</span>
                <span style="font-weight:700;font-size:15px;">Sender: ${s.sender}</span>
              </div>
              <div style="font-size:12px;color:var(--text-muted);margin-top:4px;">Detected: ${dateStr}</div>
            </div>
            <div style="display:flex;gap:8px;">
              <button class="btn btn-secondary btn-sm" onclick="window.promptPayApp.overrideScamAlert(${s.id})">Mark as Safe</button>
              <button class="btn btn-danger btn-sm" onclick="window.promptPayApp.deleteScamAlert(${s.id})">Delete</button>
            </div>
          </div>

          <div style="margin-top:12px;background:var(--bg-subtle);padding:12px;border-radius:var(--radius-md);font-family:monospace;font-size:13px;color:var(--text-main);">
            ${s.body}
          </div>

          <div style="margin-top:12px;display:flex;align-items:center;gap:16px;font-size:13px;flex-wrap:wrap;">
            <div><strong>Reason:</strong> <span style="color:var(--danger);">${s.primaryReason}</span></div>
            <div><strong>Signals:</strong> <span style="color:var(--text-secondary);">${s.detectedSignals || 'None'}</span></div>
          </div>
        </div>
      `;
    }).join('');
  }

  renderCategoriesList() {
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
            <span class="badge badge-neutral">Built-in</span>
          `}
        </div>
      `;
    }).join('');
  }
}

// Instantiate and expose globally for inline HTML callbacks
window.promptPayApp = new App();
