// Navigation and Interactive Controller with Ubiquitous User Profile Sync
import { getSupabase } from './supabaseClient.js';

document.addEventListener('DOMContentLoaded', async () => {
  // Ensure html has js and is-ready classes for Rapport styling
  document.documentElement.classList.add('js', 'is-ready');

  const supabase = getSupabase();

  // --- Element References for User Profile Everywhere ---
  const authLink = document.getElementById('btn-auth-link');
  const authLinkText = document.getElementById('auth-link-text');
  const meNameDisplay = document.getElementById('me-name-display');
  const meRoleDisplay = document.getElementById('me-role-display');
  const meEmailText = document.getElementById('me-email-text');
  const leftAvatarImg = document.getElementById('left-avatar-img');
  const topAvatarImg = document.getElementById('top-avatar-img');
  const btnMe = document.getElementById('btn-me');

  const formProfile = document.getElementById('form-profile');
  const inputName = document.getElementById('set-name');
  const inputEmail = document.getElementById('set-email');
  const selectRole = document.getElementById('set-role');
  const inputQuota = document.getElementById('set-quota');
  const btnSave = document.getElementById('btn-save');
  const btnDiscard = document.getElementById('btn-discard');
  const profileDirty = document.getElementById('profile-dirty');

  // Default state
  let profileState = {
    name: "Sofia Reyes",
    email: "sofia@rapport.app",
    role: "Account Executive",
    quota: 420000
  };

  // 1. Restore from localStorage first (Instant display without network lag)
  try {
    const saved = localStorage.getItem('rapport:profile');
    if (saved) {
      const parsed = JSON.parse(saved);
      profileState = { ...profileState, ...parsed };
    }
  } catch (e) {}

  // Function to reflect user profile in every single place across the UI
  function applyProfileEverywhere(profile) {
    if (meNameDisplay) meNameDisplay.textContent = profile.name;
    if (meRoleDisplay) meRoleDisplay.textContent = profile.role;
    if (meEmailText) meEmailText.textContent = profile.email;

    if (inputName && document.activeElement !== inputName) inputName.value = profile.name;
    if (inputEmail && document.activeElement !== inputEmail) inputEmail.value = profile.email;
    if (selectRole && document.activeElement !== selectRole) selectRole.value = profile.role;
    if (inputQuota && document.activeElement !== inputQuota) inputQuota.value = profile.quota;

    if (leftAvatarImg) {
      leftAvatarImg.alt = profile.name;
      leftAvatarImg.title = profile.name;
    }
    if (topAvatarImg) {
      topAvatarImg.alt = profile.name;
      topAvatarImg.title = profile.name;
    }
    if (btnMe) {
      btnMe.setAttribute('aria-label', `${profile.name} account`);
      btnMe.setAttribute('title', profile.name);
    }
  }

  // Initial render from state
  applyProfileEverywhere(profileState);

  // 2. Sync with Supabase Auth Session
  let currentUser = null;
  if (supabase) {
    try {
      const { data } = await supabase.auth.getSession();
      if (data?.session?.user) {
        currentUser = data.session.user;
        const metaName = currentUser.user_metadata?.full_name;
        const metaRole = currentUser.user_metadata?.role;

        profileState.name = metaName || currentUser.email.split('@')[0];
        profileState.email = currentUser.email;
        if (metaRole) profileState.role = metaRole;

        localStorage.setItem('rapport:profile', JSON.stringify(profileState));
        applyProfileEverywhere(profileState);

        if (authLinkText) authLinkText.textContent = "Sign Out";
        if (authLink) {
          authLink.href = "#";
          authLink.addEventListener('click', async (e) => {
            e.preventDefault();
            await supabase.auth.signOut();
            localStorage.removeItem('rapport:profile');
            window.location.reload();
          });
        }
      }
    } catch (err) {
      console.warn("Supabase session check error:", err);
    }
  }

  // 3. Live Synchronization: as user types in the form, update everywhere in real-time
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

  if (inputName) {
    inputName.addEventListener('input', () => {
      const val = inputName.value.trim() || "Your Name";
      if (meNameDisplay) meNameDisplay.textContent = val;
      if (leftAvatarImg) leftAvatarImg.title = val;
      if (topAvatarImg) topAvatarImg.title = val;
      checkDirty();
    });
  }

  if (inputEmail) {
    inputEmail.addEventListener('input', () => {
      const val = inputEmail.value.trim() || "your@email.com";
      if (meEmailText) meEmailText.textContent = val;
      checkDirty();
    });
  }

  if (selectRole) {
    selectRole.addEventListener('change', () => {
      if (meRoleDisplay) meRoleDisplay.textContent = selectRole.value;
      checkDirty();
    });
  }

  if (inputQuota) {
    inputQuota.addEventListener('input', checkDirty);
  }

  if (btnDiscard) {
    btnDiscard.addEventListener('click', () => {
      applyProfileEverywhere(profileState);
      checkDirty();
    });
  }

  if (formProfile) {
    formProfile.addEventListener('submit', async (e) => {
      e.preventDefault();
      const newName = inputName.value.trim() || profileState.name;
      const newEmail = inputEmail.value.trim() || profileState.email;
      const newRole = selectRole.value;
      const newQuota = Number(inputQuota.value) || profileState.quota;

      profileState = {
        name: newName,
        email: newEmail,
        role: newRole,
        quota: newQuota
      };

      localStorage.setItem('rapport:profile', JSON.stringify(profileState));
      applyProfileEverywhere(profileState);

      // Persist to Supabase if session active
      if (currentUser && supabase) {
        try {
          await supabase.auth.updateUser({
            data: { full_name: newName, role: newRole }
          });
        } catch (err) {
          console.warn("Failed to persist user update in Supabase:", err);
        }
      }

      checkDirty();
      if (profileDirty) {
        profileDirty.textContent = "Saved!";
        setTimeout(() => {
          if (profileDirty.textContent === "Saved!") profileDirty.textContent = "";
        }, 2000);
      }
    });
  }

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
