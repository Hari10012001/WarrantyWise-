/**
 * WarrantyWise Service History Controller
 * Handles Service Record Listing, Analytics KPI Cards, Search, Filtering, Details, Form Creation/Editing, and Soft Delete
 */
document.addEventListener('DOMContentLoaded', function () {
    // Security check
    if (!AuthUtil.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }

    const path = window.location.pathname;

    if (path.includes('/service-history.html')) {
        initServiceHistoryPage();
    } else if (path.includes('/service-detail.html')) {
        initServiceDetailPage();
    } else if (path.includes('/add-service-record.html')) {
        initServiceFormPage();
    }

    // Helper: XSS Safe Escaping
    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // Helper: Format Currency
    function formatCurrency(amount) {
        if (amount === null || amount === undefined) return '₹0';
        return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
    }

    // Helper: Format Date
    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    /* ==========================================================================
       1. SERVICE HISTORY INVENTORY & ANALYTICS PAGE (service-history.html)
       ========================================================================== */
    function initServiceHistoryPage() {
        let currentPage = 0;
        const pageSize = 9;

        const gridEl = document.getElementById('services-grid');
        const skeletonEl = document.getElementById('services-skeleton');
        const emptyEl = document.getElementById('services-empty');
        const errorEl = document.getElementById('services-error');
        const searchInput = document.getElementById('service-search-input');
        const typeFilter = document.getElementById('service-type-filter');
        const resetBtn = document.getElementById('reset-service-filters-btn');
        const retryBtn = document.getElementById('retry-services-btn');

        // Load Service Analytics Summary
        loadAnalyticsSummary();

        async function loadAnalyticsSummary() {
            try {
                const summary = await ApiClient.get('/service-records/analytics/summary');
                if (summary) {
                    document.getElementById('analytics-total-cost').innerText = formatCurrency(summary.totalServiceCost);
                    document.getElementById('analytics-total-services').innerText = `${summary.totalServices || 0} Total Records`;
                    document.getElementById('analytics-avg-cost').innerText = formatCurrency(summary.averageServiceCost);
                    document.getElementById('analytics-month-cost').innerText = formatCurrency(summary.costThisMonth);
                    document.getElementById('analytics-month-services').innerText = `${summary.servicesThisMonth || 0} Services this month`;
                    document.getElementById('analytics-overdue-count').innerText = summary.overdueServicesCount || 0;
                }
            } catch (e) {
                // Ignore summary load errors gracefully
            }
        }

        async function fetchServices(page = 0) {
            currentPage = page;
            if (skeletonEl) skeletonEl.classList.remove('d-none');
            if (gridEl) gridEl.innerHTML = '';
            if (emptyEl) emptyEl.classList.add('d-none');
            if (errorEl) errorEl.classList.add('d-none');

            const params = {
                page: page,
                size: pageSize,
                sortBy: 'serviceDate',
                sortDir: 'desc'
            };

            const searchVal = searchInput ? searchInput.value.trim() : '';
            const typeVal = typeFilter ? typeFilter.value : '';

            let endpoint = '/service-records/search';
            if (searchVal) params.search = searchVal;
            if (typeVal) params.serviceType = typeVal;

            try {
                const data = await ApiClient.get(endpoint, params);
                if (skeletonEl) skeletonEl.classList.add('d-none');

                if (!data || !data.content || data.content.length === 0) {
                    if (emptyEl) emptyEl.classList.remove('d-none');
                    renderPagination(0, 0);
                    return;
                }

                renderServiceGrid(data.content);
                renderPagination(data.number, data.totalPages, data.totalElements);
            } catch (err) {
                if (skeletonEl) skeletonEl.classList.add('d-none');
                if (errorEl) {
                    errorEl.classList.remove('d-none');
                    document.getElementById('services-error-msg').innerText = err.message || 'Could not fetch service records.';
                }
            }
        }

        function renderServiceGrid(services) {
            let html = '';
            services.forEach(s => {
                const productName = s.productName || 'Product';
                const provider = s.serviceProvider || 'Service Center';
                const cost = formatCurrency(s.cost);
                const status = (s.serviceStatus || 'COMPLETED').toUpperCase();

                let badgeClass = 'badge-status-active';
                if (status === 'IN_PROGRESS' || status === 'SCHEDULED') badgeClass = 'badge-status-expiring';
                else if (status === 'CANCELLED') badgeClass = 'badge-status-expired';

                html += `
                    <div class="col-12 col-md-6 col-lg-4">
                        <div class="card-custom p-4 h-100 d-flex flex-column justify-content-between card-custom-hover">
                            <div>
                                <div class="d-flex align-items-center justify-content-between mb-3">
                                    <div class="d-flex align-items-center justify-content-center bg-info-subtle text-info rounded-3" style="width: 44px; height: 44px;">
                                        <i class="bi bi-tools fs-4"></i>
                                    </div>
                                    <span class="badge-status ${badgeClass}">${status}</span>
                                </div>
                                <h5 class="fw-bold mb-1 text-truncate">${escapeHtml(productName)}</h5>
                                <div class="text-muted-custom small mb-2">Provider: ${escapeHtml(provider)} &bull; ${escapeHtml(s.serviceType || 'MAINTENANCE')}</div>
                                <div class="fs-5 fw-bold text-success mb-3">${cost}</div>
                                <div class="small text-muted-custom">Date: ${formatDate(s.serviceDate)}</div>
                            </div>
                            <div class="pt-3 border-top d-flex align-items-center justify-content-between">
                                <span class="small text-muted-custom">Next: ${formatDate(s.nextServiceDate)}</span>
                                <a href="/pages/service-detail.html?id=${s.id}" class="btn btn-sm btn-outline-secondary">
                                    Record Details <i class="bi bi-arrow-right ms-1"></i>
                                </a>
                            </div>
                        </div>
                    </div>
                `;
            });
            gridEl.innerHTML = html;
        }

        function renderPagination(page, totalPages, totalElements = 0) {
            const paginationEl = document.getElementById('services-pagination');
            const infoEl = document.getElementById('service-pagination-info');

            if (infoEl) infoEl.innerText = `Showing page ${page + 1} of ${totalPages || 1} (${totalElements} service logs)`;
            if (!paginationEl) return;

            if (totalPages <= 1) {
                paginationEl.innerHTML = '';
                return;
            }

            let html = `<li class="page-item ${page === 0 ? 'disabled' : ''}"><a class="page-link" href="#" data-page="${page - 1}">Previous</a></li>`;
            for (let i = 0; i < totalPages; i++) {
                html += `<li class="page-item ${i === page ? 'active' : ''}"><a class="page-link" href="#" data-page="${i}">${i + 1}</a></li>`;
            }
            html += `<li class="page-item ${page === totalPages - 1 ? 'disabled' : ''}"><a class="page-link" href="#" data-page="${page + 1}">Next</a></li>`;

            paginationEl.innerHTML = html;
            paginationEl.querySelectorAll('.page-link').forEach(link => {
                link.addEventListener('click', function (e) {
                    e.preventDefault();
                    const targetPage = parseInt(this.getAttribute('data-page'));
                    if (!isNaN(targetPage) && targetPage >= 0 && targetPage < totalPages) {
                        fetchServices(targetPage);
                    }
                });
            });
        }

        if (searchInput) searchInput.addEventListener('input', () => fetchServices(0));
        if (typeFilter) typeFilter.addEventListener('change', () => fetchServices(0));
        if (resetBtn) {
            resetBtn.addEventListener('click', () => {
                if (searchInput) searchInput.value = '';
                if (typeFilter) typeFilter.value = '';
                fetchServices(0);
            });
        }
        if (retryBtn) retryBtn.addEventListener('click', () => fetchServices(0));

        fetchServices(0);
    }

    /* ==========================================================================
       2. SERVICE DETAIL VIEW PAGE (service-detail.html)
       ========================================================================== */
    function initServiceDetailPage() {
        const urlParams = new URLSearchParams(window.location.search);
        const serviceId = urlParams.get('id');

        if (!serviceId) {
            UIComponents.showToast('No service record specified', 'danger');
            window.location.href = '/pages/service-history.html';
            return;
        }

        const skeletonEl = document.getElementById('service-detail-skeleton');
        const contentEl = document.getElementById('service-detail-content');

        loadServiceDetail();

        async function loadServiceDetail() {
            try {
                const s = await ApiClient.get(`/service-records/${serviceId}`);
                if (!s) throw new Error('Service record not found');

                renderDetail(s);
                if (skeletonEl) skeletonEl.classList.add('d-none');
                if (contentEl) contentEl.classList.remove('d-none');
            } catch (err) {
                UIComponents.showToast(err.message || 'Failed to load service record details', 'danger');
                setTimeout(() => window.location.href = '/pages/service-history.html', 1500);
            }
        }

        function renderDetail(s) {
            document.getElementById('breadcrumb-service-title').innerText = s.productName || 'Record Details';
            document.getElementById('detail-service-product').innerText = s.productName || 'Product';
            document.getElementById('detail-service-provider').innerText = s.serviceProvider || 'Service Provider';
            document.getElementById('detail-service-type').innerText = s.serviceType || 'ROUTINE_MAINTENANCE';

            document.getElementById('detail-service-cost').innerText = formatCurrency(s.cost);
            document.getElementById('detail-service-date').innerText = formatDate(s.serviceDate);
            document.getElementById('detail-completion-date').innerText = formatDate(s.completionDate);
            document.getElementById('detail-next-service-date').innerText = formatDate(s.nextServiceDate);

            const status = (s.serviceStatus || 'COMPLETED').toUpperCase();
            let badgeClass = 'badge-status-active';
            if (status === 'IN_PROGRESS' || status === 'SCHEDULED') badgeClass = 'badge-status-expiring';
            else if (status === 'CANCELLED') badgeClass = 'badge-status-expired';

            document.getElementById('detail-service-status-badge').innerHTML = `<span class="badge-status ${badgeClass}">${escapeHtml(status)}</span>`;
            document.getElementById('detail-service-description').innerText = s.description || 'Routine maintenance and inspection.';
            document.getElementById('detail-work-performed').innerText = s.workPerformed || 'N/A';
            document.getElementById('detail-parts-replaced').innerText = s.partsReplaced || 'None';

            const editBtn = document.getElementById('edit-service-btn');
            if (editBtn) editBtn.href = `/pages/add-service-record.html?id=${s.id}`;

            const deleteBtn = document.getElementById('delete-service-btn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', function () {
                    UIComponents.showModal(
                        'Confirm Record Deletion',
                        `<p>Are you sure you want to delete the service record for <strong>${escapeHtml(s.productName)}</strong>?</p>`,
                        `
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-danger" id="confirm-delete-service-btn">Delete Record</button>
                        `
                    );

                    setTimeout(() => {
                        const confirmBtn = document.getElementById('confirm-delete-service-btn');
                        if (confirmBtn) {
                            confirmBtn.addEventListener('click', async function () {
                                try {
                                    await ApiClient.delete(`/service-records/${s.id}`);
                                    UIComponents.showToast('Service record deleted successfully', 'success');
                                    window.location.href = '/pages/service-history.html';
                                } catch (e) {
                                    UIComponents.showToast('Failed to delete service record', 'danger');
                                }
                            });
                        }
                    }, 200);
                });
            }
        }
    }

    /* ==========================================================================
       3. ADD / EDIT SERVICE RECORD FORM PAGE (add-service-record.html)
       ========================================================================== */
    function initServiceFormPage() {
        const urlParams = new URLSearchParams(window.location.search);
        const serviceId = urlParams.get('id');
        const isEdit = !!serviceId;

        const form = document.getElementById('service-form');
        const productSelect = document.getElementById('productId');

        if (isEdit) {
            document.getElementById('form-service-title').innerText = 'Edit Service Record';
            document.getElementById('form-service-breadcrumb').innerText = 'Edit Record';
            document.getElementById('service-submit-text').innerText = 'Update Record';
        }

        loadProducts();

        async function loadProducts() {
            try {
                const res = await ApiClient.get('/products/my-products', { size: 100 });
                if (res && res.content && productSelect) {
                    res.content.forEach(p => {
                        const opt = document.createElement('option');
                        opt.value = p.id;
                        opt.innerText = `${p.name} (${p.brand ? p.brand.name : 'Generic'})`;
                        productSelect.appendChild(opt);
                    });
                }

                if (isEdit) {
                    populateForm(serviceId);
                }
            } catch (e) {
                // Handle option loading errors
            }
        }

        async function populateForm(id) {
            try {
                const s = await ApiClient.get(`/service-records/${id}`);
                if (!s) return;

                document.getElementById('service-id').value = s.id;
                if (s.productId) document.getElementById('productId').value = s.productId;
                if (s.serviceType) document.getElementById('serviceType').value = s.serviceType;
                document.getElementById('serviceProvider').value = s.serviceProvider || '';

                if (s.serviceDate) document.getElementById('serviceDate').value = s.serviceDate;
                if (s.completionDate) document.getElementById('completionDate').value = s.completionDate;
                if (s.cost) document.getElementById('cost').value = s.cost;
                if (s.nextServiceDate) document.getElementById('nextServiceDate').value = s.nextServiceDate;
                if (s.serviceStatus) document.getElementById('serviceStatus').value = s.serviceStatus;

                document.getElementById('description').value = s.description || '';
                document.getElementById('workPerformed').value = s.workPerformed || '';
                document.getElementById('partsReplaced').value = s.partsReplaced || '';
                document.getElementById('notes').value = s.notes || '';
            } catch (e) {
                UIComponents.showToast('Could not load service record for editing', 'danger');
            }
        }

        if (form) {
            form.addEventListener('submit', async function (e) {
                e.preventDefault();

                const productId = document.getElementById('productId').value;
                const serviceType = document.getElementById('serviceType').value;
                const serviceDate = document.getElementById('serviceDate').value;
                const completionDate = document.getElementById('completionDate').value;

                let isValid = true;
                if (!productId) {
                    document.getElementById('productId').classList.add('is-invalid');
                    isValid = false;
                } else {
                    document.getElementById('productId').classList.remove('is-invalid');
                }

                if (!serviceDate) {
                    document.getElementById('serviceDate').classList.add('is-invalid');
                    isValid = false;
                } else {
                    document.getElementById('serviceDate').classList.remove('is-invalid');
                }

                if (completionDate && completionDate < serviceDate) {
                    document.getElementById('completionDate').classList.add('is-invalid');
                    isValid = false;
                } else if (document.getElementById('completionDate')) {
                    document.getElementById('completionDate').classList.remove('is-invalid');
                }

                if (!isValid) return;

                const submitBtn = document.getElementById('service-submit-btn');
                const btnText = document.getElementById('service-submit-text');
                const btnSpinner = document.getElementById('service-submit-spinner');

                submitBtn.disabled = true;
                btnText.innerText = isEdit ? 'Updating...' : 'Saving...';
                btnSpinner.classList.remove('d-none');

                const payload = {
                    productId: parseInt(productId),
                    serviceType: serviceType,
                    serviceProvider: document.getElementById('serviceProvider').value.trim() || null,
                    serviceDate: serviceDate,
                    completionDate: completionDate || null,
                    cost: document.getElementById('cost').value ? parseFloat(document.getElementById('cost').value) : 0,
                    description: document.getElementById('description').value.trim() || null,
                    workPerformed: document.getElementById('workPerformed').value.trim() || null,
                    partsReplaced: document.getElementById('partsReplaced').value.trim() || null,
                    nextServiceDate: document.getElementById('nextServiceDate').value || null,
                    serviceStatus: document.getElementById('serviceStatus').value || 'COMPLETED',
                    notes: document.getElementById('notes').value.trim() || null
                };

                try {
                    if (isEdit) {
                        await ApiClient.put(`/service-records/${serviceId}`, payload);
                        UIComponents.showToast('Service record updated successfully!', 'success');
                    } else {
                        await ApiClient.post('/service-records', payload);
                        UIComponents.showToast('Service record saved successfully!', 'success');
                    }

                    setTimeout(() => window.location.href = '/pages/service-history.html', 800);
                } catch (err) {
                    UIComponents.showToast(err.message || 'Failed to save service record', 'danger');
                } finally {
                    submitBtn.disabled = false;
                    btnText.innerText = isEdit ? 'Update Record' : 'Save Record';
                    btnSpinner.classList.add('d-none');
                }
            });
        }
    }
});
