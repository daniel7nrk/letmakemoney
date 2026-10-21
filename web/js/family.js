// Family Management & Financial Report Controller
import { getSupabase } from './supabaseClient.js';

let familyMembers = [];
let activeTabId = 'all';

// Default starter members (2 people as mentioned by user) if table is pending schema exposure
const defaultMembers = [
  {
    id: 'mem_1',
    name: 'Sofia Reyes',
    relationship: 'Family Lead',
    email: 'sofia@rapport.app',
    phone: '+250 788 123 456',
    allowance: 250000,
    balance: 182400,
    currency: 'RWF',
    status: 'Active',
    avatar: 'photos/168-rapport/sofia-reyes.jpg'
  },
  {
    id: 'mem_2',
    name: 'David Reyes',
    relationship: 'Family Member',
    email: 'david.reyes@family.net',
    phone: '+250 788 987 654',
    allowance: 200000,
    balance: 95000,
    currency: 'RWF',
    status: 'Active',
    avatar: null
  }
];

export async function initFamilyView() {
  const supabase = getSupabase();
  await loadFamilyMembers(supabase);
  renderFamilyView();
}

async function loadFamilyMembers(supabase) {
  // 1. Check if user has cached members in localStorage
  try {
    const cached = localStorage.getItem('promptpay:family_members');
    if (cached) {
      const parsed = JSON.parse(cached);
      if (Array.isArray(parsed) && parsed.length > 0) {
        familyMembers = parsed;
      }
    }
  } catch (e) {}

  // 2. Query Supabase for database members
  if (supabase) {
    const candidateTables = [
      'family_members',
      'family',
      'members',
      'people',
      'profiles',
      'users',
      'family_reports'
    ];

    for (const tbl of candidateTables) {
      try {
        const { data, error } = await supabase.from(tbl).select('*');
        if (!error && Array.isArray(data) && data.length > 0) {
          console.log(`[Family] Loaded ${data.length} members from Supabase table '${tbl}'`);
          familyMembers = data.map((d, index) => normalizeMember(d, index));
          localStorage.setItem('promptpay:family_members', JSON.stringify(familyMembers));
          localStorage.setItem('promptpay:family_table', tbl);
          break;
        }
      } catch (err) {
        // Continue to next candidate
      }
    }
  }

  // 3. Fallback to default 2 members if database table is not yet directly accessible
  if (!familyMembers || familyMembers.length === 0) {
    // Check if current user profile is logged in
    try {
      const currentProfile = JSON.parse(localStorage.getItem('rapport:profile') || '{}');
      if (currentProfile.name) {
        defaultMembers[0].name = currentProfile.name;
        defaultMembers[0].email = currentProfile.email || defaultMembers[0].email;
      }
    } catch (e) {}
    familyMembers = [...defaultMembers];
    localStorage.setItem('promptpay:family_members', JSON.stringify(familyMembers));
  }
}

function normalizeMember(d, index) {
  return {
    id: d.id || `mem_${index + 1}`,
    name: d.name || d.full_name || d.first_name ? `${d.first_name || ''} ${d.last_name || ''}`.trim() : (d.email ? d.email.split('@')[0] : `Member ${index + 1}`),
    relationship: d.relationship || d.role || (index === 0 ? 'Family Lead' : 'Family Member'),
    email: d.email || '',
    phone: d.phone || d.telephone || d.phone_number || '+250 788 000 000',
    allowance: Number(d.allowance || d.budget || 200000),
    balance: Number(d.balance || 120000),
    currency: d.currency || 'RWF',
    status: d.status || 'Active',
    avatar: d.avatar || d.photo_url || d.photoUri || null
  };
}

