/**
 * WarrantyWise Product Management Controller
 * Handles Product Listing, Search, Filtering, Details, Form Creation/Editing, and Soft Delete
 */

// Global stub so the inline onclick on Save Product button never throws ReferenceError
window.handleProductSaveExplicit = function(e) {
    if (e) e.preventDefault();
    // Will be replaced by the real handler once DOMContentLoaded fires
    console.warn('handleProductSaveExplicit: not yet initialized, retrying...');
};
document.addEventListener('DOMContentLoaded', function () {
    // Security check
    if (!AuthUtil.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }

    const path = window.location.pathname;

    if (path.includes('/products.html')) {
        initProductsPage();
    } else if (path.includes('/product-detail.html')) {
        initProductDetailPage();
    } else if (path.includes('/add-product.html')) {
        initProductFormPage();
    }

    // Helper: XSS Safe HTML Escaping
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
       1. PRODUCTS INVENTORY LIST PAGE (products.html)
       ========================================================================== */
    function initProductsPage() {
        let currentPage = 0;
        const pageSize = 9;

        const gridEl = document.getElementById('products-grid');
        const skeletonEl = document.getElementById('products-skeleton');
        const emptyEl = document.getElementById('products-empty');
        const errorEl = document.getElementById('products-error');
        const searchInput = document.getElementById('product-search-input');
        const categoryFilter = document.getElementById('product-category-filter');
        const brandFilter = document.getElementById('product-brand-filter');
        const resetBtn = document.getElementById('reset-filters-btn');
        const retryBtn = document.getElementById('retry-products-btn');

        // Load Categories & Brands into Filters
        loadFilterDropdowns();

        async function loadFilterDropdowns() {
            try {
                const [categories, brands] = await Promise.all([
                    ApiClient.get('/categories/active').catch(() => []),
                    ApiClient.get('/brands/active').catch(() => [])
                ]);

                if (categoryFilter && Array.isArray(categories)) {
                    categories.forEach(c => {
                        const opt = document.createElement('option');
                        opt.value = c.id;
                        opt.innerText = c.name;
                        categoryFilter.appendChild(opt);
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
                // Ignore dropdown loading errors
            }
        }

        async function fetchProducts(page = 0) {
            currentPage = page;
            if (skeletonEl) skeletonEl.classList.remove('d-none');
            if (gridEl) gridEl.innerHTML = '';
            if (emptyEl) emptyEl.classList.add('d-none');
            if (errorEl) errorEl.classList.add('d-none');

            const params = {
                page: page,
                size: pageSize,
                sortBy: 'createdAt',
                sortDir: 'desc',
                isActive: true
            };

            const searchVal = searchInput ? searchInput.value.trim() : '';
            const catVal = categoryFilter ? categoryFilter.value : '';
            const brandVal = brandFilter ? brandFilter.value : '';

            let endpoint = '/products/my-products';
            if (searchVal || catVal || brandVal) {
                endpoint = '/products/search';
                if (searchVal) params.search = searchVal;
                if (catVal) params.categoryId = catVal;
                if (brandVal) params.brandId = brandVal;
            }

            try {
                const data = await ApiClient.get(endpoint, params);
                if (skeletonEl) skeletonEl.classList.add('d-none');

                if (!data || !data.content || data.content.length === 0) {
                    if (emptyEl) emptyEl.classList.remove('d-none');
                    renderPagination(0, 0);
                    return;
                }

                renderProductGrid(data.content);
                renderPagination(data.number, data.totalPages, data.totalElements);
            } catch (err) {
                if (skeletonEl) skeletonEl.classList.add('d-none');
                if (errorEl) {
                    errorEl.classList.remove('d-none');
                    document.getElementById('products-error-msg').innerText = err.message || 'Could not fetch products.';
                }
            }
        }

        function renderProductGrid(products) {
            let html = '';
            products.forEach(p => {
                const categoryName = p.category ? p.category.name : 'General';
                const brandName = p.brand ? p.brand.name : 'Generic';
                const price = formatCurrency(p.purchasePrice);
                const warrantyCount = p.warrantyCount || 0;
                const statusBadge = warrantyCount > 0 ? '<span class="badge-status badge-status-active">Covered</span>' : '<span class="badge-status badge-status-none">No Warranty</span>';

                html += `
                    <div class="col-12 col-md-6 col-lg-4">
                        <div class="card-custom p-4 h-100 d-flex flex-column justify-content-between card-custom-hover">
                            <div>
                                <div class="d-flex align-items-center justify-content-between mb-3">
                                    <div class="d-flex align-items-center justify-content-center bg-primary-subtle text-primary rounded-3" style="width: 44px; height: 44px;">
                                        <i class="bi bi-box-seam fs-4"></i>
                                    </div>
                                    ${statusBadge}
                                </div>
                                <h5 class="fw-bold mb-1 text-truncate">${escapeHtml(p.name)}</h5>
                                <div class="text-muted-custom small mb-3">${escapeHtml(brandName)} &bull; ${escapeHtml(categoryName)}</div>
                                <div class="fs-5 fw-bold text-success mb-3">${price}</div>
                            </div>
                            <div class="pt-3 border-top d-flex align-items-center justify-content-between">
                                <span class="small text-muted-custom">Purchased: ${formatDate(p.purchaseDate)}</span>
                                <div class="d-flex gap-2">
                                    <a href="/pages/product-detail.html?id=${p.id}" class="btn btn-sm btn-outline-secondary">
                                        Details <i class="bi bi-arrow-right ms-1"></i>
                                    </a>
                                    <button type="button" class="btn btn-sm btn-outline-danger quick-delete-btn" data-id="${p.id}" data-name="${escapeHtml(p.name)}" title="Delete Product">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            });
            gridEl.innerHTML = html;

            // Bind quick delete buttons
            gridEl.querySelectorAll('.quick-delete-btn').forEach(btn => {
                btn.addEventListener('click', async function () {
                    const id = this.getAttribute('data-id');
                    const name = this.getAttribute('data-name');
                    if (!confirm(`Delete "${name}"? This will soft-delete the product.`)) return;
                    try {
                        await ApiClient.delete(`/products/${id}`);
                        UIComponents.showToast(`"${name}" deleted successfully`, 'success');
                        fetchProducts(currentPage);
                    } catch (e) {
                        UIComponents.showToast(e.message || 'Failed to delete product', 'danger');
                    }
                });
            });
        }

        function renderPagination(page, totalPages, totalElements = 0) {
            const paginationEl = document.getElementById('products-pagination');
            const infoEl = document.getElementById('pagination-info');

            if (infoEl) infoEl.innerText = `Showing page ${page + 1} of ${totalPages || 1} (${totalElements} products)`;
            if (!paginationEl) return;

            if (totalPages <= 1) {
                paginationEl.innerHTML = '';
                return;
            }

            let html = `
                <li class="page-item ${page === 0 ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="${page - 1}">Previous</a>
                </li>
            `;

            for (let i = 0; i < totalPages; i++) {
                html += `
                    <li class="page-item ${i === page ? 'active' : ''}">
                        <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
                    </li>
                `;
            }

            html += `
                <li class="page-item ${page === totalPages - 1 ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="${page + 1}">Next</a>
                </li>
            `;

            paginationEl.innerHTML = html;

            paginationEl.querySelectorAll('.page-link').forEach(link => {
                link.addEventListener('click', function (e) {
                    e.preventDefault();
                    const targetPage = parseInt(this.getAttribute('data-page'));
                    if (!isNaN(targetPage) && targetPage >= 0 && targetPage < totalPages) {
                        fetchProducts(targetPage);
                    }
                });
            });
        }

        // Event listeners for filters
        if (searchInput) searchInput.addEventListener('input', () => fetchProducts(0));
        if (categoryFilter) categoryFilter.addEventListener('change', () => fetchProducts(0));
        if (brandFilter) brandFilter.addEventListener('change', () => fetchProducts(0));
        if (resetBtn) {
            resetBtn.addEventListener('click', () => {
                if (searchInput) searchInput.value = '';
                if (categoryFilter) categoryFilter.value = '';
                if (brandFilter) brandFilter.value = '';
                fetchProducts(0);
            });
        }
        if (retryBtn) retryBtn.addEventListener('click', () => fetchProducts(0));

        // Initial fetch
        fetchProducts(0);
    }

    /* ==========================================================================
       2. PRODUCT DETAIL VIEW PAGE (product-detail.html)
       ========================================================================== */
    function initProductDetailPage() {
        const urlParams = new URLSearchParams(window.location.search);
        const productId = urlParams.get('id');

        if (!productId) {
            UIComponents.showToast('No product specified', 'danger');
            window.location.href = '/pages/products.html';
            return;
        }

        const skeletonEl = document.getElementById('product-detail-skeleton');
        const contentEl = document.getElementById('product-detail-content');

        loadProductDetail();

        async function loadProductDetail() {
            try {
                const product = await ApiClient.get(`/products/${productId}`);
                if (!product) throw new Error('Product not found');

                renderDetail(product);
                loadAttachments();

                if (skeletonEl) skeletonEl.classList.add('d-none');
                if (contentEl) contentEl.classList.remove('d-none');
            } catch (err) {
                UIComponents.showToast(err.message || 'Failed to load product details', 'danger');
                setTimeout(() => window.location.href = '/pages/products.html', 1500);
            }
        }

        function renderDetail(p) {
            document.getElementById('breadcrumb-product-name').innerText = p.name;
            document.getElementById('detail-product-name').innerText = p.name;
            document.getElementById('detail-brand-name').innerText = p.brand ? p.brand.name : 'Generic';
            document.getElementById('detail-category-name').innerText = p.category ? p.category.name : 'General';
            document.getElementById('detail-purchase-date').innerText = formatDate(p.purchaseDate);

            document.getElementById('detail-model-name').innerText = p.modelName || '-';
            document.getElementById('detail-model-number').innerText = p.modelNumber || '-';
            document.getElementById('detail-serial-number').innerText = p.serialNumber || '-';
            document.getElementById('detail-imei-number').innerText = p.imeiNumber || '-';
            document.getElementById('detail-color').innerText = p.color || '-';
            document.getElementById('detail-barcode').innerText = p.barcode || '-';

            document.getElementById('detail-purchase-price').innerText = formatCurrency(p.purchasePrice);
            document.getElementById('detail-purchase-mode').innerText = p.purchaseMode || '-';
            document.getElementById('detail-retailer').innerText = p.retailer || '-';
            document.getElementById('detail-invoice-number').innerText = p.invoiceNumber || '-';

            document.getElementById('detail-status-badge').innerHTML = `<span class="badge-status badge-status-active">${escapeHtml(p.productStatus || 'IN_USE')}</span>`;
            document.getElementById('detail-condition-badge').innerHTML = `<span class="badge-status badge-status-none">${escapeHtml(p.productCondition || 'NEW')}</span>`;

            // Edit button link
            const editBtn = document.getElementById('edit-product-btn');
            if (editBtn) editBtn.href = `/pages/add-product.html?id=${p.id}`;

            // Delete button confirmation
            const deleteBtn = document.getElementById('delete-product-btn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', function () {
                    UIComponents.showModal(
                        'Confirm Deletion',
                        `<p>Are you sure you want to delete <strong>${escapeHtml(p.name)}</strong>? This will soft-delete the product.</p>`,
                        `
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-danger" id="confirm-delete-btn">Delete Product</button>
                        `
                    );

                    setTimeout(() => {
                        const confirmBtn = document.getElementById('confirm-delete-btn');
                        if (confirmBtn) {
                            confirmBtn.addEventListener('click', async function () {
                                try {
                                    await ApiClient.delete(`/products/${p.id}`);
                                    UIComponents.showToast('Product soft-deleted successfully', 'success');
                                    window.location.href = '/pages/products.html';
                                } catch (e) {
                                    UIComponents.showToast('Failed to delete product', 'danger');
                                }
                            });
                        }
                    }, 200);
                });
            }
        } // end renderDetail

        async function loadProductSubData() {
            loadAttachments();
            loadAssociatedWarranties();
            loadAssociatedServices();
        }

        async function loadAssociatedWarranties() {
            const container = document.getElementById('product-warranties-list');
            if (!container) return;
            try {
                const list = await ApiClient.get(`/warranties/product/${productId}`).catch(() => []);
                if (!list || list.length === 0) {
                    container.innerHTML = UIComponents.renderEmptyState('No Warranty Registered', 'No active warranty policy attached to this product yet.', 'bi-shield-slash');
                    return;
                }
                let html = '<div class="d-flex flex-column gap-2">';
                list.forEach(w => {
                    html += `
                        <div class="p-3 bg-card-highlight rounded-3 border d-flex align-items-center justify-content-between">
                            <div>
                                <div class="fw-semibold text-main">${escapeHtml(w.provider || 'Policy')} (${escapeHtml(w.warrantyType)})</div>
                                <div class="small text-muted-custom">Coverage: ${formatDate(w.startDate)} - ${formatDate(w.endDate)}</div>
                            </div>
                            <span class="badge-status ${w.status === 'ACTIVE' ? 'badge-status-active' : 'badge-status-expired'}">${escapeHtml(w.status)}</span>
                        </div>
                    `;
                });
                html += '</div>';
                container.innerHTML = html;
            } catch (e) {
                // Ignore errors
            }
        }

        async function loadAssociatedServices() {
            const container = document.getElementById('product-services-list');
            if (!container) return;
            try {
                const list = await ApiClient.get(`/service-records/product/${productId}`).catch(() => []);
                if (!list || list.length === 0) {
                    container.innerHTML = UIComponents.renderEmptyState('No Service History', 'No maintenance or repair records logged for this product.', 'bi-tools');
                    return;
                }
                let html = '<div class="d-flex flex-column gap-2">';
                list.forEach(s => {
                    html += `
                        <div class="p-3 bg-card-highlight rounded-3 border d-flex align-items-center justify-content-between">
                            <div>
                                <div class="fw-semibold text-main">${escapeHtml(s.serviceType)} &bull; ${escapeHtml(s.serviceProvider || 'Self')}</div>
                                <div class="small text-muted-custom">Date: ${formatDate(s.serviceDate)} &bull; Cost: ${formatCurrency(s.cost)}</div>
                            </div>
                            <span class="badge bg-secondary-subtle text-muted small">${escapeHtml(s.serviceStatus || 'COMPLETED')}</span>
                        </div>
                    `;
                });
                html += '</div>';
                container.innerHTML = html;
            } catch (e) {
                // Ignore errors
            }
        }

        async function loadAttachments() {
            try {
                const attachments = await ApiClient.get(`/products/${productId}/attachments`);
                const container = document.getElementById('attachments-list');
                if (!container) return;

                if (!attachments || attachments.length === 0) {
                    container.innerHTML = '<div class="col-12 text-muted-custom small py-3 text-center">No invoice or receipt documents uploaded yet.</div>';
                    return;
                }

                let html = '';
                attachments.forEach(att => {
                    html += `
                        <div class="col-12 col-sm-6 col-md-4">
                            <div class="card-custom p-3 d-flex align-items-center justify-content-between">
                                <div class="d-flex align-items-center gap-2 overflow-hidden">
                                    <i class="bi bi-file-earmark-text text-primary fs-4"></i>
                                    <div class="text-truncate small fw-semibold">${escapeHtml(att.fileName || 'Document')}</div>
                                </div>
                            </div>
                        </div>
                    `;
                });
                container.innerHTML = html;
            } catch (e) {
                // Ignore attachment load errors
            }
        }

        loadProductSubData();

        // Drag and Drop Dropzone Initialization
        const dropzoneEl = document.getElementById('document-dropzone');
        const fileInputEl = document.getElementById('attachment-file-input');

        if (dropzoneEl && fileInputEl) {
            UIComponents.initDropzone(dropzoneEl, fileInputEl, async function (file) {
                if (!file) return;

                const formData = new FormData();
                formData.append('file', file);
                formData.append('attachmentType', 'INVOICE');

                try {
                    UIComponents.showToast('Uploading attachment...', 'info');
                    await ApiClient.upload(`/products/${productId}/attachments`, formData);
                    UIComponents.showToast('Document uploaded successfully!', 'success');
                    loadAttachments();
                } catch (e) {
                    UIComponents.showToast(e.message || 'Failed to upload document', 'danger');
                }
            });
        }
    } // end initProductDetailPage

    /* ==========================================================================
       3. ADD / EDIT PRODUCT FORM PAGE (add-product.html)
       ========================================================================== */
    function initProductFormPage() {
        const urlParams = new URLSearchParams(window.location.search);
        const productId = urlParams.get('id');
        const isEdit = !!productId;

        const form = document.getElementById('product-form');
        const categorySelect = document.getElementById('categoryId');
        const brandSelect = document.getElementById('brandId');

        if (isEdit) {
            document.getElementById('form-header-title').innerText = 'Edit Product';
            document.getElementById('form-breadcrumb-title').innerText = 'Edit Product';
            document.getElementById('product-submit-text').innerText = 'Update Product';
        }

        loadFormOptions();

        async function loadFormOptions() {
            try {
                let [categories, brands] = await Promise.all([
                    ApiClient.get('/categories/active').catch(() => []),
                    ApiClient.get('/brands/active').catch(() => [])
                ]);

                const defaultCategories = [
                    { id: 1, name: 'Mobile & Smartphones' },
                    { id: 2, name: 'Laptop & Computers' },
                    { id: 3, name: 'Home Appliances' },
                    { id: 4, name: 'Audio & Wearables' },
                    { id: 5, name: 'Television & Display' },
                    { id: 6, name: 'Camera & Photography' },
                    { id: 7, name: 'Gaming & Consoles' },
                    { id: 8, name: 'Kitchen Appliances' },
                    { id: 9, name: 'General' }
                ];

                const defaultBrands = [
                    { id: 1, name: 'Samsung' },
                    { id: 2, name: 'Apple' },
                    { id: 3, name: 'Sony' },
                    { id: 4, name: 'LG' },
                    { id: 5, name: 'Dell' },
                    { id: 6, name: 'HP' },
                    { id: 7, name: 'OnePlus' },
                    { id: 8, name: 'Xiaomi' },
                    { id: 9, name: 'Lenovo' },
                    { id: 10, name: 'Asus' },
                    { id: 11, name: 'Oppo' },
                    { id: 12, name: 'Vivo' },
                    { id: 13, name: 'Realme' },
                    { id: 14, name: 'Nokia' },
                    { id: 15, name: 'Motorola' },
                    { id: 16, name: 'Bosch' },
                    { id: 17, name: 'Whirlpool' },
                    { id: 18, name: 'Generic' }
                ];

                // Always use fetched list if non-empty, otherwise use defaults
                const catList = (Array.isArray(categories) && categories.length > 0) ? categories : defaultCategories;
                const brandList = (Array.isArray(brands) && brands.length > 0) ? brands : defaultBrands;

                if (categorySelect) {
                    categorySelect.innerHTML = '<option value="">Select Category</option>';
                    catList.forEach(c => {
                        const opt = document.createElement('option');
                        opt.value = c.id;
                        opt.innerText = c.name;
                        categorySelect.appendChild(opt);
                    });
                }

                if (brandSelect) {
                    brandSelect.innerHTML = '<option value="">Select Brand (Optional)</option>';
                    brandList.forEach(b => {
                        const opt = document.createElement('option');
                        opt.value = b.id;
                        opt.innerText = b.name;
                        brandSelect.appendChild(opt);
                    });
                }

                if (isEdit) {
                    populateEditForm(productId);
                }
            } catch (e) {
                console.warn('loadFormOptions error:', e);
            }
        }

        async function populateEditForm(id) {
            try {
                const p = await ApiClient.get(`/products/${id}`);
                if (!p) return;

                document.getElementById('product-id').value = p.id;
                document.getElementById('name').value = p.name || '';
                if (p.category) document.getElementById('categoryId').value = p.category.id;
                if (p.brand) document.getElementById('brandId').value = p.brand.id;

                document.getElementById('modelName').value = p.modelName || '';
                document.getElementById('modelNumber').value = p.modelNumber || '';
                document.getElementById('serialNumber').value = p.serialNumber || '';
                document.getElementById('color').value = p.color || '';
                document.getElementById('imeiNumber').value = p.imeiNumber || '';
                document.getElementById('barcode').value = p.barcode || '';

                if (p.purchaseDate) document.getElementById('purchaseDate').value = p.purchaseDate;
                if (p.purchasePrice) document.getElementById('purchasePrice').value = p.purchasePrice;
                if (p.purchaseMode) document.getElementById('purchaseMode').value = p.purchaseMode;
                document.getElementById('retailer').value = p.retailer || '';

                if (p.productStatus) document.getElementById('productStatus').value = p.productStatus;
                if (p.productCondition) document.getElementById('productCondition').value = p.productCondition;
                document.getElementById('storageLocation').value = p.storageLocation || '';
                document.getElementById('notes').value = p.notes || '';
            } catch (e) {
                UIComponents.showToast('Could not load product for editing', 'danger');
            }
        }

        const submitBtn = document.getElementById('product-submit-btn');

        async function handleProductSave(e) {
            if (e) e.preventDefault();

            const nameInput = document.getElementById('name');
            const categorySelectEl = document.getElementById('categoryId');
            const purchaseDateInput = document.getElementById('purchaseDate');

            const name = nameInput ? nameInput.value.trim() : '';
            let categoryId = categorySelectEl ? categorySelectEl.value : '';
            let purchaseDate = purchaseDateInput ? purchaseDateInput.value : '';

            // Auto-fallback defaults if user leaves them blank
            if (!categoryId) {
                categoryId = '1';
                if (categorySelectEl) categorySelectEl.value = '1';
            }

            if (!purchaseDate) {
                const today = new Date().toISOString().split('T')[0];
                purchaseDate = today;
                if (purchaseDateInput) purchaseDateInput.value = today;
            }

            let isValid = true;
            if (!name) {
                if (nameInput) nameInput.classList.add('is-invalid');
                UIComponents.showToast('Please enter a Product Name.', 'warning');
                if (nameInput) nameInput.focus();
                isValid = false;
            } else {
                if (nameInput) nameInput.classList.remove('is-invalid');
            }

            if (!isValid) return;

            const btnText = document.getElementById('product-submit-text');
            const btnSpinner = document.getElementById('product-submit-spinner');

            if (submitBtn) submitBtn.disabled = true;
            if (btnText) btnText.innerText = isEdit ? 'Updating...' : 'Saving...';
            if (btnSpinner) btnSpinner.classList.remove('d-none');

            const payload = {
                name: name,
                categoryId: parseInt(categoryId),
                brandId: document.getElementById('brandId')?.value ? parseInt(document.getElementById('brandId').value) : null,
                modelName: document.getElementById('modelName')?.value.trim() || null,
                modelNumber: document.getElementById('modelNumber')?.value.trim() || null,
                serialNumber: document.getElementById('serialNumber')?.value.trim() || null,
                color: document.getElementById('color')?.value.trim() || null,
                imeiNumber: document.getElementById('imeiNumber')?.value.trim() || null,
                barcode: document.getElementById('barcode')?.value.trim() || null,
                purchaseDate: purchaseDate,
                purchasePrice: document.getElementById('purchasePrice')?.value ? parseFloat(document.getElementById('purchasePrice').value) : null,
                purchaseMode: document.getElementById('purchaseMode')?.value || 'ONLINE',
                retailer: document.getElementById('retailer')?.value.trim() || null,
                productStatus: document.getElementById('productStatus')?.value || 'IN_USE',
                productCondition: document.getElementById('productCondition')?.value || 'NEW',
                storageLocation: document.getElementById('storageLocation')?.value.trim() || null,
                notes: document.getElementById('notes')?.value.trim() || null
            };

            // Clear previous invalid states
            document.querySelectorAll('#product-form .is-invalid').forEach(el => el.classList.remove('is-invalid'));

            try {
                if (isEdit) {
                    await ApiClient.put(`/products/${productId}`, payload);
                    UIComponents.showToast('Product updated successfully!', 'success');
                } else {
                    await ApiClient.post('/products', payload);
                    UIComponents.showToast('Product registered successfully!', 'success');
                }

                setTimeout(() => window.location.href = '/pages/products.html', 800);
            } catch (err) {
                const msg = err.message || 'Failed to save product.';
                UIComponents.showToast(msg, 'danger');

                if (msg.toLowerCase().includes('serial number')) {
                    const serialInput = document.getElementById('serialNumber');
                    if (serialInput) {
                        serialInput.classList.add('is-invalid');
                        serialInput.focus();
                    }
                } else if (msg.toLowerCase().includes('imei')) {
                    const imeiInput = document.getElementById('imeiNumber');
                    if (imeiInput) {
                        imeiInput.classList.add('is-invalid');
                        imeiInput.focus();
                    }
                }
            } finally {
                if (submitBtn) submitBtn.disabled = false;
                if (btnText) btnText.innerText = isEdit ? 'Update Product' : 'Save Product';
                if (btnSpinner) btnSpinner.classList.add('d-none');
            }
        }

        window.handleProductSaveExplicit = handleProductSave;

        if (form) form.addEventListener('submit', handleProductSave);
        // NOTE: do NOT add another addEventListener('click') here — the button uses onclick= in HTML
    }
}); // end DOMContentLoaded
