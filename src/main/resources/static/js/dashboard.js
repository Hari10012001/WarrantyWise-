/**
 * WarrantyWise Dashboard Controller Logic
 * Connects exclusively to backend REST API: GET /api/v1/dashboard
 */
document.addEventListener('DOMContentLoaded', function () {
    if (!AuthUtil.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }

    const skeletonEl = document.getElementById('dashboard-skeleton');
    const contentEl = document.getElementById('dashboard-content');
    const errorEl = document.getElementById('dashboard-error-container');
    const errorMsgEl = document.getElementById('dashboard-error-message');
    const retryBtn = document.getElementById('retry-dashboard-btn');

    function showLoading() {
        if (skeletonEl) skeletonEl.classList.remove('d-none');
        if (contentEl) contentEl.classList.add('d-none');
        if (errorEl) errorEl.classList.add('d-none');
    }

    function showError(msg) {
        if (skeletonEl) skeletonEl.classList.add('d-none');
        if (contentEl) contentEl.classList.add('d-none');
        if (errorEl) errorEl.classList.remove('d-none');
        if (errorMsgEl) errorMsgEl.innerText = msg || 'Failed to load dashboard metrics.';
    }

    function showContent() {
        if (skeletonEl) skeletonEl.classList.add('d-none');
        if (errorEl) errorEl.classList.add('d-none');
        if (contentEl) contentEl.classList.remove('d-none');
    }

    function formatCurrency(amount) {
        if (amount === null || amount === undefined) return '₹0';
        return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
    }

    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    // Main Fetch Function
    async function loadDashboardData() {
        showLoading();
        try {
            const data = await ApiClient.get('/dashboard');
            if (!data) {
                showError('Empty dashboard payload received from server.');
                return;
            }

            renderDashboard(data);
            showContent();
        } catch (err) {
            showError(err.message || 'Error communicating with backend dashboard service.');
        }
    }

    // Render Dashboard Response
    function renderDashboard(data) {
        const overview = data.overview || {};
        const warrantySummary = data.warrantySummary || {};

        // 1. Overview KPIs
        const totalProducts = overview.totalProducts || 0;
        const activeProducts = overview.activeProducts || 0;
        const activeWarranties = overview.activeWarranties || 0;
        const expiringWarranties = overview.expiringSoonWarranties || 0;
        const expiredWarranties = overview.expiredWarranties || 0;
        const serviceCost = overview.totalServiceCost || 0;
        const serviceRecordsCount = overview.totalServiceRecords || 0;

        document.getElementById('stat-total-products').innerText = totalProducts;
        document.getElementById('stat-active-products').innerText = `${activeProducts} Active`;
        document.getElementById('stat-active-warranties').innerText = activeWarranties;
        document.getElementById('stat-expiring-warranties').innerText = expiringWarranties;
        document.getElementById('stat-service-cost').innerText = formatCurrency(serviceCost);
        document.getElementById('stat-service-records-count').innerText = `${serviceRecordsCount} Total Records`;

        const coveragePercent = totalProducts > 0 ? Math.round((activeProducts / totalProducts) * 100) : 0;
        document.getElementById('stat-coverage-percent').innerText = `${coveragePercent}% Coverage`;

        // 2. WARRANTY HEALTH SCORE CALCULATOR
        const totalWarranties = overview.totalWarranties || 0;
        let healthScore = 100;
        if (totalWarranties > 0) {
            const expiredRatio = expiredWarranties / totalWarranties;
            const expiringRatio = expiringWarranties / totalWarranties;
            healthScore = Math.max(0, Math.round(100 - (expiredRatio * 60) - (expiringRatio * 30)));
        }

        document.getElementById('health-score-val').innerText = `${healthScore} / 100`;
        const healthBar = document.getElementById('health-progress-bar');
        const healthBadge = document.getElementById('health-status-badge');

        if (healthBar) {
            healthBar.style.width = `${healthScore}%`;
            if (healthScore < 40) {
                healthBar.className = 'progress-bar bg-danger';
                if (healthBadge) { healthBadge.innerText = 'CRITICAL'; healthBadge.className = 'badge-status badge-status-expired'; }
            } else if (healthScore < 75) {
                healthBar.className = 'progress-bar bg-warning';
                if (healthBadge) { healthBadge.innerText = 'ATTENTION'; healthBadge.className = 'badge-status badge-status-expiring'; }
            } else {
                healthBar.className = 'progress-bar bg-success';
                if (healthBadge) { healthBadge.innerText = 'GOOD'; healthBadge.className = 'badge-status badge-status-active'; }
            }
        }

        document.getElementById('health-active-count').innerText = activeWarranties;
        document.getElementById('health-expiring-count').innerText = expiringWarranties;
        document.getElementById('health-expired-count').innerText = expiredWarranties;

        // 3. TODAY'S REMINDERS LIST
        const todaysReminders = data.todaysReminders || [];
        const todaysContainer = document.getElementById('todays-reminders-list');
        if (todaysContainer) {
            if (todaysReminders.length === 0) {
                todaysContainer.innerHTML = UIComponents.renderEmptyState('No Reminders Today', 'You have no scheduled reminders due today.', 'bi-check2-circle');
            } else {
                let html = '<div class="d-flex flex-column gap-2">';
                todaysReminders.forEach(r => {
                    html += `
                        <div class="p-3 bg-card-highlight rounded-3 border-start border-warning border-4">
                            <div class="d-flex align-items-center justify-content-between mb-1">
                                <span class="fw-semibold text-main small">${escapeHtml(r.title || 'Reminder')}</span>
                                <span class="badge bg-warning-subtle text-warning small">Today</span>
                            </div>
                            <div class="small text-muted-custom">${escapeHtml(r.message || '')}</div>
                        </div>
                    `;
                });
                html += '</div>';
                todaysContainer.innerHTML = html;
            }
        }

        // 4. UPCOMING EXPIRY TIMELINE
        const timelineList = data.warrantyTimeline || [];
        const timelineContainer = document.getElementById('expiry-timeline-list');
        if (timelineContainer) {
            if (!timelineList || timelineList.length === 0) {
                timelineContainer.innerHTML = UIComponents.renderEmptyState('No Expiring Warranties', 'All active warranties are safely covered for the next 30 days.', 'bi-shield-check');
            } else {
                let html = '<div class="timeline-custom">';
                timelineList.forEach(item => {
                    const daysLeft = item.daysRemaining || 0;
                    let badgeClass = daysLeft <= 0 ? 'badge-status-expired' : (daysLeft <= 7 ? 'badge-status-expired' : 'badge-status-expiring');
                    let badgeText = daysLeft < 0 ? 'EXPIRED' : (daysLeft === 0 ? 'TODAY' : `${daysLeft} DAYS LEFT`);

                    html += `
                        <div class="timeline-item">
                            <div class="timeline-item-dot"></div>
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="fw-semibold text-main">${escapeHtml(item.productName)}</div>
                                    <div class="small text-muted-custom">Provider: ${escapeHtml(item.provider || 'N/A')} &bull; Expiry: ${formatDate(item.endDate)}</div>
                                </div>
                                <span class="badge-status ${badgeClass}">${badgeText}</span>
                            </div>
                        </div>
                    `;
                });
                html += '</div>';
                timelineContainer.innerHTML = html;
            }
        }

        // 5. RECENTLY ADDED PRODUCTS
        const recentProducts = data.recentProducts || [];
        const recentProductsContainer = document.getElementById('recent-products-list');
        if (recentProductsContainer) {
            if (recentProducts.length === 0) {
                recentProductsContainer.innerHTML = UIComponents.renderEmptyState('No Products Yet', 'Add your first product to start tracking warranties.', 'bi-box-seam');
            } else {
                let html = '<div class="d-flex flex-column gap-2">';
                recentProducts.forEach(p => {
                    html += `
                        <div class="d-flex align-items-center justify-content-between p-3 bg-card-highlight rounded-3 border">
                            <div class="d-flex align-items-center gap-3">
                                <div class="bg-primary-subtle text-primary rounded-3 p-2 d-flex align-items-center justify-content-center" style="width:38px; height:38px;">
                                    <i class="bi bi-box-seam fs-5"></i>
                                </div>
                                <div>
                                    <div class="fw-semibold text-main">${escapeHtml(p.name)}</div>
                                    <div class="small text-muted-custom">${escapeHtml(p.brand ? p.brand.name : 'Generic')} &bull; ${formatCurrency(p.purchasePrice)}</div>
                                </div>
                            </div>
                            <a href="/pages/product-detail.html?id=${p.id}" class="btn btn-sm btn-outline-secondary">View</a>
                        </div>
                    `;
                });
                html += '</div>';
                recentProductsContainer.innerHTML = html;
            }
        }

        // 6. RECENT ACTIVITY LOG
        const recentActivities = data.recentActivities || [];
        const activityContainer = document.getElementById('recent-activity-list');
        if (activityContainer) {
            if (recentActivities.length === 0) {
                activityContainer.innerHTML = UIComponents.renderEmptyState('No Activity', 'No activity logged yet.', 'bi-clock-history');
            } else {
                let html = '<div class="d-flex flex-column gap-2">';
                recentActivities.forEach(act => {
                    html += `
                        <div class="d-flex align-items-center justify-content-between p-2 border-bottom">
                            <div>
                                <div class="small fw-semibold text-main">${escapeHtml(act.description)}</div>
                                <div class="small text-muted-custom" style="font-size:0.75rem;">${formatDate(act.timestamp)}</div>
                            </div>
                            <span class="badge bg-secondary-subtle text-muted small">${escapeHtml(act.action)}</span>
                        </div>
                    `;
                });
                html += '</div>';
                activityContainer.innerHTML = html;
            }
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    if (retryBtn) retryBtn.addEventListener('click', loadDashboardData);

    loadDashboardData();
});
