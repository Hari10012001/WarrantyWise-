/**
 * WarrantyWise Reports & CSV Export Controller
 * Connects to ReportController REST APIs
 */
document.addEventListener('DOMContentLoaded', function () {
    if (!AuthUtil.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }

    let activeTab = 'products';

    const headerEl = document.getElementById('reports-table-header');
    const bodyEl = document.getElementById('reports-table-body');
    const skeletonEl = document.getElementById('reports-skeleton');
    const tableContainerEl = document.getElementById('reports-table-container');
    const emptyEl = document.getElementById('reports-empty');
    const errorEl = document.getElementById('reports-error');

    const catFilter = document.getElementById('report-category-filter');
    const brandFilter = document.getElementById('report-brand-filter');
    const startDateInput = document.getElementById('report-start-date');
    const endDateInput = document.getElementById('report-end-date');
    const exportBtn = document.getElementById('export-report-csv-btn');
    const retryBtn = document.getElementById('retry-reports-btn');

    // Populate Category & Brand dropdown filters
    loadFilterOptions();

    async function loadFilterOptions() {
        try {
            const [categories, brands] = await Promise.all([
                ApiClient.get('/categories/active').catch(() => []),
                ApiClient.get('/brands/active').catch(() => [])
            ]);

            if (catFilter && Array.isArray(categories)) {
                categories.forEach(c => {
                    const opt = document.createElement('option');
                    opt.value = c.id;
                    opt.innerText = c.name;
                    catFilter.appendChild(opt);
                });
            }

            if (brandFilter && Array.isArray(brands)) {
                brands.forEach(b => {
                    const opt = document.createElement('option');
                    opt.value = b.id;
                    opt.innerText = b.name;
                    brandFilter.appendChild(opt);
                });
            }
        } catch (e) {
            // Ignore filter loading errors
        }
    }

    // Tab Navigation
    const tabBtns = document.querySelectorAll('#report-type-tabs .nav-link');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            tabBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            activeTab = this.getAttribute('data-tab') || 'products';
            fetchReportData();
        });
    });

    if (catFilter) catFilter.addEventListener('change', fetchReportData);
    if (brandFilter) brandFilter.addEventListener('change', fetchReportData);
    if (startDateInput) startDateInput.addEventListener('change', fetchReportData);
    if (endDateInput) endDateInput.addEventListener('change', fetchReportData);
    if (retryBtn) retryBtn.addEventListener('click', fetchReportData);

    if (exportBtn) {
        exportBtn.addEventListener('click', async function () {
            const params = buildFilterParams();
            const exportEndpoint = `/reports/${activeTab}/export-csv`;
            const filename = `${activeTab}_report.csv`;

            try {
                UIComponents.showToast('Generating CSV export...', 'info');
                await ApiClient.downloadCsv(exportEndpoint, params, filename);
                UIComponents.showToast('CSV report downloaded successfully!', 'success');
            } catch (err) {
                UIComponents.showToast('Failed to export CSV report', 'danger');
            }
        });
    }

    function buildFilterParams() {
        const params = {};
        if (catFilter && catFilter.value) params.categoryId = catFilter.value;
        if (brandFilter && brandFilter.value) params.brandId = brandFilter.value;
        if (startDateInput && startDateInput.value) params.startDate = startDateInput.value;
        if (endDateInput && endDateInput.value) params.endDate = endDateInput.value;
        return params;
    }

    async function fetchReportData() {
        if (skeletonEl) skeletonEl.classList.remove('d-none');
        if (tableContainerEl) tableContainerEl.classList.add('d-none');
        if (emptyEl) emptyEl.classList.add('d-none');
        if (errorEl) errorEl.classList.add('d-none');

        const params = buildFilterParams();
        const endpoint = `/reports/${activeTab}`;

        try {
            const data = await ApiClient.get(endpoint, params);
            if (skeletonEl) skeletonEl.classList.add('d-none');

            if (!data || !Array.isArray(data) || data.length === 0) {
                if (emptyEl) emptyEl.classList.remove('d-none');
                return;
            }

            renderReportTable(data);
            if (tableContainerEl) tableContainerEl.classList.remove('d-none');
        } catch (err) {
            if (skeletonEl) skeletonEl.classList.add('d-none');
            if (errorEl) {
                errorEl.classList.remove('d-none');
                document.getElementById('reports-error-msg').innerText = err.message || 'Could not fetch report data.';
            }
        }
    }

    function renderReportTable(rows) {
        if (activeTab === 'products') {
            headerEl.innerHTML = `
                <th>Product Name</th>
                <th>Category</th>
                <th>Brand</th>
                <th>Purchase Date</th>
                <th>Purchase Price</th>
                <th>Warranty Status</th>
                <th>Service Cost</th>
                <th>Services</th>
            `;

            let bodyHtml = '';
            rows.forEach(r => {
                bodyHtml += `
                    <tr>
                        <td class="fw-semibold text-main">${escapeHtml(r.name)}</td>
                        <td>${escapeHtml(r.categoryName || 'General')}</td>
                        <td>${escapeHtml(r.brandName || 'Generic')}</td>
                        <td>${formatDate(r.purchaseDate)}</td>
                        <td class="fw-semibold text-success">${formatCurrency(r.purchasePrice)}</td>
                        <td><span class="badge-status badge-status-active">${escapeHtml(r.activeWarrantyStatus || 'NO_WARRANTY')}</span></td>
                        <td>${formatCurrency(r.totalServiceCost)}</td>
                        <td>${r.serviceCount || 0}</td>
                    </tr>
                `;
            });
            bodyEl.innerHTML = bodyHtml;
        } else if (activeTab === 'warranties') {
            headerEl.innerHTML = `
                <th>Product</th>
                <th>Type</th>
                <th>Provider</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Status</th>
                <th>Days Left</th>
            `;

            let bodyHtml = '';
            rows.forEach(r => {
                const days = r.daysRemaining || 0;
                const badgeClass = days <= 7 ? 'badge-status-expired' : (days <= 30 ? 'badge-status-expiring' : 'badge-status-active');

                bodyHtml += `
                    <tr>
                        <td class="fw-semibold text-main">${escapeHtml(r.productName)}</td>
                        <td>${escapeHtml(r.warrantyType)}</td>
                        <td>${escapeHtml(r.provider || 'N/A')}</td>
                        <td>${formatDate(r.startDate)}</td>
                        <td>${formatDate(r.endDate)}</td>
                        <td><span class="badge-status ${badgeClass}">${escapeHtml(r.status)}</span></td>
                        <td class="fw-bold">${days} days</td>
                    </tr>
                `;
            });
            bodyEl.innerHTML = bodyHtml;
        } else if (activeTab === 'services') {
            headerEl.innerHTML = `
                <th>Product</th>
                <th>Service Type</th>
                <th>Provider</th>
                <th>Service Date</th>
                <th>Cost</th>
                <th>Status</th>
            `;

            let bodyHtml = '';
            rows.forEach(r => {
                bodyHtml += `
                    <tr>
                        <td class="fw-semibold text-main">${escapeHtml(r.productName)}</td>
                        <td>${escapeHtml(r.serviceType)}</td>
                        <td>${escapeHtml(r.serviceProvider || 'N/A')}</td>
                        <td>${formatDate(r.serviceDate)}</td>
                        <td class="fw-semibold text-success">${formatCurrency(r.cost)}</td>
                        <td><span class="badge-status badge-status-active">${escapeHtml(r.serviceStatus || 'COMPLETED')}</span></td>
                    </tr>
                `;
            });
            bodyEl.innerHTML = bodyHtml;
        } else if (activeTab === 'lifecycle') {
            headerEl.innerHTML = `
                <th>Product</th>
                <th>Category</th>
                <th>Brand</th>
                <th>Purchase Price</th>
                <th>Warranties</th>
                <th>Warranty Health</th>
                <th>Total Services</th>
                <th>Service Cost</th>
            `;

            let bodyHtml = '';
            rows.forEach(r => {
                const healthScore = r.warrantyHealthScore != null ? Math.round(r.warrantyHealthScore) : 0;
                let healthBadge = 'badge-status-active';
                if (healthScore < 40) healthBadge = 'badge-status-expired';
                else if (healthScore < 70) healthBadge = 'badge-status-expiring';

                bodyHtml += `
                    <tr>
                        <td class="fw-semibold text-main">${escapeHtml(r.productName)}</td>
                        <td>${escapeHtml(r.categoryName || 'General')}</td>
                        <td>${escapeHtml(r.brandName || 'Generic')}</td>
                        <td>${formatCurrency(r.purchasePrice)}</td>
                        <td>${r.totalWarranties || 0}</td>
                        <td><span class="badge-status ${healthBadge}">${healthScore}%</span></td>
                        <td>${r.totalServices || 0}</td>
                        <td class="fw-bold text-success">${formatCurrency(r.totalServiceCost)}</td>
                    </tr>
                `;
            });
            bodyEl.innerHTML = bodyHtml;
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

    function formatCurrency(amount) {
        if (amount === null || amount === undefined) return '₹0';
        return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
    }

    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    fetchReportData();
});
