/**
 * WarrantyWise Global Search Controller Logic
 * Executes multi-entity search across Products, Warranties, and Service History
 */
document.addEventListener('DOMContentLoaded', function () {
    if (!AuthUtil.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }

    const searchInput = document.getElementById('unified-search-input');
    const searchBtn = document.getElementById('unified-search-btn');

    const productsList = document.getElementById('search-products-list');
    const warrantiesList = document.getElementById('search-warranties-list');
    const servicesList = document.getElementById('search-services-list');

    const countProducts = document.getElementById('count-products');
    const countWarranties = document.getElementById('count-warranties');
    const countServices = document.getElementById('count-services');

    let debounceTimer;

    function formatCurrency(amount) {
        if (amount === null || amount === undefined) return '₹0';
        return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
    }

    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
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

    async function executeSearch() {
        const query = searchInput ? searchInput.value.trim() : '';

        if (!query) {
            renderDefaultState();
            return;
        }

        // Fetch products, warranties, and services matching query
        try {
            const [productsRes, warrantiesRes, servicesRes] = await Promise.all([
                ApiClient.get('/products/my-products', { search: query, size: 50 }).catch(() => ({ content: [] })),
                ApiClient.get('/warranties/search', { search: query, size: 50 }).catch(() => ({ content: [] })),
                ApiClient.get('/service-records', { size: 50 }).catch(() => ({ content: [] }))
            ]);

            const products = productsRes.content || [];
            const warranties = warrantiesRes.content || [];
            let services = servicesRes.content || [];

            if (query && services.length > 0) {
                services = services.filter(s => 
                    (s.productName && s.productName.toLowerCase().includes(query.toLowerCase())) ||
                    (s.serviceProvider && s.serviceProvider.toLowerCase().includes(query.toLowerCase())) ||
                    (s.serviceType && s.serviceType.toLowerCase().includes(query.toLowerCase()))
                );
            }

            renderProducts(products);
            renderWarranties(warranties);
            renderServices(services);

        } catch (err) {
            UIComponents.showToast('Failed to execute search', 'danger');
        }
    }

    function renderProducts(items) {
        if (countProducts) countProducts.innerText = items.length;
        if (!productsList) return;

        if (items.length === 0) {
            productsList.innerHTML = `<div class="col-12">${UIComponents.renderEmptyState('No Products Found', 'Try adjusting your search keywords.', 'bi-search')}</div>`;
            return;
        }

        let html = '';
        items.forEach(p => {
            html += `
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card-custom p-4 h-100 card-custom-hover">
                        <div class="d-flex align-items-center gap-3 mb-3">
                            <div class="bg-primary-subtle text-primary rounded-3 p-2 d-flex align-items-center justify-content-center" style="width:44px; height:44px;">
                                <i class="bi bi-box-seam fs-4"></i>
                            </div>
                            <div>
                                <h6 class="fw-bold mb-0 text-main">${escapeHtml(p.name)}</h6>
                                <span class="small text-muted-custom">${escapeHtml(p.brand ? p.brand.name : 'Generic')} &bull; ${escapeHtml(p.category ? p.category.name : 'General')}</span>
                            </div>
                        </div>
                        <div class="small text-muted-custom mb-3">Model: ${escapeHtml(p.modelName || 'N/A')} &bull; Serial: ${escapeHtml(p.serialNumber || 'N/A')}</div>
                        <div class="border-top pt-3 d-flex align-items-center justify-content-between">
                            <span class="fw-bold text-success">${formatCurrency(p.purchasePrice)}</span>
                            <a href="/pages/product-detail.html?id=${p.id}" class="btn btn-sm btn-outline-secondary">View Product</a>
                        </div>
                    </div>
                </div>
            `;
        });
        productsList.innerHTML = html;
    }

    function renderWarranties(items) {
        if (countWarranties) countWarranties.innerText = items.length;
        if (!warrantiesList) return;

        if (items.length === 0) {
            warrantiesList.innerHTML = `<div class="col-12">${UIComponents.renderEmptyState('No Warranties Found', 'Try adjusting your search keywords.', 'bi-shield-slash')}</div>`;
            return;
        }

        let html = '';
        items.forEach(w => {
            html += `
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card-custom p-4 h-100 card-custom-hover">
                        <div class="d-flex align-items-center justify-content-between mb-3">
                            <span class="badge-status ${w.status === 'ACTIVE' ? 'badge-status-active' : 'badge-status-expired'}">${escapeHtml(w.status)}</span>
                            <span class="small text-muted-custom">${w.daysRemaining || 0} Days Left</span>
                        </div>
                        <h6 class="fw-bold mb-1 text-main">${escapeHtml(w.productName)}</h6>
                        <div class="small text-muted-custom mb-3">Provider: ${escapeHtml(w.provider || 'N/A')} &bull; Type: ${escapeHtml(w.warrantyType)}</div>
                        <div class="border-top pt-3 d-flex align-items-center justify-content-between">
                            <span class="small text-muted-custom">${formatDate(w.startDate)} - ${formatDate(w.endDate)}</span>
                            <a href="/pages/warranty-detail.html?id=${w.id}" class="btn btn-sm btn-outline-secondary">Policy Details</a>
                        </div>
                    </div>
                </div>
            `;
        });
        warrantiesList.innerHTML = html;
    }

    function renderServices(items) {
        if (countServices) countServices.innerText = items.length;
        if (!servicesList) return;

        if (items.length === 0) {
            servicesList.innerHTML = `<div class="col-12">${UIComponents.renderEmptyState('No Service Records Found', 'Try adjusting your search keywords.', 'bi-tools')}</div>`;
            return;
        }

        let html = '';
        items.forEach(s => {
            html += `
                <div class="col-12 col-md-6 col-lg-4">
                    <div class="card-custom p-4 h-100 card-custom-hover">
                        <div class="d-flex align-items-center justify-content-between mb-3">
                            <span class="badge bg-secondary-subtle text-muted small">${escapeHtml(s.serviceType)}</span>
                            <span class="fw-bold text-success">${formatCurrency(s.cost)}</span>
                        </div>
                        <h6 class="fw-bold mb-1 text-main">${escapeHtml(s.productName)}</h6>
                        <div class="small text-muted-custom mb-3">Provider: ${escapeHtml(s.serviceProvider || 'Self')} &bull; Date: ${formatDate(s.serviceDate)}</div>
                        <div class="border-top pt-3 text-end">
                            <a href="/pages/service-detail.html?id=${s.id}" class="btn btn-sm btn-outline-secondary">View Record</a>
                        </div>
                    </div>
                </div>
            `;
        });
        servicesList.innerHTML = html;
    }

    function renderDefaultState() {
        executeSearch();
    }

    if (searchInput) {
        searchInput.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(executeSearch, 300);
        });
    }

    if (searchBtn) searchBtn.addEventListener('click', executeSearch);

    // Initial search execution
    executeSearch();
});
