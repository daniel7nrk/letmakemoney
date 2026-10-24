// Offline Storage Service with IndexedDB and localStorage Fallback
import { DEFAULT_CATEGORIES, INITIAL_TRANSACTIONS, INITIAL_SCAM_ALERTS } from '../models/DefaultData.js';

class StorageService {
  constructor() {
    this.dbName = 'PromptPayWebDB';
    this.version = 1;
    this.db = null;
    this.ready = this.init();
  }

  async init() {
    if (!window.indexedDB) {
      console.warn('IndexedDB not supported, falling back to localStorage');
      this.initLocalStorageDefaults();
      return;
    }

    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.version);

      request.onupgradeneeded = (e) => {
        const db = e.target.result;
        if (!db.objectStoreNames.contains('transactions')) {
          db.createObjectStore('transactions', { keyPath: 'id' });
        }
        if (!db.objectStoreNames.contains('categories')) {
          db.createObjectStore('categories', { keyPath: 'id' });
        }
        if (!db.objectStoreNames.contains('scam_alerts')) {
          db.createObjectStore('scam_alerts', { keyPath: 'id' });
        }
        if (!db.objectStoreNames.contains('settings')) {
          db.createObjectStore('settings', { keyPath: 'key' });
        }
      };

      request.onsuccess = async (e) => {
        this.db = e.target.result;
        await this.seedInitialDataIfEmpty();
        resolve();
      };

