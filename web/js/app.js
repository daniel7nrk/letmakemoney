// Navigation and Interactive Controller for Rapport Duplicate

document.addEventListener('DOMContentLoaded', () => {
  // Ensure html has js and is-ready classes for Rapport styling
  document.documentElement.classList.add('js', 'is-ready');

  // --- Top Navigation Tabs Routing ---
  const topTabs = document.querySelectorAll('.tabs .tab');
  const mainTabsPill = document.getElementById('main-tabs-pill');
  const tabsTrack = document.querySelector('.tabs__track');
  const allViews = document.querySelectorAll('.view');
  const dockSettingsBtn = document.querySelector('.dock__btn[data-target="settings"]');

  function updateMainTabsPill(activeEl) {
    if (!mainTabsPill || !tabsTrack || !activeEl) {
      if (mainTabsPill) mainTabsPill.style.opacity = '0';
      return;
    }
    const trackRect = tabsTrack.getBoundingClientRect();
    const tabRect = activeEl.getBoundingClientRect();

    mainTabsPill.style.opacity = '1';
    mainTabsPill.style.left = `${tabRect.left - trackRect.left}px`;
    mainTabsPill.style.top = `${tabRect.top - trackRect.top}px`;
    mainTabsPill.style.width = `${tabRect.width}px`;
    mainTabsPill.style.height = `${tabRect.height}px`;
  }

  function showView(targetName) {
    allViews.forEach(view => {
      if (view.dataset.view === targetName) {
        view.classList.add('is-active');
        view.removeAttribute('hidden');
      } else {
        view.classList.remove('is-active');
        view.setAttribute('hidden', '');
      }
    });

    // Update active state in top tabs
    let foundTab = null;
    topTabs.forEach(t => {
      if (t.dataset.target === targetName) {
        t.classList.add('is-active');
        foundTab = t;
      } else {
        t.classList.remove('is-active');
      }
    });

    if (foundTab) {
      updateMainTabsPill(foundTab);
      if (dockSettingsBtn) dockSettingsBtn.removeAttribute('aria-current');
    } else {
      updateMainTabsPill(null);
      if (targetName === 'settings' && dockSettingsBtn) {
        dockSettingsBtn.setAttribute('aria-current', 'page');
      }
    }
  }

  topTabs.forEach(tab => {
    tab.addEventListener('click', (e) => {
      e.preventDefault();
      const target = tab.dataset.target;
      location.hash = `#/${target}`;
      showView(target);
    });
  });

  if (dockSettingsBtn) {
    dockSettingsBtn.addEventListener('click', (e) => {
      e.preventDefault();
      location.hash = '#/settings';
      showView('settings');
    });
  }

  // Handle URL hash on load
  const currentHash = location.hash.replace('#/', '') || 'settings';
  showView(currentHash);

  window.addEventListener('hashchange', () => {
    const hash = location.hash.replace('#/', '') || 'settings';
    showView(hash);
  });

  // --- Settings Nav Pill & Sub-tabs ---
  const navPill = document.getElementById('tab-pill');
  const setNav = document.querySelector('.set-nav');
  const setTabs = document.querySelectorAll('.set-tab');
  const subPanels = {
    profile: document.getElementById('panel-profile'),
    team: document.getElementById('panel-team'),
    notifications: document.getElementById('panel-notifications'),
    appearance: document.getElementById('panel-appearance'),
    data: document.getElementById('panel-data')
  };

  function updateSetPill(activeTab, animated = true) {
    if (!navPill || !setNav || !activeTab) return;
    const navRect = setNav.getBoundingClientRect();
    const tabRect = activeTab.getBoundingClientRect();

    navPill.style.transition = animated ? 'all 0.25s cubic-bezier(0.16, 1, 0.3, 1)' : 'none';
    navPill.style.opacity = '1';
    navPill.style.left = `${tabRect.left - navRect.left}px`;
    navPill.style.top = `${tabRect.top - navRect.top}px`;
    navPill.style.width = `${tabRect.width}px`;
    navPill.style.height = `${tabRect.height}px`;
  }

  const initialSetTab = document.querySelector('.set-tab[aria-selected="true"]');
  if (initialSetTab) {
    setTimeout(() => updateSetPill(initialSetTab, false), 50);
  }

  window.addEventListener('resize', () => {
    const active = document.querySelector('.set-tab[aria-selected="true"]');
    if (active) updateSetPill(active, false);
  });

  setTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const targetSec = tab.dataset.sec;

      setTabs.forEach(t => {
        const isSelected = t === tab;
        t.setAttribute('aria-selected', isSelected ? 'true' : 'false');
        t.tabIndex = isSelected ? 0 : -1;
      });

      updateSetPill(tab, true);

      Object.entries(subPanels).forEach(([key, panel]) => {
        if (!panel) return;
        panel.style.display = key === targetSec ? 'block' : 'none';
      });
    });
  });

  // --- Profile Form Interaction ---
  const formProfile = document.getElementById('form-profile');
  const inputName = document.getElementById('set-name');
  const inputEmail = document.getElementById('set-email');
  const selectRole = document.getElementById('set-role');
  const inputQuota = document.getElementById('set-quota');
  const btnSave = document.getElementById('btn-save');
  const btnDiscard = document.getElementById('btn-discard');
  const profileDirty = document.getElementById('profile-dirty');
  const meName = document.getElementById('me-name-display');
  const meRole = document.getElementById('me-role-display');
  const meMail = document.getElementById('me-email-text');

  let profileState = {
    name: "Sofia Reyes",
    email: "sofia@rapport.app",
    role: "Account Executive",
    quota: 420000
  };

  function checkDirty() {
    const isDirty = (
      inputName.value.trim() !== profileState.name ||
      inputEmail.value.trim() !== profileState.email ||
      selectRole.value !== profileState.role ||
      Number(inputQuota.value) !== profileState.quota
    );

    if (btnSave) btnSave.disabled = !isDirty;
    if (btnDiscard) btnDiscard.disabled = !isDirty;
    if (profileDirty) profileDirty.textContent = isDirty ? 'Unsaved changes' : '';
  }

  [inputName, inputEmail, selectRole, inputQuota].forEach(el => {
    if (!el) return;
    el.addEventListener('input', checkDirty);
    el.addEventListener('change', checkDirty);
  });

  if (btnDiscard) {
    btnDiscard.addEventListener('click', () => {
      inputName.value = profileState.name;
      inputEmail.value = profileState.email;
      selectRole.value = profileState.role;
      inputQuota.value = profileState.quota;
      checkDirty();
    });
  }

  if (formProfile) {
    formProfile.addEventListener('submit', (e) => {
      e.preventDefault();
      profileState = {
        name: inputName.value.trim() || profileState.name,
        email: inputEmail.value.trim() || profileState.email,
        role: selectRole.value,
        quota: Number(inputQuota.value) || profileState.quota
      };
      if (meName) meName.textContent = profileState.name;
      if (meRole) meRole.textContent = profileState.role;
      if (meMail) meMail.textContent = profileState.email;
      checkDirty();
    });
  }

  // --- Theme Toggle ---
  const btnTheme = document.getElementById('btn-theme-toggle');
  if (btnTheme) {
    btnTheme.addEventListener('click', () => {
      const current = document.documentElement.dataset.theme;
      const next = current === 'dark' ? 'light' : 'dark';
      document.documentElement.dataset.theme = next;
    });
  }
});
