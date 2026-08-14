/**
 * WarrantyWise Warranty Management Controller
 * Handles Warranty Listing, Search, Status Tabs, Filtering, Details, Form Creation/Editing, and Soft Delete
 */
document.addEventListener('DOMContentLoaded', function () {
    // Security check
    if (!AuthUtil.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }

    const path = window.location.pathname;

    if (path.includes('/warranties.html')) {
        initWarrantiesPage();
    } else if (path.includes('/warranty-detail.html')) {
        initWarrantyDetailPage();
    } else if (path.includes('/add-warranty.html')) {
        initWarrantyFormPage();
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

    // Helper: Format Date
    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    /* ==========================================================================
       1. WARRANTIES LIST PAGE (warranties.html)
       ========================================================================== */
    function initWarrantiesPage() {
        let currentPage = 0;
        let activeStatus = '';
        const pageSize = 9;

        const gridEl = document.getElementById('warranties-grid');
        const skeletonEl = document.getElementById('warranties-skeleton');
        const emptyEl = document.getElementById('warranties-empty');
        const errorEl = document.getElementById('warranties-error');
        const searchInput = document.getElementById('warranty-search-input');
        const typeFilter = document.getElementById('warranty-type-filter');
        const resetBtn = document.getElementById('reset-warranty-filters-btn');
        const retryBtn = document.getElementById('retry-warranties-btn');

        // Status Tabs Click Event
        const tabBtns = document.querySelectorAll('#warranty-status-tabs .nav-link');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                tabBtns.forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                activeStatus = this.getAttribute('data-status') || '';
                fetchWarranties(0);
            });
        });

        async function fetchWarranties(page = 0) {
            currentPage = page;
            if (skeletonEl) skeletonEl.classList.remove('d-none');
            if (gridEl) gridEl.innerHTML = '';
            if (emptyEl) emptyEl.classList.add('d-none');
            if (errorEl) errorEl.classList.add('d-none');

            const params = {
                page: page,
                size: pageSize,
                sortBy: 'endDate',
                sortDir: 'asc'
            };

            const searchVal = searchInput ? searchInput.value.trim() : '';
            const typeVal = typeFilter ? typeFilter.value : '';

            let endpoint = '/warranties/search';
            if (activeStatus) params.status = activeStatus;
            if (searchVal) params.search = searchVal;
            if (typeVal) params.warrantyType = typeVal;

            try {
                const data = await ApiClient.get(endpoint, params);
                if (skeletonEl) skeletonEl.classList.add('d-none');

                if (!data || !data.content || data.content.length === 0) {
                    if (emptyEl) emptyEl.classList.remove('d-none');
                    renderPagination(0, 0);
                    return;
                }

                renderWarrantyGrid(data.content);
                renderPagination(data.number, data.totalPages, data.totalElements);
            } catch (err) {
                if (skeletonEl) skeletonEl.classList.add('d-none');
                if (errorEl) {
                    errorEl.classList.remove('d-none');
                    document.getElementById('warranties-error-msg').innerText = err.message || 'Could not fetch warranty records.';
                }
            }
        }

        function renderWarrantyGrid(warranties) {
            let html = '';
            warranties.forEach(w => {
                const productName = w.productName || 'Product';
                const provider = w.provider || 'Provider';
                const days = w.daysRemaining !== null && w.daysRemaining !== undefined ? w.daysRemaining : 0;
                
                let badgeClass = 'badge-status-active';
                let statusText = `${days} Days Left`;
                if (w.status === 'EXPIRED' || days < 0) {
                    badgeClass = 'badge-status-expired';
                    statusText = 'Expired';
                } else if (w.status === 'EXPIRING_SOON' || days <= 30) {
                    badgeClass = 'badge-status-expiring';
                    statusText = `${days} Days (Expiring)`;
                }

                html += `
                    <div class="col-12 col-md-6 col-lg-4">
                        <div class="card-custom p-4 h-100 d-flex flex-column justify-content-between card-custom-hover">
                            <div>
                                <div class="d-flex align-items-center justify-content-between mb-3">
                                    <div class="d-flex align-items-center justify-content-center bg-success-subtle text-success rounded-3" style="width: 44px; height: 44px;">
                                        <i class="bi bi-shield-check fs-4"></i>
                                    </div>
                                    <span class="badge-status ${badgeClass}">${statusText}</span>
                                </div>
                                <h5 class="fw-bold mb-1 text-truncate">${escapeHtml(productName)}</h5>
                                <div class="text-muted-custom small mb-3">Provider: ${escapeHtml(provider)} &bull; ${escapeHtml(w.warrantyType || 'MANUFACTURER')}</div>
                                <div class="small text-muted-custom mb-1">Coverage: ${formatDate(w.startDate)} - ${formatDate(w.endDate)}</div>
                            </div>
                            <div class="pt-3 border-top d-flex align-items-center justify-content-between">
                                <span class="small text-muted-custom">${escapeHtml(w.status)}</span>
                                <a href="/pages/warranty-detail.html?id=${w.id}" class="btn btn-sm btn-outline-secondary">
                                    Policy Details <i class="bi bi-arrow-right ms-1"></i>
                                </a>
                            </div>
                        </div>
                    </div>
                `;
            });
            gridEl.innerHTML = html;
        }

        function renderPagination(page, totalPages, totalElements = 0) {
            const paginationEl = document.getElementById('warranties-pagination');
            const infoEl = document.getElementById('warranty-pagination-info');

            if (infoEl) infoEl.innerText = `Showing page ${page + 1} of ${totalPages || 1} (${totalElements} policies)`;
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
                        fetchWarranties(targetPage);
                    }
                });
            });
        }

        if (searchInput) searchInput.addEventListener('input', () => fetchWarranties(0));
        if (typeFilter) typeFilter.addEventListener('change', () => fetchWarranties(0));
        if (resetBtn) {
            resetBtn.addEventListener('click', () => {
                if (searchInput) searchInput.value = '';
                if (typeFilter) typeFilter.value = '';
                fetchWarranties(0);
            });
        }
        if (retryBtn) retryBtn.addEventListener('click', () => fetchWarranties(0));

        fetchWarranties(0);
    }

    /* ==========================================================================
       2. WARRANTY DETAIL VIEW PAGE (warranty-detail.html)
       ========================================================================== */
    function initWarrantyDetailPage() {
        const urlParams = new URLSearchParams(window.location.search);
        const warrantyId = urlParams.get('id');

        if (!warrantyId) {
            UIComponents.showToast('No warranty policy specified', 'danger');
            window.location.href = '/pages/warranties.html';
            return;
        }

        const skeletonEl = document.getElementById('warranty-detail-skeleton');
        const contentEl = document.getElementById('warranty-detail-content');

        loadWarrantyDetail();

        async function loadWarrantyDetail() {
            try {
                const w = await ApiClient.get(`/warranties/${warrantyId}`);
                if (!w) throw new Error('Warranty policy not found');

                renderDetail(w);
                if (skeletonEl) skeletonEl.classList.add('d-none');
                if (contentEl) contentEl.classList.remove('d-none');
            } catch (err) {
                UIComponents.showToast(err.message || 'Failed to load warranty policy details', 'danger');
                setTimeout(() => window.location.href = '/pages/warranties.html', 1500);
            }
        }

        function renderDetail(w) {
            document.getElementById('breadcrumb-warranty-title').innerText = w.productName || 'Policy Details';
            document.getElementById('detail-product-title').innerText = w.productName || 'Product';
            document.getElementById('detail-provider-name').innerText = w.provider || 'N/A';
            document.getElementById('detail-warranty-type').innerText = w.warrantyType || 'MANUFACTURER';

            document.getElementById('detail-start-date').innerText = formatDate(w.startDate);
            document.getElementById('detail-end-date').innerText = formatDate(w.endDate);

            const days = w.daysRemaining !== null && w.daysRemaining !== undefined ? w.daysRemaining : 0;
            document.getElementById('detail-days-remaining').innerText = days < 0 ? '0 Days (Expired)' : `${days} Days`;

            let badgeClass = 'badge-status-active';
            if (w.status === 'EXPIRED' || days < 0) badgeClass = 'badge-status-expired';
            else if (w.status === 'EXPIRING_SOON' || days <= 30) badgeClass = 'badge-status-expiring';

            document.getElementById('detail-warranty-status').innerHTML = `<span class="badge-status ${badgeClass}">${escapeHtml(w.status || 'ACTIVE')}</span>`;
            document.getElementById('detail-coverage-details').innerText = w.coverageDetails || 'Standard coverage terms apply.';
            document.getElementById('detail-terms-conditions').innerText = w.termsAndConditions || 'No specific exclusions noted.';

            const editBtn = document.getElementById('edit-warranty-btn');
            if (editBtn) editBtn.href = `/pages/add-warranty.html?id=${w.id}`;

            const deleteBtn = document.getElementById('delete-warranty-btn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', function () {
                    UIComponents.showModal(
                        'Confirm Policy Deletion',
                        `<p>Are you sure you want to delete the warranty policy for <strong>${escapeHtml(w.productName)}</strong>?</p>`,
                        `
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-danger" id="confirm-delete-warranty-btn">Delete Policy</button>
                        `
                    );

                    setTimeout(() => {
                        const confirmBtn = document.getElementById('confirm-delete-warranty-btn');
                        if (confirmBtn) {
                            confirmBtn.addEventListener('click', async function () {
                                try {
                                    await ApiClient.delete(`/warranties/${w.id}`);
                                    UIComponents.showToast('Warranty policy deleted successfully', 'success');
                                    window.location.href = '/pages/warranties.html';
                                } catch (e) {
                                    UIComponents.showToast('Failed to delete warranty policy', 'danger');
                                }
                            });
                        }
                    }, 200);
                });
            }
        }
    }

    /* ==========================================================================
       3. ADD / EDIT WARRANTY FORM PAGE (add-warranty.html)
       ========================================================================== */
    function initWarrantyFormPage() {
        const urlParams = new URLSearchParams(window.location.search);
        const warrantyId = urlParams.get('id');
        const isEdit = !!warrantyId;

        const form = document.getElementById('warranty-form');
        const productSelect = document.getElementById('productId');

        if (isEdit) {
            document.getElementById('form-warranty-title').innerText = 'Edit Warranty Policy';
            document.getElementById('form-warranty-breadcrumb').innerText = 'Edit Policy';
            document.getElementById('warranty-submit-text').innerText = 'Update Policy';
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
                    populateForm(warrantyId);
                }
            } catch (e) {
                // Handle option loading errors
            }
        }

        async function populateForm(id) {
            try {
                const w = await ApiClient.get(`/warranties/${id}`);
                if (!w) return;

                document.getElementById('warranty-id').value = w.id;
                if (w.productId) document.getElementById('productId').value = w.productId;
                if (w.warrantyType) document.getElementById('warrantyType').value = w.warrantyType;
                document.getElementById('provider').value = w.provider || '';

                if (w.startDate) document.getElementById('startDate').value = w.startDate;
                if (w.endDate) document.getElementById('endDate').value = w.endDate;
                if (w.status) document.getElementById('status').value = w.status;

                document.getElementById('coverageDetails').value = w.coverageDetails || '';
                document.getElementById('termsAndConditions').value = w.termsAndConditions || '';
            } catch (e) {
                UIComponents.showToast('Could not load warranty details for editing', 'danger');
            }
        }

        if (form) {
            form.addEventListener('submit', async function (e) {
                e.preventDefault();

                const productId = document.getElementById('productId').value;
                const warrantyType = document.getElementById('warrantyType').value;
                const startDate = document.getElementById('startDate').value;
                const endDate = document.getElementById('endDate').value;

                let isValid = true;
                if (!productId) {
                    document.getElementById('productId').classList.add('is-invalid');
                    isValid = false;
                } else {
                    document.getElementById('productId').classList.remove('is-invalid');
                }

                if (!startDate) {
                    document.getElementById('startDate').classList.add('is-invalid');
                    isValid = false;
                } else {
                    document.getElementById('startDate').classList.remove('is-invalid');
                }

                if (!endDate || endDate < startDate) {
                    document.getElementById('endDate').classList.add('is-invalid');
                    isValid = false;
                } else {
                    document.getElementById('endDate').classList.remove('is-invalid');
                }

                if (!isValid) return;

                const submitBtn = document.getElementById('warranty-submit-btn');
                const btnText = document.getElementById('warranty-submit-text');
                const btnSpinner = document.getElementById('warranty-submit-spinner');

                submitBtn.disabled = true;
                btnText.innerText = isEdit ? 'Updating...' : 'Saving...';
                btnSpinner.classList.remove('d-none');

                const payload = {
                    productId: parseInt(productId),
                    warrantyType: warrantyType,
                    provider: document.getElementById('provider').value.trim() || null,
                    policyNumber: document.getElementById('policyNumber')?.value.trim() || null,
                    startDate: startDate,
                    endDate: endDate,
                    coverageDetails: document.getElementById('coverageDetails').value.trim() || null,
                    termsAndConditions: document.getElementById('termsAndConditions').value.trim() || null,
                    status: document.getElementById('status').value || 'ACTIVE'
                };

                try {
                    if (isEdit) {
                        await ApiClient.put(`/warranties/${warrantyId}`, payload);
                        UIComponents.showToast('Warranty policy updated successfully!', 'success');
                    } else {
                        await ApiClient.post('/warranties', payload);
                        UIComponents.showToast('Warranty policy saved successfully!', 'success');
                    }

                    setTimeout(() => window.location.href = '/pages/warranties.html', 800);
                } catch (err) {
                    UIComponents.showToast(err.message || 'Failed to save warranty policy', 'danger');
                } finally {
                    submitBtn.disabled = false;
                    btnText.innerText = isEdit ? 'Update Policy' : 'Save Policy';
                    btnSpinner.classList.add('d-none');
                }
            });
        }
    }
});
