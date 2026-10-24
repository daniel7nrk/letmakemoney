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

  // Default Profile State
  let profileState = {
    name: "Sofia Reyes",
    email: "sofia@rapport.app",
    role: "Account Executive",
    quota: 420000
  };

  // 1. Restore profile from localStorage first
  try {
    const saved = localStorage.getItem('rapport:profile');
    if (saved) {
      const parsed = JSON.parse(saved);
      profileState = { ...profileState, ...parsed };
    }
  } catch (e) {}

  // ==========================================
  // --- Supabase Family Users Module ---
  // ==========================================
  const DEFAULT_SUPABASE_USERS = [
    {
      id: 'usr_cizigire_1',
      name: 'Francis Cizigire',
      email: 'francis@cizigire.org',
      relation: 'Spouse',
      tag: 'Co-Organizer',
      spent: 1950,
      budget: 2500,
      color: '#f2c94c',
      avTone: 'av--t0'
    },
    {
      id: 'usr_cizigire_2',
      name: 'David Cizigire',
      email: 'david@cizigire.org',
      relation: 'Child',
      tag: 'College',
      spent: 1140,
      budget: 1500,
      color: '#c8431f',
      avTone: 'av--t2'
    },
    {
      id: 'usr_cizigire_3',
      name: 'Lisa Cizigire',
      email: 'lisa@cizigire.org',
      relation: 'Child',
      tag: 'High School',
      spent: 540,
      budget: 800,
      color: '#6cc59a',
      avTone: 'av--t1'
    }
  ];

  let supabaseFamilyUsers = [...DEFAULT_SUPABASE_USERS];
  try {
    const savedFamily = localStorage.getItem('rapport:supabase_family_users');
    if (savedFamily) {
      const parsed = JSON.parse(savedFamily);
      if (Array.isArray(parsed) && parsed.length > 0) {
        supabaseFamilyUsers = parsed;
      }
    }
  } catch (e) {}

  function saveSupabaseFamilyUsers() {
    try {
      localStorage.setItem('rapport:supabase_family_users', JSON.stringify(supabaseFamilyUsers));
    } catch (e) {}
  }

  // Fetch all users directly from Supabase
  async function syncUsersFromSupabase() {
    if (supabase) {
      try {
        const { data, error } = await supabase.from('profiles').select('*');
        if (!error && Array.isArray(data) && data.length > 0) {
          const mapped = data.map((row, idx) => ({
            id: row.id || `usr_${idx}`,
            name: row.full_name || (row.email ? row.email.split('@')[0] : `Member ${idx + 1}`),
            email: row.email || '',
            relation: row.role || 'Family Member',
            tag: row.tag || 'Supabase User',
            spent: Number(row.monthly_spent || 400 + (idx * 250)),
            budget: Number(row.monthly_budget || 1400 + (idx * 300)),
            color: ['#f2c94c', '#c8431f', '#6cc59a', '#8cc7b4'][idx % 4],
            avTone: ['av--t0', 'av--t1', 'av--t2', 'av--t3'][idx % 4]
          }));
          supabaseFamilyUsers = mapped;
          saveSupabaseFamilyUsers();
        }
      } catch (err) {
        console.warn("Supabase profiles query:", err);
      }
    }
    renderFamilyPage();
  }

  // Current user's personal portion of spending & budget
  const userFamilySpent = 2850;
  const userFamilyBudget = 4000;

  function renderFamilyPage() {
    try {
      const famStatTotalSpent = document.getElementById('fam-stat-total-spent');
      const famStatBudget = document.getElementById('fam-stat-budget');
      const famStatMemberCount = document.getElementById('fam-stat-member-count');
      const famBudgetPill = document.getElementById('fam-budget-pill');
      const famOverviewProgressLabel = document.getElementById('fam-overview-progress-label');
      const famOverviewRemainingLabel = document.getElementById('fam-overview-remaining-label');
      const famStackedMeter = document.getElementById('fam-stacked-meter');
      const famMeterLegend = document.getElementById('fam-meter-legend');

      const famUserName = document.getElementById('fam-user-name');
      const famUserEmail = document.getElementById('fam-user-email');
      const famUserSpent = document.getElementById('fam-user-spent');
      const famUserBudget = document.getElementById('fam-user-budget');
      const famUserBar = document.getElementById('fam-user-bar');
      const famUserShare = document.getElementById('fam-user-share');
      const famUserRemaining = document.getElementById('fam-user-remaining');

      const famOtherCount = document.getElementById('fam-other-count');
      const famMembersList = document.getElementById('fam-members-list');

      // Separate current user from the rest of the Supabase family members
      const currentEmail = (profileState.email || '').toLowerCase().trim();
      const currentName = (profileState.name || '').toLowerCase().trim();

      const otherMembers = supabaseFamilyUsers.filter(u => {
        const uEmail = (u.email || '').toLowerCase().trim();
        const uName = (u.name || '').toLowerCase().trim();
        if (currentEmail && uEmail && uEmail === currentEmail) return false;
        if (currentName && uName && uName === currentName) return false;
        return true;
      });

      // Totals (Current user + other Supabase members)
      const otherSpent = otherMembers.reduce((sum, m) => sum + Number(m.spent || 0), 0);
      const otherBudget = otherMembers.reduce((sum, m) => sum + Number(m.budget || 0), 0);

      const totalSpent = userFamilySpent + otherSpent;
      const totalBudget = userFamilyBudget + otherBudget;
      const remainingBudget = Math.max(0, totalBudget - totalSpent);
      const totalUsagePct = totalBudget > 0 ? ((totalSpent / totalBudget) * 100).toFixed(1) : 0;

      // 1. Header KPIs
      if (famStatTotalSpent) {
        famStatTotalSpent.textContent = `$${totalSpent.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
      }
      if (famStatBudget) {
        famStatBudget.textContent = `$${totalBudget.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
      }
      if (famStatMemberCount) {
        famStatMemberCount.textContent = `${otherMembers.length + 1} active`;
      }

      // 2. Overview Card
      if (famBudgetPill) famBudgetPill.textContent = `${totalUsagePct}% Used`;
      if (famOverviewProgressLabel) {
        famOverviewProgressLabel.textContent = `$${totalSpent.toLocaleString('en-US', { minimumFractionDigits: 2 })} spent of $${totalBudget.toLocaleString('en-US', { minimumFractionDigits: 2 })} household limit`;
      }
      if (famOverviewRemainingLabel) {
        famOverviewRemainingLabel.textContent = `$${remainingBudget.toLocaleString('en-US', { minimumFractionDigits: 2 })} remaining`;
      }

      // Stacked Distribution Meter
      if (famStackedMeter) {
        famStackedMeter.innerHTML = '';
        const userSharePct = totalSpent > 0 ? ((userFamilySpent / totalSpent) * 100).toFixed(1) : 0;
        
        const userSeg = document.createElement('div');
        userSeg.className = 'fam-meter-seg fam-seg--user';
        userSeg.style.width = `${userSharePct}%`;
        userSeg.title = `You (${profileState.name}): $${userFamilySpent.toLocaleString()} (${userSharePct}%)`;
        famStackedMeter.appendChild(userSeg);

        otherMembers.forEach(m => {
          const seg = document.createElement('div');
          seg.className = 'fam-meter-seg';
          seg.style.background = m.color || '#f2c94c';
          const mSharePct = totalSpent > 0 ? ((m.spent / totalSpent) * 100).toFixed(1) : 0;
          seg.style.width = `${mSharePct}%`;
          seg.title = `${m.name}: $${Number(m.spent).toLocaleString()} (${mSharePct}%)`;
          famStackedMeter.appendChild(seg);
        });
      }

      // Legend
      if (famMeterLegend) {
        famMeterLegend.innerHTML = '';
        const userSharePct = totalSpent > 0 ? Math.round((userFamilySpent / totalSpent) * 100) : 0;

        const userLeg = document.createElement('span');
        const displayName = profileState.name ? profileState.name.split(' ')[0] : 'You';
        userLeg.innerHTML = `<i class="fam-dot" style="background: var(--pine);"></i> <strong>You (${displayName})</strong>: $${userFamilySpent.toLocaleString()} (${userSharePct}%)`;
        famMeterLegend.appendChild(userLeg);

        otherMembers.forEach(m => {
          const leg = document.createElement('span');
          const mShare = totalSpent > 0 ? Math.round((m.spent / totalSpent) * 100) : 0;
          leg.innerHTML = `<i class="fam-dot" style="background: ${m.color || '#f2c94c'};"></i> <strong>${m.name}</strong>: $${Number(m.spent).toLocaleString()} (${mShare}%)`;
          famMeterLegend.appendChild(leg);
        });
      }

      // 3. Current User Card
      if (famUserName) famUserName.textContent = profileState.name;
      if (famUserEmail) famUserEmail.textContent = profileState.email;
      if (famUserSpent) famUserSpent.textContent = `$${userFamilySpent.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
      if (famUserBudget) famUserBudget.textContent = `$${userFamilyBudget.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
      if (famUserBar) {
        const userUsageRatio = Math.min(1, userFamilySpent / userFamilyBudget);
        famUserBar.style.transform = `scaleX(${userUsageRatio})`;
      }
      if (famUserShare) {
        const userShareOfTotal = totalSpent > 0 ? Math.round((userFamilySpent / totalSpent) * 100) : 0;
        famUserShare.textContent = `${userShareOfTotal}% of total family spending`;
      }
      if (famUserRemaining) {
        const userLeft = Math.max(0, userFamilyBudget - userFamilySpent);
        famUserRemaining.textContent = `$${userLeft.toLocaleString('en-US', { minimumFractionDigits: 2 })} left`;
      }

      // 4. Other Family Members Card
      if (famOtherCount) famOtherCount.textContent = `${otherMembers.length} ${otherMembers.length === 1 ? 'Member' : 'Members'}`;
      if (famMembersList) {
        famMembersList.innerHTML = '';
        if (otherMembers.length === 0) {
          famMembersList.innerHTML = `
            <div style="text-align: center; padding: 36px 16px; color: var(--muted);">
              <svg class="ico" viewBox="0 0 24 24" style="width: 28px; height: 28px; margin: 0 auto 10px; opacity: 0.5;">
                <circle cx="9" cy="8" r="3.5"/><path d="M3 19c.5-3 3-4.5 6-4.5s5.5 1.5 6 4.5"/><circle cx="16.5" cy="9.5" r="2.5"/><path d="M16 14.5c2 0 4.5 1 5 3.5"/>
              </svg>
              <strong style="display: block; font-size: 14.5px; color: var(--text); margin-bottom: 4px;">No other members in Supabase yet</strong>
              <p style="font-size: 12.5px; max-width: 380px; margin: 0 auto;">As other family members create accounts or sign in via Supabase, their profiles and allowances will automatically update here.</p>
            </div>
          `;
        } else {
          otherMembers.forEach(m => {
            const row = document.createElement('div');
            row.className = 'fam-member-row';

            const initials = m.name.split(' ').filter(Boolean).map(n => n[0]).join('').slice(0, 2).toUpperCase() || 'FM';
            const ratio = m.budget > 0 ? Math.min(1, m.spent / m.budget) : 0;
            const pct = Math.round(ratio * 100);
            const isNearLimit = pct >= 90;
            const statusClass = isNearLimit ? 'badge--tomato' : 'badge--plain';
            const statusText = isNearLimit ? 'Near Limit' : 'On Track';

            row.innerHTML = `
              <div class="av ${m.avTone || 'av--t0'} av--md">${initials}</div>
              <div style="min-width: 0;">
                <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                  <strong style="font-size: 14px;">${m.name}</strong>
                  <span class="status status--active" style="font-size: 11px;">${m.relation || 'Member'}</span>
                  ${m.tag ? `<span class="badge badge--plain" style="font-size: 10px;">${m.tag}</span>` : ''}
                </div>
                <small style="color: var(--muted); font-size: 12px;">${m.email ? `${m.email} · ` : ''}$${Number(m.spent).toLocaleString()} spent · $${Math.max(0, m.budget - m.spent).toLocaleString()} remaining</small>
              </div>
              <div class="fam-member-row__quota">
                <div style="display: flex; justify-content: space-between; align-items: baseline; font-size: 12px; margin-bottom: 4px;">
                  <span style="color: var(--muted);">${pct}% of allowance</span>
                  <span style="font-weight: 600;">$${Number(m.spent).toLocaleString()} / $${Number(m.budget).toLocaleString()}</span>
                </div>
                <span class="meter" role="meter" aria-label="${m.name} allowance">
                  <span class="meter__fill" style="background: ${m.color || '#f2c94c'}; transform: scaleX(${ratio}); transform-origin: left center;"></span>
                </span>
              </div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span class="badge ${statusClass}">${statusText}</span>
              </div>
            `;

            famMembersList.appendChild(row);
          });
        }
      }
    } catch (err) {
      console.warn("renderFamilyPage error:", err);
    }
  }

  // ==========================================
  // --- Notifications Module State & Logic ---
  // ==========================================
  const DEFAULT_NOTIFICATIONS = [
    {
      id: 'notif_1',
      title: 'Payment Request Received',
      desc: 'Sarah Jenkins requested $120.00 for Household Groceries & Supplies.',
      type: 'requests',
      iconType: 'request',
      unread: true,
      time: '12m ago',
      today: true,
      actionText: 'Accept & Pay',
      actionTarget: 'transactions'
    },
    {
      id: 'notif_2',
      title: 'Family Allowance Notice',
      desc: 'David Jenkins spent $45.20 at Campus Bookstore. (76% of monthly allowance used).',
      type: 'family',
      iconType: 'family',
      unread: true,
      time: '42m ago',
      today: true,
      actionText: 'View Family',
      actionTarget: 'family'
    },
    {
      id: 'notif_3',
      title: 'Direct Payroll Deposit',
      desc: 'PromptPay payroll deposit of $4,850.00 has cleared into your primary checking account.',
      type: 'payments',
      iconType: 'payment',
      unread: true,
      time: '2h ago',
      today: true,
      actionText: 'View Balance',
      actionTarget: 'dashboard'
    },
    {
      id: 'notif_4',
      title: 'Auto-Debit Scheduled',
      desc: 'Upcoming auto-debit of $320.00 for Equipment Financing is scheduled for tomorrow.',
      type: 'payments',
      iconType: 'payment',
      unread: false,
      time: '4h ago',
      today: true,
      actionText: 'View Loans',
      actionTarget: 'loans'
    },
    {
      id: 'notif_5',
      title: 'New Device Sign-in Detected',
      desc: 'Successful web session login from Windows PC (Edge browser, IP 185.199.110.153).',
      type: 'security',
      iconType: 'security',
      unread: false,
      time: 'Yesterday',
      today: false,
      actionText: 'Review Security',
      actionTarget: 'settings'
    },
    {
      id: 'notif_6',
      title: 'Monthly Subscription Paid',
      desc: 'Netflix Family 4K subscription ($22.99) was automatically processed.',
      type: 'payments',
      iconType: 'payment',
      unread: false,
      time: '2 days ago',
      today: false
    },
    {
      id: 'notif_7',
      title: 'Cloud Backup Complete',
      desc: 'Workspace ledger and family budgeting settings synchronized to Supabase Cloud.',
      type: 'system',
      iconType: 'system',
      unread: false,
      time: '3 days ago',
      today: false
    }
  ];

  let notifications = [...DEFAULT_NOTIFICATIONS];
  try {
    const savedNotifs = localStorage.getItem('rapport:notifications');
    if (savedNotifs) {
      notifications = JSON.parse(savedNotifs);
    }
  } catch (e) {}

  function saveNotifications() {
    try {
      localStorage.setItem('rapport:notifications', JSON.stringify(notifications));
    } catch (e) {}
  }

  let activeNotifFilter = 'all';
  let notifSearchQuery = '';

  function updateBellBadge() {
    const bellBadge = document.getElementById('bell-badge');
    const unreadCount = notifications.filter(n => n.unread).length;
    if (bellBadge) {
      bellBadge.style.display = unreadCount > 0 ? 'block' : 'none';
    }
  }

  function getNotifIconSvg(iconType) {
    switch (iconType) {
      case 'payment':
        return `<svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="14" rx="2.5"/><path d="M3 10h18M7 15h4"/></svg>`;
      case 'request':
        return `<svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 4v12m0 0-4-4m4 4 4-4M4 19h16"/></svg>`;
      case 'family':
        return `<svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="8" r="3.5"/><path d="M3 19c.5-3 3-4.5 6-4.5s5.5 1.5 6 4.5"/><circle cx="16.5" cy="9.5" r="2.5"/><path d="M16 14.5c2 0 4.5 1 5 3.5"/></svg>`;
      case 'security':
        return `<svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`;
      default:
        return `<svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z"/><path d="M10 20.5a2 2 0 0 0 4 0"/></svg>`;
    }
  }

  function renderNotificationsPage() {
    try {
      const statUnread = document.getElementById('notif-stat-unread');
      const statToday = document.getElementById('notif-stat-today');
      const statTotal = document.getElementById('notif-stat-total');
      const listContainer = document.getElementById('notif-list-container');

      const unreadCount = notifications.filter(n => n.unread).length;
      const todayCount = notifications.filter(n => n.today).length;

      if (statUnread) statUnread.textContent = unreadCount;
      if (statToday) statToday.textContent = todayCount;
      if (statTotal) statTotal.textContent = notifications.length;

      updateBellBadge();

      if (!listContainer) return;

      // Filter notifications
      let filtered = notifications.filter(n => {
        if (activeNotifFilter === 'unread') return n.unread;
        if (activeNotifFilter === 'payments') return n.type === 'payments';
        if (activeNotifFilter === 'requests') return n.type === 'requests';
        if (activeNotifFilter === 'family') return n.type === 'family';
        if (activeNotifFilter === 'security') return n.type === 'security';
        return true;
      });

      if (notifSearchQuery) {
        const q = notifSearchQuery.toLowerCase();
        filtered = filtered.filter(n =>
          n.title.toLowerCase().includes(q) ||
          n.desc.toLowerCase().includes(q)
        );
      }

      listContainer.innerHTML = '';

      if (filtered.length === 0) {
        listContainer.innerHTML = `
          <div style="text-align: center; padding: 48px 16px; color: var(--muted);">
            <svg class="ico" viewBox="0 0 24 24" style="width: 32px; height: 32px; margin: 0 auto 10px; opacity: 0.5;">
              <path d="M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z"/>
              <path d="M10 20.5a2 2 0 0 0 4 0"/>
            </svg>
            <strong style="display: block; font-size: 15px; color: var(--text); margin-bottom: 4px;">No notifications found</strong>
            <p style="font-size: 13px;">You are all caught up with your PromptPay account!</p>
          </div>
        `;
        return;
      }

      filtered.forEach(item => {
        const el = document.createElement('div');
        el.className = `notif-item ${item.unread ? 'is-unread' : ''}`;
        el.dataset.id = item.id;

        const iconBoxClass = `notif-icon-box notif-icon-box--${item.iconType || 'system'}`;
        const iconSvg = getNotifIconSvg(item.iconType);

        el.innerHTML = `
          <div class="${iconBoxClass}">
            ${iconSvg}
            ${item.unread ? '<span class="notif-unread-dot" title="Unread"></span>' : ''}
          </div>
          <div class="notif-content">
            <div class="notif-title-row">
              <span class="notif-title">${item.title}</span>
              ${item.unread ? '<span class="badge badge--tomato" style="font-size: 10px; padding: 2px 7px;">New</span>' : ''}
              <span class="notif-time">${item.time}</span>
            </div>
            <p class="notif-desc">${item.desc}</p>
            ${item.actionText ? `
              <div class="notif-actions">
                <button type="button" class="btn btn--ink btn--sm notif-action-btn" data-target="${item.actionTarget || ''}" style="height: 30px; font-size: 12px; padding: 0 12px;">
                  ${item.actionText}
                </button>
              </div>
            ` : ''}
          </div>
          <div class="notif-item__side">
            <button type="button" class="ibtn ibtn--sm ibtn--glass btn-toggle-read" data-id="${item.id}" title="${item.unread ? 'Mark as read' : 'Mark as unread'}" aria-label="Toggle read status">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true" style="width: 14px; height: 14px; opacity: ${item.unread ? '0.9' : '0.4'};">
                <circle cx="12" cy="12" r="8"/>
                ${item.unread ? '<circle cx="12" cy="12" r="3" fill="currentColor"/>' : ''}
              </svg>
            </button>
            <button type="button" class="ibtn ibtn--sm ibtn--glass btn-delete-notif" data-id="${item.id}" title="Dismiss notification" aria-label="Dismiss notification">
              <svg class="ico" viewBox="0 0 24 24" aria-hidden="true" style="width: 14px; height: 14px;">
                <path d="M18 6 6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
        `;

        listContainer.appendChild(el);
      });

      // Wire up Toggle Read status
      listContainer.querySelectorAll('.btn-toggle-read').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const id = btn.dataset.id;
          const targetItem = notifications.find(n => n.id === id);
          if (targetItem) {
            targetItem.unread = !targetItem.unread;
            saveNotifications();
            renderNotificationsPage();
          }
        });
      });

      // Wire up Delete notification
      listContainer.querySelectorAll('.btn-delete-notif').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const id = btn.dataset.id;
          notifications = notifications.filter(n => n.id !== id);
          saveNotifications();
          renderNotificationsPage();
        });
      });

      // Wire up Action buttons
      listContainer.querySelectorAll('.notif-action-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const target = btn.dataset.target;
          if (target) {
            location.hash = `#/${target}`;
            showView(target);
          }
        });
      });

    } catch (err) {
      console.warn("renderNotificationsPage error:", err);
    }
  }

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

    const famUserName = document.getElementById('fam-user-name');
    const famUserEmail = document.getElementById('fam-user-email');
    if (famUserName) famUserName.textContent = profile.name;
    if (famUserEmail) famUserEmail.textContent = profile.email;

    renderFamilyPage();
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
      }
    } catch (err) {
      console.warn("Supabase session check error:", err);
    }
  }

  // Reflect signed-in status and sign-out handler across local & Supabase sessions
  const hasSavedProfile = localStorage.getItem('rapport:profile');
  if ((currentUser || hasSavedProfile) && authLinkText && authLink) {
    authLinkText.textContent = "Sign Out";
    authLink.href = "#";
    authLink.addEventListener('click', async (e) => {
      e.preventDefault();
      if (supabase) {
        try { await supabase.auth.signOut(); } catch (err) {}
      }
      localStorage.removeItem('rapport:profile');
      window.location.reload();
    });
  }

  // 3. Live Synchronization: as user types in the form, update everywhere in real-time
  function checkDirty() {
    if (!inputName || !inputEmail || !selectRole || !inputQuota) return;
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

  // --- Navigation & Routing for Top Tabs and Left Dock ---
  const topTabs = document.querySelectorAll('.tabs .tab');
  const mainTabsPill = document.getElementById('main-tabs-pill');
  const tabsTrack = document.querySelector('.tabs__track');
  const allViews = document.querySelectorAll('.view');
  const allDockBtns = document.querySelectorAll('.dock__btn[data-target]');

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

    // Update Top Tabs
    let foundTab = null;
    topTabs.forEach(t => {
      if (t.dataset.target === targetName) {
        t.classList.add('is-active');
        foundTab = t;
      } else {
        t.classList.remove('is-active');
      }
    });
    updateMainTabsPill(foundTab);

    // Update Dock Buttons
    allDockBtns.forEach(btn => {
      if (btn.dataset.target === targetName) {
        btn.classList.add('is-active');
        btn.setAttribute('aria-current', 'page');
      } else {
        btn.classList.remove('is-active');
        btn.removeAttribute('aria-current');
      }
    });

    if (targetName === 'family') {
      renderFamilyPage();
    }
    if (targetName === 'notification') {
      renderNotificationsPage();
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

  allDockBtns.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const target = btn.dataset.target;
      location.hash = `#/${target}`;
      showView(target);
    });
  });

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

  // --- Top Bell Notification Button ---
  const btnBell = document.getElementById('btn-bell');
  if (btnBell) {
    btnBell.addEventListener('click', () => {
      location.hash = '#/notification';
      showView('notification');
    });
  }

  // --- Notifications Toolbar Handlers ---
  const notifChips = document.querySelectorAll('.notif-filter');
  notifChips.forEach(chip => {
    chip.addEventListener('click', () => {
      notifChips.forEach(c => c.setAttribute('aria-pressed', 'false'));
      chip.setAttribute('aria-pressed', 'true');
      activeNotifFilter = chip.dataset.filter || 'all';
      renderNotificationsPage();
    });
  });

  const notifSearchInput = document.getElementById('notif-search-input');
  if (notifSearchInput) {
    notifSearchInput.addEventListener('input', (e) => {
      notifSearchQuery = e.target.value.trim();
      renderNotificationsPage();
    });
  }

  const btnMarkAllRead = document.getElementById('btn-mark-all-read');
  if (btnMarkAllRead) {
    btnMarkAllRead.addEventListener('click', () => {
      notifications.forEach(n => { n.unread = false; });
      saveNotifications();
      renderNotificationsPage();
    });
  }

  const btnClearAllNotifs = document.getElementById('btn-clear-all-notifs');
  if (btnClearAllNotifs) {
    btnClearAllNotifs.addEventListener('click', () => {
      notifications = [];
      saveNotifications();
      renderNotificationsPage();
    });
  }

  // Initial notification badge update
  updateBellBadge();

  // --- Sync With Supabase Action ---
  const btnSyncSupabase = document.getElementById('btn-sync-supabase');
  if (btnSyncSupabase) {
    btnSyncSupabase.addEventListener('click', async () => {
      btnSyncSupabase.disabled = true;
      const prevHtml = btnSyncSupabase.innerHTML;
      btnSyncSupabase.innerHTML = '<span>Syncing...</span>';
      await syncUsersFromSupabase();
      setTimeout(() => {
        btnSyncSupabase.disabled = false;
        btnSyncSupabase.innerHTML = prevHtml;
      }, 500);
    });
  }
});