      request.onerror = (e) => {
        console.error('IndexedDB open error:', e);
        this.initLocalStorageDefaults();
        resolve();
      };
    });
  }

  initLocalStorageDefaults() {
    if (!localStorage.getItem('promptpay_categories')) {
      localStorage.setItem('promptpay_categories', JSON.stringify(DEFAULT_CATEGORIES));
    }
    if (!localStorage.getItem('promptpay_transactions')) {
      localStorage.setItem('promptpay_transactions', JSON.stringify(INITIAL_TRANSACTIONS));
    }
    if (!localStorage.getItem('promptpay_scam_alerts')) {
      localStorage.setItem('promptpay_scam_alerts', JSON.stringify(INITIAL_SCAM_ALERTS));
    }
    if (!localStorage.getItem('promptpay_profile')) {
      localStorage.setItem('promptpay_profile', JSON.stringify({
        name: 'Jane Doe',
        countryCode: 'RWANDA',
        currencyCode: 'RWF',
        scamFilteringEnabled: true,
        languageCode: 'en',
        lockMode: 'NONE',
        pinHash: ''
      }));
    }
  }

  async seedInitialDataIfEmpty() {
    const cats = await this.getAll('categories');
    if (cats.length === 0) {
      for (const cat of DEFAULT_CATEGORIES) {
        await this.put('categories', cat);
      }
    }

    const txs = await this.getAll('transactions');
    if (txs.length === 0) {
      for (const tx of INITIAL_TRANSACTIONS) {
        await this.put('transactions', tx);
      }
    }

    const scams = await this.getAll('scam_alerts');
    if (scams.length === 0) {
      for (const scam of INITIAL_SCAM_ALERTS) {
        await this.put('scam_alerts', scam);
      }
    }

    const profile = await this.get('settings', 'user_profile');
    if (!profile) {
      await this.put('settings', {
        key: 'user_profile',
        name: 'Jane Doe',
        countryCode: 'RWANDA',
        currencyCode: 'RWF',
        scamFilteringEnabled: true,
        languageCode: 'en',
        lockMode: 'NONE',
        pinHash: ''
      });
    }
  }

  // Generic IndexedDB operations
  async getAll(storeName) {
    if (!this.db) {
      const data = localStorage.getItem(`promptpay_${storeName}`);
      return data ? JSON.parse(data) : [];
    }
    return new Promise((resolve) => {
      const tx = this.db.transaction(storeName, 'readonly');
      const store = tx.objectStore(storeName);
      const req = store.getAll();
      req.onsuccess = () => resolve(req.result || []);
      req.onerror = () => resolve([]);
    });
  }

  async get(storeName, key) {
    if (!this.db) {
      const all = await this.getAll(storeName);
      return all.find(item => (item.id || item.key) === key) || null;
    }
    return new Promise((resolve) => {
      const tx = this.db.transaction(storeName, 'readonly');
      const store = tx.objectStore(storeName);
      const req = store.get(key);
      req.onsuccess = () => resolve(req.result || null);
      req.onerror = () => resolve(null);
    });
  }

  async put(storeName, item) {
    if (!this.db) {
      const all = await this.getAll(storeName);
      const idx = all.findIndex(x => (x.id || x.key) === (item.id || item.key));
      if (idx >= 0) all[idx] = item;
      else all.push(item);
      localStorage.setItem(`promptpay_${storeName}`, JSON.stringify(all));
      return item;
    }
    return new Promise((resolve, reject) => {
      const tx = this.db.transaction(storeName, 'readwrite');
      const store = tx.objectStore(storeName);
      const req = store.put(item);
      req.onsuccess = () => resolve(item);
      req.onerror = () => reject(req.error);
    });
  }

  async delete(storeName, key) {
    if (!this.db) {
      const all = await this.getAll(storeName);
      const filtered = all.filter(x => (x.id || x.key) !== key);
      localStorage.setItem(`promptpay_${storeName}`, JSON.stringify(filtered));
      return true;
    }
    return new Promise((resolve) => {
      const tx = this.db.transaction(storeName, 'readwrite');
      const store = tx.objectStore(storeName);
      const req = store.delete(key);
      req.onsuccess = () => resolve(true);
      req.onerror = () => resolve(false);
    });
  }

  // High-level accessors
  async getTransactions() {
    const list = await this.getAll('transactions');
    return list.sort((a, b) => b.dateEpochMillis - a.dateEpochMillis);
  }

  async addTransaction(tx) {
    return this.put('transactions', tx);
  }

  async updateTransaction(tx) {
    return this.put('transactions', tx);
  }

  async deleteTransaction(id) {
    return this.delete('transactions', id);
  }

  async getCategories() {
    return this.getAll('categories');
  }

  async saveCategory(cat) {
    return this.put('categories', cat);
  }

  async deleteCategory(id) {
    return this.delete('categories', id);
  }

  async getScamAlerts() {
    const list = await this.getAll('scam_alerts');
    return list.sort((a, b) => b.dateEpochMillis - a.dateEpochMillis);
  }

  async addScamAlert(alert) {
    return this.put('scam_alerts', alert);
  }

  async deleteScamAlert(id) {
    return this.delete('scam_alerts', id);
  }

  async getUserProfile() {
    const profile = await this.get('settings', 'user_profile');
    if (!profile) {
      return {
        name: 'Jane Doe',
        countryCode: 'RWANDA',
        currencyCode: 'RWF',
        scamFilteringEnabled: true,
        languageCode: 'en',
        lockMode: 'NONE',
        pinHash: ''
      };
    }
    return {
      ...profile,
      languageCode: profile.languageCode || 'en',
      lockMode: profile.lockMode || 'NONE',
      pinHash: profile.pinHash || ''
    };
  }

  async saveUserProfile(profile) {
    return this.put('settings', { ...profile, key: 'user_profile' });
  }

  // Backup & Restore
  async exportBackup() {
    const transactions = await this.getTransactions();
    const categories = await this.getCategories();
    const scamAlerts = await this.getScamAlerts();
    const profile = await this.getUserProfile();

    const data = {
      version: 1,
      timestamp: Date.now(),
      profile,
      categories,
      transactions,
      scamAlerts
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `PromptPay_Backup_${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(a);
    a.click();
    setTimeout(() => {
      if (a.parentNode) a.parentNode.removeChild(a);
      URL.revokeObjectURL(url);
    }, 250);
  }

  async importBackup(jsonString) {
    try {
      const data = JSON.parse(jsonString);
      if (data.categories) {
        for (const c of data.categories) await this.put('categories', c);
      }
      if (data.transactions) {
        for (const t of data.transactions) await this.put('transactions', t);
      }
      if (data.scamAlerts) {
        for (const s of data.scamAlerts) await this.put('scam_alerts', s);
      }
      if (data.profile) {
        await this.saveUserProfile(data.profile);
      }
      return true;
    } catch (err) {
      console.error('Import backup error:', err);
      return false;
    }
  }
}

export const storage = new StorageService();
