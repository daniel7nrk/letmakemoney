// Zero-Dependency Pure SVG Interactive Charts Component

export class Charts {
  /**
   * Render an interactive Area/Line Timeline Chart of expenses
   */
  static renderTimeline(container, transactions, currencyCode = "RWF") {
    if (!container) return;

    // Filter confirmed expense transactions
    const expenses = transactions
      .filter(t => t.status === "CONFIRMED" && t.amount < 0)
      .sort((a, b) => a.dateEpochMillis - b.dateEpochMillis);

    if (expenses.length === 0) {
      container.innerHTML = `<div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--text-muted);font-size:13px;">No expense history available</div>`;
      return;
    }

    // Group by Day (last 7 or 14 days)
    const dayMap = new Map();
    for (const t of expenses) {
      const d = new Date(t.dateEpochMillis);
      const key = `${d.getMonth() + 1}/${d.getDate()}`;
      const curr = dayMap.get(key) || 0;
      dayMap.set(key, curr + Math.abs(t.amount));
    }

    const labels = Array.from(dayMap.keys());
    const values = Array.from(dayMap.values());

    const width = 600;
    const height = 240;
    const padding = { top: 20, right: 30, bottom: 40, left: 60 };

    const maxVal = Math.max(...values, 1000);
    const chartW = width - padding.left - padding.right;
    const chartH = height - padding.top - padding.bottom;

    const points = values.map((val, idx) => {
      const x = padding.left + (idx / Math.max(values.length - 1, 1)) * chartW;
      const y = padding.top + chartH - (val / maxVal) * chartH;
      return { x, y, val, label: labels[idx] };
    });

    // Build SVG path
    let linePath = `M ${points[0].x} ${points[0].y}`;
    for (let i = 1; i < points.length; i++) {
      const p = points[i];
      const prev = points[i - 1];
      const cx = (prev.x + p.x) / 2;
      linePath += ` C ${cx} ${prev.y}, ${cx} ${p.y}, ${p.x} ${p.y}`;
    }

    const areaPath = `${linePath} L ${points[points.length - 1].x} ${padding.top + chartH} L ${points[0].x} ${padding.top + chartH} Z`;

    const svg = `
      <svg class="svg-chart" viewBox="0 0 ${width} ${height}">
        <defs>
          <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#2f5c50" stop-opacity="0.32"/>
            <stop offset="100%" stop-color="#2f5c50" stop-opacity="0.0"/>
          </linearGradient>
        </defs>

        <!-- Horizontal Grid Lines -->
        <line x1="${padding.left}" y1="${padding.top}" x2="${width - padding.right}" y2="${padding.top}" class="chart-grid-line" />
        <line x1="${padding.left}" y1="${padding.top + chartH / 2}" x2="${width - padding.right}" y2="${padding.top + chartH / 2}" class="chart-grid-line" />
        <line x1="${padding.left}" y1="${padding.top + chartH}" x2="${width - padding.right}" y2="${padding.top + chartH}" stroke="var(--border)" stroke-width="1.5" />

        <!-- Y Axis Labels -->
        <text x="${padding.left - 10}" y="${padding.top + 4}" font-size="11" fill="var(--text-muted)" text-anchor="end">${Math.round(maxVal).toLocaleString()}</text>
        <text x="${padding.left - 10}" y="${padding.top + chartH / 2 + 4}" font-size="11" fill="var(--text-muted)" text-anchor="end">${Math.round(maxVal / 2).toLocaleString()}</text>
        <text x="${padding.left - 10}" y="${padding.top + chartH + 4}" font-size="11" fill="var(--text-muted)" text-anchor="end">0</text>

        <!-- Area & Line -->
        <path d="${areaPath}" fill="url(#areaGrad)" />
        <path d="${linePath}" class="chart-line" />

        <!-- Dots and X Axis Labels -->
        ${points.map((p) => `
          <circle cx="${p.x}" cy="${p.y}" r="5" class="chart-dot">
            <title>${p.label}: ${p.val.toLocaleString()} ${currencyCode}</title>
          </circle>
          <text x="${p.x}" y="${padding.top + chartH + 20}" font-size="11" fill="var(--text-muted)" text-anchor="middle">${p.label}</text>
        `).join('')}
      </svg>
    `;

    container.innerHTML = svg;
  }

  /**
   * Render a Category Spending Donut Chart with Legend
   */
  static renderCategoryDonut(container, transactions, categories, currencyCode = "RWF") {
    if (!container) return;

    const catTotals = new Map();
    let totalExpense = 0;

    for (const t of transactions) {
      if (t.status === "CONFIRMED" && t.amount < 0) {
        const amt = Math.abs(t.amount);
        const cat = t.category || "Uncategorized";
        catTotals.set(cat, (catTotals.get(cat) || 0) + amt);
        totalExpense += amt;
      }
    }

    if (totalExpense === 0) {
      container.innerHTML = `<div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--text-muted);font-size:13px;">No expense data for breakdown</div>`;
      return;
    }

    const items = Array.from(catTotals.entries())
      .map(([name, amount]) => {
        const catDef = categories.find(c => c.name === name);
        const color = catDef ? catDef.colorHex : "#94A3B8";
        const icon = catDef ? catDef.iconName : "🏷️";
        const percent = Math.round((amount / totalExpense) * 100);
        return { name, amount, color, icon, percent };
      })
      .sort((a, b) => b.amount - a.amount);

    // SVG Donut calculation
    const size = 180;
    const strokeWidth = 28;
    const radius = (size - strokeWidth) / 2;
    const circumference = 2 * Math.PI * radius;

    let offset = 0;
    const circlesSvg = items.map((item) => {
      const strokeDasharray = `${(item.percent / 100) * circumference} ${circumference}`;
      const strokeDashoffset = -offset;
      offset += (item.percent / 100) * circumference;

      return `
        <circle cx="${size / 2}" cy="${size / 2}" r="${radius}"
          fill="transparent"
          stroke="${item.color}"
          stroke-width="${strokeWidth}"
          stroke-dasharray="${strokeDasharray}"
          stroke-dashoffset="${strokeDashoffset}"
          style="transition: stroke-width 0.2s ease; cursor: pointer;"
        >
          <title>${item.name}: ${item.amount.toLocaleString()} ${currencyCode} (${item.percent}%)</title>
        </circle>
      `;
    }).join('');

    const donutSvg = `
      <div style="display:flex;align-items:center;justify-content:center;position:relative;">
        <svg width="${size}" height="${size}" viewBox="0 0 ${size} ${size}" style="transform: rotate(-90deg);">
          ${circlesSvg}
        </svg>
        <div style="position:absolute;display:flex;flex-direction:column;align-items:center;pointer-events:none;">
          <span style="font-size:11px;color:var(--text-muted);font-weight:600;">TOTAL</span>
          <span style="font-size:15px;font-weight:700;color:var(--text-main);">${Math.round(totalExpense).toLocaleString()}</span>
        </div>
      </div>
    `;

    const legendHtml = `
      <div class="donut-legend">
        ${items.slice(0, 4).map(item => `
          <div class="legend-item">
            <div class="legend-label-group">
              <span class="legend-color" style="background: ${item.color};"></span>
              <span style="font-weight: 500;">${item.name}</span>
            </div>
            <span style="font-weight: 700; color: var(--text-secondary);">${item.percent}%</span>
          </div>
        `).join('')}
      </div>
    `;

    container.innerHTML = donutSvg + legendHtml;
  }
}
