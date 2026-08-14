/**
 * WarrantyWise Dark Mode & Theme Manager Utility
 */
const ThemeUtil = (function () {
    const THEME_KEY = 'warrantywise_theme';

    return {
        init: function () {
            const savedTheme = localStorage.getItem(THEME_KEY) || 'light';
            this.setTheme(savedTheme);
        },

        getTheme: function () {
            return document.documentElement.getAttribute('data-bs-theme') || 'light';
        },

        setTheme: function (theme) {
            document.documentElement.setAttribute('data-bs-theme', theme);
            localStorage.setItem(THEME_KEY, theme);
            
            // Update toggle icon if present
            const themeBtn = document.getElementById('theme-toggle-btn');
            if (themeBtn) {
                const icon = themeBtn.querySelector('i');
                if (icon) {
                    icon.className = theme === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
                }
            }
        },

        toggle: function () {
            const currentTheme = this.getTheme();
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            this.setTheme(newTheme);
        }
    };
})();