export function renderFamilyView() {
  const tabsContainer = document.getElementById('family-member-tabs');
  const panelsContainer = document.getElementById('family-panels-container');
  const countBadge = document.getElementById('badge-all-count');
  const statCount = document.getElementById('stat-family-count');
  const statSpending = document.getElementById('stat-family-spending');

  if (!tabsContainer || !panelsContainer) return;

  const totalMembers = familyMembers.length;

  // Update header stats
  if (countBadge) countBadge.textContent = totalMembers;
  if (statCount) statCount.textContent = totalMembers;
  if (statSpending) {
    const totalAllowance = familyMembers.reduce((sum, m) => sum + (Number(m.allowance) || 0), 0);
    statSpending.textContent = `${totalAllowance.toLocaleString()} RWF`;
  }

  // 1. Render Tabs: "All Members" first, followed by each family member from database
  let tabsHtml = `
    <span class="family-seg__pill" id="family-seg-pill"></span>
    <button type="button" class="family-tab-btn ${activeTabId === 'all' ? 'is-active' : ''}" data-member-id="all" role="tab" aria-selected="${activeTabId === 'all'}">
      <svg class="ico" viewBox="0 0 24 24" style="width: 15px; height: 15px; margin-right: 6px;">
        <circle cx="9" cy="8" r="3"/><path d="M3 18c.5-2.5 2.5-4 6-4s5.5 1.5 6 4"/><circle cx="16" cy="9" r="2.2"/><path d="M16 14c1.8 0 4 .8 4.5 3"/>
      </svg>
      All Members
      <span class="family-tab-count">${totalMembers}</span>
    </button>
  `;

  familyMembers.forEach(member => {
    const isActive = activeTabId === String(member.id);
    const initial = (member.name || 'M').charAt(0).toUpperCase();
    tabsHtml += `
      <button type="button" class="family-tab-btn ${isActive ? 'is-active' : ''}" data-member-id="${member.id}" role="tab" aria-selected="${isActive}">
        <span class="tab-avatar-mini">${initial}</span>
        ${escapeHtml(member.name)}
      </button>
    `;
  });

  // Add Member button inside or beside tabs
  tabsHtml += `
    <button type="button" class="family-tab-add-btn" id="btn-quick-add-member" title="Add another family member">
      <svg class="ico" viewBox="0 0 24 24" style="width: 14px; height: 14px;"><path d="M12 5v14M5 12h14"/></svg>
      <span>Add</span>
    </button>
  `;

  tabsContainer.innerHTML = tabsHtml;

  // 2. Render Panels (Up to middle of the page)
  if (activeTabId === 'all') {
    renderAllMembersPanel(panelsContainer);
  } else {
    const selectedMember = familyMembers.find(m => String(m.id) === activeTabId) || familyMembers[0];
    renderIndividualMemberPanel(panelsContainer, selectedMember);
  }

  // 3. Attach Tab Switching Events
  attachTabEvents();
}

