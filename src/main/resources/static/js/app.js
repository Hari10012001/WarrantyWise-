/**
 * WarrantyWise Main Application Initializer & Global Controller
 */
document.addEventListener('DOMContentLoaded', function () {
    // 1. Initialize Theme
    ThemeUtil.init();

    // 2. Setup User Session UI if authenticated
    if (AuthUtil.isAuthenticated()) {
        const user = AuthUtil.getUser();
        if (user) {
            // Update User Avatar and Name
            const userAvatar = document.getElementById('user-avatar-text');
            const userName = document.getElementById('user-display-name');
            const userRole = document.getElementById('user-display-role');

            const initials = user.fullName ? user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() : 'U';

            if (userAvatar) userAvatar.innerText = initials;
            if (userName) userName.innerText = user.fullName || user.email;
            if (userRole) userRole.innerText = user.role === 'ROLE_ADMIN' ? 'Administrator' : 'User';

            // Show Admin Sidebar items if admin
            if (user.role === 'ROLE_ADMIN') {
                document.querySelectorAll('.admin-only').forEach(el => el.classList.remove('d-none'));
            }
        }

        // Fetch Unread Notification Count
        ApiClient.get('/notifications/unread-count')
            .then(count => {
                const badge = document.getElementById('notification-badge-count');
                if (badge) {
                    if (count > 0) {
                        badge.innerText = count > 99 ? '99+' : count;
                        badge.classList.remove('d-none');
                    } else {
                        badge.classList.add('d-none');
                    }
                }
            })
            .catch(() => {
                // Silently handle if notification count endpoint fails
            });
    }

    // 3. Highlight Active Navigation Item based on URL
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sidebar-nav-item').forEach(item => {
        const href = item.getAttribute('href');
        if (href && currentPath.includes(href) && href !== '#') {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // 4. Setup Global Event Listeners
    const themeBtn = document.getElementById('theme-toggle-btn');
    if (themeBtn) {
        themeBtn.addEventListener('click', function () {
            ThemeUtil.toggle();
        });
    }

    const sidebarToggleBtn = document.getElementById('sidebar-toggle-btn');
    if (sidebarToggleBtn) {
        sidebarToggleBtn.addEventListener('click', function () {
            UIComponents.toggleSidebar();
        });
    }

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function (e) {
            e.preventDefault();
            AuthUtil.logout();
        });
    }
});
