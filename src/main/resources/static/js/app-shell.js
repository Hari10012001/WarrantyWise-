/**
 * WarrantyWise Authenticated Application Shell Controller
 * Manages Sidebar, Header, Command Palette (Ctrl+K), Notifications Dropdown & Profile
 */
document.addEventListener('DOMContentLoaded', function () {
    // 1. Enforce Protected Page Security: Redirect to login if unauthenticated
    const currentPath = window.location.pathname;
    const isPublicPage = currentPath.includes('/login.html') || currentPath.includes('/register.html') || currentPath === '/';

    if (!AuthUtil.isAuthenticated() && !isPublicPage) {
        window.location.href = '/pages/login.html';
        return;
    }

    // 2. Initialize Theme System
    ThemeUtil.init();

    // 3. Populate User Profile Data into Shell Header & Sidebar
    if (AuthUtil.isAuthenticated()) {
        const user = AuthUtil.getUser();
        if (user) {
            const userAvatar = document.getElementById('user-avatar-text');
            const userName = document.getElementById('user-display-name');
            const userRole = document.getElementById('user-display-role');

            const initials = user.fullName
                ? user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()
                : (user.email ? user.email[0].toUpperCase() : 'U');

            if (userAvatar) userAvatar.innerText = initials;
            if (userName) userName.innerText = user.fullName || user.email;
            if (userRole) userRole.innerText = user.role === 'ROLE_ADMIN' ? 'Administrator' : 'Standard User';

            // Role-based visibility for Admin Navigation items
            if (user.role === 'ROLE_ADMIN') {
                document.querySelectorAll('.admin-only').forEach(el => el.classList.remove('d-none'));
            }
        }

        // Fetch Unread Notification Count & Dropdown Data
        updateNotificationsSummary();
    }

    // 4. Highlight Active Navigation Link based on current window route
    document.querySelectorAll('.sidebar-nav-item').forEach(item => {
        const href = item.getAttribute('href');
        if (href && currentPath.includes(href) && href !== '#') {
            item.classList.add('active');
            item.setAttribute('aria-current', 'page');
        } else {
            item.classList.remove('active');
            item.removeAttribute('aria-current');
        }
    });

    // 5. Sidebar Toggle Logic
    const sidebarToggleBtn = document.getElementById('sidebar-toggle-btn');
    const sidebarOverlayBackdrop = document.getElementById('sidebar-backdrop');

    function toggleSidebar() {
        if (window.innerWidth < 992) {
            document.body.classList.toggle('sidebar-open');
            if (sidebarOverlayBackdrop) {
                sidebarOverlayBackdrop.classList.toggle('d-none');
            }
        } else {
            document.body.classList.toggle('sidebar-collapsed');
        }
    }

    if (sidebarToggleBtn) sidebarToggleBtn.addEventListener('click', toggleSidebar);
    if (sidebarOverlayBackdrop) {
        sidebarOverlayBackdrop.addEventListener('click', function () {
            document.body.classList.remove('sidebar-open');
            sidebarOverlayBackdrop.classList.add('d-none');
        });
    }

    // 6. COMMAND PALETTE (CTRL + K) IMPLEMENTATION
    initCommandPalette();

    function initCommandPalette() {
        let paletteBackdrop = document.getElementById('command-palette-backdrop');
        if (!paletteBackdrop) {
            paletteBackdrop = document.createElement('div');
            paletteBackdrop.id = 'command-palette-backdrop';
            paletteBackdrop.className = 'd-none';
            paletteBackdrop.innerHTML = `
                <div class="command-palette-card">
                    <div class="command-palette-input-wrap">
                        <i class="bi bi-search text-muted fs-5"></i>
                        <input type="text" id="command-palette-input" class="command-palette-input" placeholder="Type a command or search (e.g. Products, Add Warranty, Reports)..." autocomplete="off" />
                        <span class="badge bg-secondary text-uppercase small">Esc to close</span>
                    </div>
                    <div class="command-palette-results" id="command-palette-results">
                        <!-- Results dynamically inserted here -->
                    </div>
                </div>
            `;
            document.body.appendChild(paletteBackdrop);
        }

        const input = document.getElementById('command-palette-input');
        const results = document.getElementById('command-palette-results');

        const commands = [
            { title: 'Dashboard', icon: 'bi-grid-1x2', url: '/pages/dashboard.html', category: 'Navigation' },
            { title: 'All Products Inventory', icon: 'bi-box-seam', url: '/pages/products.html', category: 'Products' },
            { title: 'Add New Product', icon: 'bi-plus-circle', url: '/pages/add-product.html', category: 'Products' },
            { title: 'Warranty Management', icon: 'bi-shield-check', url: '/pages/warranties.html', category: 'Warranties' },
            { title: 'Register Warranty Policy', icon: 'bi-shield-plus', url: '/pages/add-warranty.html', category: 'Warranties' },
            { title: 'Service History & Analytics', icon: 'bi-tools', url: '/pages/service-history.html', category: 'Services' },
            { title: 'Add Service Record', icon: 'bi-journal-plus', url: '/pages/add-service-record.html', category: 'Services' },
            { title: 'Reminders & Notifications Hub', icon: 'bi-bell', url: '/pages/notifications.html', category: 'Alerts' },
            { title: 'Reports & CSV Export', icon: 'bi-file-earmark-bar-graph', url: '/pages/reports.html', category: 'Analytics' },
            { title: 'Toggle Dark / Light Theme', icon: 'bi-moon-stars', action: () => ThemeUtil.toggle(), category: 'Settings' },
            { title: 'Logout Account', icon: 'bi-box-arrow-right', action: () => AuthUtil.logout(), category: 'Account' }
        ];

        function renderResults(filterText = '') {
            const query = filterText.toLowerCase().trim();
            const filtered = commands.filter(c => c.title.toLowerCase().includes(query) || c.category.toLowerCase().includes(query));

            if (filtered.length === 0) {
                results.innerHTML = `<div class="p-3 text-center text-muted small">No commands matching "${filterText}"</div>`;
                return;
            }

            let html = '';
            filtered.forEach((c, idx) => {
                html += `
                    <div class="command-palette-item ${idx === 0 ? 'active' : ''}" data-url="${c.url || ''}" data-idx="${idx}">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi ${c.icon} fs-5 text-primary"></i>
                            <span class="fw-semibold">${c.title}</span>
                        </div>
                        <span class="badge bg-secondary-subtle text-muted small">${c.category}</span>
                    </div>
                `;
            });
            results.innerHTML = html;

            results.querySelectorAll('.command-palette-item').forEach((el, idx) => {
                el.addEventListener('click', () => {
                    closePalette();
                    if (filtered[idx].action) {
                        filtered[idx].action();
                    } else if (filtered[idx].url) {
                        window.location.href = filtered[idx].url;
                    }
                });
            });
        }

        function openPalette() {
            paletteBackdrop.classList.remove('d-none');
            input.value = '';
            renderResults();
            setTimeout(() => input.focus(), 50);
        }

        function closePalette() {
            paletteBackdrop.classList.add('d-none');
        }

        // Global Keyboard Shortcut: Ctrl+K or Cmd+K
        document.addEventListener('keydown', function (e) {
            if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
                e.preventDefault();
                if (paletteBackdrop.classList.contains('d-none')) {
                    openPalette();
                } else {
                    closePalette();
                }
            } else if (e.key === 'Escape') {
                closePalette();
            }
        });

        if (input) {
            input.addEventListener('input', () => renderResults(input.value));
        }

        paletteBackdrop.addEventListener('click', (e) => {
            if (e.target === paletteBackdrop) closePalette();
        });

        // Also bind header search bar click to Command Palette if available
        const searchInput = document.getElementById('global-search-input');
        if (searchInput) {
            searchInput.addEventListener('focus', function () {
                this.blur();
                openPalette();
            });
        }
    }

    // 7. NOTIFICATIONS SUMMARY UPDATE & DROPDOWN ENGINE
    async function updateNotificationsSummary() {
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
            // Silently ignore
        }
    }

    // 8. Theme Toggle Button Event
    const themeBtn = document.getElementById('theme-toggle-btn');
    if (themeBtn) {
        themeBtn.addEventListener('click', function () {
            ThemeUtil.toggle();
        });
    }

    // 9. Logout Event Listener
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function (e) {
            e.preventDefault();
            AuthUtil.logout();
        });
    }
});
