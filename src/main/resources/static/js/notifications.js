/**
 * WarrantyWise Notification Center & Reminder Controller
 * Connects to NotificationController & ReminderController REST APIs
 */
document.addEventListener('DOMContentLoaded', function () {
    if (!AuthUtil.isAuthenticated()) {
        window.location.href = '/pages/login.html';
        return;
    }

    let activeTab = 'all';
    let currentPage = 0;
    const pageSize = 10;

    const listEl = document.getElementById('notifications-list');
    const skeletonEl = document.getElementById('notifications-skeleton');
    const emptyEl = document.getElementById('notifications-empty');
    const errorEl = document.getElementById('notifications-error');
    const categoryFilter = document.getElementById('notification-category-filter');
    const markAllReadBtn = document.getElementById('mark-all-read-btn');
    const generateRemindersBtn = document.getElementById('generate-reminders-btn');
    const retryBtn = document.getElementById('retry-notifications-btn');

    // Tab Navigation
    const tabBtns = document.querySelectorAll('#notification-tabs .nav-link');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            tabBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            activeTab = this.getAttribute('data-tab') || 'all';
            fetchNotifications(0);
        });
    });

    if (categoryFilter) {
        categoryFilter.addEventListener('change', () => fetchNotifications(0));
    }

    if (markAllReadBtn) {
        markAllReadBtn.addEventListener('click', async function () {
            try {
                await ApiClient.put('/notifications/read-all');
                UIComponents.showToast('All notifications marked as read', 'success');
                updateUnreadBadge();
                fetchNotifications(currentPage);
            } catch (err) {
                UIComponents.showToast(err.message || 'Failed to mark notifications as read', 'danger');
            }
        });
    }

    if (generateRemindersBtn) {
        generateRemindersBtn.addEventListener('click', async function () {
            try {
                UIComponents.showToast('Generating reminder records...', 'info');
                const res = await ApiClient.post('/reminders/generate');
                UIComponents.showToast(res || 'Reminders refreshed successfully!', 'success');
                updateUnreadBadge();
                fetchNotifications(currentPage);
            } catch (err) {
                UIComponents.showToast('Failed to generate reminders', 'danger');
            }
        });
    }

    if (retryBtn) {
        retryBtn.addEventListener('click', () => fetchNotifications(0));
    }

    // Helper: auto-generate reminders silently on load
    async function autoGenerateReminders() {
        try {
            await ApiClient.post('/reminders/generate');
        } catch (e) { /* silent */ }
    }

    // Main Fetch Function
    async function fetchNotifications(page = 0) {
        currentPage = page;
        if (skeletonEl) skeletonEl.classList.remove('d-none');
        if (listEl) listEl.innerHTML = '';
        if (emptyEl) emptyEl.classList.add('d-none');
        if (errorEl) errorEl.classList.add('d-none');

        const categoryVal = categoryFilter ? categoryFilter.value : '';
        let endpoint = '/notifications';

        if (activeTab === 'unread') {
            endpoint = '/notifications/unread';
        } else if (activeTab === 'today-reminders') {
            endpoint = '/reminders/today';
        } else if (activeTab === 'upcoming-reminders') {
            endpoint = '/reminders/upcoming';
        } else if (categoryVal && activeTab === 'all') {
            endpoint = `/notifications/category/${categoryVal}`;
        }

        const params = { page: page, size: pageSize };

        try {
            const data = await ApiClient.get(endpoint, params);
            if (skeletonEl) skeletonEl.classList.add('d-none');

            // Handle both paginated {content, totalPages} and plain array responses
            let items = [];
            let totalPages = 0;
            let totalElements = 0;
            let pageNumber = 0;

            if (data && Array.isArray(data.content)) {
                // Paginated response
                items = data.content;
                totalPages = data.totalPages || 0;
                totalElements = data.totalElements || 0;
                pageNumber = data.number || 0;
            } else if (Array.isArray(data)) {
                // Plain array response
                items = data;
                totalPages = 1;
                totalElements = data.length;
                pageNumber = 0;
            }

            if (items.length === 0) {
                if (emptyEl) emptyEl.classList.remove('d-none');
                renderPagination(0, 0);
                return;
            }

            const isReminder = activeTab.includes('reminders');
            renderNotificationsList(items, isReminder);
            renderPagination(pageNumber, totalPages, totalElements);
            updateUnreadBadge();
        } catch (err) {
            if (skeletonEl) skeletonEl.classList.add('d-none');
            if (errorEl) {
                errorEl.classList.remove('d-none');
                const msgEl = document.getElementById('notifications-error-msg');
                if (msgEl) msgEl.innerText = err.message || 'Error fetching notifications.';
            }
        }
    }

    // Render Items List
    function renderNotificationsList(items, isReminderList = false) {
        let html = '';
        items.forEach(item => {
            const isRead = item.isRead === true;
            const cardBg = isRead ? 'card-custom p-4' : 'card-custom p-4 bg-card-highlight border-start border-primary border-4';
            const dateStr = formatDate(item.notificationDate || item.reminderDate || item.createdAt);
            const category = item.category || 'SYSTEM';

            let catBadgeClass = 'bg-primary-subtle text-primary';
            if (category === 'WARRANTY_EXPIRY') catBadgeClass = 'bg-warning-subtle text-warning';
            else if (category === 'SERVICE_DUE') catBadgeClass = 'bg-info-subtle text-info';

            html += `
                <div class="${cardBg}">
                    <div class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-2 mb-2">
                        <div class="d-flex align-items-center gap-2">
                            <span class="badge ${catBadgeClass} px-2 py-1">${escapeHtml(category)}</span>
                            <h5 class="fw-bold mb-0 fs-6 text-main">${escapeHtml(item.title || 'Notification')}</h5>
                        </div>
                        <span class="small text-muted-custom">${dateStr}</span>
                    </div>
                    <p class="text-muted-custom small mb-3">${escapeHtml(item.message || '')}</p>
                    <div class="d-flex align-items-center justify-content-between border-top pt-2">
                        <div class="small text-muted-custom">
                            Product: <span class="fw-semibold text-main">${escapeHtml(item.productName || 'General System')}</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            ${isReminderList ? `
                                <button type="button" class="btn btn-sm btn-outline-secondary snooze-reminder-btn" data-id="${item.id}">
                                    <i class="bi bi-alarm me-1"></i> Snooze 7d
                                </button>
                            ` : `
                                <button type="button" class="btn btn-sm ${isRead ? 'btn-outline-secondary' : 'btn-outline-primary'} toggle-read-btn" data-id="${item.id}" data-read="${isRead}">
                                    <i class="bi ${isRead ? 'bi-envelope' : 'bi-envelope-paper-fill'} me-1"></i> ${isRead ? 'Mark Unread' : 'Mark Read'}
                                </button>
                                <button type="button" class="btn btn-sm btn-outline-danger dismiss-notification-btn" data-id="${item.id}">
                                    <i class="bi bi-x-circle me-1"></i> Dismiss
                                </button>
                            `}
                        </div>
                    </div>
                </div>
            `;
        });
        listEl.innerHTML = html;

        // Bind Card Action Handlers
        bindCardActions();
    }

    function bindCardActions() {
        // Toggle Read / Unread
        document.querySelectorAll('.toggle-read-btn').forEach(btn => {
            btn.addEventListener('click', async function () {
                const id = this.getAttribute('data-id');
                const isRead = this.getAttribute('data-read') === 'true';
                try {
                    const endpoint = isRead ? `/notifications/${id}/unread` : `/notifications/${id}/read`;
                    await ApiClient.put(endpoint);
                    UIComponents.showToast(`Notification marked as ${isRead ? 'unread' : 'read'}`, 'success');
                    updateUnreadBadge();
                    fetchNotifications(currentPage);
                } catch (e) {
                    UIComponents.showToast('Failed to update status', 'danger');
                }
            });
        });

        // Dismiss Notification
        document.querySelectorAll('.dismiss-notification-btn').forEach(btn => {
            btn.addEventListener('click', async function () {
                const id = this.getAttribute('data-id');
                try {
                    await ApiClient.put(`/notifications/${id}/dismiss`);
                    UIComponents.showToast('Notification dismissed', 'info');
                    updateUnreadBadge();
                    fetchNotifications(currentPage);
                } catch (e) {
                    UIComponents.showToast('Failed to dismiss notification', 'danger');
                }
            });
        });

        // Snooze Reminder
        document.querySelectorAll('.snooze-reminder-btn').forEach(btn => {
            btn.addEventListener('click', async function () {
                const id = this.getAttribute('data-id');
                try {
                    await ApiClient.put(`/reminders/${id}/snooze?days=7`);
                    UIComponents.showToast('Reminder snoozed for 7 days', 'success');
                    updateUnreadBadge();
                    fetchNotifications(currentPage);
                } catch (e) {
                    UIComponents.showToast('Failed to snooze reminder', 'danger');
                }
            });
        });
    }

    // Helper: Update Header Unread Badge
    async function updateUnreadBadge() {
        try {
            const count = await ApiClient.get('/notifications/unread-count').catch(() => 0);
            const badge = document.getElementById('notification-badge-count');
            if (badge) {
                if (count > 0) {
                    badge.innerText = count > 99 ? '99+' : count;
                    badge.classList.remove('d-none');
                } else {
                    badge.classList.add('d-none');
                }
            }
        } catch (e) {
            // Ignore badge update errors
        }
    }

    function renderPagination(page, totalPages, totalElements = 0) {
        const paginationEl = document.getElementById('notifications-pagination');
        const infoEl = document.getElementById('notification-pagination-info');

        if (infoEl) infoEl.innerText = `Showing page ${page + 1} of ${totalPages || 1} (${totalElements} alerts)`;
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
                    fetchNotifications(targetPage);
                }
            });
        });
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

    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    // On load: auto-generate reminders silently then fetch notifications
    autoGenerateReminders().finally(() => fetchNotifications(0));
});
