/**
 * WarrantyWise Reusable UI Components Engine
 * Handles Toast Notifications, Drag & Drop File Upload, Skeleton Loaders, Empty & Error States
 */
const UIComponents = (function () {
    return {
        // 1. TOAST NOTIFICATION ENGINE
        showToast: function (message, type = 'info', duration = 4000) {
            let container = document.getElementById('toast-container');
            if (!container) {
                container = document.createElement('div');
                container.id = 'toast-container';
                document.body.appendChild(container);
            }

            const iconMap = {
                success: 'bi-check-circle-fill text-success',
                warning: 'bi-exclamation-triangle-fill text-warning',
                danger: 'bi-x-circle-fill text-danger',
                info: 'bi-info-circle-fill text-info'
            };

            const toast = document.createElement('div');
            toast.className = `toast-custom toast-custom-${type}`;
            toast.innerHTML = `
                <i class="bi ${iconMap[type] || 'bi-info-circle-fill text-info'} fs-5 me-2"></i>
                <div class="flex-grow-1 fs-6 fw-medium">${message}</div>
                <button type="button" class="btn-close ms-2" aria-label="Close" onclick="this.parentElement.remove()"></button>
            `;

            container.appendChild(toast);

            setTimeout(() => {
                if (toast.parentElement) {
                    toast.style.opacity = '0';
                    toast.style.transform = 'translateX(100%)';
                    toast.style.transition = 'all 0.3s ease';
                    setTimeout(() => toast.remove(), 300);
                }
            }, duration);
        },

        // 2. MODAL ENGINE
        showModal: function (title, bodyHtml, footerButtons = '') {
            let modalEl = document.getElementById('global-app-modal');
            if (!modalEl) {
                modalEl = document.createElement('div');
                modalEl.id = 'global-app-modal';
                modalEl.className = 'modal fade';
                modalEl.tabIndex = -1;
                modalEl.innerHTML = `
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content bg-card-custom shadow-lg border">
                            <div class="modal-header border-bottom">
                                <h5 class="modal-title fw-bold" id="global-modal-title"></h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body" id="global-modal-body"></div>
                            <div class="modal-footer border-top" id="global-modal-footer"></div>
                        </div>
                    </div>
                `;
                document.body.appendChild(modalEl);
            }

            document.getElementById('global-modal-title').innerText = title;
            document.getElementById('global-modal-body').innerHTML = bodyHtml;
            document.getElementById('global-modal-footer').innerHTML = footerButtons || '<button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Close</button>';

            const bsModal = new bootstrap.Modal(modalEl);
            bsModal.show();
            return bsModal;
        },

        // 3. SKELETON SHIMMER LOADER
        renderSkeleton: function (count = 3) {
            let html = '';
            for (let i = 0; i < count; i++) {
                html += `
                    <div class="card-custom mb-3 p-4">
                        <div class="skeleton skeleton-title"></div>
                        <div class="skeleton skeleton-text" style="width: 80%;"></div>
                        <div class="skeleton skeleton-text" style="width: 45%;"></div>
                    </div>
                `;
            }
            return html;
        },

        // 4. PROFESSIONAL EMPTY STATE
        renderEmptyState: function (title, description, icon = 'bi-inbox', actionBtnHtml = '') {
            return `
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <i class="bi ${icon}"></i>
                    </div>
                    <div class="empty-state-title">${title}</div>
                    <div class="empty-state-text">${description}</div>
                    ${actionBtnHtml ? `<div>${actionBtnHtml}</div>` : ''}
                </div>
            `;
        },

        // 5. PROFESSIONAL ERROR STATE
        renderErrorState: function (message, retryBtnId = 'retry-btn') {
            return `
                <div class="card-custom p-5 text-center my-4 border-danger-subtle bg-danger-subtle bg-opacity-10">
                    <div class="empty-state-icon bg-danger-subtle text-danger mb-3" style="width:54px; height:54px; font-size:1.5rem;">
                        <i class="bi bi-exclamation-octagon"></i>
                    </div>
                    <h5 class="fw-bold text-main mb-2">Unable to Load Data</h5>
                    <p class="text-muted-custom small mb-4">${message || 'A network error occurred while connecting to backend REST services.'}</p>
                    <div>
                        <button type="button" id="${retryBtnId}" class="btn btn-sm btn-outline-secondary">
                            <i class="bi bi-arrow-clockwise me-1"></i> Retry Request
                        </button>
                    </div>
                </div>
            `;
        },

        // 6. DRAG AND DROP ATTACHMENT UPLOAD UX
        initDropzone: function (dropzoneEl, fileInputEl, onFileSelect) {
            if (!dropzoneEl || !fileInputEl) return;

            ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
                dropzoneEl.addEventListener(eventName, preventDefaults, false);
            });

            function preventDefaults(e) {
                e.preventDefault();
                e.stopPropagation();
            }

            ['dragenter', 'dragover'].forEach(eventName => {
                dropzoneEl.addEventListener(eventName, () => dropzoneEl.classList.add('dragover'), false);
            });

            ['dragleave', 'drop'].forEach(eventName => {
                dropzoneEl.addEventListener(eventName, () => dropzoneEl.classList.remove('dragover'), false);
            });

            dropzoneEl.addEventListener('drop', (e) => {
                const dt = e.dataTransfer;
                const files = dt.files;
                if (files && files.length > 0) {
                    fileInputEl.files = files;
                    if (onFileSelect) onFileSelect(files[0]);
                }
            });

            dropzoneEl.addEventListener('click', () => fileInputEl.click());

            fileInputEl.addEventListener('change', () => {
                if (fileInputEl.files && fileInputEl.files.length > 0) {
                    if (onFileSelect) onFileSelect(fileInputEl.files[0]);
                }
            });
        }
    };
})();