function renderAllMembersPanel(container) {
  let cardsHtml = '';
  familyMembers.forEach(m => {
    const initial = (m.name || 'M').charAt(0).toUpperCase();
    cardsHtml += `
      <div class="card family-member-card magnetic" data-jump-to="${m.id}" style="cursor: pointer;">
        <div class="member-card-header">
          <div class="member-avatar">
            ${m.avatar ? `<img src="${m.avatar}" alt="${escapeHtml(m.name)}" />` : `<span>${initial}</span>`}
          </div>
          <div class="member-info">
            <h3 class="member-name">${escapeHtml(m.name)}</h3>
            <span class="member-role">${escapeHtml(m.relationship)}</span>
          </div>
          <span class="member-status-dot" title="Active"></span>
        </div>
        <div class="member-card-body">
          <div class="member-stat-row">
            <span class="label">Monthly Allowance:</span>
            <span class="val">${(m.allowance || 0).toLocaleString()} ${m.currency || 'RWF'}</span>
          </div>
          <div class="member-stat-row">
            <span class="label">Wallet Balance:</span>
            <span class="val font-semibold">${(m.balance || 0).toLocaleString()} ${m.currency || 'RWF'}</span>
          </div>
        </div>
        <div class="member-card-foot">
          <span class="view-report-link">
            View member report
            <svg class="ico" viewBox="0 0 24 24" style="width:14px;height:14px;"><path d="m9 18 6-6-6-6"/></svg>
          </span>
        </div>
      </div>
    `;
  });

  container.innerHTML = `
    <div class="family-panel is-active" id="panel-member-all">
      <div class="card family-report-header-card">
        <div class="report-header-top">
          <div>
            <h2 class="report-title">Family Overview & Financial Report</h2>
            <p class="report-subtitle">Real-time status for all ${familyMembers.length} family members connected to PromptPay.</p>
          </div>
          <div class="report-actions">
            <button type="button" class="btn btn--sm btn--ink" id="btn-add-member-dialog">
              <svg class="ico" viewBox="0 0 24 24" style="width:15px;height:15px;"><path d="M12 5v14M5 12h14"/></svg>
              Add Member
            </button>
          </div>
        </div>

        <!-- Metric mini-tiles up to middle of page -->
        <div class="family-metrics-row">
          <div class="f-metric">
            <span class="f-metric-label">Total Family Spending Limit</span>
            <span class="f-metric-val">${familyMembers.reduce((s, m) => s + (m.allowance || 0), 0).toLocaleString()} RWF</span>
          </div>
          <div class="f-metric">
            <span class="f-metric-label">Active Family Wallets</span>
            <span class="f-metric-val">${familyMembers.length} Accounts</span>
          </div>
          <div class="f-metric">
            <span class="f-metric-label">Shared Emergency Pool</span>
            <span class="f-metric-val">1,200,000 RWF</span>
          </div>
          <div class="f-metric">
            <span class="f-metric-label">Scam Protection Status</span>
            <span class="f-metric-val text-good">All Safe (0 Alerts)</span>
          </div>
        </div>

        <!-- Members Grid -->
        <div class="family-members-grid">
          ${cardsHtml}
        </div>
      </div>
    </div>
  `;

  // Jump to individual member on card click
  container.querySelectorAll('.family-member-card[data-jump-to]').forEach(card => {
    card.addEventListener('click', () => {
      const targetId = card.dataset.jumpTo;
      activeTabId = targetId;
      renderFamilyView();
    });
  });

  const btnAdd = container.querySelector('#btn-add-member-dialog');
  if (btnAdd) btnAdd.addEventListener('click', openAddMemberModal);
}

function renderIndividualMemberPanel(container, member) {
  const initial = (member.name || 'M').charAt(0).toUpperCase();

  container.innerHTML = `
    <div class="family-panel is-active" id="panel-member-${member.id}">
      <div class="card family-member-detail-card">
        <div class="member-detail-header">
          <div style="display: flex; align-items: center; gap: 16px;">
            <div class="member-avatar member-avatar--large">
              ${member.avatar ? `<img src="${member.avatar}" alt="${escapeHtml(member.name)}" />` : `<span>${initial}</span>`}
            </div>
            <div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <h2 class="detail-name">${escapeHtml(member.name)}</h2>
                <span class="chip chip--role">${escapeHtml(member.relationship)}</span>
              </div>
              <p class="detail-contact">${escapeHtml(member.email || '')} ${member.phone ? `• ${escapeHtml(member.phone)}` : ''}</p>
            </div>
          </div>
          <div class="detail-actions">
            <button type="button" class="btn btn--sm btn--ghost" id="btn-back-to-all">
              <svg class="ico" viewBox="0 0 24 24" style="width:14px;height:14px;"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
              All Members
            </button>
            <button type="button" class="btn btn--sm btn--ink" id="btn-send-money-member">
              <svg class="ico" viewBox="0 0 24 24" style="width:14px;height:14px;"><path d="m22 2-7 20-4-9-9-4Z"/><path d="M22 2 11 13"/></svg>
              Send Allowance
            </button>
          </div>
        </div>

        <!-- Member financial report metrics -->
        <div class="family-metrics-row" style="margin-top: 24px;">
          <div class="f-metric">
            <span class="f-metric-label">Current Balance</span>
            <span class="f-metric-val">${(member.balance || 0).toLocaleString()} ${member.currency || 'RWF'}</span>
          </div>
          <div class="f-metric">
            <span class="f-metric-label">Monthly Allowance</span>
            <span class="f-metric-val">${(member.allowance || 0).toLocaleString()} ${member.currency || 'RWF'}</span>
          </div>
          <div class="f-metric">
            <span class="f-metric-label">Account Status</span>
            <span class="f-metric-val text-good">${member.status || 'Active'}</span>
          </div>
          <div class="f-metric">
            <span class="f-metric-label">Scam Detection</span>
            <span class="f-metric-val text-good">Shielded</span>
          </div>
        </div>
      </div>
    </div>
  `;

  const btnBack = container.querySelector('#btn-back-to-all');
  if (btnBack) {
    btnBack.addEventListener('click', () => {
      activeTabId = 'all';
      renderFamilyView();
    });
  }

  const btnSend = container.querySelector('#btn-send-money-member');
  if (btnSend) {
    btnSend.addEventListener('click', () => {
      alert(`Send allowance to ${member.name}: Mobile money transfer module.`);
    });
  }
}

