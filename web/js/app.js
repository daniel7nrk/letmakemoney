// Rapport Workspace Settings Page Controller

document.addEventListener('DOMContentLoaded', () => {
  // Ensure html has js and is-ready classes
  document.documentElement.classList.add('js', 'is-ready');

  // --- State ---
  let initialProfile = {
    name: "Sofia Reyes",
    email: "sofia@rapport.app",
    role: "Account Executive",
    quota: 420000
  };

  let currentProfile = { ...initialProfile };

  // --- Element References ---
  const navPill = document.getElementById('tab-pill');
  const setNav = document.querySelector('.set-nav');
  const setTabs = document.querySelectorAll('.set-tab');
  const tabPanels = {
    profile: document.getElementById('panel-profile'),
    team: document.getElementById('panel-team'),
    notifications: document.getElementById('panel-notifications'),
    appearance: document.getElementById('panel-appearance'),
    data: document.getElementById('panel-data')
  };

  // Profile Elements
  const formProfile = document.getElementById('form-profile');
  const inputName = document.getElementById('set-name');
  const inputEmail = document.getElementById('set-email');
  const selectRole = document.getElementById('set-role');
  const inputQuota = document.getElementById('set-quota');
  const btnSave = document.getElementById('btn-save');
  const btnDiscard = document.getElementById('btn-discard');
  const profileDirty = document.getElementById('profile-dirty');
  const profileErr = document.getElementById('profile-err');

  // Aside Profile Display
  const meNameDisplay = document.getElementById('me-name-display');
  const meRoleDisplay = document.getElementById('me-role-display');
  const meEmailText = document.getElementById('me-email-text');
  const meAvatar = document.getElementById('me-avatar');

  // Toast
  const toast = document.getElementById('toast');
  const toastMsg = document.getElementById('toast-msg');
  let toastTimer = null;

  function showToast(msg) {
    if (!toast) return;
    toastMsg.textContent = msg;
    toast.classList.add('is-show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
      toast.classList.remove('is-show');
    }, 2800);
  }

  // --- Pill Alignment Function ---
  function updatePill(activeTab, animated = true) {
    if (!navPill || !setNav || !activeTab) return;
    const navRect = setNav.getBoundingClientRect();
    const tabRect = activeTab.getBoundingClientRect();

    if (!animated) {
      navPill.style.transition = 'none';
    } else {
      navPill.style.transition = 'all 0.25s cubic-bezier(0.16, 1, 0.3, 1)';
    }

    navPill.style.opacity = '1';
    navPill.style.left = `${tabRect.left - navRect.left}px`;
    navPill.style.top = `${tabRect.top - navRect.top}px`;
    navPill.style.width = `${tabRect.width}px`;
    navPill.style.height = `${tabRect.height}px`;
  }

  // Initial pill position
  const activeTab = document.querySelector('.set-tab[aria-selected="true"]');
  if (activeTab) {
    setTimeout(() => updatePill(activeTab, false), 50);
  }

  window.addEventListener('resize', () => {
    const cur = document.querySelector('.set-tab[aria-selected="true"]');
    if (cur) updatePill(cur, false);
  });

  // --- Tab Navigation ---
  setTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const targetSec = tab.dataset.sec;

      // Update Tab Selection
      setTabs.forEach(t => {
        const isSelected = t === tab;
        t.setAttribute('aria-selected', isSelected ? 'true' : 'false');
        t.tabIndex = isSelected ? 0 : -1;
      });

      updatePill(tab, true);

      // Show Selected Panel
      Object.entries(tabPanels).forEach(([key, panel]) => {
        if (!panel) return;
        if (key === targetSec) {
          panel.style.display = 'block';
          panel.style.opacity = '0';
          panel.style.transform = 'translateY(6px)';
          requestAnimationFrame(() => {
            panel.style.transition = 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)';
            panel.style.opacity = '1';
            panel.style.transform = 'translateY(0)';
          });
        } else {
          panel.style.display = 'none';
        }
      });
    });
  });

  // --- Profile Form Dirty Checking & Submit ---
  function checkProfileDirty() {
    const isDirty = (
      inputName.value.trim() !== currentProfile.name ||
      inputEmail.value.trim() !== currentProfile.email ||
      selectRole.value !== currentProfile.role ||
      Number(inputQuota.value) !== currentProfile.quota
    );

    if (btnSave) btnSave.disabled = !isDirty;
    if (btnDiscard) btnDiscard.disabled = !isDirty;
    if (profileDirty) profileDirty.textContent = isDirty ? 'Unsaved changes' : '';
    if (profileErr) profileErr.textContent = '';
  }

  [inputName, inputEmail, selectRole, inputQuota].forEach(el => {
    if (!el) return;
    el.addEventListener('input', checkProfileDirty);
    el.addEventListener('change', checkProfileDirty);
  });

  if (btnDiscard) {
    btnDiscard.addEventListener('click', () => {
      inputName.value = currentProfile.name;
      inputEmail.value = currentProfile.email;
      selectRole.value = currentProfile.role;
      inputQuota.value = currentProfile.quota;
      checkProfileDirty();
    });
  }

  if (formProfile) {
    formProfile.addEventListener('submit', (e) => {
      e.preventDefault();
      const newName = inputName.value.trim();
      const newEmail = inputEmail.value.trim();
      const newRole = selectRole.value;
      const newQuota = Number(inputQuota.value);

      if (!newName) {
        if (profileErr) profileErr.textContent = "Your name can't be empty.";
        inputName.focus();
        return;
      }
      if (!/^\S+@\S+\.\S+$/.test(newEmail)) {
        if (profileErr) profileErr.textContent = "That email doesn't look right.";
        inputEmail.focus();
        return;
      }
      if (isNaN(newQuota) || newQuota <= 0) {
        if (profileErr) profileErr.textContent = "Enter a quota above $0.";
        inputQuota.focus();
        return;
      }

      // Save
      currentProfile = {
        name: newName,
        email: newEmail,
        role: newRole,
        quota: newQuota
      };

      if (meNameDisplay) meNameDisplay.textContent = newName;
      if (meRoleDisplay) meRoleDisplay.textContent = newRole;
      if (meEmailText) meEmailText.textContent = newEmail;
      if (meAvatar) meAvatar.title = newName;

      checkProfileDirty();
      showToast("Profile saved");
    });
  }

  // --- Theme Toggling ---
  const btnThemeToggle = document.getElementById('btn-theme-toggle');
  const themeSegBtns = document.querySelectorAll('[data-theme-val]');

  function applyTheme(theme) {
    let effective = theme;
    if (theme === 'system') {
      effective = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    document.documentElement.dataset.theme = effective;
    localStorage.setItem('rapport:theme', JSON.stringify(theme));

    themeSegBtns.forEach(btn => {
      btn.classList.toggle('active', btn.dataset.themeVal === theme);
      btn.setAttribute('aria-checked', btn.dataset.themeVal === theme ? 'true' : 'false');
    });
  }

  // Restore saved theme
  try {
    const saved = JSON.parse(localStorage.getItem('rapport:theme') || '"light"');
    applyTheme(saved);
  } catch (e) {
    applyTheme('light');
  }

  if (btnThemeToggle) {
    btnThemeToggle.addEventListener('click', () => {
      const current = document.documentElement.dataset.theme;
      const next = current === 'dark' ? 'light' : 'dark';
      applyTheme(next);
      showToast(`${next.charAt(0).toUpperCase() + next.slice(1)} theme on`);
    });
  }

  themeSegBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const chosen = btn.dataset.themeVal;
      applyTheme(chosen);
      showToast(`${chosen.charAt(0).toUpperCase() + chosen.slice(1)} theme on`);
    });
  });

  // Week Starts On
  const weekBtns = document.querySelectorAll('[data-week]');
  weekBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      weekBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const day = btn.dataset.week === '1' ? 'Monday' : 'Sunday';
      showToast(`Weeks start on ${day}`);
    });
  });

  // --- Switches ---
  const switches = document.querySelectorAll('.switch');
  switches.forEach(sw => {
    sw.addEventListener('click', () => {
      const isChecked = sw.getAttribute('aria-checked') === 'true';
      sw.setAttribute('aria-checked', String(!isChecked));
      const parentItem = sw.closest('.set-item');
      const label = parentItem ? parentItem.querySelector('b')?.textContent : 'Alert';
      showToast(`${label} ${!isChecked ? 'on' : 'off'}`);
    });
  });

  // --- Data Export & Reset ---
  document.querySelectorAll('[data-export]').forEach(btn => {
    btn.addEventListener('click', () => {
      const type = btn.dataset.export;
      let filename = `rapport-${type}.csv`;
      let content = "Item,Value\nSample,1\n";
      let mime = "text/csv";

      if (type === 'json') {
        filename = `rapport-backup-${new Date().toISOString().slice(0, 10)}.json`;
        content = JSON.stringify({ settings: currentProfile }, null, 2);
        mime = "application/json";
      }

      const blob = new Blob([content], { type: mime });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = filename;
      a.click();
      URL.revokeObjectURL(a.href);
      showToast(`Exported ${type}`);
    });
  });

  const btnResetData = document.getElementById('btn-reset-data');
  if (btnResetData) {
    btnResetData.addEventListener('click', () => {
      currentProfile = { ...initialProfile };
      inputName.value = currentProfile.name;
      inputEmail.value = currentProfile.email;
      selectRole.value = currentProfile.role;
      inputQuota.value = currentProfile.quota;
      if (meNameDisplay) meNameDisplay.textContent = currentProfile.name;
      if (meRoleDisplay) meRoleDisplay.textContent = currentProfile.role;
      if (meEmailText) meEmailText.textContent = currentProfile.email;
      checkProfileDirty();
      showToast("Demo data reset");
    });
  }

  // --- Invite Teammate button feedback ---
  const btnInvite = document.getElementById('btn-invite-team');
  if (btnInvite) {
    btnInvite.addEventListener('click', () => {
      showToast("Invite link copied to clipboard");
    });
  }
});