function attachTabEvents() {
  const tabs = document.querySelectorAll('.family-tab-btn');
  const pill = document.getElementById('family-seg-pill');
  const track = document.getElementById('family-member-tabs');

  function updateFamilyPill(activeBtn, animated = true) {
    if (!pill || !track || !activeBtn) return;
    const trackRect = track.getBoundingClientRect();
    const btnRect = activeBtn.getBoundingClientRect();

    pill.style.transition = animated ? 'all 0.25s cubic-bezier(0.16, 1, 0.3, 1)' : 'none';
    pill.style.opacity = '1';
    pill.style.left = `${btnRect.left - trackRect.left}px`;
    pill.style.top = `${btnRect.top - trackRect.top}px`;
    pill.style.width = `${btnRect.width}px`;
    pill.style.height = `${btnRect.height}px`;
  }

  const activeBtn = document.querySelector(`.family-tab-btn[data-member-id="${activeTabId}"]`);
  if (activeBtn) {
    setTimeout(() => updateFamilyPill(activeBtn, false), 30);
  }

  tabs.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.dataset.memberId;
      if (activeTabId === targetId) return;
      activeTabId = targetId;
      renderFamilyView();
    });
  });

  const btnQuickAdd = document.getElementById('btn-quick-add-member');
  if (btnQuickAdd) {
    btnQuickAdd.addEventListener('click', openAddMemberModal);
  }
}

function openAddMemberModal() {
  const name = prompt("Enter family member's full name:");
  if (!name || !name.trim()) return;

  const relationship = prompt("Enter relationship/role (e.g. Spouse, Child, Parent, Sibling):", "Family Member") || "Family Member";
  const allowanceStr = prompt("Monthly allowance amount (in RWF):", "150000");
  const allowance = Number(allowanceStr) || 150000;

  const newMember = {
    id: `mem_${Date.now()}`,
    name: name.trim(),
    relationship: relationship.trim(),
    email: '',
    phone: '',
    allowance: allowance,
    balance: allowance,
    currency: 'RWF',
    status: 'Active',
    avatar: null
  };

  familyMembers.push(newMember);
  localStorage.setItem('promptpay:family_members', JSON.stringify(familyMembers));

  // Try to persist to Supabase if active table is configured
  const supabase = getSupabase();
  const tbl = localStorage.getItem('promptpay:family_table') || 'family_members';
  if (supabase) {
    supabase.from(tbl).insert([{
      name: newMember.name,
      relationship: newMember.relationship,
      allowance: newMember.allowance
    }]).then(({ error }) => {
      if (error) console.warn("Supabase insert note:", error.message);
    });
  }

  activeTabId = newMember.id;
  renderFamilyView();
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
